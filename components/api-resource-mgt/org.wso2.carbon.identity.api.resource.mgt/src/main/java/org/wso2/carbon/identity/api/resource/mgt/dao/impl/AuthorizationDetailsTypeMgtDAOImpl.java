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

package org.wso2.carbon.identity.api.resource.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil.parseSchema;
import static org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil.toJsonString;
import static org.wso2.carbon.identity.api.resource.mgt.util.FilterQueriesUtil.getAuthorizationDetailsTypesFilterQueryBuilder;

/**
 * Implementation of the {@link AuthorizationDetailsTypeMgtDAO} interface for managing
 * Authorization Detail Types in the database.
 *
 * <p>This class interacts with the persistence layer to perform CRUD operations on
 * authorization detail types associated with APIs. It supports tenant-aware operations
 * and ensures data isolation between tenants.</p>
 */
public class AuthorizationDetailsTypeMgtDAOImpl implements AuthorizationDetailsTypeMgtDAO {

    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            final List<AuthorizationDetailsType> addedAuthorizationDetailsTypes =
                    this.addAuthorizationDetailsTypes(dbConnection, apiId, authorizationDetailsTypes, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return addedAuthorizationDetailsTypes;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                    .ERROR_CODE_ERROR_WHILE_ADDING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            Connection dbConnection, String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
            Integer tenantId) throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(authorizationDetailsTypes)) {
            return authorizationDetailsTypes;
        }

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_AUTHORIZATION_DETAILS_TYPE)) {

            for (AuthorizationDetailsType authzDetailsType : authorizationDetailsTypes) {
                prepStmt.setString(3, apiId);
                prepStmt.setObject(7, tenantId);

                if (this.isAuthorizationDetailsTypeExists(dbConnection, apiId, authzDetailsType.getType(), tenantId)) {
                    throw APIResourceManagementUtil.handleClientException(
                            APIResourceManagementConstants.ErrorMessages.ERROR_CODE_AUTHORIZATION_DETAILS_TYPE_EXISTS,
                            authzDetailsType.getType());
                }

                authzDetailsType.setId(UUID.randomUUID().toString());
                prepStmt.setString(1, authzDetailsType.getId());
                prepStmt.setString(2, authzDetailsType.getType());
                prepStmt.setString(4, authzDetailsType.getName());
                prepStmt.setString(5, authzDetailsType.getDescription());
                prepStmt.setString(6, toJsonString(authzDetailsType.getSchema()));
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                    .ERROR_CODE_ERROR_WHILE_ADDING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return authorizationDetailsTypes;
    }

    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection
                     .prepareStatement(SQLConstants.DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, type);
            preparedStatement.setObject(3, tenantId);
            preparedStatement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection
                     .prepareStatement(SQLConstants.DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, typeId);
            preparedStatement.setObject(3, tenantId);
            preparedStatement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            this.deleteAuthorizationDetailsTypesByApiId(dbConnection, apiId, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(Connection dbConnection, String apiId, Integer tenantId)
            throws APIResourceMgtException {

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setObject(2, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type,
                                                                              Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            return this.getAuthorizationDetailsTypeByApiIdAndType(dbConnection, apiId, type, tenantId);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId,
                                                                                Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, typeId);
            preparedStatement.setObject(3, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return this.buildAuthorizationDetailsType(resultSet);
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return null;
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(List<ExpressionNode> expressionNodes,
                                                                       Integer tenantId)
            throws APIResourceMgtException {

        FilterQueryBuilder filterQueryBuilder = getAuthorizationDetailsTypesFilterQueryBuilder(expressionNodes);
        final String sqlStmt = this.buildGetAuthorizationDetailsTypesSqlStatement(filterQueryBuilder.getFilterQuery());
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlStmt)) {

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

            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
            while (resultSet.next()) {
                authorizationDetailsTypes.add(this.buildAuthorizationDetailsType(resultSet));
            }
            return authorizationDetailsTypes;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            return this.getAuthorizationDetailsTypesByApiId(dbConnection, apiId, tenantId);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(Connection dbConnection, String apiId,
                                                                              Integer tenantId)
            throws APIResourceMgtException {

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setObject(2, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
            while (resultSet.next()) {
                authorizationDetailsTypes.add(this.buildAuthorizationDetailsType(resultSet));
            }
            return authorizationDetailsTypes;
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public boolean isAuthorizationDetailsTypeExists(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException {

        return this.getAuthorizationDetailsTypeByApiIdAndType(apiId, type, tenantId) != null;
    }

    @Override
    public void updateAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                                Integer tenantId) throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            this.updateAuthorizationDetailsTypes(dbConnection, apiId, authorizationDetailsTypes, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void updateAuthorizationDetailsTypes(Connection dbConnection, String apiId,
                                                List<AuthorizationDetailsType> authorizationDetailsTypes,
                                                Integer tenantId) throws APIResourceMgtException {

        try (PreparedStatement prepStmt =
                     dbConnection.prepareStatement(SQLConstants.UPDATE_AUTHORIZATION_DETAILS_TYPES)) {

            prepStmt.setString(5, apiId);
            prepStmt.setObject(7, tenantId);
            for (final AuthorizationDetailsType authorizationDetailsType : authorizationDetailsTypes) {

                if (isBlank(authorizationDetailsType.getType()) || isBlank(authorizationDetailsType.getId())) {
                    continue;
                }

                prepStmt.setString(1, authorizationDetailsType.getName());
                prepStmt.setString(2, authorizationDetailsType.getType());
                prepStmt.setString(3, authorizationDetailsType.getDescription());
                prepStmt.setObject(4, toJsonString(authorizationDetailsType.getSchema()));
                prepStmt.setString(6, authorizationDetailsType.getId());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    private boolean isAuthorizationDetailsTypeExists(Connection dbConnection, String apiId, String type,
                                                     Integer tenantId) throws APIResourceMgtException {

        return this.getAuthorizationDetailsTypeByApiIdAndType(dbConnection, apiId, type, tenantId) != null;
    }

    private AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(Connection dbConnection, String apiId,
                                                                               String type, Integer tenantId)
            throws APIResourceMgtException {

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, type);
            preparedStatement.setObject(3, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return this.buildAuthorizationDetailsType(resultSet);
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return null;
    }

    private AuthorizationDetailsType buildAuthorizationDetailsType(final ResultSet resultSet)
            throws SQLException {

        final String authorizationDetailsTypeId = resultSet.getString(SQLConstants.ID_COLUMN_NAME);

        if (StringUtils.isBlank(authorizationDetailsTypeId)) {
            return null;
        }

        return new AuthorizationDetailsType.AuthorizationDetailsTypesBuilder()
                .id(authorizationDetailsTypeId)
                .type(resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME))
                .name(resultSet.getString(SQLConstants.NAME_COLUMN_NAME))
                .description(resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME))
                .schema(parseSchema(resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME)))
                .build();
    }

    private String buildGetAuthorizationDetailsTypesSqlStatement(final String filterQuery) {

        return String.format(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_TENANT_ID_FORMAT,
                filterQuery, SQLConstants.GET_SCOPES_BY_TENANT_ID_TAIL);
    }
}
