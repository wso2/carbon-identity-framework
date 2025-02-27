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
import org.wso2.carbon.identity.application.common.cache.UserDefinedLocalAuthenticatorsCache;
import org.wso2.carbon.identity.application.common.cache.UserDefinedLocalAuthenticatorsCacheEntry;
import org.wso2.carbon.identity.application.common.cache.UserDefinedLocalAuthenticatorsCacheKey;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;

import java.util.List;

/**
 * Implements caching layer for the AuthenticatorManagementDAO.
 */
public class CacheBackedAuthenticatorMgtDAO implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedAuthenticatorMgtDAO.class);
    private final AuthenticatorCache authenticatorCache;
    private final UserDefinedLocalAuthenticatorsCache userDefinedLocalAuthenticatorsCache;
    private final AuthenticatorManagementFacade authenticatorMgtFacade;

    public CacheBackedAuthenticatorMgtDAO(AuthenticatorManagementDAO authenticatorManagementDAO) {

        authenticatorMgtFacade = new AuthenticatorManagementFacade(authenticatorManagementDAO);
        authenticatorCache = AuthenticatorCache.getInstance();
        userDefinedLocalAuthenticatorsCache = UserDefinedLocalAuthenticatorsCache.getInstance();
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig createdConfig = authenticatorMgtFacade.addUserDefinedLocalAuthenticator(
                authenticatorConfig, tenantId);

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(authenticatorConfig.getName());
        authenticatorCache.addToCache(cacheKey, new AuthenticatorCacheEntry(createdConfig), tenantId);
        LOG.debug("Added cache entry for newly created authenticator " + authenticatorConfig.getName());
        userDefinedLocalAuthenticatorsCache.clearCacheEntry(new UserDefinedLocalAuthenticatorsCacheKey(tenantId),
                tenantId);
        LOG.debug("Deleted Cache entry for all user defined local authenticators of tenant id: " + tenantId);
        return createdConfig;
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig
                existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig,
                int tenantId) throws AuthenticatorMgtException {

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(existingAuthenticatorConfig.getName());
        authenticatorCache.clearCacheEntry(cacheKey, tenantId);
        LOG.debug("Deleted cache entry of updating authenticator " + existingAuthenticatorConfig.getName());
        userDefinedLocalAuthenticatorsCache.clearCacheEntry(new UserDefinedLocalAuthenticatorsCacheKey(tenantId),
                tenantId);
        LOG.debug("Deleted Cache entry for all user defined local authenticators of tenant id: " + tenantId);

        return authenticatorMgtFacade.updateUserDefinedLocalAuthenticator(
                existingAuthenticatorConfig, newAuthenticatorConfig, tenantId);
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        AuthenticatorCacheKey cacheKey = new AuthenticatorCacheKey(authenticatorConfigName);
        AuthenticatorCacheEntry entry = authenticatorCache.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            LOG.debug("Cache entry found for authenticator " + authenticatorConfigName);
            return entry.getAuthenticatorConfig();
        }

        LOG.debug("Cache entry not found for authenticator " + authenticatorConfigName + ". Fetching from DB.");
        UserDefinedLocalAuthenticatorConfig authenticatorConfig = authenticatorMgtFacade
                .getUserDefinedLocalAuthenticator(authenticatorConfigName, tenantId);

        authenticatorCache.addToCache(cacheKey, new AuthenticatorCacheEntry(authenticatorConfig), tenantId);
        LOG.debug("Entry fetched from DB for authenticator " + authenticatorConfigName + ". Adding cache entry.");
        return authenticatorConfig;
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorsCacheKey cacheKey = new UserDefinedLocalAuthenticatorsCacheKey(tenantId);
        UserDefinedLocalAuthenticatorsCacheEntry entry =
                userDefinedLocalAuthenticatorsCache.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            LOG.debug("Cache entry found for all user defined local authenticators of tenant id: " + tenantId);
            return entry.getUserDefinedLocalAuthenticators();
        }

        LOG.debug("Cache entry not found for all user defined local authenticators of tenant id: " + tenantId +
                ". Fetching from DB.");
        List<UserDefinedLocalAuthenticatorConfig> userDefinedLocalAuthenticators =
                authenticatorMgtFacade.getAllUserDefinedLocalAuthenticators(tenantId);

        if (userDefinedLocalAuthenticators != null) {
            userDefinedLocalAuthenticatorsCache.addToCache(cacheKey,
                    new UserDefinedLocalAuthenticatorsCacheEntry(userDefinedLocalAuthenticators), tenantId);
            LOG.debug("Entry fetched from DB for all user defined local authenticators of tenant id: " + tenantId +
                    ". Adding cache entry.");
        }
        return userDefinedLocalAuthenticators;
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, 
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        authenticatorCache.clearCacheEntry(new AuthenticatorCacheKey(authenticatorConfigName), tenantId);
        LOG.debug("Deleted cache entry of deleting authenticator " + authenticatorConfigName);
        userDefinedLocalAuthenticatorsCache.clearCacheEntry(new UserDefinedLocalAuthenticatorsCacheKey(tenantId),
                tenantId);
        LOG.debug("Deleted Cache entry for all user defined authenticators of tenant id: " + tenantId);
        authenticatorMgtFacade.deleteUserDefinedLocalAuthenticator(authenticatorConfigName, authenticatorConfig,
                tenantId);
    }

    @Override
    public boolean isExistingAuthenticatorName(String authenticatorConfigName, int tenantId)
            throws AuthenticatorMgtException {

        return authenticatorMgtFacade.isExistingAuthenticatorName(authenticatorConfigName, tenantId);
    }
}
