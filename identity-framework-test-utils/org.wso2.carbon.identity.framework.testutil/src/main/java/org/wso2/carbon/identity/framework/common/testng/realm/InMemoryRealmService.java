/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple In Memory realm service for mocking.
 */
public class InMemoryRealmService implements RealmService {

    private static final Log log = LogFactory.getLog(InMemoryRealmService.class);
    private Map<Integer, UserRealm> userRealmMap = new HashMap();
    private RealmConfiguration bootstrapRealmConfig = null;
    private UserRealm bootstrapRealm = null;
    private TenantManager tenantManager;

    public InMemoryRealmService(int tenantId) throws UserStoreException {
        this.setup();

        try {
            this.bootstrapRealm = this.initializeRealm(this.bootstrapRealmConfig, tenantId);
        } catch (Exception var3) {
            String msg = "Error in init bootstrap realm";
            throw new UserStoreException(msg, var3);
        }
    }

    public UserRealm getUserRealm(RealmConfiguration tenantRealmConfig) throws UserStoreException {
        int tenantId = tenantRealmConfig.getTenantId();
        if (tenantId == -1234) {
            return this.bootstrapRealm;
        } else {
            UserRealm userRealm = (UserRealm) this.userRealmMap.get(Integer.valueOf(tenantId));
            if (userRealm == null) {
                userRealm = this.initializeRealm(tenantRealmConfig, tenantId);
                this.userRealmMap.put(Integer.valueOf(tenantId), userRealm);
            } else {
                long existingRealmPersistedTime = -1L;
                long newRealmConfigPersistedTime = -1L;
                if (userRealm.getRealmConfiguration().getPersistedTimestamp() != null) {
                    existingRealmPersistedTime = userRealm.getRealmConfiguration().getPersistedTimestamp().getTime();
                }

                if (tenantRealmConfig.getPersistedTimestamp() != null) {
                    newRealmConfigPersistedTime = tenantRealmConfig.getPersistedTimestamp().getTime();
                }

                if (existingRealmPersistedTime != newRealmConfigPersistedTime) {
                    userRealm = this.initializeRealm(tenantRealmConfig, tenantId);
                    this.userRealmMap.put(Integer.valueOf(tenantId), userRealm);
                }
            }

            return userRealm;
        }
    }

    public void setUserRealm(int tenantId, UserRealm realm) throws UserStoreException {
        this.userRealmMap.put(Integer.valueOf(tenantId), realm);
    }

    public RealmConfiguration getBootstrapRealmConfiguration() {
        return this.bootstrapRealmConfig;
    }

    public void setup() throws UserStoreException {

        try {
            RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
            InputStream inStream = this.getClass().getResourceAsStream("/users/user-mgt-users.xml");

            try {
                this.bootstrapRealmConfig = builder.buildRealmConfiguration(inStream);
            } finally {
                inStream.close();
            }
        } catch (Exception e) {
            String msg = "Failed to initialize the user manager. ";
            throw new UserStoreException(msg, e);
        }

        this.tenantManager = new InMemoryTenantManager();
    }

    public UserRealm getBootstrapRealm() throws UserStoreException {
        return this.bootstrapRealm;
    }

    public void setTenantManager(org.wso2.carbon.user.api.TenantManager tenantManager)
            throws org.wso2.carbon.user.api.UserStoreException {
        this.setTenantManager((TenantManager) tenantManager);
    }

    public TenantManager getTenantManager() {
        return this.tenantManager;
    }

    public org.wso2.carbon.user.api.UserRealm getTenantUserRealm(int tenantId) throws UserStoreException {
        return this.bootstrapRealm;
    }

    public UserRealm initializeRealm(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {
        UserRealm userRealm = new MockRealm();
        userRealm.init(realmConfig, (Map) null, (Map) null, tenantId);
        return userRealm;
    }

    public void setTenantManager(TenantManager t) {
    }

    public MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder() throws UserStoreException {
        return null;
    }

    public UserRealm getCachedUserRealm(int tenantId) throws UserStoreException {
        return this.userRealmMap.get(Integer.valueOf(tenantId));
    }

    public void clearCachedUserRealm(int i) throws UserStoreException {
    }

    public TenantMgtConfiguration getTenantMgtConfiguration() {
        TenantMgtConfiguration tenantMgtConfig = null;
        return tenantMgtConfig;
    }

    public void addCustomUserStore(String realmName, String userStoreClassName, Map<String, String> properties,
            int tenantId) throws UserStoreException {
    }

    public void setBootstrapRealmConfiguration(RealmConfiguration arg0) {
    }
}
