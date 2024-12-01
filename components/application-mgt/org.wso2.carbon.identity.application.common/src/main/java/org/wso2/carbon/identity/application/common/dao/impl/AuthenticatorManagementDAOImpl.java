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
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtSQLConstants.Column;
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtSQLConstants.Query;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildServerException;

/**
 * This class implements the AuthenticatorManagementDAO interface which perform CRUD operation on database.
 */
public class AuthenticatorManagementDAOImpl implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(AuthenticatorManagementDAOImpl.class);
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";
    public static final String LOCAL_IDP_NAME = "LOCAL";

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection, Query.ADD_AUTHENTICATOR_SQL)) {
            statement.setString(Column.NAME, authenticatorConfig.getName());
            statement.setString(Column.DISPLAY_NAME, authenticatorConfig.getDisplayName());
            statement.setString(Column.DEFINED_BY, authenticatorConfig.getDefinedByType().toString());
            statement.setString(Column.AUTHENTICATION_TYPE, authenticatorConfig.getAuthenticationType().toString());
            statement.setInt(Column.IS_ENABLED, authenticatorConfig.isEnabled() ? 1 : 0);
            statement.setString(Column.IDP_NAME, LOCAL_IDP_NAME);
            statement.setInt(Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            if (authenticatorConfig.getProperties() != null) {

                int authenticatorConfigID = getAuthenticatorIdentifier(dbConnection, authenticatorConfig.getName(),
                        tenantId);
                addAuthenticatorProperties(dbConnection, authenticatorConfigID, authenticatorConfig.getProperties(),
                        tenantId);
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, authenticatorConfig.getName(), tenantId);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while adding the authenticator: %s in tenant domain: %s. " +
                                "Rolling back added Authenticator information.", authenticatorConfig.getName(),
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_ADDING_AUTHENTICATOR, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig existingAuthenticatorConfig,
            UserDefinedLocalAuthenticatorConfig updatedAuthenticatorConfig, int tenantId)
            throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            if (isBasicInfoUpdated(existingAuthenticatorConfig, updatedAuthenticatorConfig)) {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                        Query.UPDATE_AUTHENTICATOR_SQL)) {
                    statement.setString(Column.DISPLAY_NAME, updatedAuthenticatorConfig.getDisplayName());
                    statement.setInt(Column.IS_ENABLED, updatedAuthenticatorConfig.isEnabled() ? 1 : 0);
                    statement.setString(Column.NAME, existingAuthenticatorConfig.getName());
                    statement.setInt(Column.TENANT_ID, tenantId);
                    statement.executeUpdate();
                }
            }

            // Will delete all the properties of given authenticator and add the updated properties.
            int authenticatorConfigID = getAuthenticatorIdentifier(dbConnection,
                    existingAuthenticatorConfig.getName(), tenantId);
            deletedAuthenticatorProperties(dbConnection, authenticatorConfigID, tenantId);
            addAuthenticatorProperties(dbConnection, authenticatorConfigID, updatedAuthenticatorConfig.getProperties(),
                    tenantId);

            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, updatedAuthenticatorConfig.getName(), tenantId);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the authenticator: %s in tenant domain: %s. " +
                                "Rolling back updated Authenticator information.",
                                existingAuthenticatorConfig.getName(), IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_UPDATING_AUTHENTICATOR, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            return getUserDefinedLocalAuthenticatorByName(dbConnection, authenticatorConfigName, tenantId);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticator(int tenantId)
            throws AuthenticatorMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
             NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                Query.GET_ALL_USER_DEFINED_AUTHENTICATOR_SQL)) {
            statement.setString(Column.DEFINED_BY, DefinedByType.USER.toString());
            statement.setInt(Column.TENANT_ID, tenantId);

            List<UserDefinedLocalAuthenticatorConfig> allUserDefinedLocalConfigs = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UserDefinedLocalAuthenticatorConfig config = getLocalAuthenticatorConfigBasedOnType(
                            rs.getString(Column.AUTHENTICATION_TYPE));
                    config.setName(rs.getString(Column.NAME));
                    config.setDisplayName(rs.getString(Column.DISPLAY_NAME));
                    config.setEnabled(rs.getString(Column.IS_ENABLED).equals(IS_TRUE_VALUE));
                    config.setDefinedByType(DefinedByType.valueOf(rs.getString(Column.DEFINED_BY)));
                    allUserDefinedLocalConfigs.add(config);
                }
            }

            return allUserDefinedLocalConfigs;
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while retrieving the all user defined local authenticators in tenant " +
                                "domain: %s.", IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_DELETING_AUTHENTICATOR, e);
        }
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, 
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                Query.DELETE_AUTHENTICATOR_SQL)) {
            statement.setString(Column.NAME, authenticatorConfigName);
            statement.setInt(Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the authenticator: %s in tenant domain: %s. " +
                                "Rolling back deleted Authenticator information.", authenticatorConfigName,
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_DELETING_AUTHENTICATOR, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticatorByName(
            Connection dbConnection, String authenticatorConfigName, int tenantId)
            throws AuthenticatorMgtServerException {

        UserDefinedLocalAuthenticatorConfig config = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection, Query.GET_AUTHENTICATOR_SQL)) {
            statement.setString(Column.NAME, authenticatorConfigName);
            statement.setInt(Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    config = getLocalAuthenticatorConfigBasedOnType(rs.getString(Column.AUTHENTICATION_TYPE));
                    config.setName(rs.getString(Column.NAME));
                    config.setDisplayName(rs.getString(Column.DISPLAY_NAME));
                    config.setEnabled(rs.getString(Column.IS_ENABLED).equals(IS_TRUE_VALUE));
                    config.setDefinedByType(DefinedByType.valueOf(rs.getString(Column.DEFINED_BY)));
                }
            }

            if (config == null) {
                return null;
            }

            int authenticatorConfigID = getAuthenticatorIdentifier(dbConnection, config.getName(), tenantId);
            try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                    Query.GET_AUTHENTICATOR_PROP_SQL)) {
                statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                statementProp.setInt(Column.TENANT_ID, tenantId);

                try (ResultSet rs = statementProp.executeQuery()) {
                    List<Property> properties = new ArrayList<>();
                    while (rs.next()) {
                        Property property = new Property();
                        property.setName(rs.getString(Column.PROPERTY_KEY));
                        property.setValue(rs.getString(Column.PROPERTY_VALUE));
                        property.setConfidential(Boolean.parseBoolean(rs.getString(Column.IS_SECRET)));
                        properties.add(property);
                    }
                    config.setProperties(properties.toArray(new Property[0]));
                }
            }

            IdentityDatabaseUtil.commitTransaction(dbConnection);
            return config;
        } catch (SQLException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME, e);
        }
    }

    private UserDefinedLocalAuthenticatorConfig getLocalAuthenticatorConfigBasedOnType(String authenticationType) {

        if (AuthenticationType.VERIFICATION.toString().equals(authenticationType)) {
            return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.VERIFICATION);
        }
        return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.IDENTIFICATION);
    }

    private boolean isBasicInfoUpdated(UserDefinedLocalAuthenticatorConfig existingAuthenticatorConfig,
                                       UserDefinedLocalAuthenticatorConfig updatedAuthenticatorConfig) {

        return !existingAuthenticatorConfig.getDisplayName().equals(updatedAuthenticatorConfig.getDisplayName()) ||
                existingAuthenticatorConfig.isEnabled() != updatedAuthenticatorConfig.isEnabled();
    }

    private int getAuthenticatorIdentifier(Connection dbConnection, String authenticatorConfigName,
                                               int tenantId) throws AuthenticatorMgtServerException, SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                Query.GET_AUTHENTICATOR_ID_SQL)) {
            statement.setString(Column.NAME, authenticatorConfigName);
            statement.setInt(Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(Column.ID);
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        }
        throw buildServerException(AuthenticatorMgtError.ERROR_CODE_NO_AUTHENTICATOR_FOUND, authenticatorConfigName);
    }

    private void deletedAuthenticatorProperties(Connection dbConnection, int authenticatorConfigID, int tenantId)
            throws SQLException {

        try (NamedPreparedStatement statementDeleteProp = new NamedPreparedStatement(dbConnection,
                Query.DELETE_AUTHENTICATOR_PROP_SQL)) {
            statementDeleteProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
            statementDeleteProp.setInt(Column.TENANT_ID, tenantId);
            statementDeleteProp.executeUpdate();
        }
    }

    private void addAuthenticatorProperties(Connection dbConnection, int authenticatorConfigID, Property[] properties,
                                            int tenantId) throws SQLException {

        try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                Query.ADD_AUTHENTICATOR_PROP_SQL)) {
            for (Property prop : properties) {
                statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                statementProp.setInt(Column.TENANT_ID, tenantId);
                statementProp.setString(Column.PROPERTY_KEY, prop.getName());
                statementProp.setString(Column.PROPERTY_VALUE, prop.getValue());
                if (prop.isConfidential()) {
                    statementProp.setString(Column.IS_SECRET, IS_TRUE_VALUE);
                } else {
                    statementProp.setString(Column.IS_SECRET, IS_FALSE_VALUE);
                }
                statementProp.executeUpdate();
            }
        }
    }
}
