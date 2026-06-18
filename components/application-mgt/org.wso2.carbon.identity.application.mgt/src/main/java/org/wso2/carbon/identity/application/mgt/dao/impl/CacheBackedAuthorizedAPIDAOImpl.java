/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedAPICache;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedAPICacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedAPICacheKey;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedScopesCache;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedScopesCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedScopesCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.util.List;

/**
 * Cache backed implementation of {@link AuthorizedAPIDAO}.
 */
public class CacheBackedAuthorizedAPIDAOImpl implements AuthorizedAPIDAO {

    private final AuthorizedAPIDAO authorizedAPIDAO;

    private static AuthorizedAPICache authorizedAPICache;

    private static AuthorizedScopesCache authorizedScopesCache;

    private static final Log LOG = LogFactory.getLog(CacheBackedAuthorizedAPIDAOImpl.class);

    public CacheBackedAuthorizedAPIDAOImpl(AuthorizedAPIDAO authorizedAPIDAO) {

        this.authorizedAPIDAO = authorizedAPIDAO;
        authorizedAPICache = AuthorizedAPICache.getInstance();
        authorizedScopesCache = AuthorizedScopesCache.getInstance();
    }

    @Override
    public void addAuthorizedAPI(String applicationId, String apiId, String policyId, List<Scope> scopes, int tenantId)
            throws IdentityApplicationManagementException {

        clearAuthorizedScopesFromCache(applicationId, tenantId);
        authorizedAPIDAO.addAuthorizedAPI(applicationId, apiId, policyId, scopes, tenantId);
    }

    @Override
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        return authorizedAPIDAO.getAuthorizedAPIs(applicationId, tenantId);
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes, List<String> removedScopes,
                                   int tenantId) throws IdentityApplicationManagementException {

        this.patchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, null, null, tenantId);
    }

    @Override
    public void deleteAuthorizedAPI(String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        clearAuthorizedAPIFromCache(appId, apiId, tenantId);
        clearAuthorizedScopesFromCache(appId, tenantId);
        authorizedAPIDAO.deleteAuthorizedAPI(appId, apiId, tenantId);
    }

    @Override
    public List<AuthorizedScopes> getAuthorizedScopes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        // Sub-orgs inherit root org API resources; cache only for tenants and resolve orgs live to avoid stale entries.
        if (isOrganization(tenantId)) {
            return authorizedAPIDAO.getAuthorizedScopes(applicationId, tenantId);
        }

        List<AuthorizedScopes> authorizedScopes = getAuthorizedScopesFromCache(applicationId, tenantId);
        if (authorizedScopes == null) {
            authorizedScopes = authorizedAPIDAO.getAuthorizedScopes(applicationId, tenantId);
            // getAuthorizedScopes never returns null; cache the result (incl. empty lists) to avoid
            // re-querying applications that have no authorized scopes.
            addAuthorizedScopesToCache(applicationId, authorizedScopes, tenantId);
        }
        return authorizedScopes;
    }

    /**
     * Whether the tenant is an organization, defaulting to {@code true} (bypass cache) if it cannot be resolved,
     * so an org-resolution error degrades to a live read rather than breaking the lookup.
     */
    private boolean isOrganization(int tenantId) {

        try {
            return OrganizationManagementUtil.isOrganization(tenantId);
        } catch (OrganizationManagementException e) {
            LOG.error("Error checking whether tenant id: " + tenantId + " is an organization.", e);
            return true;
        }
    }

    private List<AuthorizedScopes> getAuthorizedScopesFromCache(String appId, int tenantId) {

        if (StringUtils.isNotBlank(appId)) {
            AuthorizedScopesCacheKey cacheKey = new AuthorizedScopesCacheKey(appId);
            AuthorizedScopesCacheEntry cacheEntry = authorizedScopesCache.getValueFromCache(cacheKey, tenantId);
            if (cacheEntry != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authorized scopes found in cache for application id: " + appId + " in tenant id: "
                            + tenantId);
                }
                return cacheEntry.getAuthorizedScopes();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorized scopes not found in cache for application id: " + appId + " in tenant id: "
                        + tenantId);
            }
        } else {
            LOG.debug("Application id is empty. Cannot retrieve authorized scopes from cache.");
        }
        return null;
    }

    private void addAuthorizedScopesToCache(String appId, List<AuthorizedScopes> authorizedScopes, int tenantId) {

        if (StringUtils.isNotBlank(appId)) {
            AuthorizedScopesCacheKey cacheKey = new AuthorizedScopesCacheKey(appId);
            AuthorizedScopesCacheEntry cacheEntry = new AuthorizedScopesCacheEntry(authorizedScopes);
            authorizedScopesCache.addToCacheOnRead(cacheKey, cacheEntry, tenantId);
        } else {
            LOG.debug("Application id is empty. Cannot add authorized scopes to cache.");
        }
    }

    private void clearAuthorizedScopesFromCache(String appId, int tenantId) {

        if (StringUtils.isNotBlank(appId)) {
            AuthorizedScopesCacheKey cacheKey = new AuthorizedScopesCacheKey(appId);
            authorizedScopesCache.clearCacheEntry(cacheKey, tenantId);
        } else {
            LOG.debug("Application id is empty. Cannot clear authorized scopes from cache.");
        }
    }

    @Override
    public AuthorizedAPI getAuthorizedAPI(String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        AuthorizedAPI authorizedAPI = getAuthorizedAPIFromCache(appId, apiId, tenantId);
        if (authorizedAPI == null) {
            try {
                authorizedAPI = authorizedAPIDAO.getAuthorizedAPI(appId, apiId, tenantId);
                if (authorizedAPI != null) {
                    addToCache(appId, apiId, authorizedAPI, tenantId);
                }
            } catch (IdentityApplicationManagementException e) {
                String error = "Error while retrieving authorized API for application id: " + appId + " and api id: "
                        + apiId + " in tenant id: " + tenantId;
                throw new IdentityApplicationManagementException(error, e);
            }
        }
        return authorizedAPI;
    }

    private AuthorizedAPI getAuthorizedAPIFromCache(String appId, String apiId, int tenantId) {

        AuthorizedAPI authorizedAPI = null;
        if (StringUtils.isNotBlank(appId)) {
            AuthorizedAPICacheKey cacheKey = new AuthorizedAPICacheKey(appId, apiId);
            AuthorizedAPICacheEntry cacheEntry = authorizedAPICache.getValueFromCache(cacheKey, tenantId);
            if (cacheEntry != null) {
                authorizedAPI = cacheEntry.getAuthorizedAPI();
            }
        } else {
            LOG.debug("Application id is empty. Cannot retrieve authorized APIs from cache.");
        }

        if (authorizedAPI == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorized APIs not found in cache for application id: " + appId + " in tenant id: "
                        + tenantId);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorized APIs found in cache for application id: " + appId + " in tenant id: "
                        + tenantId);
            }
        }
        return authorizedAPI;
    }

    private void addToCache(String appId, String apiId, AuthorizedAPI authorizedAPI, int tenantId) {

        if (StringUtils.isNotBlank(appId)) {
            AuthorizedAPICacheKey cacheKey = new AuthorizedAPICacheKey(appId, apiId);
            AuthorizedAPICacheEntry cacheEntry = new AuthorizedAPICacheEntry(authorizedAPI);
            authorizedAPICache.addToCacheOnRead(cacheKey, cacheEntry, tenantId);
        } else {
            LOG.debug("Application id is empty. Cannot add authorized APIs to cache.");
        }
    }

    private void clearAuthorizedAPIFromCache(String appId, String apiId, int tenantId) {

        if (StringUtils.isNotBlank(appId)) {
            AuthorizedAPICacheKey cacheKey = new AuthorizedAPICacheKey(appId, apiId);
            authorizedAPICache.clearCacheEntry(cacheKey, tenantId);
        } else {
            LOG.debug("Application id is empty. Cannot clear authorized APIs from cache.");
        }
    }

    @Override
    public void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, int tenantId)
            throws IdentityApplicationManagementException {

        clearAuthorizedScopesFromCache(applicationId, tenantId);
        this.authorizedAPIDAO.addAuthorizedAPI(applicationId, authorizedAPI, tenantId);
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> scopesToAdd, List<String> scopesToRemove,
            List<String> authorizationDetailsTypesToAdd, List<String> authorizationDetailsTypesToRemove, int tenantId)
            throws IdentityApplicationManagementException {

        clearAuthorizedAPIFromCache(appId, apiId, tenantId);
        clearAuthorizedScopesFromCache(appId, tenantId);
        this.authorizedAPIDAO.patchAuthorizedAPI(appId, apiId, scopesToAdd, scopesToRemove,
                authorizationDetailsTypesToAdd, authorizationDetailsTypesToRemove, tenantId);
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizedAuthorizationDetailsTypes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        return this.authorizedAPIDAO.getAuthorizedAuthorizationDetailsTypes(applicationId, tenantId);
    }
}
