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
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtClientException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.UserDefinedAuthenticatorEndpointConfigManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.List;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined local
 * authenticators.
 */
public class AuthenticatorManagementFacade implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(AuthenticatorManagementFacade.class);

    private final AuthenticatorManagementDAO dao;
    private UserDefinedAuthenticatorEndpointConfigManager endpointConfigManager =
            new UserDefinedAuthenticatorEndpointConfigManager();

    public AuthenticatorManagementFacade(AuthenticatorManagementDAO dao) {

        this.dao = dao;
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {


        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                endpointConfigManager.addEndpointConfigurations(authenticatorConfig, tenantId);
                return endpointConfigManager.resolveEndpointConfigurations(
                        dao.addUserDefinedLocalAuthenticator(authenticatorConfig, tenantId), tenantId);
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            LOG.debug("Error while creating the user defined local authenticator: " + authenticatorConfig.getName() +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back created authenticator information, and associated action.");
            throw handleAuthenticatorMgtException(e.getCause());
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig
            existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig,
            int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                endpointConfigManager.updateEndpointConfigurations(newAuthenticatorConfig, existingAuthenticatorConfig,
                        tenantId);
                return endpointConfigManager.resolveEndpointConfigurations(
                            dao.updateUserDefinedLocalAuthenticator(existingAuthenticatorConfig, newAuthenticatorConfig,
                                    tenantId), tenantId);
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            LOG.debug("Error while updating the user defined local authenticator: " + newAuthenticatorConfig
                    .getName() + " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back updated authenticator information, and associated action.");
            throw handleAuthenticatorMgtException(e.getCause());
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    endpointConfigManager.resolveEndpointConfigurations(dao.getUserDefinedLocalAuthenticator(
                            authenticatorConfigName, tenantId), tenantId));
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            LOG.debug("Error while retrieving the user defined local authenticator: " + authenticatorConfigName +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId));
            throw handleAuthenticatorMgtException(e.getCause());
        }
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
            throws AuthenticatorMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                List<UserDefinedLocalAuthenticatorConfig> configList =
                        dao.getAllUserDefinedLocalAuthenticators(tenantId);
                for (UserDefinedLocalAuthenticatorConfig config : configList) {
                    endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
                }
                return configList;
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            LOG.debug("Error while retrieving all user defined local authenticators in Tenant Domain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId));
            throw handleAuthenticatorMgtException(e.getCause());
        }
    }

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
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            LOG.debug("Error while deleting the user defined local authenticator: " + authenticatorConfigName +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back deleted authenticator information, and associated action.");
            throw handleAuthenticatorMgtException(e.getCause());
        }
    }

    /**
     * Handle the authenticator management client exception.
     *
     * @param throwable Throwable object.
     * @throws AuthenticatorMgtClientException If an authenticator management client exception.
     */
    private static AuthenticatorMgtException handleAuthenticatorMgtException(Throwable throwable)
            throws AuthenticatorMgtException {

        if (throwable instanceof AuthenticatorMgtClientException) {
            AuthenticatorMgtClientException error = (AuthenticatorMgtClientException) throwable;
            throw new AuthenticatorMgtClientException(error.getErrorCode(), error.getMessage(), error.getDescription());
        }

        AuthenticatorMgtServerException error = (AuthenticatorMgtServerException) throwable;
        throw new AuthenticatorMgtServerException(error.getErrorCode(), error.getMessage(), error.getDescription());
    }
}
