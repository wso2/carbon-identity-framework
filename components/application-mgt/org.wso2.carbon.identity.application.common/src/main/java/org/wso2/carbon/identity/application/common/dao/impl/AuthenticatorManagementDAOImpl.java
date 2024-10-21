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
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtConstants.ErrorMessages;
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtSQLConstants;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the AuthenticatorManagementDAO interface which perform CRUD operation on database.
 */
public class AuthenticatorManagementDAOImpl implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(AuthenticatorManagementDAOImpl.class);
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";
    public static final String LOCAL_IDP_NAME = "LOCAL";

    public AuthenticatorManagementDAOImpl() {
    }

    @Override
    public LocalAuthenticatorConfig addUserDefinedLocalAuthenticator(LocalAuthenticatorConfig authenticatorConfig,
                Integer tenantId, AuthenticationType type) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.ADD_AUTHENTICATOR_SQL)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfig.getName());
            statement.setString(AuthenticatorMgtSQLConstants.Column.DISPLAY_NAME, authenticatorConfig.getDisplayName());
            statement.setString(AuthenticatorMgtSQLConstants.Column.DEFINED_BY,
                    authenticatorConfig.getDefinedByType().toString());
            statement.setString(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_TYPE, type.toString());
            int isEnabled = authenticatorConfig.isEnabled() ? 1 : 0;
            statement.setInt(AuthenticatorMgtSQLConstants.Column.IS_ENABLED, isEnabled);
            statement.setString(AuthenticatorMgtSQLConstants.Column.IDP_NAME, LOCAL_IDP_NAME);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            if (authenticatorConfig.getProperties() != null) {

                int configId = getAuthenticatorIdentifier(dbConnection, authenticatorConfig.getName(), tenantId);
                try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                        AuthenticatorMgtSQLConstants.Query.ADD_AUTHENTICATOR_PROP_SQL)) {

                    for (Property property : authenticatorConfig.getProperties()) {
                        statementProp.setInt(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_ID, configId);
                        statementProp.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
                        statementProp.setString(AuthenticatorMgtSQLConstants.Column.PROPERTY_KEY, property.getName());
                        statementProp.setString(AuthenticatorMgtSQLConstants.Column.PROPERTY_VALUE,
                                property.getValue());
                        if (property.isConfidential()) {
                            statementProp.setString(AuthenticatorMgtSQLConstants.Column.IS_SECRET, IS_TRUE_VALUE);
                        } else {
                            statementProp.setString(AuthenticatorMgtSQLConstants.Column.IS_SECRET, IS_FALSE_VALUE);
                        }
                        statementProp.executeUpdate();
                    }
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, authenticatorConfig.getName(), tenantId);
        } catch (SQLException | AuthenticatorMgtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while adding the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back added Authenticator information.", authenticatorConfig.getName(),
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);

            ErrorMessages error = ErrorMessages.ERROR_WHILE_ADDING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public LocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(LocalAuthenticatorConfig
                existingAuthenticatorConfig, LocalAuthenticatorConfig updatedAuthenticatorConfig, Integer tenantId)
                throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {

            if (isBasicInfoUpdated(existingAuthenticatorConfig, updatedAuthenticatorConfig)) {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                        AuthenticatorMgtSQLConstants.Query.UPDATE_AUTHENTICATOR_SQL)) {

                    statement.setString(AuthenticatorMgtSQLConstants.Column.DISPLAY_NAME,
                            updatedAuthenticatorConfig.getDisplayName());
                    int isEnabled = updatedAuthenticatorConfig.isEnabled() ? 1 : 0;
                    statement.setInt(AuthenticatorMgtSQLConstants.Column.IS_ENABLED, isEnabled);
                    statement.setString(AuthenticatorMgtSQLConstants.Column.NAME,
                            existingAuthenticatorConfig.getName());
                    statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
                    statement.executeUpdate();
                }
            }

            // Will delete all the properties of given authenticator and add the updated properties.
            int configId = getAuthenticatorIdentifier(dbConnection, existingAuthenticatorConfig.getName(), tenantId);
            try (NamedPreparedStatement statementDeleteProp = new NamedPreparedStatement(dbConnection,
                    AuthenticatorMgtSQLConstants.Query.DELETE_AUTHENTICATOR_PROP_SQL)) {

                statementDeleteProp.setInt(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_ID, configId);
                statementDeleteProp.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
                statementDeleteProp.executeUpdate();
            }

            try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                    AuthenticatorMgtSQLConstants.Query.ADD_AUTHENTICATOR_PROP_SQL)) {

                for (Property prop : updatedAuthenticatorConfig.getProperties()) {
                    statementProp.setInt(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_ID, configId);
                    statementProp.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
                    statementProp.setString(AuthenticatorMgtSQLConstants.Column.PROPERTY_KEY, prop.getName());
                    statementProp.setString(AuthenticatorMgtSQLConstants.Column.PROPERTY_VALUE, prop.getValue());
                    if (prop.isConfidential()) {
                        statementProp.setString(AuthenticatorMgtSQLConstants.Column.IS_SECRET, IS_TRUE_VALUE);
                    } else {
                        statementProp.setString(AuthenticatorMgtSQLConstants.Column.IS_SECRET, IS_FALSE_VALUE);
                    }
                    statementProp.executeUpdate();
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, updatedAuthenticatorConfig.getName(), tenantId);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back updated Authenticator information.",
                                existingAuthenticatorConfig.getName(), IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            ErrorMessages error = ErrorMessages.ERROR_WHILE_UPDATING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public LocalAuthenticatorConfig getUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            LocalAuthenticatorConfig config = getUserDefinedLocalAuthenticatorByName(
                    dbConnection, authenticatorConfigName, tenantId);

            return config;
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.DELETE_AUTHENTICATOR_SQL)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfigName);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back deleted Authenticator information.", authenticatorConfigName,
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            ErrorMessages error = ErrorMessages.ERROR_WHILE_DELETING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private LocalAuthenticatorConfig getUserDefinedLocalAuthenticatorByName(
            Connection dbConnection, String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtServerException {

        LocalAuthenticatorConfig config = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.GET_AUTHENTICATOR_SQL)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfigName);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    config = new LocalAuthenticatorConfig();
                    config.setName(rs.getString(AuthenticatorMgtSQLConstants.Column.NAME));
                    config.setDisplayName(rs.getString(AuthenticatorMgtSQLConstants.Column.DISPLAY_NAME));
                    config.setEnabled(rs.getString(AuthenticatorMgtSQLConstants.Column.IS_ENABLED)
                            .equals(IS_TRUE_VALUE));
                    config.setDefinedByType(DefinedByType.valueOf(
                            rs.getString(AuthenticatorMgtSQLConstants.Column.DEFINED_BY)));
                }
            }

            if (config == null) {
                return null;
            }

            int configId = getAuthenticatorIdentifier(dbConnection, config.getName(), tenantId);
            try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                    AuthenticatorMgtSQLConstants.Query.GET_AUTHENTICATOR_PROP_SQL)) {

                statementProp.setInt(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_ID, configId);
                statementProp.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);

                try (ResultSet rs = statementProp.executeQuery()) {
                    List<Property> properties = new ArrayList<>();
                    while (rs.next()) {
                        Property property = new Property();
                        property.setName(rs.getString(AuthenticatorMgtSQLConstants.Column.PROPERTY_KEY));
                        property.setValue(rs.getString(AuthenticatorMgtSQLConstants.Column.PROPERTY_VALUE));
                        property.setConfidential(Boolean.parseBoolean(
                                rs.getString(AuthenticatorMgtSQLConstants.Column.IS_SECRET)));
                        properties.add(property);
                    }
                    config.setProperties(properties.toArray(new Property[0]));
                }
            }

            IdentityDatabaseUtil.commitTransaction(dbConnection);
            return config;
        } catch (SQLException e) {
            ErrorMessages error = ErrorMessages.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        }
    }

    private boolean isBasicInfoUpdated(LocalAuthenticatorConfig existingAuthenticatorConfig,
                                       LocalAuthenticatorConfig updatedAuthenticatorConfig) {

        return !existingAuthenticatorConfig.getDisplayName().equals(updatedAuthenticatorConfig.getDisplayName()) ||
                existingAuthenticatorConfig.isEnabled() != updatedAuthenticatorConfig.isEnabled();
    }

    private Integer getAuthenticatorIdentifier(Connection dbConnection, String authenticatorConfigName,
                                               Integer tenantId) throws AuthenticatorMgtServerException, SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.GET_AUTHENTICATOR_ID_SQL)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfigName);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(AuthenticatorMgtSQLConstants.Column.ID);
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        }

        throw new AuthenticatorMgtServerException(String.format("Authenticator with name: %s not found in the database."
                , authenticatorConfigName));
    }
}
