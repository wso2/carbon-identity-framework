/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public abstract class SessionDataStore {

    public static final BlockingDeque<SessionContextDO> sessionContextQueue = new LinkedBlockingDeque();
    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";
    private static final Log log = LogFactory.getLog(SessionDataStore.class);
    public static boolean redisEnabled = false;
    private static int maxSessionDataPoolSize = 100;
    private static boolean enablePersist;
    private static volatile SessionDataStore instance;


    static {
        try {
            String maxPoolSizeValue = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.PoolSize");
            if (StringUtils.isNotBlank(maxPoolSizeValue)) {
                if (log.isDebugEnabled()) {
                    log.debug("Session data pool size config value: " + maxPoolSizeValue);
                }
                maxSessionDataPoolSize = Integer.parseInt(maxPoolSizeValue);
            }
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while parsing the configurations : ", e);
            }
            log.warn("An error occurred while parsing one or more pool size configurations. Default values would be used.");
        }
    }

    /**
     * Returning instance of one of the implementation of SessionDataStore.
     *
     * @return {@link RDBMSSessionDataStore} or {@link RedisSessionDataStore}
     */
    public static SessionDataStore getInstance() {

        String enablePersistVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Enable");
        enablePersist = true;
        if (enablePersistVal != null) {
            enablePersist = Boolean.parseBoolean(enablePersistVal);
        }

        String redisEnabledVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.RedisEnable");
        if (StringUtils.isNotBlank(redisEnabledVal)) {
            redisEnabled = Boolean.parseBoolean(redisEnabledVal);
        }
        if (instance == null) {
            synchronized (SessionDataStore.class) {
                if (instance == null) {
                    if (redisEnabled == true) {
                        instance = new RedisSessionDataStore();
                    } else {
                        instance = new RDBMSSessionDataStore();
                    }

                }
            }
        }
        return instance;
    }

    /**
     * Abstract method for saving or updating session data abstract method.
     *
     * @param key      Session id.
     * @param type     Session type.
     * @param entry    Session object.
     * @param nanoTime Session created time.
     * @param tenantId Tenant id.
     * @return
     */
    public abstract void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId);

    /**
     * Abstract method for getting session context data object.
     *
     * @param key  Session id.
     * @param type Session type.
     * @return {@link SessionContextDO}
     */
    public abstract SessionContextDO getSessionContextData(String key, String type);

    /**
     * Abstract method for Removing the session data.
     *
     * @param key      Session id.
     * @param type     Session type.
     * @param nanoTime Session created time.
     * @return
     */
    public abstract void removeSessionData(String key, String type, long nanoTime);

    /**
     * @deprecated This is now run as a part of the {@link #removeExpiredSessionData()} due to a possible deadlock as
     * mentioned in IDENTITY-5131.
     */
    public abstract void removeExpiredOperationData();

    /**
     * Abstract method for removes temporary authn context data from the server if temporary data cleanup is enabled.
     *
     * @param key  Session id.
     * @param type Session type.
     * @return
     */
    public abstract void removeTempAuthnContextData(String key, String type);

    /**
     * Cleans the session data and operation data (if enabled) from the DB.
     *
     * @param
     * @return
     */
    public abstract void removeExpiredSessionData();

    /**
     * @param key  Session id.
     * @param type Session type.
     * @return null or {@link SessionContextDO}
     */
    public Object getSessionData(String key, String type) {

        SessionContextDO sessionContextDO = getSessionContextData(key, type);
        return sessionContextDO != null ? sessionContextDO.getEntry() : null;
    }

    /**
     * Storing or updating  SessionData when there is no tenantId.
     *
     * @param key   Session id.
     * @param type  Session type.
     * @param entry Session object.
     */
    public void storeSessionData(String key, String type, Object entry) {

        storeSessionData(key, type, entry, MultitenantConstants.INVALID_TENANT_ID);
    }

    /**
     * Storing or updating sessiondata main method by calling persistSessionData method when there is a tenantId.
     *
     * @param key      Session id.
     * @param type     Session type.
     * @param entry    Session object.
     * @param tenantId Tenant id.
     */
    public void storeSessionData(String key, String type, Object entry, int tenantId) {

        if (!enablePersist) {
            return;
        }
        long nanoTime = FrameworkUtils.getCurrentStandardNano();
        if (maxSessionDataPoolSize > 0 && !isTempCache(type)) {
            sessionContextQueue.push(new SessionContextDO(key, type, entry, nanoTime, tenantId));
        } else {
            persistSessionData(key, type, entry, nanoTime, tenantId);
        }
    }

    protected long getCleanupTimeout(String type, int tenantId) {

        if (isTempCache(type)) {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getTempDataCleanUpTimeout());
        } else if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            return TimeUnit.SECONDS.toNanos(IdPManagementUtil.getRememberMeTimeout(tenantDomain));
        } else {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getCleanUpTimeout());
        }
    }

    /**
     * Clearing session data main method by calling removeSessionData.
     *
     * @param key  Session id.
     * @param type Session type.
     */
    public void clearSessionData(String key, String type) {

        if (!enablePersist) {
            return;
        }
        long nanoTime = FrameworkUtils.getCurrentStandardNano();
        if (maxSessionDataPoolSize > 0 && !isTempCache(type)) {
            sessionContextQueue.push(new SessionContextDO(key, type, null, nanoTime));
        } else {
            removeSessionData(key, type, nanoTime);
        }
    }

    /**
     * Method to stop running tasks, when the component is deactivated.
     */
    public void stopService() {

        TempAuthContextDataDeleteTask.shutdown();
        SessionDataPersistTask.shutdown();
    }

    protected boolean isTempCache(String type) {

        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, type);

        if (identityCacheConfig != null) {
            return identityCacheConfig.isTemporary();
        }
        return false;
    }

    protected BlockingDeque getSessionContextQueue() {

        return this.sessionContextQueue;
    }


    protected int getIntProperty(String propertyName, Integer defaultValue) {

        String stringVal = IdentityUtil.getProperty(propertyName);
        if (StringUtils.isNotBlank(stringVal)) {
            return Integer.parseInt(stringVal);
        }
        return defaultValue;
    }

    protected Boolean getBooleanProperty(String propertyName, Boolean defaultValue) {

        String stringVal = IdentityUtil.getProperty(propertyName);
        if (StringUtils.isNotBlank(stringVal)) {
            return Boolean.parseBoolean(stringVal);
        }
        return defaultValue;
    }

}
