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

package org.wso2.carbon.identity.api.resource.mgt;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.APIResourceManagementDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.CacheBackedAPIResourceMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.model.APIResourceSearchResult;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * API resource management service.
 */
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
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before);
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
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        result.setTotalCount(CACHE_BACKED_DAO.getAPIResourcesCount(tenantId, expressionNodes));
        result.setAPIResources(CACHE_BACKED_DAO.getAPIResourcesWithRequiredAttributes(limit, tenantId, sortOrder,
                expressionNodes, requiredAttributes));
        return result;
    }

    @Override
    public APIResource getAPIResourceById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

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
            return CACHE_BACKED_DAO.addAPIResource(apiResource, tenantId);
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE, e);
        }
    }

    @Override
    public void deleteAPIResourceById(String apiResourceId, String tenantDomain) throws APIResourceMgtException {

        CACHE_BACKED_DAO.deleteAPIResourceById(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                                  String tenantDomain) throws APIResourceMgtException {

        CACHE_BACKED_DAO.updateAPIResource(apiResource, addedScopes, removedScopes,
                IdentityTenantUtil.getTenantId(tenantDomain));
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

        CACHE_BACKED_DAO.deleteAllScopes(apiResourceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAPIScopeByScopeName(String apiResourceId, String scopeName, String tenantDomain)
            throws APIResourceMgtException {

        CACHE_BACKED_DAO.deleteScope(apiResourceId, scopeName, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void putScopes(String apiResourceId, List<Scope> currentScopes, List<Scope> scopes, String tenantDomain)
            throws APIResourceMgtException {

        CACHE_BACKED_DAO.putScopes(apiResourceId, currentScopes, scopes, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Scope> getScopesByTenantDomain(String tenantDomain, String filter) throws APIResourceMgtException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, null, null);
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

    /**
     * Get the filter node as a list.
     *
     * @param filter Filter string.
     * @param after  After cursor.
     * @param before Before cursor.
     * @throws APIResourceMgtClientException Error when validate filters.
     */
    private List<ExpressionNode> getExpressionNodes(String filter, String after, String before)
            throws APIResourceMgtClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        filter = StringUtils.isBlank(filter) ? StringUtils.EMPTY : filter;
        String paginatedFilter = this.getPaginatedFilter(filter, after, before);
        try {
            if (StringUtils.isNotBlank(paginatedFilter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(paginatedFilter);
                Node rootNode = filterTreeBuilder.buildTree();
                this.setExpressionNodeList(rootNode, expressionNodes);
            }
            return expressionNodes;
        } catch (IOException | IdentityException e) {
            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT);
        }
    }

    /**
     * Get pagination filter.
     *
     * @param paginatedFilter Filter string.
     * @param after           After cursor.
     * @param before          Before cursor.
     * @return Filter string.
     * @throws APIResourceMgtClientException Error when validate filters.
     */
    private String getPaginatedFilter(String paginatedFilter, String after, String before) throws
            APIResourceMgtClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and " +
                        APIResourceManagementConstants.BEFORE_GT + decodedString :
                        APIResourceManagementConstants.BEFORE_GT + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and " +
                        APIResourceManagementConstants.AFTER_LT + decodedString :
                        APIResourceManagementConstants.AFTER_LT + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION);
        }
        return paginatedFilter;
    }

    /**
     * Set the node values as list of expression.
     *
     * @param node       filter node.
     * @param expression list of expression.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression) {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                expression.add((ExpressionNode) node);
            }
        } else if (node instanceof OperationNode) {
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }
}
