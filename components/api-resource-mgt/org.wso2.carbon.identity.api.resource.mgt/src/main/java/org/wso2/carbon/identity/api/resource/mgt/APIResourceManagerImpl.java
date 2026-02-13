/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt;

import org.apache.commons.lang.StringUtils;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.APIResourceManagementDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.CacheBackedAPIResourceMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.model.APIResourceSearchResult;
import org.wso2.carbon.identity.api.resource.mgt.publisher.APIResourceManagerEventPublisherProxy;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.api.resource.mgt.util.FilterQueriesUtil;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.util.List;

/**
 * API resource management service.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.api.resource.mgt.APIResourceManager",
                "service.scope=singleton"
        }
)
public class APIResourceManagerImpl implements APIResourceManager {

    private static final APIResourceManager INSTANCE = new APIResourceManagerImpl();

    private static final CacheBackedAPIResourceMgtDAO CACHE_BACKED_DAO =
            new CacheBackedAPIResourceMgtDAO(new APIResourceManagementDAOImpl());

    private APIResourceManagerImpl() {

    }

    public static APIResourceManager getInstance() {

        return INSTANCE;
    }

    @Override
    public APIResourceSearchResult getAPIResources(String after, String before, Integer limit, String filter,
                                                   String sortOrder, String tenantDomain)
            throws APIResourceMgtException {

        APIResourceSearchResult result = new APIResourceSearchResult();
        List<ExpressionNode> expressionNodes = FilterQueriesUtil.getExpressionNodes(filter, after, before);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        result.setTotalCount(CACHE_BACKED_DAO.getAPIResourcesCount(tenantId, expressionNodes));
        result.setAPIResources(CACHE_BACKED_DAO.getAPIResources(limit, tenantId, sortOrder, expressionNodes));
        return result;
    }

    @Override
    public APIResourceSearchResult getAPIResourcesWithRequiredAttributes(String after, String before, Integer limit,
                                                                         String filter, String sortOrder,
                                                                         String tenantDomain,
                                                                         List<String> requiredAttributes)
            throws APIResourceMgtException {

        APIResourceSearchResult result = new APIResourceSearchResult();
        List<ExpressionNode> expressionNodes = FilterQueriesUtil.getExpressionNodes(filter, after, before);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        result.setTotalCount(CACHE_BACKED_DAO.getAPIResourcesCount(tenantId, expressionNodes));
        result.setAPIResources(CACHE_BACKED_DAO.getAPIResourcesWithRequiredAttributes(limit, tenantId, sortOrder,
                expressionNodes, requiredAttributes));
        return result;
    }

    @Override
    public APIResource getAPIResourceById(String apiResourceId, String tenantDomain) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                String rootOrgTenantDomain = OrganizationManagementUtil
                        .getRootOrgTenantDomainBySubOrgTenantDomain(tenantDomain);
                APIResource apiResource = CACHE_BACKED_DAO.getAPIResourceById(apiResourceId,
                        IdentityTenantUtil.getTenantId(rootOrgTenantDomain));

                // Return the API resource only if its type is inheritable.
                return (apiResource != null
                        && APIResourceManagementUtil.isAllowedAPIResourceTypeForOrganizations(apiResource.getType()))
                        ? apiResource
                        : null;
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                    ERROR_CODE_ERROR_WHILE_RETRIEVING_ROOT_ORGANIZATION_TENANT_DOMAIN, e, tenantDomain);
        }
        return CACHE_BACKED_DAO.getAPIResourceById(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public APIResource addAPIResource(APIResource apiResource, String tenantDomain)
            throws APIResourceMgtException {

        try {
            /*
            Restrict API resource creation for organizations.
            Check whether the tenant is an organization and return a client error if it is.
            */
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                throw APIResourceManagementUtil.handleClientException(
                        APIResourceManagementConstants.ErrorMessages.ERROR_CODE_CREATION_RESTRICTED);
            }

            if (StringUtils.isBlank(apiResource.getIdentifier())) {
                throw APIResourceManagementUtil.handleClientException(
                        APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_IDENTIFIER_VALUE);
            }

            APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
            publisherProxy.publishPreAddAPIResourceWithException(apiResource, tenantDomain);

            // Check whether the API resource already exists. This is being handled in the service layer since the
            // system APIs are registered in the database in a tenant-agnostic manner.
            if (getAPIResourceByIdentifier(apiResource.getIdentifier(), tenantDomain) != null) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants
                        .ErrorMessages.ERROR_CODE_API_RESOURCE_ALREADY_EXISTS, tenantDomain);
            }

            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            // If the API resource is a system API, set the tenant id to 0 since they are not tenant specific.
            if (APIResourceManagementUtil.isSystemAPI(apiResource.getType())) {
                tenantId = 0;
            }

            APIResource apiResourceCreated = CACHE_BACKED_DAO.addAPIResource(apiResource, tenantId);
            publisherProxy.publishPostAddAPIResource(apiResource, tenantDomain);
            return apiResourceCreated;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE, e);
        }
    }

    @Override
    public void deleteAPIResourceById(String apiResourceId, String tenantDomain) throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPreDeleteAPIResourceByIdWithException(apiResourceId, tenantDomain);
        CACHE_BACKED_DAO.deleteAPIResourceById(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostDeleteAPIResourceById(apiResourceId, tenantDomain);
    }

    @Override
    public void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                                  String tenantDomain) throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPreUpdateAPIResourceWithException(apiResource, addedScopes, removedScopes, tenantDomain);
        CACHE_BACKED_DAO.updateAPIResource(apiResource, addedScopes, removedScopes,
                IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostUpdateAPIResource(apiResource, addedScopes, removedScopes, tenantDomain);
    }

    @Override
    public void updateScopeMetadata(Scope scope, APIResource apiResource, String tenantDomain)
            throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPreUpdateScopeMetadataWithException(scope, apiResource, tenantDomain);
        CACHE_BACKED_DAO.updateScopeMetadata(scope, apiResource, IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostUpdateScopeMetadataWithException(scope, apiResource, tenantDomain);
    }

    @Override
    public APIResource getAPIResourceByIdentifier(String apiResourceIdentifier, String tenantDomain)
            throws APIResourceMgtException {

        return CACHE_BACKED_DAO.getAPIResourceByIdentifier(apiResourceIdentifier,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Scope> getAPIScopesById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

        return CACHE_BACKED_DAO.getScopesByAPI(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAPIScopesById(String apiResourceId, String tenantDomain) throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPreDeleteAPIScopesByIdWithException(apiResourceId, tenantDomain);
        CACHE_BACKED_DAO.deleteAllScopes(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostDeleteAPIScopesById(apiResourceId, tenantDomain);
    }

    @Override
    public void deleteAPIScopeByScopeName(String apiResourceId, String scopeName, String tenantDomain)
            throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPreDeleteAPIScopeByScopeNameWithException(apiResourceId, scopeName, tenantDomain);
        CACHE_BACKED_DAO.deleteScope(apiResourceId, scopeName, IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostDeleteAPIScopeByScopeName(apiResourceId, scopeName, tenantDomain);
    }

    @Override
    public void putScopes(String apiResourceId, List<Scope> currentScopes, List<Scope> scopes, String tenantDomain)
            throws APIResourceMgtException {

        APIResourceManagerEventPublisherProxy publisherProxy = APIResourceManagerEventPublisherProxy.getInstance();
        publisherProxy.publishPrePutScopesWithException(apiResourceId, currentScopes, scopes, tenantDomain);
        CACHE_BACKED_DAO.putScopes(apiResourceId, currentScopes, scopes, IdentityTenantUtil.getTenantId(tenantDomain));
        publisherProxy.publishPostPutScopes(apiResourceId, currentScopes, scopes, tenantDomain);
    }

    @Override
    public List<Scope> getScopesByTenantDomain(String tenantDomain, String filter) throws APIResourceMgtException {

        List<ExpressionNode> expressionNodes = FilterQueriesUtil.getExpressionNodes(filter, null, null);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return CACHE_BACKED_DAO.getScopesByTenantId(tenantId, expressionNodes);
    }

    @Override
    public Scope getScopeByName(String scopeName, String tenantDomain) throws APIResourceMgtException {

        return CACHE_BACKED_DAO.getScopeByNameAndTenantId(scopeName, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<APIResource> getScopeMetadata(List<String> scopeNames, String tenantDomain)
            throws APIResourceMgtException {

        return CACHE_BACKED_DAO.getScopeMetadata(scopeNames, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Scope> getSystemAPIScopes(String tenantDomain) throws APIResourceMgtException {

        List<Scope> systemScopes = getScopesByTenantDomain(tenantDomain,
                APIResourceManagementConstants.INTERNAL_SCOPE_FILTER);
        systemScopes.addAll(getScopesByTenantDomain(tenantDomain, APIResourceManagementConstants.CONSOLE_SCOPE_FILTER));
        return systemScopes;
    }

}
