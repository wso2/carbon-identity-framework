/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ApplicationMgtValidator {

    private static Log log = LogFactory.getLog(ApplicationMgtValidator.class);

    private static final String AUTHENTICATOR_NOT_AVAILABLE = "Authenticator %s is not available in the server.";
    private static final String AUTHENTICATOR_NOT_CONFIGURED =
            "Authenticator %s is not configured for %s identity Provider.";
    private static final String PROVISIONING_CONNECTOR_NOT_CONFIGURED = "No Provisioning connector configured for %s.";
    private static final String FEDERATED_IDP_NOT_AVAILABLE =
            "Federated Identity Provider %s is not available in the server.";
    private static final String CLAIM_DIALECT_NOT_AVAILABLE = "Claim Dialect %s is not available in the server.";
    private static final String CLAIM_NOT_AVAILABLE = "Local claim %s is not available in the server.";
    private static final String ROLE_NOT_AVAILABLE = "Local Role %s is not available in the server.";
    public static final String IS_HANDLER = "IS_HANDLER";


    public void validateSPConfigurations(ServiceProvider serviceProvider, String tenantDomain,
                                            String userName) throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();
        validateLocalAndOutBoundAuthenticationConfig(validationMsg, serviceProvider.getLocalAndOutBoundAuthenticationConfig(),
                tenantDomain);
        validateRequestPathAuthenticationConfig(validationMsg, serviceProvider.getRequestPathAuthenticatorConfigs(), tenantDomain);
        validateOutBoundProvisioning(validationMsg, serviceProvider.getOutboundProvisioningConfig(), tenantDomain);
        validateClaimsConfigs(validationMsg, serviceProvider.getClaimConfig(),
                serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null ? serviceProvider
                        .getLocalAndOutBoundAuthenticationConfig().getSubjectClaimUri() : null, tenantDomain);
        validateRoleConfigs(validationMsg, serviceProvider.getPermissionAndRoleConfig(), tenantDomain);

        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    /**
     * Validate local and outbound authenticator related configurations and append to the validation msg list.
     *
     * @param validationMsg                        validation error messages
     * @param localAndOutBoundAuthenticationConfig local and out bound authentication config
     * @param tenantDomain                         tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception when unable to get the
     *                                                authenticator params
     */
    private void validateLocalAndOutBoundAuthenticationConfig(List<String> validationMsg,
            LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (localAndOutBoundAuthenticationConfig == null) {
            return;
        }

        AuthenticationStep[] authenticationSteps = localAndOutBoundAuthenticationConfig.getAuthenticationSteps();
        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return;
        }
        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        Map<String, Property[]> allLocalAuthenticators = Arrays.stream(applicationMgtService
                .getAllLocalAuthenticators(tenantDomain))
                .collect(Collectors.toMap(LocalAuthenticatorConfig::getName, LocalAuthenticatorConfig::getProperties));

        AtomicBoolean isAuthenticatorIncluded = new AtomicBoolean(false);

        for (AuthenticationStep authenticationStep : authenticationSteps) {
            for (IdentityProvider idp : authenticationStep.getFederatedIdentityProviders()) {
                validateFederatedIdp(idp, isAuthenticatorIncluded, validationMsg, tenantDomain);
            }
            for (LocalAuthenticatorConfig localAuth : authenticationStep.getLocalAuthenticatorConfigs()) {
                if (!allLocalAuthenticators.keySet().contains(localAuth.getName())) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_AVAILABLE, localAuth.getName()));
                } else if (!isAuthenticatorIncluded.get()) {
                    Property[] properties = allLocalAuthenticators.get(localAuth.getName());
                    if (properties.length == 0) {
                        isAuthenticatorIncluded.set(true);
                    } else {
                        for (Property property : properties) {
                            if (!(IS_HANDLER.equals(property.getName()) && Boolean.valueOf(property.getValue()))) {
                                isAuthenticatorIncluded.set(true);
                            }
                        }
                    }
                }
            }
        }
        if (!isAuthenticatorIncluded.get()) {
            validationMsg.add("No authenticator have been registered in the authentication flow.");
        }
    }

    /**
     * Validate request path authenticator related configurations and append to the validation msg list.
     *
     * @param validationMsg                        validation error messages
     * @param requestPathAuthenticatorConfigs request path authentication config
     * @param tenantDomain                         tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception when unable to get the
     *                                                authenticator params
     */
    private void validateRequestPathAuthenticationConfig(List<String> validationMsg,
                                                         RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs,
                                                         String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        Map<String, Property[]> allRequestPathAuthenticators = Arrays.stream(applicationMgtService
                .getAllRequestPathAuthenticators(tenantDomain))
                .collect(Collectors.toMap(RequestPathAuthenticatorConfig::getName,
                        RequestPathAuthenticatorConfig::getProperties));

        if (requestPathAuthenticatorConfigs != null) {
            for (RequestPathAuthenticatorConfig config : requestPathAuthenticatorConfigs) {
                if (!allRequestPathAuthenticators.keySet().contains(config.getName())) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_AVAILABLE, config.getName()));
                }
            }
        }
    }

    private void validateFederatedIdp(IdentityProvider idp, AtomicBoolean isAuthenticatorIncluded, List<String>
            validationMsg, String tenantDomain) {

        try {
            IdentityProvider savedIdp = IdentityProviderManager.getInstance().getIdPByName(idp
                    .getIdentityProviderName(), tenantDomain, false);
            if (savedIdp.getId() == null) {
                validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                        idp.getIdentityProviderName()));
            } else if (savedIdp.getFederatedAuthenticatorConfigs() != null) {
                isAuthenticatorIncluded.set(true);
                List<String> savedIdpAuthenticators = Arrays.stream(savedIdp
                        .getFederatedAuthenticatorConfigs()).map(FederatedAuthenticatorConfig::getName)
                        .collect(Collectors.toList());
                for (FederatedAuthenticatorConfig federatedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    if (!savedIdpAuthenticators.contains(federatedAuth.getName())) {
                        validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                                federatedAuth.getName(), idp.getIdentityProviderName()));
                    }
                }
            } else {
                for (FederatedAuthenticatorConfig federatedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                            federatedAuth.getName(), idp.getIdentityProviderName()));
                }
            }
        } catch (IdentityProviderManagementException e) {
            String errorMsg = String.format(FEDERATED_IDP_NOT_AVAILABLE, idp.getIdentityProviderName());
            log.error(errorMsg, e);
            validationMsg.add(errorMsg);
        }
    }

    /**
     * Validate outbound provisioning related configurations and append to the validation msg list.
     *
     * @param validationMsg                        validation error messages
     * @param outboundProvisioningConfig Outbound provisioning config
     * @param tenantDomain               tenant domain
     */
    private void validateOutBoundProvisioning(List<String> validationMsg,
                                              OutboundProvisioningConfig outboundProvisioningConfig,
                                              String tenantDomain) {

        if (outboundProvisioningConfig == null
                || outboundProvisioningConfig.getProvisioningIdentityProviders() == null) {
            return;
        }

        for (IdentityProvider idp : outboundProvisioningConfig.getProvisioningIdentityProviders()) {
            try {
                IdentityProvider savedIdp = IdentityProviderManager.getInstance().getIdPByName(
                        idp.getIdentityProviderName(), tenantDomain, false);
                if (savedIdp == null) {
                    validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                            idp.getIdentityProviderName()));
                } else if (savedIdp.getDefaultProvisioningConnectorConfig() == null &&
                        savedIdp.getProvisioningConnectorConfigs() == null) {
                    validationMsg.add(String.format(PROVISIONING_CONNECTOR_NOT_CONFIGURED,
                            idp.getIdentityProviderName()));
                }
            } catch (IdentityProviderManagementException e) {
                validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                        idp.getIdentityProviderName()));
            }
        }
    }

    /**
     * Validate claim related configurations and append to the validation msg list.
     *
     * @param validationMsg                        validation error messages
     * @param claimConfig  claim config
     * @param subjectClaimUri Subject claim Uri
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private void validateClaimsConfigs(List<String> validationMsg, ClaimConfig claimConfig, String subjectClaimUri,
                                       String tenantDomain) throws IdentityApplicationManagementException {

        if (claimConfig == null) {
            return;
        }

        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        String[] allLocalClaimUris = applicationMgtService.getAllLocalClaimUris(tenantDomain);

        ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
        if (claimMappings != null) {
            for (ClaimMapping claimMapping : claimMappings) {
                String claimUri = claimMapping.getLocalClaim().getClaimUri();
                if (!Arrays.asList(allLocalClaimUris).contains(claimUri)) {
                    validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, claimUri, tenantDomain));
                }
            }
        }

        if (claimConfig.isLocalClaimDialect()) {
            String roleClaimUri = claimConfig.getRoleClaimURI();
            String userClaimUri = claimConfig.getUserClaimURI();
            if (StringUtils.isNotBlank(roleClaimUri) && !Arrays.asList(allLocalClaimUris).contains(roleClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, roleClaimUri, tenantDomain));
            }
            if (StringUtils.isNotBlank(userClaimUri) && !Arrays.asList(allLocalClaimUris).contains(userClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, userClaimUri, tenantDomain));
            }
            if (StringUtils.isNotBlank(subjectClaimUri) && !Arrays.asList(allLocalClaimUris).contains(subjectClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, subjectClaimUri, tenantDomain));
            }
        }

        String[] spClaimDialects = claimConfig.getSpClaimDialects();
        if (spClaimDialects != null) try {
            ClaimMetadataManagementServiceImpl claimAdminService = new ClaimMetadataManagementServiceImpl();
            List<ClaimDialect> serverClaimMapping = claimAdminService.getClaimDialects(tenantDomain);
            if (serverClaimMapping != null) {
                List<String> serverDialectURIS = serverClaimMapping.stream()
                        .map(ClaimDialect::getClaimDialectURI).collect(Collectors.toList());
                for (String spClaimDialect : spClaimDialects) {
                    if (!serverDialectURIS.contains(spClaimDialect)) {
                        validationMsg.add(String.format(CLAIM_DIALECT_NOT_AVAILABLE, spClaimDialect, tenantDomain));
                    }
                }
            }
        } catch (ClaimMetadataException e) {
            validationMsg.add(String.format("Error in getting claim dialect for %s. ", tenantDomain));
        }
    }

    /**
     * Validate local roles in role mapping configuration.
     *
     * @param validationMsg                        validation error messages
     * @param permissionsAndRoleConfig permission and role configurations
     * @param tenantDomain tenant domain
     */
    private void validateRoleConfigs(List<String> validationMsg, PermissionsAndRoleConfig permissionsAndRoleConfig,
                                     String tenantDomain) {

        if (permissionsAndRoleConfig == null || permissionsAndRoleConfig.getRoleMappings() == null) {
            return;
        }

        try {
            UserStoreManager userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager();
            for (RoleMapping roleMapping : permissionsAndRoleConfig.getRoleMappings()) {
                if (!userStoreManager.isExistingRole(roleMapping.getLocalRole().getLocalRoleName())) {
                    validationMsg.add(String.format(ROLE_NOT_AVAILABLE, roleMapping.getLocalRole().getLocalRoleName()));
                    break;
                }
            }
        } catch (UserStoreException e) {
            validationMsg.add(String.format("Error when checking the existence of local roles in %s.", tenantDomain));
        }
    }
}

