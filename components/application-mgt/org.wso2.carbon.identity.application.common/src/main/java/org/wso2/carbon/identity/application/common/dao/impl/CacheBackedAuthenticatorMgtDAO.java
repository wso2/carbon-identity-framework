/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.cache.AuthenticatorCache;
import org.wso2.carbon.identity.application.common.cache.AuthenticatorCacheEntry;
import org.wso2.carbon.identity.application.common.cache.AuthenticatorCacheKey;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;

import java.util.List;

/**
 * This class implements the Cache backed AuthenticatorManagementDAO interface.
 */
public class CacheBackedAuthenticatorMgtDAO implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedAuthenticatorMgtDAO.class);
    private final AuthenticatorCache authenticatorCache;
    private final AuthenticatorManagementDAO authenticatorManagementDAO;

    public CacheBackedAuthenticatorMgtDAO(AuthenticatorManagementDAO authenticatorManagementDAO) {

        this.authenticatorManagementDAO = authenticatorManagementDAO;
        authenticatorCache = AuthenticatorCache.getInstance();
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig,
            int tenantId, AuthenticationType type) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig createdConfig = authenticatorManagementDAO.addUserDefinedLocalAuthenticator(
                authenticatorConfig, tenantId, type);

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(authenticatorConfig.getName());
        authenticatorCache.addToCache(cacheKey, new AuthenticatorCacheEntry(createdConfig), tenantId);
        return createdConfig;
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig
                existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig,
                int tenantId) throws AuthenticatorMgtException {

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(existingAuthenticatorConfig.getName());
        authenticatorCache.clearCacheEntry(cacheKey, tenantId);

        return authenticatorManagementDAO.updateUserDefinedLocalAuthenticator(
                existingAuthenticatorConfig, newAuthenticatorConfig, tenantId);
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(authenticatorConfigName);
        AuthenticatorCacheEntry entry = authenticatorCache.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            return entry.getAuthenticatorConfig();
        }

        return authenticatorManagementDAO.getUserDefinedLocalAuthenticator(authenticatorConfigName, tenantId);
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticator(int tenantId)
            throws AuthenticatorMgtException {

        return authenticatorManagementDAO.getAllUserDefinedLocalAuthenticator(tenantId);
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, 
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        authenticatorCache.clearCacheEntry(new AuthenticatorCacheKey(authenticatorConfigName), tenantId);
        authenticatorManagementDAO.deleteUserDefinedLocalAuthenticator(authenticatorConfigName, authenticatorConfig,
                tenantId);
    }
}
