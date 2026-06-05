/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceCacheById;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceCacheByIdentifier;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceCacheEntry;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceIdCacheKey;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceIdentifierCacheKey;
import org.wso2.carbon.identity.api.resource.mgt.cache.ScopeMetadataCache;
import org.wso2.carbon.identity.api.resource.mgt.cache.ScopeMetadataCacheEntry;
import org.wso2.carbon.identity.api.resource.mgt.cache.ScopeMetadataCacheKey;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.APIResourceManagementDAO;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;

/**
 * This class implements the {@link APIResourceManagementDAO} interface.
 */
public class CacheBackedAPIResourceMgtDAO implements APIResourceManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedAPIResourceMgtDAO.class);
    private final APIResourceCacheByIdentifier apiResourceCacheByIdentifier;
    private final APIResourceCacheById apiResourceCacheById;
    private final ScopeMetadataCache scopeMetadataCache;
    private final APIResourceManagementDAO apiResourceManagementDAO;

    public CacheBackedAPIResourceMgtDAO(APIResourceManagementDAO apiResourceManagementDAO) {

        this.apiResourceManagementDAO = apiResourceManagementDAO;
        apiResourceCacheByIdentifier = APIResourceCacheByIdentifier.getInstance();
        apiResourceCacheById = APIResourceCacheById.getInstance();
        scopeMetadataCache = ScopeMetadataCache.getInstance();
    }

    @Override
    public List<APIResource> getAPIResources(Integer limit, Integer tenantId, String sortOrder,
                                             List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        return apiResourceManagementDAO.getAPIResources(limit, tenantId, sortOrder, expressionNodes);
    }

    @Override
    public List<APIResource> getAPIResourcesWithRequiredAttributes(Integer limit, Integer tenantId, String sortOrder,
                                                                   List<ExpressionNode> expressionNodes,
                                                                   List<String> requiredAttributes)
            throws APIResourceMgtException {

        return apiResourceManagementDAO.getAPIResourcesWithRequiredAttributes(limit, tenantId, sortOrder,
                expressionNodes, requiredAttributes);
    }

    @Override
    public Integer getAPIResourcesCount(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        return apiResourceManagementDAO.getAPIResourcesCount(tenantId, expressionNodes);
    }

    @Override
    public List<Scope> getScopesByAPI(String apiId, Integer tenantId) throws APIResourceMgtServerException {

        APIResourceIdCacheKey cacheKey = new APIResourceIdCacheKey(apiId);
        APIResourceCacheEntry entry = apiResourceCacheById.getValueFromCache(cacheKey, tenantId);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + apiId);
            }
            return entry.getAPIResource().getScopes();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for API Resource " + apiId + ". Fetching entry from DB");
        }
        return apiResourceManagementDAO.getScopesByAPI(apiId, tenantId);
    }

    @Override
    public APIResource addAPIResource(APIResource apiResource, Integer tenantId) throws APIResourceMgtException {

        // A new API resource introduces new tenant scopes; invalidate the tenant scope metadata cache.
        clearScopeMetadataCache(tenantId);
        return apiResourceManagementDAO.addAPIResource(apiResource, tenantId);
    }

    @Override
    public boolean isAPIResourceExist(String identifier, Integer tenantId) throws APIResourceMgtException {

        APIResourceIdentifierCacheKey cacheKey = new APIResourceIdentifierCacheKey(identifier);
        APIResourceCacheEntry entry = apiResourceCacheByIdentifier.getValueFromCache(cacheKey, tenantId);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + identifier);
            }
            return true;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry not found for API Resource " + identifier + ". Fetching entry from DB");
            }
        }
        return apiResourceManagementDAO.isAPIResourceExist(identifier, tenantId);
    }

    @Override
    public boolean isAPIResourceExistById(String apiId, Integer tenantId) throws APIResourceMgtException {

        APIResourceIdCacheKey cacheKey = new APIResourceIdCacheKey(apiId);
        APIResourceCacheEntry entry = apiResourceCacheById.getValueFromCache(cacheKey, tenantId);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + apiId);
            }
            return true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for API Resource " + apiId + ". Fetching entry from DB");
        }
        return apiResourceManagementDAO.isAPIResourceExistById(apiId, tenantId);
    }

    @Override
    public APIResource getAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException {

        APIResourceIdCacheKey cacheKey = new APIResourceIdCacheKey(apiId);
        APIResourceCacheEntry entry = apiResourceCacheById.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + apiId);
            }
            return entry.getAPIResource();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for API Resource " + apiId + ". Fetching entry from DB");
        }

        APIResource apiResource = apiResourceManagementDAO.getAPIResourceById(apiId, tenantId);

        if (apiResource != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for API Resource " + apiId + ". Updating cache");
            }
            apiResourceCacheById.addToCacheOnRead(cacheKey, new APIResourceCacheEntry(apiResource), tenantId);
            if (apiResource.getIdentifier() != null) {
                APIResourceIdentifierCacheKey apiResourceIdentifierCacheKey = new APIResourceIdentifierCacheKey(
                        apiResource.getIdentifier());
                apiResourceCacheByIdentifier.addToCacheOnRead(apiResourceIdentifierCacheKey,
                        new APIResourceCacheEntry(apiResource), tenantId);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for API Resource " + apiId + " not found in cache or DB");
            }
        }

        return apiResource;
    }

    @Override
    public APIResource getAPIResourceByIdentifier(String identifier, Integer tenantId)
            throws APIResourceMgtException {

        APIResourceIdentifierCacheKey cacheKey = new APIResourceIdentifierCacheKey(identifier);
        APIResourceCacheEntry entry = apiResourceCacheByIdentifier.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + identifier);
            }
            return entry.getAPIResource();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry not found for API Resource " + identifier + ". Fetching entry from DB");
            }
        }

        APIResource apiResource = apiResourceManagementDAO.getAPIResourceByIdentifier(identifier, tenantId);

        if (apiResource != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for API Resource " + identifier + ". Updating cache");
            }
            apiResourceCacheByIdentifier.addToCacheOnRead(cacheKey, new APIResourceCacheEntry(apiResource), tenantId);
            if (apiResource.getId() != null) {
                APIResourceIdCacheKey apiResourceIdCacheKey = new APIResourceIdCacheKey(apiResource.getId());
                apiResourceCacheById.addToCacheOnRead(apiResourceIdCacheKey,
                        new APIResourceCacheEntry(apiResource), tenantId);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for API Resource " + identifier + " not found in cache or DB");
            }
        }

        return apiResource;
    }

    @Override
    public void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                                  Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(apiResource.getIdentifier(), apiResource.getId(), tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.updateAPIResource(apiResource, addedScopes, removedScopes, tenantId);
    }

    @Override
    public void updateScopeMetadata(Scope scope, APIResource apiResource, Integer tenantId)
            throws APIResourceMgtException {

        clearAPIResourceCache(apiResource.getIdentifier(), apiResource.getId(), tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.updateScopeMetadata(scope, apiResource, tenantId);
    }

    @Override
    public void updateScopeMetadataById(Scope scope, APIResource apiResource, Integer tenantId)
            throws APIResourceMgtException {

        clearAPIResourceCache(apiResource.getIdentifier(), apiResource.getId(), tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.updateScopeMetadataById(scope, apiResource, tenantId);
    }

    @Override
    public void deleteAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.deleteAPIResourceById(apiId, tenantId);
    }

    @Override
    public boolean isScopeExistByName(String name, Integer tenantId) throws APIResourceMgtException {

        return apiResourceManagementDAO.isScopeExistByName(name, tenantId);
    }

    @Override
    public boolean isScopeExistById(String scopeId, Integer tenantId) throws APIResourceMgtException {

        return apiResourceManagementDAO.isScopeExistById(scopeId, tenantId);
    }

    @Override
    public Scope getScopeByNameAndTenantId(String name, Integer tenantId) throws APIResourceMgtException {

        return apiResourceManagementDAO.getScopeByNameAndTenantId(name, tenantId);
    }

    @Override
    public Scope getScopeByNameTenantIdAPIId(String name, Integer tenantId, String apiId)
            throws APIResourceMgtException {

        return apiResourceManagementDAO.getScopeByNameTenantIdAPIId(name, tenantId, apiId);
    }

    @Override
    public List<Scope> getScopesByTenantId(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        // Only the unfiltered "all tenant scopes" lookup is cached (the hot path used on the OAuth token
        // flow when authorize_all_scopes is enabled), and only when the cache knob is on. Filtered lookups
        // pass through to avoid a key explosion.
        if (!isScopeValidationCacheEnabled() || expressionNodes != null && !expressionNodes.isEmpty()) {
            return apiResourceManagementDAO.getScopesByTenantId(tenantId, expressionNodes);
        }

        ScopeMetadataCacheKey cacheKey =
                new ScopeMetadataCacheKey(APIResourceManagementConstants.TENANT_SCOPE_METADATA_CACHE_KEY);
        ScopeMetadataCacheEntry entry = scopeMetadataCache.getValueFromCache(cacheKey, tenantId);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scope metadata cache hit for tenant id: " + tenantId);
            }
            return entry.getScopes();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scope metadata cache miss for tenant id: " + tenantId + ". Fetching from DB.");
        }
        List<Scope> scopes = apiResourceManagementDAO.getScopesByTenantId(tenantId, expressionNodes);
        scopeMetadataCache.addToCacheOnRead(cacheKey, new ScopeMetadataCacheEntry(scopes), tenantId);
        return scopes;
    }

    private boolean isScopeValidationCacheEnabled() {

        return Boolean.parseBoolean(
                IdentityUtil.getProperty(APIResourceManagementConstants.ENABLE_SCOPE_VALIDATION_CACHE));
    }

    private void clearScopeMetadataCache(int tenantId) {

        ScopeMetadataCacheKey cacheKey =
                new ScopeMetadataCacheKey(APIResourceManagementConstants.TENANT_SCOPE_METADATA_CACHE_KEY);
        scopeMetadataCache.clearCacheEntry(cacheKey, tenantId);
    }

    @Override
    public void addScopes(List<Scope> scopes, String apiId, Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.addScopes(scopes, apiId, tenantId);
    }

    @Override
    public void deleteAllScopes(String apiId, Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.deleteAllScopes(apiId, tenantId);
    }

    @Override
    public void deleteScope(String apiId, String scopeName, Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.deleteScope(apiId, scopeName, tenantId);
    }

    @Override
    public void deleteScopeById(String apiId, String scopeId, Integer tenantId) throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.deleteScopeById(apiId, scopeId, tenantId);
    }

    @Override
    public void putScopes(String apiId, List<Scope> currentScopes, List<Scope> scopes, Integer tenantId)
            throws APIResourceMgtException {

        clearAPIResourceCache(null, apiId, tenantId);
        clearScopeMetadataCache(tenantId);
        apiResourceManagementDAO.putScopes(apiId, currentScopes, scopes, tenantId);
    }

    @Override
    public List<ApplicationBasicInfo> getSubscribedApplications(String apiId) throws APIResourceMgtException {

        return apiResourceManagementDAO.getSubscribedApplications(apiId);
    }

    @Override
    public List<APIResource> getScopeMetadata(List<String> scopeNames, Integer tenantId)
            throws APIResourceMgtException {

            return apiResourceManagementDAO.getScopeMetadata(scopeNames, tenantId);
    }

    private void clearAPIResourceCache(String identifier, String resourceId, int tenantId) throws
            APIResourceMgtException {

        // clearing cache entries related to the API Resource.
        APIResource apiResource = null;
        if (StringUtils.isNotBlank(resourceId)) {
            apiResource = this.getAPIResourceById(resourceId, tenantId);
        }
        if (StringUtils.isNotBlank(identifier)) {
            apiResource = this.getAPIResourceByIdentifier(identifier, tenantId);
        }

        if (apiResource != null) {

            resourceId = resourceId != null ? resourceId : apiResource.getId();
            identifier = identifier != null ? identifier : apiResource.getIdentifier();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing entry for API Resource " + apiResource.getName() + " of tenantId:"
                        + tenantId + " from cache.");
            }

            APIResourceIdCacheKey apiResourceIdCacheKey = new APIResourceIdCacheKey(resourceId);
            apiResourceCacheById.clearCacheEntry(apiResourceIdCacheKey, tenantId);

            APIResourceIdentifierCacheKey apiResourceIdentifierCacheKey = new APIResourceIdentifierCacheKey(identifier);
            apiResourceCacheByIdentifier.clearCacheEntry(apiResourceIdentifierCacheKey, tenantId);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for API Resource " + identifier + " not found in cache or DB");
            }
        }
    }
}
