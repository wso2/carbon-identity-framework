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
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
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
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Authenticator.ACTION_ID_PROPERTY;

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

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.executeInsert(Query.ADD_AUTHENTICATOR_SQL,
                (statement -> {
                    statement.setString(Column.NAME, authenticatorConfig.getName());
                    statement.setString(Column.DISPLAY_NAME, authenticatorConfig.getDisplayName());
                    statement.setString(Column.DEFINED_BY, authenticatorConfig.getDefinedByType().toString());
                    statement.setString(Column.AUTHENTICATION_TYPE, authenticatorConfig.getAuthenticationType()
                            .toString());
                    statement.setInt(Column.IS_ENABLED, authenticatorConfig.isEnabled() ? 1 : 0);
                    statement.setString(Column.IDP_NAME, LOCAL_IDP_NAME);
                    statement.setInt(Column.TENANT_ID, tenantId);
                }), null, false);

            int authenticatorConfigID = getAuthenticatorIdentifier(authenticatorConfig.getName(), tenantId);
            addAuthenticatorProperties(authenticatorConfig.getName(), authenticatorConfigID,
                    authenticatorConfig.getProperties(), tenantId);

            return getUserDefinedLocalAuthenticatorByName(authenticatorConfig.getName(), tenantId);
        } catch (DataAccessException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while adding the authenticator: %s in tenant domain: %s. " +
                                "Rolling back added Authenticator information.", authenticatorConfig.getName(),
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_ADDING_AUTHENTICATOR, e);
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig existingAuthenticatorConfig,
            UserDefinedLocalAuthenticatorConfig updatedAuthenticatorConfig, int tenantId)
            throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.executeUpdate(Query.UPDATE_AUTHENTICATOR_SQL,
                statement -> {
                    statement.setString(Column.DISPLAY_NAME, updatedAuthenticatorConfig.getDisplayName());
                    statement.setInt(Column.IS_ENABLED, updatedAuthenticatorConfig.isEnabled() ? 1 : 0);
                    statement.setString(Column.NAME, existingAuthenticatorConfig.getName());
                    statement.setInt(Column.TENANT_ID, tenantId);
                    statement.executeUpdate();
                });

            return getUserDefinedLocalAuthenticatorByName(updatedAuthenticatorConfig.getName(), tenantId);
        } catch (DataAccessException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the authenticator: %s in tenant domain: %s. " +
                                "Rolling back updated Authenticator information.",
                                existingAuthenticatorConfig.getName(), IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_UPDATING_AUTHENTICATOR, e);
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            return getUserDefinedLocalAuthenticatorByName(authenticatorConfigName, tenantId);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
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

                    int authenticatorConfigID = getAuthenticatorIdentifier(config.getName(), tenantId);
                    try (NamedPreparedStatement statementProp = new NamedPreparedStatement(dbConnection,
                            Query.GET_AUTHENTICATOR_PROP_SQL)) {
                        statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                        statementProp.setInt(Column.TENANT_ID, tenantId);

                        try (ResultSet rsProp = statementProp.executeQuery()) {
                            List<Property> properties = new ArrayList<>();
                            while (rsProp.next()) {
                                Property property = new Property();
                                property.setName(rsProp.getString(Column.PROPERTY_KEY));
                                property.setValue(rsProp.getString(Column.PROPERTY_VALUE));
                                property.setConfidential(false);
                                properties.add(property);
                            }
                            config.setProperties(properties.toArray(new Property[0]));
                        }
                    }

                    allUserDefinedLocalConfigs.add(config);
                }
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
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

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {

            jdbcTemplate.executeUpdate(Query.DELETE_AUTHENTICATOR_SQL,
                    statement -> {
                        statement.setString(Column.NAME, authenticatorConfigName);
                        statement.setInt(Column.TENANT_ID, tenantId);
                        statement.executeUpdate();
                    });
        } catch (DataAccessException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the authenticator: %s in tenant domain: %s. " +
                                "Rolling back deleted Authenticator information.", authenticatorConfigName,
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_DELETING_AUTHENTICATOR, e);
        }
    }

    private UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticatorByName(String authenticatorConfigName,
            int tenantId) throws AuthenticatorMgtServerException, DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        UserDefinedLocalAuthenticatorConfig authConfig = jdbcTemplate.fetchSingleRecord(Query.GET_AUTHENTICATOR_SQL,
                (resultSet, rowNumber) -> {
                    UserDefinedLocalAuthenticatorConfig config = getLocalAuthenticatorConfigBasedOnType(
                            resultSet.getString(Column.AUTHENTICATION_TYPE));
                    config.setName(resultSet.getString(Column.NAME));
                    config.setDisplayName(resultSet.getString(Column.DISPLAY_NAME));
                    config.setEnabled(resultSet.getString(Column.IS_ENABLED).equals(IS_TRUE_VALUE));
                    config.setDefinedByType(DefinedByType.USER);
                    return config;
                },
                statement -> {
                    statement.setString(Column.NAME, authenticatorConfigName);
                    statement.setInt(Column.TENANT_ID, tenantId);
                    statement.setString(Column.DEFINED_BY, DefinedByType.USER.toString());
                });

        if (authConfig == null) {
            return null;
        }

        int authenticatorConfigID = getAuthenticatorIdentifier(authenticatorConfigName, tenantId);
        List<Property> properties = new ArrayList<>();
        jdbcTemplate.fetchSingleRecord(Query.GET_AUTHENTICATOR_PROP_SQL,
                (resultSet, rowNumber) -> {
                    Property property = new Property();
                    property.setName(resultSet.getString(Column.PROPERTY_KEY));
                    property.setValue(resultSet.getString(Column.PROPERTY_VALUE));
                    property.setConfidential(false);
                    properties.add(property);
                    return null;
                },
                statementProp -> {
                    statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                    statementProp.setInt(Column.TENANT_ID, tenantId);
                });
        authConfig.setProperties(properties.toArray(new Property[0]));
        return authConfig;
    }

    private UserDefinedLocalAuthenticatorConfig getLocalAuthenticatorConfigBasedOnType(String authenticationType) {

        if (AuthenticationType.VERIFICATION.toString().equals(authenticationType)) {
            return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.VERIFICATION);
        }
        return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.IDENTIFICATION);
    }

    private int getAuthenticatorIdentifier(String authenticatorConfigName, int tenantId)
            throws AuthenticatorMgtServerException, DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String id = jdbcTemplate.fetchSingleRecord(Query.GET_AUTHENTICATOR_ID_SQL,
            (resultSet, rowNumber) -> resultSet.getString(Column.ID),
            statement -> {
                statement.setString(Column.NAME, authenticatorConfigName);
                statement.setInt(Column.TENANT_ID, tenantId);
            });

        if (id != null) {
            return Integer.parseInt(id);
        }
        throw buildServerException(AuthenticatorMgtError.ERROR_CODE_NO_AUTHENTICATOR_FOUND,
                    authenticatorConfigName);
    }

    private void addAuthenticatorProperties(String authenticatorName,
                                            int authenticatorConfigID, Property[] properties, int tenantId)
            throws DataAccessException, AuthenticatorMgtServerException {

        if (!(properties.length == 1 && ACTION_ID_PROPERTY.equals(properties[0].getName()))) {
            throw buildServerException(AuthenticatorMgtError.ERROR_CODE_HAVING_MULTIPLE_PROP,
                    properties[0].getName());
        }

        Property prop = properties[0];
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.executeInsert(Query.ADD_AUTHENTICATOR_PROP_SQL,
            (statementProp -> {
                statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                statementProp.setInt(Column.TENANT_ID, tenantId);
                statementProp.setString(Column.PROPERTY_KEY, prop.getName());
                statementProp.setString(Column.PROPERTY_VALUE, prop.getValue());
                if (prop.isConfidential()) {
                    statementProp.setString(Column.IS_SECRET, IS_TRUE_VALUE);
                } else {
                    statementProp.setString(Column.IS_SECRET, IS_FALSE_VALUE);
                }
            }), null, false);
    }
}
