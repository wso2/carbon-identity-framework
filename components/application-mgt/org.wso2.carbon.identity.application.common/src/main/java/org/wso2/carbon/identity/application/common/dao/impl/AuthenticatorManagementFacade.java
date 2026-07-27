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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtClientException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;
import org.wso2.carbon.identity.application.common.util.UserDefinedAuthenticatorEndpointConfigManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.util.List;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildServerException;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Authenticator.ACTION_ID_PROPERTY;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined local
 * authenticators.
 */
public class AuthenticatorManagementFacade implements AuthenticatorManagementDAO {

    private final AuthenticatorManagementDAO dao;
    private final UserDefinedAuthenticatorEndpointConfigManager endpointConfigManager =
            new UserDefinedAuthenticatorEndpointConfigManager();

    public AuthenticatorManagementFacade(AuthenticatorManagementDAO dao) {

        this.dao = dao;
    }

    /**
     * Invoke external service to store associated data (endpoint configuration) and create the user defined local
     * authenticator to the DB.
     *
     * @param authenticatorConfig User defined local authenticator configuration.
     * @param tenantId            Tenant ID.
     * @return User defined local authenticator configuration.
     * @throws AuthenticatorMgtException If an error occurs while adding the user defined local authenticator.
     */
    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                endpointConfigManager.addEndpointConfigurations(authenticatorConfig, tenantId);
                validateAuthenticatorProperties(authenticatorConfig);
                return endpointConfigManager.resolveEndpointConfigurations(
                        dao.addUserDefinedLocalAuthenticator(authenticatorConfig, tenantId), tenantId);
            });
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(AuthenticatorMgtError.ERROR_WHILE_ADDING_AUTHENTICATOR, e,
                    authenticatorConfig.getName());
        }
    }

    /**
     * Invoke external service to update associated data (endpoint configuration) and update the user defined local
     * authenticator in DB.
     *
     * @param existingAuthenticatorConfig Existing user defined local authenticator configuration.
     * @param newAuthenticatorConfig      New user defined local authenticator configuration.
     * @param tenantId                    Tenant ID.
     * @return Updated user defined local authenticator configuration.
     * @throws AuthenticatorMgtException If an error occurs while updating the user defined local authenticator.
     */
    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig
            existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig,
            int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                endpointConfigManager.updateEndpointConfigurations(newAuthenticatorConfig, existingAuthenticatorConfig,
                        tenantId);
                validateAuthenticatorProperties(newAuthenticatorConfig);
                return endpointConfigManager.resolveEndpointConfigurations(
                            dao.updateUserDefinedLocalAuthenticator(existingAuthenticatorConfig, newAuthenticatorConfig,
                                    tenantId), tenantId);
            });
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(AuthenticatorMgtError.ERROR_WHILE_UPDATING_AUTHENTICATOR, e,
                    newAuthenticatorConfig.getName());
        }
    }

    /**
     * Get user defined local authenticator by name and resolving associated data (endpoint configurations)
     * by invoking external service.
     *
     * @param authenticatorConfigName Name of the user defined local authenticator.
     * @param tenantId                Tenant ID.
     * @return User defined local authenticator.
     * @throws AuthenticatorMgtException If an error occurs while retrieving the user defined local authenticator.
     */
    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    endpointConfigManager.resolveEndpointConfigurations(dao.getUserDefinedLocalAuthenticator(
                            authenticatorConfigName, tenantId), tenantId));
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(AuthenticatorMgtError.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME, e,
                    authenticatorConfigName);
        }
    }

    /**
     * Get all user defined local authenticators and resolving associated data (endpoint configurations)
     * by invoking external service.
     *
     * @param tenantId Tenant ID.
     * @return List of user defined local authenticators.
     * @throws AuthenticatorMgtException If an error occurs while retrieving all user defined local authenticators.
     */
    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
            throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                List<UserDefinedLocalAuthenticatorConfig> configList =
                        dao.getAllUserDefinedLocalAuthenticators(tenantId);
                // TODO: Utilize a batch operation once issue:https://github.com/wso2/product-is/issues/21783 is done.
                for (UserDefinedLocalAuthenticatorConfig config : configList) {
                    endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
                }
                return configList;
            });
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(
                    AuthenticatorMgtError.ERROR_WHILE_ALL_RETRIEVING_AUTHENTICATOR, e, StringUtils.EMPTY);
        }
    }

    /**
     * Invoke external service to delete associated data (endpoint configuration) and delete the user defined local
     * authenticator in DB.
     *
     * @param authenticatorConfigName Name of the user defined local authenticator.
     * @param authenticatorConfig     User defined local authenticator configuration.
     * @param tenantId                Tenant ID.
     * @throws AuthenticatorMgtException If an error occurs while deleting the user defined local authenticator.
     */
    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, UserDefinedLocalAuthenticatorConfig
            authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                endpointConfigManager.deleteEndpointConfigurations(authenticatorConfig, tenantId);
                dao.deleteUserDefinedLocalAuthenticator(authenticatorConfigName, authenticatorConfig, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(AuthenticatorMgtError.ERROR_WHILE_DELETING_AUTHENTICATOR, e,
                    StringUtils.EMPTY);
        }
    }

    @Override
    public boolean isExistingAuthenticatorName(String authenticatorName, int tenantId)
            throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(
                    template -> dao.isExistingAuthenticatorName(authenticatorName, tenantId));
        } catch (TransactionException e) {
            throw handleAuthenticatorMgtException(AuthenticatorMgtError
                            .ERROR_WHILE_CHECKING_FOR_EXISTING_AUTHENTICATOR_BY_NAME, e, authenticatorName);
        }
    }

    /**
     * Handle the authenticator management client exception.
     *
     * @param throwable Throwable object.
     * @throws AuthenticatorMgtClientException If an authenticator management client exception.
     */
    private static AuthenticatorMgtException handleAuthenticatorMgtException(AuthenticatorMgtError
            authenticatorMgtError, Throwable throwable, String... data) throws AuthenticatorMgtException {

        if (throwable.getCause() instanceof AuthenticatorMgtClientException) {
            AuthenticatorMgtClientException error = (AuthenticatorMgtClientException) throwable.getCause();
            throw new AuthenticatorMgtClientException(error.getErrorCode(), error.getMessage(), error.getDescription());
        }

        throw buildServerException(authenticatorMgtError, throwable, data);
    }
    
    private void validateAuthenticatorProperties(UserDefinedLocalAuthenticatorConfig authenticatorConfig) 
            throws AuthenticatorMgtServerException {

        // User defined local authenticator should have only one property which is the action id.
        Property[] properties = authenticatorConfig.getProperties();
        if (!(properties.length == 1 && ACTION_ID_PROPERTY.equals(properties[0].getName()))) {
            throw buildServerException(AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError
                            .ERROR_CODE_HAVING_MULTIPLE_PROP, authenticatorConfig.getName());
        }
    }
}
