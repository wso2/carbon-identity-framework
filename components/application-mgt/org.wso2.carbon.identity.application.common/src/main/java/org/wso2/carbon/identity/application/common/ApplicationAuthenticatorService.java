/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.dao.impl.AuthenticatorManagementDAOImpl;
import org.wso2.carbon.identity.application.common.dao.impl.CacheBackedAuthenticatorMgtDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;
import org.wso2.carbon.identity.application.common.util.UserDefinedLocalAuthenticatorValidator;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildClientException;
import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildRuntimeServerException;

/**
 * Application authenticator service.
 */
public class ApplicationAuthenticatorService {

    private static volatile ApplicationAuthenticatorService instance;
    private static final Log LOG = LogFactory.getLog(ApplicationAuthenticatorService.class);
    private static final AuthenticatorManagementDAO dao =
            new CacheBackedAuthenticatorMgtDAO(new AuthenticatorManagementDAOImpl());

    private List<LocalAuthenticatorConfig> localAuthenticators = new ArrayList<>();
    private List<FederatedAuthenticatorConfig> federatedAuthenticators = new ArrayList<>();
    private List<RequestPathAuthenticatorConfig> requestPathAuthenticators = new ArrayList<>();
    private UserDefinedLocalAuthenticatorValidator authenticatorValidator =
            new UserDefinedLocalAuthenticatorValidator();

    public static ApplicationAuthenticatorService getInstance() {
        if (instance == null) {
            synchronized (ApplicationAuthenticatorService.class) {
                if (instance == null) {
                    instance = new ApplicationAuthenticatorService();
                }
            }
        }
        return instance;
    }

    /**
     * This method is used to get the list of SYSTEM defined local authenticator configurations.
     *
     * @deprecated It is recommended to use {@link #getAllSystemDefinedLocalAuthenticators()},
     * which returning both SYSTEM and USER defined local application authenticator configurations.
     */
    @Deprecated
    public List<LocalAuthenticatorConfig> getLocalAuthenticators() {
        return this.localAuthenticators;
    }

    /**
     * This returns list of all SYSTEM defined local authenticator configurations.
     *
     * @return Retrieved LocalAuthenticatorConfig.
     */
    public List<LocalAuthenticatorConfig> getAllSystemDefinedLocalAuthenticators() {

        return this.localAuthenticators;
    }

    /**
     * This returns list of all SYSTEM and USER defined local authenticator configurations.
     *
     * @param tenantDomain  Tenant domain.
     * @return Retrieved LocalAuthenticatorConfig.
     */
    public List<LocalAuthenticatorConfig> getAllLocalAuthenticators(String tenantDomain)
            throws AuthenticatorMgtException {

        List<LocalAuthenticatorConfig> configList = new ArrayList<>(getAllUserDefinedLocalAuthenticators(tenantDomain));
        configList.addAll(localAuthenticators);
        return configList;
    }

    /**
     * This returns user defined local authenticators.
     *
     * @param tenantDomain  Tenant domain.
     * @return Retrieved LocalAuthenticatorConfig.
     */
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(String tenantDomain)
            throws AuthenticatorMgtException {

        return dao.getAllUserDefinedLocalAuthenticators(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    public List<FederatedAuthenticatorConfig> getFederatedAuthenticators() {
        return this.federatedAuthenticators;
    }

    public List<RequestPathAuthenticatorConfig> getRequestPathAuthenticators() {
        return this.requestPathAuthenticators;
    }

    /**
     * This returns only SYSTEM defined local authenticator by name.
     *
     * @param name  The name of the Local Application Authenticator configuration.
     * @return Retrieved LocalAuthenticatorConfig.
     *
     * @deprecated It is recommended to use {@link #getLocalAuthenticatorByName(String, String)},
     * which supports retrieving both USER and SYSTEM defined Local Application Authenticator configuration by name.
     */
    @Deprecated
    public LocalAuthenticatorConfig getLocalAuthenticatorByName(String name) {
        for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
            if (localAuthenticator.getName().equals(name)) {
                return localAuthenticator;
            }
        }
        return null;
    }

    /**
     * Retrieve both USER and SYSTEM defined Local Application Authenticator configuration by name.
     *
     * @param name                  The name of the Local Application Authenticator configuration.
     * @param tenantDomain          Tenant domain.
     * @return Retrieved LocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while retrieving the authenticator configuration by name.
     */
    public LocalAuthenticatorConfig getLocalAuthenticatorByName(String name, String tenantDomain)
            throws AuthenticatorMgtException {

        /* First, check whether an authenticator by the given name is in the system defined authenticators list.
         If not, check in user defined authenticators. */
        for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
            if (localAuthenticator.getName().equals(name)) {
                return localAuthenticator;
            }
        }
        return getUserDefinedLocalAuthenticator(name, tenantDomain);
    }

    /**
     * This returns only SYSTEM defined federated authenticator by name.
     *
     * @param name  The name of the federated application authenticator configuration.
     * @return Retrieved FederatedAuthenticatorConfig.
     *
     * @deprecated It is recommended to use getFederatedAuthenticatorByName(String, String) in
     * org.wso2.carbon.idp.mgt.IdentityProviderManager class which supports retrieving both USER and SYSTEM defined
     * federated application authenticator configuration by name, or getSystemFederatedAuthenticatorByName(String)
     * to retrieve only SYSTEM defined federated application authenticator configuration by name.
     */
    @Deprecated
    public FederatedAuthenticatorConfig getFederatedAuthenticatorByName(String name) {
        for (FederatedAuthenticatorConfig federatedAuthenticator : federatedAuthenticators) {
            if (federatedAuthenticator.getName().equals(name)) {
                return federatedAuthenticator;
            }
        }
        return null;
    }

    public FederatedAuthenticatorConfig getSystemDefinedFederatedAuthenticatorByName(String name) {

        for (FederatedAuthenticatorConfig federatedAuthenticator : federatedAuthenticators) {
            if (federatedAuthenticator.getName().equals(name)) {
                return federatedAuthenticator;
            }
        }
        return null;
    }

    public RequestPathAuthenticatorConfig getRequestPathAuthenticatorByName(String name) {
        for (RequestPathAuthenticatorConfig reqPathAuthenticator : requestPathAuthenticators) {
            if (reqPathAuthenticator.getName().equals(name)) {
                return reqPathAuthenticator;
            }
        }
        return null;
    }

    /**
     * Add a system defined Local Application Authenticator configuration.
     *
     * @param authenticator The Local Application Authenticator configuration.
     */
    public void addLocalAuthenticator(LocalAuthenticatorConfig authenticator) {

        if (authenticator != null) {
            if (authenticator.getDefinedByType() != DefinedByType.SYSTEM) {
                throw buildRuntimeServerException(
                        AuthenticatorMgtError.ERROR_CODE_INVALID_DEFINED_BY_AUTH_PROVIDED, null);
            }
            localAuthenticators.add(authenticator);
        }
    }

    public void removeLocalAuthenticator(LocalAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            localAuthenticators.remove(authenticator);
        }
    }

    public void addFederatedAuthenticator(FederatedAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            federatedAuthenticators.add(authenticator);
        }
    }

    public void removeFederatedAuthenticator(FederatedAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            federatedAuthenticators.remove(authenticator);
        }
    }

    public void addRequestPathAuthenticator(RequestPathAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            requestPathAuthenticators.add(authenticator);
        }
    }

    public void removeRequestPathAuthenticator(RequestPathAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            requestPathAuthenticators.remove(authenticator);
        }
    }

    /**
     * Create a user defined Local Application Authenticator configuration.
     *
     * @param authenticatorConfig  The Local Application Authenticator configuration.
     * @param tenantDomain         Tenant domain.
     * @return Updated LocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while creating the authenticator configuration.
     */
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, String tenantDomain)
            throws AuthenticatorMgtException {

        if (isExistingAuthenticatorName(authenticatorConfig.getName(), tenantDomain)) {
            throw buildClientException(AuthenticatorMgtError.ERROR_AUTHENTICATOR_ALREADY_EXIST,
                    authenticatorConfig.getName());
        }
        authenticatorValidator.validateAuthenticatorName(authenticatorConfig.getName());
        authenticatorValidator.validateDisplayName(authenticatorConfig.getDisplayName());
        if (authenticatorConfig.getImageUrl() != null) {
            authenticatorValidator.validateUrl(authenticatorConfig.getImageUrl());
        }

        return dao.addUserDefinedLocalAuthenticator(
                authenticatorConfig, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Update a user defined Local Application Authenticator configuration.
     *
     * @param authenticatorConfig   The Local Application Authenticator configuration.
     * @param tenantDomain          Tenant Domain.
     * @return Updated UserDefinedLocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while updating the authenticator configuration.
     */
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, String tenantDomain)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig existingConfig = resolveExistingAuthenticator(
                authenticatorConfig.getName(), tenantDomain);
        if (existingConfig == null) {
            throw buildClientException(AuthenticatorMgtError.ERROR_NOT_FOUND_AUTHENTICATOR,
                    authenticatorConfig.getName());
        }
        authenticatorValidator.validateDisplayName(authenticatorConfig.getDisplayName());
        if (authenticatorConfig.getImageUrl() != null) {
            authenticatorValidator.validateUrl(authenticatorConfig.getImageUrl());
        }

        return dao.updateUserDefinedLocalAuthenticator(
                existingConfig, authenticatorConfig, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Update a Local Application Authenticator configuration.
     *
     * @param authenticatorName   Name of Local Application Authenticator configuration to be deleted.
     * @param tenantDomain        Tenant domain.
     * @throws AuthenticatorMgtException If an error occurs while deleting the authenticator configuration.
     */
    public void deleteUserDefinedLocalAuthenticator(String authenticatorName, String tenantDomain)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig existingConfig = resolveExistingAuthenticator(
                authenticatorName, tenantDomain);
        if (existingConfig == null) {
            return;
        }

        dao.deleteUserDefinedLocalAuthenticator(authenticatorName, existingConfig,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Retrieve a Local Application Authenticator configuration by name.
     *
     * @param authenticatorName   Name of Local Application Authenticator configuration to be deleted.
     * @param tenantDomain        Tenant domain.
     * @return Retrieved UserDefinedLocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while retrieving the authenticator configuration.
     */
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(String authenticatorName,
            String tenantDomain) throws AuthenticatorMgtException {

        return dao.getUserDefinedLocalAuthenticator(
                authenticatorName, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Check whether any local or federated authenticator configuration exists with the given name.
     *
     * @param authenticatorName Name of the authenticator.
     * @param tenantDomain      Tenant domain.
     * @return True if an authenticator with the given name exists.
     * @throws AuthenticatorMgtException If an error occurs while checking the existence of the authenticator.
     */
    public boolean isExistingAuthenticatorName(String authenticatorName, String tenantDomain)
            throws AuthenticatorMgtException {

        // Check whether an authenticator with the given name exists in the database.
        if (dao.isExistingAuthenticatorName(authenticatorName, IdentityTenantUtil.getTenantId(tenantDomain))) {
            return true;
        }

        /* Check whether an authenticator with the given name exists in the system defined authenticators
        which are not saved in database. */
        for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
            if (localAuthenticator.getName().equals(authenticatorName)) {
                return true;
            }
        }

        /* Check whether an authenticator with the given name exists in the federated defined authenticators
        which are not saved in database. */
        for (FederatedAuthenticatorConfig federatedAuthenticator : federatedAuthenticators) {
            if (federatedAuthenticator.getName().equals(authenticatorName)) {
                return true;
            }
        }
        return false;
    }

    private UserDefinedLocalAuthenticatorConfig resolveExistingAuthenticator(String authenticatorName,
            String tenantDomain) throws AuthenticatorMgtException {

        return dao.getUserDefinedLocalAuthenticator(authenticatorName, IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
