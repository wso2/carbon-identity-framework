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

package org.wso2.carbon.identity.api.resource.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.APIResourceManagementDAO;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.api.resource.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil;
import org.wso2.carbon.identity.api.resource.mgt.util.FilterQueriesUtil;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.APIResourceProperty;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.AFTER;
import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.BEFORE;

/**
 * This class implements the {@link APIResourceManagementDAO} interface.
 */
public class APIResourceManagementDAOImpl implements APIResourceManagementDAO {

    private final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO;

    public APIResourceManagementDAOImpl() {

        this(new AuthorizationDetailsTypeMgtDAOImpl());
    }

    public APIResourceManagementDAOImpl(final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO) {

        this.authorizationDetailsTypeMgtDAO = authorizationDetailsTypeMgtDAO;
    }

    @Override
    public List<APIResource> getAPIResources(Integer limit, Integer tenantId, String sortOrder,
                                             List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        return getAPIResourcesList(limit, tenantId, sortOrder, expressionNodes);
    }

    @Override
    public List<APIResource> getAPIResourcesWithRequiredAttributes(Integer limit, Integer tenantId, String sortOrder,
                                                                   List<ExpressionNode> expressionNodes,
                                                                   List<String> requiredAttributes)
            throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(requiredAttributes) ||
                !requiredAttributes.contains(APIResourceManagementConstants.PROPERTIES)) {
            return getAPIResourcesList(limit, tenantId, sortOrder, expressionNodes);
        }
        return getAPIResourcesListWithProperties(limit, tenantId, sortOrder, expressionNodes);
    }

    @Override
    public Integer getAPIResourcesCount(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        // Remove the after and before attributes from the expression nodes list,
        // since we are not considering them when getting the total count.
        List<ExpressionNode> expressionNodesCopy = new ArrayList<>(expressionNodes);
        if (CollectionUtils.isNotEmpty(expressionNodesCopy)) {
            expressionNodesCopy.removeIf(expressionNode -> AFTER.equals(expressionNode.getAttributeValue()) ||
                    BEFORE.equals(expressionNode.getAttributeValue()));
        }
        FilterQueryBuilder filterQueryBuilder = FilterQueriesUtil.getApiResourceFilterQueryBuilder(expressionNodesCopy);

        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        String getAPIResourcesCountSqlStmtTail = SQLConstants.GET_API_RESOURCES_COUNT_TAIL;

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                tenantId = getRootOrganizationTenantId(tenantId);
                getAPIResourcesCountSqlStmtTail = SQLConstants.GET_API_RESOURCES_COUNT_FOR_ORGANIZATIONS_TAIL;
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                    .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }

        String sqlStmt = SQLConstants.GET_API_RESOURCES_COUNT + filterQueryBuilder.getFilterQuery() +
                getAPIResourcesCountSqlStmtTail;

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt);
            int filterAttrSize = 0;
            if (filterAttributeValue != null) {
                for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                    prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
                }
                filterAttrSize = filterAttributeValue.entrySet().size();
            }
            prepStmt.setInt(filterAttrSize + 1, tenantId);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new APIResourceMgtServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES
                            .getCode(),
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES
                            .getMessage(), e);
        }
        return 0;
    }

    @Override
    public APIResource addAPIResource(APIResource apiResource, Integer tenantId) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants.ErrorMessages
                        .ERROR_CODE_ADDING_API_RESOURCE_NOT_SUPPORTED_FOR_ORGANIZATIONS);
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                            ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        String generatedAPIId = UUID.randomUUID().toString();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_API_RESOURCE);
                prepStmt.setString(1, generatedAPIId);
                prepStmt.setString(2, apiResource.getType());
                prepStmt.setString(3, apiResource.getName());
                prepStmt.setString(4, apiResource.getIdentifier());
                prepStmt.setString(5, apiResource.getDescription());
                prepStmt.setObject(6, tenantId == 0 ? null : tenantId);
                prepStmt.setBoolean(7, apiResource.isAuthorizationRequired());
                prepStmt.executeUpdate();
                prepStmt.clearParameters();

                if (CollectionUtils.isNotEmpty(apiResource.getScopes())) {
                    // Add scopes.
                    addScopes(dbConnection, generatedAPIId, apiResource.getScopes(), tenantId, apiResource.getType());
                }
                if (CollectionUtils.isNotEmpty(apiResource.getProperties())) {
                    // Add properties.
                    addAPIResourceProperties(dbConnection, generatedAPIId, apiResource.getProperties());
                }
                if (CollectionUtils.isNotEmpty(apiResource.getAuthorizationDetailsTypes()) &&
                        AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsEnabled()) {
                    // Add authorization details types.
                    this.authorizationDetailsTypeMgtDAO.addAuthorizationDetailsTypes(dbConnection,
                            generatedAPIId, apiResource.getAuthorizationDetailsTypes(), tenantId);
                }
                IdentityDatabaseUtil.commitTransaction(dbConnection);

                return getAPIResourceById(generatedAPIId, tenantId);
            } catch (SQLException | APIResourceMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE, e);
        }
    }

    @Override
    public List<Scope> getScopesByAPI(String apiId, Integer tenantId) throws APIResourceMgtServerException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        List<Scope> scopes = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_SCOPES_BY_API_ID)) {

            preparedStatement.setString(1, apiId);
            preparedStatement.setInt(2, tenantIdToSearchScopes);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Scope scope = new Scope(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DISPLAY_NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME)
                );
                scopes.add(scope);
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPES, e);
        }
        return scopes;
    }

    @Override
    public boolean isAPIResourceExist(String identifier, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.IS_API_RESOURCE_EXIST_BY_IDENTIFIER)) {

            preparedStatement.setString(1, identifier);
            preparedStatement.setInt(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_API_RESOURCE_EXISTENCE,
                    e);
        }
        return false;
    }

    @Override
    public boolean isAPIResourceExistById(String apiId, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_BY_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setInt(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_API_RESOURCE_EXISTENCE,
                    e);
        }
        return false;
    }

    @Override
    public APIResource getAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_BY_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setInt(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<APIResourceProperty> apiResourceProperties = getAPIResourcePropertiesByAPIId(dbConnection, apiId);

            final APIResource apiResource = getApiResource(resultSet, apiResourceProperties);
            this.assignAuthorizationDetailsTypesToApiResource(apiResource, tenantId);

            return apiResource;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES, e);
        }
    }

    @Override
    public APIResource getAPIResourceByIdentifier(String identifier, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_BY_IDENTIFIER)) {
            preparedStatement.setString(1, identifier);
            preparedStatement.setInt(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<APIResourceProperty> apiResourceProperties =
                    getAPIResourcePropertiesByAPIIdentifier(dbConnection, identifier, tenantId);

            final APIResource apiResource = getApiResource(resultSet, apiResourceProperties);
            this.assignAuthorizationDetailsTypesToApiResource(apiResource, tenantId);

            return apiResource;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES, e);
        }
    }

    @Override
    public void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                                  Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.UPDATE_API_RESOURCE)) {
            try {
                preparedStatement.setString(1, apiResource.getName());
                preparedStatement.setString(2, apiResource.getDescription());
                preparedStatement.setString(3, apiResource.getType());
                preparedStatement.setString(4, apiResource.getId());
                preparedStatement.executeUpdate();

                // If the API resource is a system API, set the tenant id to 0 since they are not tenant specific.
                if (APIResourceManagementUtil.isSystemAPI(apiResource.getType())) {
                    tenantId = 0;
                }

                if (CollectionUtils.isNotEmpty(removedScopes)) {
                    // Delete Scopes.
                    deleteScopes(dbConnection, removedScopes, tenantId);
                }

                if (CollectionUtils.isNotEmpty(addedScopes)) {
                    // Add Scopes.
                    addScopes(dbConnection, apiResource.getId(), addedScopes, tenantId, apiResource.getType());
                }

                if (CollectionUtils.isNotEmpty(apiResource.getAuthorizationDetailsTypes()) &&
                        AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsEnabled()) {
                    // Update authorization details types
                    this.authorizationDetailsTypeMgtDAO.updateAuthorizationDetailsTypes(dbConnection,
                            apiResource.getId(), apiResource.getAuthorizationDetailsTypes(), tenantId);
                }

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException | APIResourceMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_API_RESOURCE, e);
        }
    }

    @Override
    public void updateScopeMetadata(Scope scope, APIResource apiResource, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.UPDATE_SCOPE_METADATA)) {
            try {
                preparedStatement.setString(1, scope.getDisplayName());
                preparedStatement.setString(2, scope.getDescription());
                preparedStatement.setString(3, scope.getName());
                preparedStatement.setInt(4, tenantId);
                preparedStatement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_SCOPE_METADATA, e);
        }
    }

    @Override
    public void deleteAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants.ErrorMessages
                        .ERROR_CODE_DELETING_API_RESOURCE_NOT_SUPPORTED_FOR_ORGANIZATIONS);
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                            ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {

                if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsEnabled()) {
                    // Remove authorization details types
                    this.authorizationDetailsTypeMgtDAO
                            .deleteAuthorizationDetailsTypesByApiId(dbConnection, apiId, tenantId);
                }

                PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_SCOPES_BY_API);
                prepStmt.setString(1, apiId);
                prepStmt.setInt(2, tenantId);
                prepStmt.executeUpdate();

                prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_API_RESOURCE);
                prepStmt.setString(1, apiId);
                prepStmt.setInt(2, tenantId);
                prepStmt.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_API_RESOURCE, e);
        }
    }

    @Override
    public boolean isScopeExistByName(String name, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            return isScopeExists(dbConnection, name, tenantId);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_EXISTENCE_OF_SCOPE, e);
        }
    }

    private boolean isScopeExists(Connection connection, String name, Integer tenantId)
            throws APIResourceMgtServerException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, tenantIdToSearchScopes);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_EXISTENCE_OF_SCOPE, e);
        }
    }

    @Override
    public boolean isScopeExistById(String scopeId, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.IS_SCOPE_EXIST_BY_ID)) {
            preparedStatement.setString(1, scopeId);
            preparedStatement.setInt(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_EXISTENCE_OF_SCOPE, e);
        }
        return false;
    }

    @Override
    public Scope getScopeByNameAndTenantId(String name, Integer tenantId) throws APIResourceMgtException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                    .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, tenantIdToSearchScopes);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Scope(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DISPLAY_NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.SCOPE_API_ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.TENANT_ID_COLUMN_NAME)
                );
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_SCOPES, e);
        }
        return null;
    }

    @Override
    public Scope getScopeByNameTenantIdAPIId(String name, Integer tenantId, String apiId)
            throws APIResourceMgtException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_API_ID)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, apiId);
            preparedStatement.setInt(3, tenantIdToSearchScopes);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Scope(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DISPLAY_NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.SCOPE_API_ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.TENANT_ID_COLUMN_NAME)
                );
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_SCOPES, e);
        }
        return null;
    }

    @Override
    public List<Scope> getScopesByTenantId(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            FilterQueryBuilder filterQueryBuilder = FilterQueriesUtil.getScopeFilterQueryBuilder(expressionNodes);
            String query = SQLConstants.GET_SCOPES_BY_TENANT_ID + filterQueryBuilder.getFilterQuery() +
                    SQLConstants.GET_SCOPES_BY_TENANT_ID_TAIL;
            try {
                if (OrganizationManagementUtil.isOrganization(tenantId)) {
                    FilterQueryBuilder filterQueryBuilderForOrg =
                            FilterQueriesUtil.getScopeFilterQueryBuilderForOrganizations(expressionNodes);
                    tenantId = getRootOrganizationTenantId(tenantId);
                    query = SQLConstants.GET_SCOPES_BY_TENANT_ID_FOR_ORGANIZATIONS + filterQueryBuilderForOrg
                            .getFilterQuery() + SQLConstants.GET_SCOPES_BY_TENANT_ID_FOR_ORGANIZATIONS_TAIL;
                }
            } catch (OrganizationManagementException e) {
                throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                                .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                        IdentityTenantUtil.getTenantDomain(tenantId));
            }
            PreparedStatement preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setInt(1, tenantId);
            int filterAttrSize = 0;
            if (filterQueryBuilder.getFilterAttributeValue() != null) {
                for (Map.Entry<Integer, String> prepareStatement :
                        filterQueryBuilder.getFilterAttributeValue().entrySet()) {
                    preparedStatement.setString(prepareStatement.getKey(), prepareStatement.getValue());
                }
                filterAttrSize = filterQueryBuilder.getFilterAttributeValue().entrySet().size();
            }
            preparedStatement.setInt(filterAttrSize + 1, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Scope> scopesList = new ArrayList<>();
            while (resultSet.next()) {
                scopesList.add(new Scope(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DISPLAY_NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.SCOPE_API_ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.TENANT_ID_COLUMN_NAME)
                ));
            }
            return scopesList;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_SCOPES, e);
        }
    }

    @Override
    public void addScopes(List<Scope> scopes, String apiId, Integer tenantId) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants.ErrorMessages
                        .ERROR_CODE_ADDING_SCOPES_NOT_SUPPORTED_FOR_ORGANIZATIONS);
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                            ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            addScopes(dbConnection, apiId, scopes, tenantId, null);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_SCOPES, e);
        }
    }

    @Override
    public void deleteAllScopes(String apiId, Integer tenantId) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants.ErrorMessages
                        .ERROR_CODE_DELETING_SCOPES_NOT_SUPPORTED_FOR_ORGANIZATIONS);
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                            ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            deleteScopeByAPIId(dbConnection, apiId, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_SCOPES, e);
        }
    }

    @Override
    public void deleteScope(String apiId, String scopeName, Integer tenantId) throws APIResourceMgtException {

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                throw APIResourceManagementUtil.handleClientException(APIResourceManagementConstants.ErrorMessages
                        .ERROR_CODE_DELETING_SCOPES_NOT_SUPPORTED_FOR_ORGANIZATIONS);
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages.
                            ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            deleteScopeByName(dbConnection, scopeName, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_SCOPES, e);
        }
    }

    @Override
    public void putScopes(String apiId, List<Scope> currentScopes, List<Scope> scopes, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                // Delete the existing scopes and commit.
                deleteScopeByAPIId(dbConnection, apiId, tenantId);
                IdentityDatabaseUtil.commitTransaction(dbConnection);
                // Add the new scopes and commit.
                addScopes(dbConnection, apiId, scopes, tenantId, null);
                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (APIResourceMgtException e) {

                // Rollback the transaction if any error occurred and add back the previous scopes.
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                addScopes(dbConnection, apiId, currentScopes, tenantId, null);
                IdentityDatabaseUtil.commitTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_SCOPES, e);
        }
    }

    @Override
    public List<ApplicationBasicInfo> getSubscribedApplications(String apiId) {
        return null;
    }

    @Override
    public List<APIResource> getScopeMetadata(List<String> scopeNames, Integer tenantId)
            throws APIResourceMgtException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        if (CollectionUtils.isEmpty(scopeNames)) {
            return new ArrayList<>();
        }
        String query = SQLConstants.GET_SCOPE_METADATA;
        String placeholders = String.join(",", Collections.nCopies(scopeNames.size(), "?"));
        query = query.replace(SQLConstants.SCOPE_LIST_PLACEHOLDER, placeholders);
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = dbConnection.prepareStatement(query)) {
            prepStmt.setInt(1, tenantIdToSearchScopes);
            int scopeIndex = 2;
            for (String scopeName : scopeNames) {
                prepStmt.setString(scopeIndex, scopeName);
                scopeIndex++;
            }
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                Map<String, APIResource> apiResources = new HashMap<>();
                while (resultSet.next()) {
                    String apiId = resultSet.getString(SQLConstants.API_RESOURCE_ID_COLUMN_NAME);
                    Scope scope = new Scope.ScopeBuilder()
                            .name(resultSet.getString(SQLConstants.SCOPE_QUALIFIED_NAME_COLUMN_NAME))
                            .displayName(resultSet.getString(SQLConstants.SCOPE_DISPLAY_NAME_COLUMN_NAME))
                            .description(resultSet.getString(SQLConstants.SCOPE_DESCRIPTION_COLUMN_NAME))
                            .build();
                    if (apiResources.containsKey(apiId)) {
                        List<Scope> scopeList = new ArrayList<>(apiResources.get(apiId).getScopes());
                        scopeList.add(scope);
                        apiResources.get(apiId).setScopes(scopeList);
                    } else {
                        APIResource apiResource = new APIResource.APIResourceBuilder()
                                .id(apiId)
                                .name(resultSet.getString(SQLConstants.API_RESOURCE_NAME_COLUMN_NAME))
                                .scopes(Collections.singletonList(scope))
                                .build();
                        apiResources.put(apiId, apiResource);
                    }
                }
                return new ArrayList<>(apiResources.values());
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPE_METADATA, e);
        }
    }

    /**
     * Get API resources list.
     *
     * @param limit           API resources limit.
     * @param tenantId        Tenant ID.
     * @param sortOrder       Order to sort the results.
     * @param expressionNodes Expression nodes.
     * @return API resources list.
     * @throws APIResourceMgtException If an error occurs while retrieving API resources.
     */
    private List<APIResource> getAPIResourcesList(Integer limit, Integer tenantId, String sortOrder,
                                                  List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        FilterQueryBuilder filterQueryBuilder = FilterQueriesUtil.getApiResourceFilterQueryBuilder(expressionNodes);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

        List<APIResource> apiResources = new ArrayList<>();

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            String databaseName = dbConnection.getMetaData().getDatabaseProductName();
            String sqlStmt = buildGetAPIResourcesSqlStatement(databaseName, tenantId,
                    filterQueryBuilder.getFilterQuery(), sortOrder, limit);
            PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt);

            if (filterAttributeValue != null) {
                for (Map.Entry<Integer, String> entry : filterAttributeValue.entrySet()) {
                    // PostgreSQL requires the value to be sent as integer for SERIAL datatype columns.
                    if (databaseName.contains(IdentityCoreConstants.POSTGRE_SQL)
                            && isValueOfCursorKey(entry.getKey(), filterQueryBuilder)) {
                        prepStmt.setInt(entry.getKey(), Integer.parseInt(entry.getValue()));
                        continue;
                    }
                    prepStmt.setString(entry.getKey(), entry.getValue());
                }
            }

            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                        .id(rs.getString(SQLConstants.ID_COLUMN_NAME))
                        .cursorKey(rs.getInt(SQLConstants.CURSOR_KEY_COLUMN_NAME))
                        .name(rs.getString(SQLConstants.NAME_COLUMN_NAME))
                        .identifier(rs.getString(SQLConstants.IDENTIFIER_COLUMN_NAME))
                        .description(rs.getString(SQLConstants.DESCRIPTION_COLUMN_NAME))
                        .type(rs.getString(SQLConstants.TYPE_COLUMN_NAME))
                        .requiresAuthorization(rs.getBoolean(SQLConstants.REQUIRES_AUTHORIZATION_COLUMN_NAME))
                        .tenantId(rs.getInt(SQLConstants.TENANT_ID_COLUMN_NAME));
                apiResources.add(apiResourceBuilder.build());
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES, e);
        }
        return apiResources;
    }

    /**
     * Check whether the value of the key belongs to the CURSOR_KEY column.
     *
     * @param key                Key of the filter.
     * @param filterQueryBuilder Filter query builder.
     */
    private boolean isValueOfCursorKey(int key, FilterQueryBuilder filterQueryBuilder) {

        String filterForKey = filterQueryBuilder.getFilterQuery().split("AND")[key - 1];
        return filterForKey.contains(SQLConstants.CURSOR_KEY_COLUMN_NAME);
    }

    /**
     * Get API resources list.
     *
     * @param limit           API resources limit.
     * @param tenantId        Tenant ID.
     * @param sortOrder       Order to sort the results.
     * @param expressionNodes Expression nodes.
     * @return API resources list.
     * @throws APIResourceMgtException If an error occurs while retrieving API resources.
     */
    private List<APIResource> getAPIResourcesListWithProperties(Integer limit, Integer tenantId, String sortOrder,
                                                                List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException {

        FilterQueryBuilder filterQueryBuilder = FilterQueriesUtil.getApiResourceFilterQueryBuilder(expressionNodes);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

        Map<String, APIResource> apiResourceMap = new LinkedHashMap<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            String databaseName = dbConnection.getMetaData().getDatabaseProductName();
            String sqlStmt = buildGetAPIResourcesWithPropertiesSqlStatement(databaseName, tenantId,
                    filterQueryBuilder.getFilterQuery(), sortOrder, limit);
            PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt);

            if (filterAttributeValue != null) {
                for (Map.Entry<Integer, String> entry : filterAttributeValue.entrySet()) {
                    prepStmt.setString(entry.getKey(), entry.getValue());
                }
            }

            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                String apiResourceId = rs.getString(SQLConstants.API_RESOURCE_ID_COLUMN_NAME);
                if (!apiResourceMap.containsKey(apiResourceId)) {
                    List<APIResourceProperty> apiResourceProperties = new ArrayList<>();
                    APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                            .id(rs.getString(SQLConstants.API_RESOURCE_ID_COLUMN_NAME))
                            .cursorKey(rs.getInt(SQLConstants.CURSOR_KEY_COLUMN_NAME))
                            .name(rs.getString(SQLConstants.API_RESOURCE_NAME_COLUMN_NAME))
                            .identifier(rs.getString(SQLConstants.API_RESOURCE_IDENTIFIER_COLUMN_NAME))
                            .description(rs.getString(SQLConstants.API_RESOURCE_DESCRIPTION_COLUMN_NAME))
                            .type(rs.getString(SQLConstants.API_RESOURCE_TYPE_COLUMN_NAME))
                            .requiresAuthorization(rs.getBoolean(SQLConstants.REQUIRES_AUTHORIZATION_COLUMN_NAME))
                            .tenantId(rs.getInt(SQLConstants.API_RESOURCE_TENANT_ID_COLUMN_NAME))
                            .properties(apiResourceProperties);
                    apiResourceMap.put(apiResourceId, apiResourceBuilder.build());
                }
                String propertyId = rs.getString(SQLConstants.API_RESOURCE_PROPERTY_ID_COLUMN_NAME);
                if (StringUtils.isNotBlank(propertyId)) {
                    APIResourceProperty apiResourceProperty = new APIResourceProperty();
                    apiResourceProperty.setName(rs.getString(SQLConstants.API_RESOURCE_PROPERTY_NAME_COLUMN_NAME));
                    apiResourceProperty.setValue(
                            rs.getString(SQLConstants.API_RESOURCE_PROPERTY_VALUE_COLUMN_NAME));
                    apiResourceMap.get(apiResourceId).getProperties().add(apiResourceProperty);
                }
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES, e);
        }
        return new ArrayList<>(apiResourceMap.values());
    }

    /**
     * Get API resource from the result set.
     *
     * @param resultSet Result set.
     * @return API resource.
     * @throws SQLException If an error occurs while retrieving API resource.
     */
    private static APIResource getApiResource(ResultSet resultSet, List<APIResourceProperty> apiResourceProperties)
            throws SQLException {

        List<Scope> scopes = new ArrayList<>();
        APIResource apiResource = null;
        while (resultSet.next()) {
            if (apiResource == null) {
                APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                        .id(resultSet.getString(SQLConstants.API_RESOURCE_ID_COLUMN_NAME))
                        .name(resultSet.getString(SQLConstants.API_RESOURCE_NAME_COLUMN_NAME))
                        .identifier(resultSet.getString(SQLConstants.API_RESOURCE_IDENTIFIER_COLUMN_NAME))
                        .description(resultSet.getString(SQLConstants.API_RESOURCE_DESCRIPTION_COLUMN_NAME))
                        .type(resultSet.getString(SQLConstants.API_RESOURCE_TYPE_COLUMN_NAME))
                        .requiresAuthorization(resultSet.getBoolean(
                                SQLConstants.REQUIRES_AUTHORIZATION_COLUMN_NAME))
                        .tenantId(resultSet.getInt(SQLConstants.API_RESOURCE_TENANT_ID_COLUMN_NAME))
                        .properties(apiResourceProperties);
                apiResource = apiResourceBuilder.build();
            }
            if (resultSet.getString(SQLConstants.SCOPE_ID_COLUMN_NAME) != null) {
                Scope.ScopeBuilder scopeBuilder = new Scope.ScopeBuilder()
                        .id(resultSet.getString(SQLConstants.SCOPE_ID_COLUMN_NAME))
                        .name(resultSet.getString(SQLConstants.SCOPE_QUALIFIED_NAME_COLUMN_NAME))
                        .displayName(resultSet.getString(SQLConstants.SCOPE_DISPLAY_NAME_COLUMN_NAME))
                        .description(resultSet.getString(SQLConstants.SCOPE_DESCRIPTION_COLUMN_NAME));
                scopes.add(scopeBuilder.build());
            }
        }
        if (apiResource != null) {
            apiResource.setScopes(scopes);
        }
        return apiResource;
    }

    /**
     * Build the SQL statement to retrieve API resources.
     *
     * @param databaseName Database name.
     * @param tenantId     Tenant ID.
     * @param filterQuery  Filter query.
     * @param sortOrder    Sort order.
     * @param limit        Limit.
     * @return SQL statement to retrieve API resources.
     */
    private String buildGetAPIResourcesSqlStatement(String databaseName, Integer tenantId, String filterQuery,
                                                    String sortOrder, Integer limit) throws APIResourceMgtException {

        String sqlStmtHead = SQLConstants.GET_API_RESOURCES;
        String sqlStmtTail = SQLConstants.GET_API_RESOURCES_TAIL;
        int initialTenantId = MultitenantConstants.SUPER_TENANT_ID;

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                initialTenantId = tenantId;
                tenantId = getRootOrganizationTenantId(tenantId);
                sqlStmtTail = SQLConstants.GET_API_RESOURCES_TAIL_FOR_ORGANIZATIONS;
            }
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }

        if (databaseName.contains(SQLConstants.MICROSOFT)) {
            sqlStmtHead = SQLConstants.GET_API_RESOURCES_MSSQL;
            sqlStmtTail = SQLConstants.GET_API_RESOURCES_TAIL_MSSQL;

            try {
                if (OrganizationManagementUtil.isOrganization(initialTenantId)) {
                    sqlStmtTail = SQLConstants.GET_API_RESOURCES_TAIL_FOR_ORGANIZATIONS_MSSQL;
                }
            } catch (OrganizationManagementException e) {
                throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                                .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                        IdentityTenantUtil.getTenantDomain(tenantId));
            }

            return String.format(sqlStmtHead, limit) + filterQuery + String.format(sqlStmtTail, tenantId, sortOrder);
        } else if (databaseName.contains(SQLConstants.ORACLE)) {
            sqlStmtTail = SQLConstants.GET_API_RESOURCES_TAIL_ORACLE;
        }

        return sqlStmtHead + filterQuery + String.format(sqlStmtTail, tenantId, sortOrder, limit);
    }

    private String buildGetAPIResourcesWithPropertiesSqlStatement(String databaseName, Integer tenantId,
                                                                  String filterQuery, String sortOrder, Integer limit)
            throws APIResourceMgtException {

        String selectionQuery = databaseName.contains(SQLConstants.H2)
                ? SQLConstants.GET_API_RESOURCES_WITH_PROPERTIES_SELECTION_H2
                : SQLConstants.GET_API_RESOURCES_WITH_PROPERTIES_SELECTION;
        String getAPIResourcesSqlStmt = buildGetAPIResourcesSqlStatement(databaseName, tenantId, filterQuery, sortOrder,
                limit);
        String joinQuery = SQLConstants.GET_API_RESOURCES_WITH_PROPERTIES_JOIN;

        return selectionQuery + getAPIResourcesSqlStmt + String.format(joinQuery, sortOrder);
    }

    /**
     * Delete scopes by name.
     *
     * @param dbConnection Database connection.
     * @param scopeName    Scope name.
     * @param tenantId     Tenant ID.
     * @throws APIResourceMgtException If an error occurs while deleting scopes.
     */
    private void deleteScopeByName(Connection dbConnection, String scopeName, Integer tenantId)
            throws APIResourceMgtException {

        try {
            PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_SCOPE_BY_NAME);
            prepStmt.setString(1, scopeName);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_SCOPES, e);
        }
    }

    /**
     * Delete scopes by API ID.
     *
     * @param dbConnection Database connection.
     * @param apiId        API ID.
     * @param tenantId     Tenant ID.
     * @throws APIResourceMgtException If an error occurs while deleting scopes.
     */
    private void deleteScopeByAPIId(Connection dbConnection, String apiId, Integer tenantId)
            throws APIResourceMgtException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_SCOPES_BY_API)) {
            prepStmt.setString(1, apiId);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_SCOPES, e);
        }
    }

    /**
     * Add scopes to the API resource.
     *
     * @param dbConnection Database connection.
     * @param apiId        API resource id.
     * @param scopes       List of scopes.
     * @param tenantId     Tenant id.
     * @throws APIResourceMgtException If an error occurs while adding scopes.
     */
    private void addScopes(Connection dbConnection, String apiId, List<Scope> scopes, Integer tenantId,
                           String apiResourceType) throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(scopes)) {
            return;
        }

        try {
            PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_SCOPE);
            for (Scope scope : scopes) {

                boolean scopeExists;
                if (isManagementOrOrganizationAPIResourceType(apiResourceType)) {
                    scopeExists = isScopeExistsByApiId(dbConnection, scope.getName(), tenantId, apiId);
                } else {
                    scopeExists = isScopeExists(dbConnection, scope.getName(), tenantId);
                }

                if (scopeExists) {
                    throw APIResourceManagementUtil.handleClientException(
                            APIResourceManagementConstants.ErrorMessages.ERROR_CODE_SCOPE_ALREADY_EXISTS,
                            String.valueOf(tenantId));
                }

                prepStmt.setString(1, UUID.randomUUID().toString());
                prepStmt.setString(2, scope.getName());
                prepStmt.setString(3, scope.getDisplayName());
                prepStmt.setString(4, scope.getDescription());
                prepStmt.setString(5, apiId);
                prepStmt.setObject(6, tenantId == 0 ? null : tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_SCOPES, e);
        }
    }

    /**
     * Delete scopes from the API resource.
     *
     * @param dbConnection Database connection.
     * @param scopes       List of scopes.
     * @param tenantId     Tenant id.
     * @throws APIResourceMgtException If an error occurs while deleting scopes.
     */
    private void deleteScopes(Connection dbConnection, List<String> scopes, Integer tenantId)
            throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(scopes)) {
            return;
        }
        try {
            PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_SCOPE_BY_NAME);
            for (String scope : scopes) {
                if (isScopeExists(dbConnection, scope, tenantId)) {
                    prepStmt.setString(1, scope);
                    prepStmt.setObject(2, tenantId == 0 ? null : tenantId);
                    prepStmt.addBatch();
                }
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_SCOPES, e);
        }
    }

    /**
     * Get API resource properties by API ID.
     *
     * @param dbConnection Database connection.
     * @param apiId        API resource id.
     * @return List of API resource properties.
     * @throws APIResourceMgtException If an error occurs while retrieving API resource properties.
     */
    private List<APIResourceProperty> getAPIResourcePropertiesByAPIId(Connection dbConnection, String apiId)
            throws APIResourceMgtException {

        List<APIResourceProperty> properties = new ArrayList<>();
        try {
            String databaseName = dbConnection.getMetaData().getDatabaseProductName();
            PreparedStatement prepStmt = databaseName.contains(SQLConstants.H2) ?
                    dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_PROPERTIES_BY_API_ID_H2) :
                    dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_PROPERTIES_BY_API_ID);
            prepStmt.setString(1, apiId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    APIResourceProperty property = new APIResourceProperty();
                    property.setName(rs.getString(SQLConstants.NAME_COLUMN_NAME));
                    property.setValue(rs.getString(SQLConstants.VALUE_COLUMN_NAME));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCE_PROPERTIES, e);
        }
        return properties;
    }

    /**
     * Get API resource properties by API Identifier.
     *
     * @param dbConnection  Database connection.
     * @param apiIdentifier API resource id.
     * @return List of API resource properties.
     * @throws APIResourceMgtException If an error occurs while retrieving API resource properties.
     */
    private List<APIResourceProperty> getAPIResourcePropertiesByAPIIdentifier(
            Connection dbConnection, String apiIdentifier, Integer tenantId) throws APIResourceMgtException {

        List<APIResourceProperty> properties = new ArrayList<>();
        try {
            String databaseName = dbConnection.getMetaData().getDatabaseProductName();
            PreparedStatement prepStmt = databaseName.contains(SQLConstants.H2) ?
                    dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_PROPERTIES_BY_API_IDENTIFIER_H2) :
                    dbConnection.prepareStatement(SQLConstants.GET_API_RESOURCE_PROPERTIES_BY_API_IDENTIFIER);
            prepStmt.setString(1, apiIdentifier);
            prepStmt.setInt(2, tenantId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    APIResourceProperty property = new APIResourceProperty();
                    property.setName(rs.getString(SQLConstants.NAME_COLUMN_NAME));
                    property.setValue(rs.getString(SQLConstants.VALUE_COLUMN_NAME));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCE_PROPERTIES, e);
        }
        return properties;
    }

    /**
     * Add API resource properties.
     *
     * @param dbConnection Database connection.
     * @param apiId        API resource id.
     * @param properties   List of API resource properties.
     * @throws APIResourceMgtException If an error occurs while adding API resource properties.
     */
    private void addAPIResourceProperties(Connection dbConnection, String apiId, List<APIResourceProperty> properties)
            throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(properties)) {
            return;
        }

        try {
            String databaseName = dbConnection.getMetaData().getDatabaseProductName();
            String query = databaseName.contains(SQLConstants.H2) ? SQLConstants.ADD_API_RESOURCE_PROPERTY_H2 :
                    SQLConstants.ADD_API_RESOURCE_PROPERTY;
            PreparedStatement prepStmt = dbConnection.prepareStatement(query);
            for (APIResourceProperty property : properties) {
                prepStmt.setString(1, apiId);
                prepStmt.setString(2, property.getName());
                prepStmt.setString(3, property.getValue());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE_PROPERTIES,
                    e);
        }
    }

    private int getRootOrganizationTenantId(int tenantId) throws OrganizationManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        String orgId = APIResourceManagementServiceComponentHolder.getInstance().getOrganizationManager()
                .resolveOrganizationId(tenantDomain);
        String rootOrganizationId = APIResourceManagementServiceComponentHolder.getInstance()
                .getOrganizationManager().getPrimaryOrganizationId(orgId);
        String rootTenantDomain = APIResourceManagementServiceComponentHolder.getInstance()
                .getOrganizationManager().resolveTenantDomain(rootOrganizationId);
        return IdentityTenantUtil.getTenantId(rootTenantDomain);
    }

    /**
     * Populates the authorization details types for the specified API resource if available.
     *
     * @param apiResource The API resource to update with authorization detail types.
     * @param tenantId    The tenant ID associated with the API resource.
     * @throws APIResourceMgtException If an error occurs while retrieving authorization detail types.
     */
    private void assignAuthorizationDetailsTypesToApiResource(final APIResource apiResource, final Integer tenantId)
            throws APIResourceMgtException {

        if (apiResource == null || AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return;
        }

        final List<AuthorizationDetailsType> authorizationDetailsTypes =
                this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantId);

        if (CollectionUtils.isNotEmpty(authorizationDetailsTypes)) {
            apiResource.setAuthorizationDetailsTypes(authorizationDetailsTypes);
        }
    }

    /**
     * Check whether the API resource is a tenant or organization API resource type.
     *
     * @param apiResourceType The API resource type to check.
     * @return true if the API resource is a management or organization API resource type, false otherwise.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resource type.
     */
    private boolean isManagementOrOrganizationAPIResourceType(String apiResourceType) throws APIResourceMgtException {

        return APIResourceManagementConstants.APIResourceTypes.TENANT.equals(apiResourceType)
                || APIResourceManagementConstants.APIResourceTypes.ORGANIZATION.equals(apiResourceType);
    }

    private boolean isScopeExistsByApiId(Connection connection, String name, Integer tenantId, String apiId)
            throws APIResourceMgtServerException {

        int tenantIdToSearchScopes;
        try {
            tenantIdToSearchScopes = OrganizationManagementUtil.isOrganization(tenantId) ?
                    getRootOrganizationTenantId(tenantId) : tenantId;
        } catch (OrganizationManagementException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RESOLVING_ORGANIZATION_FOR_TENANT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, tenantIdToSearchScopes);
            preparedStatement.setString(3, apiId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_CHECKING_EXISTENCE_OF_SCOPE, e);
        }
    }
}
