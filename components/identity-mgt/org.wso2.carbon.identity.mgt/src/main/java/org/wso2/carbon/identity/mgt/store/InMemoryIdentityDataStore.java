/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.Map;

/**
 *
 */
public class InMemoryIdentityDataStore extends UserIdentityDataStore {

    private static final String IDENTITY_LOGIN_DATA_CACHE_MANAGER = "IDENTITY_LOGIN_DATA_CACHE_MANAGER";
    private static final String IDENTITY_LOGIN_DATA_CACHE = "IDENTITY_LOGIN_DATA_CACHE";

    private static Log log = LogFactory.getLog(InMemoryIdentityDataStore.class);

    protected Cache<String, UserIdentityClaimsDO> getCache() {
        CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(InMemoryIdentityDataStore.IDENTITY_LOGIN_DATA_CACHE_MANAGER);
        Cache<String, UserIdentityClaimsDO> cache = manager.getCache(InMemoryIdentityDataStore.IDENTITY_LOGIN_DATA_CACHE);
        return cache;
    }

    @Override
    public void store(UserIdentityClaimsDO userIdentityDTO, UserStoreManager userStoreManager)
            throws IdentityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            if (userIdentityDTO != null && userIdentityDTO.getUserName() != null) {
                String userName = userIdentityDTO.getUserName();
                if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
                    if (!IdentityUtil.isUserStoreCaseSensitive((org.wso2.carbon.user.core.UserStoreManager)
                            userStoreManager)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Case insensitive user store found. Changing username from : " + userName +
                                    " to : " + userName.toLowerCase());
                        }
                        userName = userName.toLowerCase();
                    }
                }

                if (log.isDebugEnabled()) {
                    StringBuilder data = new StringBuilder("{");
                    if (userIdentityDTO.getUserIdentityDataMap() != null) {
                        for (Map.Entry<String, String> entry : userIdentityDTO.getUserIdentityDataMap().entrySet()) {
                            data.append("[").append(entry.getKey()).append(" = ").append(entry.getValue()).append("], ");
                        }
                    }
                    if (data.indexOf(",") >= 0) {
                        data.deleteCharAt(data.lastIndexOf(","));
                    }
                    data.append("}");
                    log.debug("Storing UserIdentityClaimsDO to cache for user: " + userName + " with claims: " + data);
                }

                org.wso2.carbon.user.core.UserStoreManager store = (org.wso2.carbon.user.core.UserStoreManager) userStoreManager;
                String domainName = store.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                String key = domainName + userStoreManager.getTenantId() + userName;

                Cache<String, UserIdentityClaimsDO> cache = getCache();
                if (cache != null) {
                    cache.put(key, userIdentityDTO);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while obtaining tenant ID from user store manager");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public UserIdentityClaimsDO load(String userName, UserStoreManager userStoreManager) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            Cache<String, UserIdentityClaimsDO> cache = getCache();
            if (userName != null && cache != null) {
                if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
                    if (!IdentityUtil.isUserStoreCaseSensitive((org.wso2.carbon.user.core.UserStoreManager)
                            userStoreManager)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Case insensitive user store found. Changing username from : " + userName +
                                    " to : " + userName.toLowerCase());
                        }
                        userName = userName.toLowerCase();
                    }
                }

                org.wso2.carbon.user.core.UserStoreManager store = (org.wso2.carbon.user.core.UserStoreManager) userStoreManager;

                String domainName = store.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                UserIdentityClaimsDO userIdentityDTO = (UserIdentityClaimsDO) cache.get(domainName + userStoreManager
                        .getTenantId() + userName);

                if (userIdentityDTO != null && log.isDebugEnabled()) {
                    StringBuilder data = new StringBuilder("{");
                    if (userIdentityDTO.getUserIdentityDataMap() != null) {
                        for (Map.Entry<String, String> entry : userIdentityDTO.getUserIdentityDataMap().entrySet()) {
                            data.append("[" + entry.getKey() + " = " + entry.getValue() + "], ");
                        }
                    }
                    if (data.indexOf(",") >= 0) {
                        data.deleteCharAt(data.lastIndexOf(","));
                    }
                    data.append("}");
                    log.debug("Loaded UserIdentityClaimsDO from cache for user :" + userName + " with claims: " + data);

                }
                return userIdentityDTO;
            }
        } catch (UserStoreException e) {
            log.error("Error while obtaining tenant ID from user store manager");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return null;
    }

    @Override
    public void remove(String userName, UserStoreManager userStoreManager) throws IdentityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Cache<String, UserIdentityClaimsDO> cache = getCache();
            if (userName == null) {
                return;
            }
            if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
                if (!IdentityUtil.isUserStoreCaseSensitive((org.wso2.carbon.user.core.UserStoreManager) userStoreManager)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Case insensitive user store found. Changing username from : " + userName + " to : " +
                                userName.toLowerCase());
                    }
                    userName = userName.toLowerCase();
                }
            }
            org.wso2.carbon.user.core.UserStoreManager store = (org.wso2.carbon.user.core.UserStoreManager)
                    userStoreManager;
            String domainName = store.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_DOMAIN_NAME);

            cache.remove(domainName + userStoreManager.getTenantId() + userName);
        } catch (UserStoreException e) {
            log.error("Error while obtaining tenant ID from user store manager");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
