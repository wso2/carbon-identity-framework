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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildServerException;

/**
 * This class implements the AuthenticatorManagementDAO interface which perform CRUD operation on database.
 */
public class AuthenticatorManagementDAOImpl implements AuthenticatorManagementDAO {

    private static final String IS_TRUE_VALUE = "1";
    private static final String IS_FALSE_VALUE = "0";
    private static final String LOCAL_IDP_NAME = "LOCAL";

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            int authenticatorConfigID = jdbcTemplate.withTransaction(template ->
                template.executeInsert(Query.ADD_AUTHENTICATOR_SQL,
                    statement -> {
                        statement.setString(Column.NAME, authenticatorConfig.getName());
                        statement.setString(Column.DISPLAY_NAME, authenticatorConfig.getDisplayName());
                        statement.setString(Column.IMAGE_URL, authenticatorConfig.getImageUrl());
                        statement.setString(Column.DESCRIPTION, authenticatorConfig.getDescription());
                        statement.setString(Column.DEFINED_BY, authenticatorConfig.getDefinedByType().toString());
                        statement.setString(Column.AUTHENTICATION_TYPE, authenticatorConfig.getAuthenticationType()
                                .toString());
                        statement.setString(Column.IS_ENABLED,
                                authenticatorConfig.isEnabled() ? IS_TRUE_VALUE : IS_FALSE_VALUE);
                        statement.setString(Column.IDP_NAME, LOCAL_IDP_NAME);
                        statement.setInt(Column.TENANT_ID, tenantId);
                    }, null, true));

            if (authenticatorConfigID == 0) {
                authenticatorConfigID = getAuthenticatorEntryId(authenticatorConfig.getName(), tenantId);
            }
            addAuthenticatorProperty(authenticatorConfigID, authenticatorConfig.getProperties(), tenantId);

            return getUserDefinedLocalAuthenticatorByName(authenticatorConfig.getName(), tenantId);
        } catch (TransactionException e) {
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
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.UPDATE_AUTHENTICATOR_SQL,
                    statement -> {
                        statement.setString(Column.DISPLAY_NAME, updatedAuthenticatorConfig.getDisplayName());
                        statement.setString(Column.IMAGE_URL, updatedAuthenticatorConfig.getImageUrl());
                        statement.setString(Column.DESCRIPTION, updatedAuthenticatorConfig.getDescription());
                        statement.setString(Column.IS_ENABLED,
                                updatedAuthenticatorConfig.isEnabled() ? IS_TRUE_VALUE : IS_FALSE_VALUE);
                        statement.setString(Column.NAME, existingAuthenticatorConfig.getName());
                        statement.setInt(Column.TENANT_ID, tenantId);
                    });
                return null;
            });

            return getUserDefinedLocalAuthenticatorByName(updatedAuthenticatorConfig.getName(), tenantId);
        } catch (TransactionException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_UPDATING_AUTHENTICATOR, e);
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        try {
            return getUserDefinedLocalAuthenticatorByName(authenticatorConfigName, tenantId);
        } catch (TransactionException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME, e);
        }
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
            throws AuthenticatorMgtException {

        try {
            NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
            List<UserDefinedLocalAuthenticatorConfig> allUserDefinedLocalConfigs = new ArrayList<>();
            List<AuthenticatorConfigDaoModel> configDaoModels = jdbcTemplate.withTransaction(
                template -> template.executeQuery(Query.GET_ALL_USER_DEFINED_AUTHENTICATOR_SQL,
                    (resultSet, rowNumber) -> {
                        UserDefinedLocalAuthenticatorConfig config = getLocalAuthenticatorConfigBasedOnType(
                                resultSet.getString(Column.AUTHENTICATION_TYPE));
                        config.setName(resultSet.getString(Column.NAME));
                        config.setDisplayName(resultSet.getString(Column.DISPLAY_NAME));
                        config.setImageUrl(resultSet.getString(Column.IMAGE_URL));
                        config.setDescription(resultSet.getString(Column.DESCRIPTION));
                        config.setEnabled(resultSet.getString(Column.IS_ENABLED).equals(IS_TRUE_VALUE));
                        config.setDefinedByType(DefinedByType.valueOf(resultSet.getString(Column.DEFINED_BY)));
                        return new AuthenticatorConfigDaoModel(resultSet.getInt(Column.ID), config);
                    },
                    statement -> {
                        statement.setString(Column.DEFINED_BY, DefinedByType.USER.toString());
                        statement.setInt(Column.TENANT_ID, tenantId);
                        statement.setString(Column.IDP_NAME, LOCAL_IDP_NAME);
                    }));

            for (AuthenticatorConfigDaoModel config: configDaoModels) {
                UserDefinedLocalAuthenticatorConfig retrievedConfigs = config.getConfig();
                retrievedConfigs.setProperties(getAuthenticatorProperties(config.getEntryId(), tenantId));
                allUserDefinedLocalConfigs.add(retrievedConfigs);
            }
            return allUserDefinedLocalConfigs;
        } catch (TransactionException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME, e);
        }
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, 
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.DELETE_AUTHENTICATOR_SQL,
                    statement -> {
                        statement.setString(Column.NAME, authenticatorConfigName);
                        statement.setInt(Column.TENANT_ID, tenantId);
                        statement.executeUpdate();
                    });
                return null;
            });
        } catch (TransactionException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_DELETING_AUTHENTICATOR, e);
        }
    }

    public boolean isExistingAuthenticatorName(String authenticatorName, int tenantId)
            throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            ResultSet results = jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(Query.IS_AUTHENTICATOR_EXISTS_BY_NAME_SQL,
                        (resultSet, rowNumber) -> resultSet,
                        statement -> {
                            statement.setString(Column.NAME, authenticatorName);
                            statement.setInt(Column.TENANT_ID, tenantId);
                        }));
            return results != null;
        } catch (TransactionException e) {
            throw buildServerException(AuthenticatorMgtError.ERROR_WHILE_CHECKING_FOR_EXISTING_AUTHENTICATOR_BY_NAME, e,
                    authenticatorName);
        }
    }

    private UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticatorByName(String authenticatorConfigName,
            int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        AuthenticatorConfigDaoModel configDaoModel = jdbcTemplate.withTransaction(template ->
            template.fetchSingleRecord(Query.GET_USER_DEFINED_LOCAL_AUTHENTICATOR_SQL,
                (resultSet, rowNumber) -> {
                    UserDefinedLocalAuthenticatorConfig config = getLocalAuthenticatorConfigBasedOnType(
                            resultSet.getString(Column.AUTHENTICATION_TYPE));
                    config.setName(resultSet.getString(Column.NAME));
                    config.setDisplayName(resultSet.getString(Column.DISPLAY_NAME));
                    config.setEnabled(resultSet.getString(Column.IS_ENABLED).equals(IS_TRUE_VALUE));
                    config.setDefinedByType(DefinedByType.USER);
                    config.setImageUrl(resultSet.getString(Column.IMAGE_URL));
                    config.setDescription(resultSet.getString(Column.DESCRIPTION));
                    return new AuthenticatorConfigDaoModel(resultSet.getInt(Column.ID), config);
                },
                statement -> {
                    statement.setString(Column.NAME, authenticatorConfigName);
                    statement.setInt(Column.TENANT_ID, tenantId);
                    statement.setString(Column.DEFINED_BY, DefinedByType.USER.toString());
                    statement.setString(Column.IDP_NAME, LOCAL_IDP_NAME);
                }));

        if (configDaoModel == null) {
            return null;
        }

        UserDefinedLocalAuthenticatorConfig config = configDaoModel.getConfig();
        config.setProperties(getAuthenticatorProperties(configDaoModel.getEntryId(), tenantId));
        return config;
    }

    private UserDefinedLocalAuthenticatorConfig getLocalAuthenticatorConfigBasedOnType(String authenticationType) {

        if (AuthenticationType.VERIFICATION.toString().equals(authenticationType)) {
            return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.VERIFICATION);
        }
        return new UserDefinedLocalAuthenticatorConfig(AuthenticationType.IDENTIFICATION);
    }

    private int getAuthenticatorEntryId(String authenticatorConfigName, int tenantId)
            throws AuthenticatorMgtServerException, TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        int id = jdbcTemplate.withTransaction(template ->
            template.fetchSingleRecord(Query.GET_AUTHENTICATOR_ID_SQL,
               (resultSet, rowNumber) -> resultSet.getInt(Column.ID),
               statement -> {
                   statement.setString(Column.NAME, authenticatorConfigName);
                   statement.setInt(Column.TENANT_ID, tenantId);
               }));

        if (id != 0) {
            return id;
        }
        throw buildServerException(AuthenticatorMgtError.ERROR_CODE_NO_AUTHENTICATOR_FOUND,
                    authenticatorConfigName);
    }

    private void addAuthenticatorProperty(int authenticatorConfigID, Property[] properties, int tenantId)
            throws TransactionException {

        Property prop = properties[0];
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template ->
            template.executeInsert(Query.ADD_AUTHENTICATOR_PROP_SQL,
                statementProp -> {
                    statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                    statementProp.setInt(Column.TENANT_ID, tenantId);
                    statementProp.setString(Column.PROPERTY_KEY, prop.getName());
                    statementProp.setString(Column.PROPERTY_VALUE, prop.getValue());
                    statementProp.setString(Column.IS_SECRET, IS_FALSE_VALUE);
                }, null, false));
    }

    private Property[] getAuthenticatorProperties(int authenticatorConfigID, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        List<Property> properties = jdbcTemplate.withTransaction(template ->
            template.executeQuery(Query.GET_AUTHENTICATOR_PROP_SQL,
                (resultSet, rowNumber) -> {
                    Property property = new Property();
                    property.setName(resultSet.getString(Column.PROPERTY_KEY));
                    property.setValue(resultSet.getString(Column.PROPERTY_VALUE));
                    property.setConfidential(false);
                    return property;
                },
                statementProp -> {
                    statementProp.setInt(Column.AUTHENTICATOR_ID, authenticatorConfigID);
                    statementProp.setInt(Column.TENANT_ID, tenantId);
                }));
        return properties.toArray(new Property[0]);
    }

    /**
     * This class represents the user defined local authenticator configuration with entry id from DAO.
     */
    private static class AuthenticatorConfigDaoModel {

        private final int entryId;
        private final UserDefinedLocalAuthenticatorConfig config;

        private AuthenticatorConfigDaoModel(int entryId, UserDefinedLocalAuthenticatorConfig config) {
            this.entryId = entryId;
            this.config = config;
        }

        public int getEntryId() {
            return entryId;
        }

        public UserDefinedLocalAuthenticatorConfig getConfig() {
            return config;
        }
    }
}
