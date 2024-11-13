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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import static org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil.assertRichAuthorizationRequestsEnabled;
import static org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled;
import static org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil.parseSchema;
import static org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil.toJsonString;
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

    private static final Log log = LogFactory.getLog(AuthorizationDetailsTypeMgtDAOImpl.class);

    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {

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

        assertRichAuthorizationRequestsEnabled();

        if (CollectionUtils.isEmpty(authorizationDetailsTypes)) {
            return authorizationDetailsTypes;
        }

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_AUTHORIZATION_DETAILS_TYPE)) {

            prepStmt.setString(3, apiId);
            prepStmt.setObject(7, tenantId);
            for (AuthorizationDetailsType authzDetailsType : authorizationDetailsTypes) {

                this.assertAuthorizationDetailsTypeExistence(dbConnection, authzDetailsType.getType(), tenantId);

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

        if (isRichAuthorizationRequestsDisabled()) {
            return;
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
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

        if (isRichAuthorizationRequestsDisabled()) {
            return;
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
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

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {

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

        if (isRichAuthorizationRequestsDisabled()) {
            return;
        }

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

        if (isRichAuthorizationRequestsDisabled()) {
            return null;
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
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

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId,
                                                                                Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            return this.getAuthorizationDetailsTypeByApiIdAndTypeId(dbConnection, apiId, typeId, tenantId);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(List<ExpressionNode> expressionNodes,
                                                                       Integer tenantId)
            throws APIResourceMgtException {

        if (isRichAuthorizationRequestsDisabled()) {
            return null;
        }

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

        final List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();

        if (isRichAuthorizationRequestsDisabled()) {
            return authorizationDetailsTypes;
        }

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setObject(2, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
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

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {

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

        assertRichAuthorizationRequestsEnabled();

        try (PreparedStatement prepStmt =
                     dbConnection.prepareStatement(SQLConstants.UPDATE_AUTHORIZATION_DETAILS_TYPES)) {

            prepStmt.setString(5, apiId);
            prepStmt.setObject(7, tenantId);
            for (final AuthorizationDetailsType authorizationDetailsType : authorizationDetailsTypes) {

                final String newType = authorizationDetailsType.getType();
                final String typeId = authorizationDetailsType.getId();

                if (isBlank(typeId)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Authorization details type ID is missing for type %s", newType));
                    }
                    continue;
                }

                final AuthorizationDetailsType existingAuthorizationDetailsType =
                        this.getAuthorizationDetailsTypeByApiIdAndTypeId(dbConnection, apiId, typeId, tenantId);

                if (existingAuthorizationDetailsType == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Unable to find an authorization details type for ID %s", typeId));
                    }
                    continue;
                }

                if (!existingAuthorizationDetailsType.getType().equals(newType)) {
                    assertAuthorizationDetailsTypeExistence(dbConnection, newType, tenantId);
                }

                prepStmt.setString(1, isBlank(authorizationDetailsType.getName()) ?
                        existingAuthorizationDetailsType.getName() : authorizationDetailsType.getName());
                prepStmt.setString(2, isBlank(newType) ?
                        existingAuthorizationDetailsType.getType() : newType);
                prepStmt.setString(3, authorizationDetailsType.getDescription());
                prepStmt.setObject(4, toJsonString(authorizationDetailsType.getSchema()));
                prepStmt.setString(6, typeId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    private boolean isAuthorizationDetailsTypeExists(Connection dbConnection, String type, Integer tenantId)
            throws APIResourceMgtException {

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_TYPE)) {
            preparedStatement.setString(1, type);
            preparedStatement.setObject(2, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return this.buildAuthorizationDetailsType(resultSet) != null;
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return false;
    }

    private AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(Connection dbConnection, String apiId,
                                                                                 String typeId, Integer tenantId)
            throws SQLException {

        if (isRichAuthorizationRequestsDisabled()) {
            return null;
        }

        try (PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, typeId);
            preparedStatement.setObject(3, tenantId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return this.buildAuthorizationDetailsType(resultSet);
            }
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

    private void assertAuthorizationDetailsTypeExistence(Connection dbConnection, String type, Integer tenantId)
            throws APIResourceMgtException {

        if (isBlank(type)) {
            return;
        }

        if (this.isAuthorizationDetailsTypeExists(dbConnection, type, tenantId)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Authorization details type '%s' already exists for tenant.", type));
            }

            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_AUTHORIZATION_DETAILS_TYPE_EXISTS, type);
        }
    }
}
