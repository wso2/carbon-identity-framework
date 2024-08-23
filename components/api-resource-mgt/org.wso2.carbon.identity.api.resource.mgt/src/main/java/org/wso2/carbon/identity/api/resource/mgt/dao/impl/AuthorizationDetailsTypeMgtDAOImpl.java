package org.wso2.carbon.identity.api.resource.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AuthorizationDetailsTypeMgtDAOImpl implements AuthorizationDetailsTypeMgtDAO {

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type,
                                                                              Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, type);
            preparedStatement.setObject(3, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new AuthorizationDetailsType(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME)
                );
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return null;
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(final Integer tenantId)
            throws APIResourceMgtException {

        List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPES)) {
            preparedStatement.setObject(1, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                authorizationDetailsTypes.add(new AuthorizationDetailsType(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME)
                ));
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return authorizationDetailsTypes;
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId)
            throws APIResourceMgtException {

        List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setObject(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                authorizationDetailsTypes.add(new AuthorizationDetailsType(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME)
                ));
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return authorizationDetailsTypes;
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByType(String type, Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.GET_AUTHORIZATION_DETAILS_TYPE_BY_TYPE)) {
            preparedStatement.setString(1, type);
            preparedStatement.setObject(2, tenantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new AuthorizationDetailsType(
                        resultSet.getString(SQLConstants.ID_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME),
                        resultSet.getString(SQLConstants.NAME_COLUMN_NAME),
                        resultSet.getString(SQLConstants.DESCRIPTION_COLUMN_NAME),
                        resultSet.getString(SQLConstants.AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME)
                );
            }
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_AUTHORIZATION_DETAILS_TYPES, e);
        }
        return null;
    }

    /**
     * Add authorization details types to the API resource.
     *
     * @param apiId                     API resource id.
     * @param authorizationDetailsTypes List of authorization details types.
     * @param tenantId                  Tenant id.
     * @throws APIResourceMgtException If an error occurs while adding authorization details types.
     */
    @Override
    public void addAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                             Integer tenantId) throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(authorizationDetailsTypes)) {
            return;
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = dbConnection.getMetaData().getDatabaseProductName().contains(SQLConstants.H2)
                     ? dbConnection.prepareStatement(SQLConstants.ADD_AUTHORIZATION_DETAILS_TYPE_H2)
                     : dbConnection.prepareStatement(SQLConstants.ADD_AUTHORIZATION_DETAILS_TYPE)) {

            for (AuthorizationDetailsType authorizationDetailsType : authorizationDetailsTypes) {

                if (this.getAuthorizationDetailsTypeByType(authorizationDetailsType.getType(), tenantId) != null) {
                    throw APIResourceManagementUtil.handleClientException(
                            APIResourceManagementConstants.ErrorMessages.ERROR_CODE_AUTHORIZATION_DETAILS_TYPE_EXISTS,
                            String.valueOf(tenantId));
                }

                prepStmt.setString(1, UUID.randomUUID().toString());
                prepStmt.setString(2, authorizationDetailsType.getType());
                prepStmt.setString(3, apiId);
                prepStmt.setString(4, authorizationDetailsType.getName());
                prepStmt.setString(5, authorizationDetailsType.getDescription());
                prepStmt.setString(6, authorizationDetailsType.getSchema());
                prepStmt.setObject(7, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants.ErrorMessages
                    .ERROR_CODE_ERROR_WHILE_ADDING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId) throws APIResourceMgtException {
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setObject(2, tenantId);
            preparedStatement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }

    @Override
    public void updateAuthorizationDetailsType(AuthorizationDetailsType authorizationDetailsType, Integer tenantId)
            throws APIResourceMgtException {

        updateAuthorizationDetailsTypes(Collections.singletonList(authorizationDetailsType), tenantId);
    }

    @Override
    public void updateAuthorizationDetailsTypes(List<AuthorizationDetailsType> authorizationDetailsTypes,
                                                Integer tenantId)
            throws APIResourceMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement =
                     dbConnection.prepareStatement(SQLConstants.UPDATE_AUTHORIZATION_DETAILS_TYPES)) {

            for (AuthorizationDetailsType authorizationDetailsType : authorizationDetailsTypes) {
                preparedStatement.setString(1, authorizationDetailsType.getName());
                preparedStatement.setString(2, authorizationDetailsType.getDescription());
                preparedStatement.setString(3, authorizationDetailsType.getSchema());
                preparedStatement.setString(4, authorizationDetailsType.getType());
                preparedStatement.setObject(5, tenantId);
                preparedStatement.addBatch();
            }
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            throw APIResourceManagementUtil.handleServerException(APIResourceManagementConstants
                    .ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_AUTHORIZATION_DETAILS_TYPES, e);
        }
    }
}
