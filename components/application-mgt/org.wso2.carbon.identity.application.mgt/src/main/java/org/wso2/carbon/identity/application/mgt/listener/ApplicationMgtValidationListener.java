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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ApplicationMgtValidationListener extends AbstractApplicationMgtListener {

    private static Log log = LogFactory.getLog(ApplicationMgtValidationListener.class);

    private static final String AUTHENTICATOR_NOT_AVAILABLE = "Authenticator %s is not available in the server";
    private static final String AUTHENTICATOR_NOT_CONFIGURED =
            "Authenticator %s is not configured for %s identity Provider";
    private static final String PROVISIONING_CONNECTOR_NOT_CONFIGURED = "No Provisioning connector configured for %s";
    private static final String FEDERATED_IDP_NOT_AVAILABLE =
            "Federated Identity Provider %s is not available in the server";
    private static final String CLAIM_DIALECT_NOT_AVAILABLE = "Claim Dialect %s is not available for tenant %s";
    private static final String CLAIM_NOT_AVAILABLE = "Local claim %s is not available for tenant %s";
    public static final String IS_HANDLER = "IS_HANDLER";

    @Override
    public int getDefaultOrderId() {

        return 10;
    }

    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        if (StringUtils.isBlank(serviceProvider.getApplicationName())) {
            // check for required attributes.
            throw new IdentityApplicationManagementException("Application Name is required");
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider savedSP = appDAO.getApplication(serviceProvider.getApplicationName(), tenantDomain);
        if (savedSP != null) {
            throw new IdentityApplicationManagementException("Already an application available with the same name.");
        }
        return true;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain,
                                          String userName) throws IdentityApplicationManagementException {

        validateLocalAndOutBoundAuthenticationConfig(serviceProvider.getLocalAndOutBoundAuthenticationConfig(),
                tenantDomain);
        validateOutBoundProvisioning(serviceProvider.getOutboundProvisioningConfig(), tenantDomain);
        validateClaimsConfigs(serviceProvider.getClaimConfig(), tenantDomain);
        return true;
    }

    /**
     * Validate local and outbound authenticator related configurations and append to the validation msg list.
     *
     * @param localAndOutBoundAuthenticationConfig local and out bound authentication config
     * @param tenantDomain                         tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception when unable to get the
     * authenticator params
     */
    private void validateLocalAndOutBoundAuthenticationConfig(
            LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig, String tenantDomain)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();

        if (localAndOutBoundAuthenticationConfig == null) {
            return;
        }

        AuthenticationStep[] authenticationSteps = localAndOutBoundAuthenticationConfig.getAuthenticationSteps();
        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return;
        }
        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        Map<String, Property[]> allLocalAuthenticators = Arrays.stream( applicationMgtService
                .getAllLocalAuthenticators(tenantDomain))
                .collect(Collectors.toMap(LocalAuthenticatorConfig::getName, LocalAuthenticatorConfig::getProperties));

        AtomicBoolean isAuthenticatorIncluded = new AtomicBoolean(false);

        Arrays.stream(authenticationSteps).forEach(authenticationStep -> {
            Arrays.stream(authenticationStep.getFederatedIdentityProviders()).forEach(idp -> {
                try {
                    IdentityProvider savedIdp = IdentityProviderManager.getInstance().getIdPByName(idp
                            .getIdentityProviderName(), tenantDomain, false);
                    if (savedIdp.getId() == null) {
                        validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                                idp.getIdentityProviderName()));
                    } else if (savedIdp.getFederatedAuthenticatorConfigs() != null) {
                        List<String> savedIdpAuthenticators = Arrays.stream(savedIdp
                                .getFederatedAuthenticatorConfigs()).map(FederatedAuthenticatorConfig::getName)
                                .collect(Collectors.toList());
                        Arrays.stream(idp.getFederatedAuthenticatorConfigs()).forEach(federatedAuth -> {
                            if (!savedIdpAuthenticators.contains(federatedAuth.getName())) {
                                validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                                        federatedAuth.getName(), idp.getIdentityProviderName()));
                            } else {
                                isAuthenticatorIncluded.set(true);
                            }
                        });
                    } else {
                        Arrays.stream(idp.getFederatedAuthenticatorConfigs()).forEach(federatedAuth ->
                                validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                                        federatedAuth.getName(), idp.getIdentityProviderName())));
                    }
                } catch (IdentityProviderManagementException e) {
                    String errorMsg = String.format(FEDERATED_IDP_NOT_AVAILABLE, idp.getIdentityProviderName());
                    log.error(errorMsg, e);
                    validationMsg.add(errorMsg);
                }
            });
            Arrays.stream(authenticationStep.getLocalAuthenticatorConfigs()).forEach(localAuth -> {
                if (!allLocalAuthenticators.keySet().contains(localAuth.getName())) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_AVAILABLE, localAuth.getName()));
                } else if (!isAuthenticatorIncluded.get()){
                    Property[] properties = allLocalAuthenticators.get(localAuth.getName());
                    if (properties.length == 0) {
                        isAuthenticatorIncluded.set(true);
                    } else {
                        Arrays.stream(properties).forEach(property -> {
                            if (!(IS_HANDLER.equals(property.getName()) && Boolean.valueOf(property.getValue()))) {
                                isAuthenticatorIncluded.set(true);
                            }
                        });
                    }
                }
            });
        });
        if (!isAuthenticatorIncluded.get()) {
            validationMsg.add("No authenticator have been registered in the authentication flow.");
        }
        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    /**
     * Validate outbound provisioning related configurations and append to the validation msg list.
     *
     * @param outboundProvisioningConfig Outbound provisioning config
     * @param tenantDomain               tenant domain
     */
    private void validateOutBoundProvisioning(OutboundProvisioningConfig outboundProvisioningConfig, String
            tenantDomain) throws IdentityApplicationManagementValidationException {

        List<String> validationMsg = new ArrayList<>();

        if (outboundProvisioningConfig == null
                || outboundProvisioningConfig.getProvisioningIdentityProviders() == null) {
            return;
        }

        Arrays.stream(outboundProvisioningConfig.getProvisioningIdentityProviders()).forEach(idp -> {
            try {
                IdentityProvider savedIdp = IdentityProviderManager.getInstance().getIdPByName(
                        idp.getIdentityProviderName(), tenantDomain, false);
                if (savedIdp == null) {
                    validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                            idp.getIdentityProviderName()));
                } else if (savedIdp.getDefaultProvisioningConnectorConfig() == null ||
                        savedIdp.getProvisioningConnectorConfigs().length == 0) {
                    validationMsg.add(String.format(PROVISIONING_CONNECTOR_NOT_CONFIGURED,
                            idp.getIdentityProviderName()));
                }
            } catch (IdentityProviderManagementException e) {
                validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                        idp.getIdentityProviderName()));
            }
        });
        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    /**
     * Validate claim related configurations and append to the validation msg list.
     *
     * @param claimConfig   claim config
     * @param tenantDomain  tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private void validateClaimsConfigs(ClaimConfig claimConfig, String tenantDomain) throws
            IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();

        if (claimConfig == null) {
            return;
        }

        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        String[] allLocalClaimUris = applicationMgtService.getAllLocalClaimUris(tenantDomain);

        ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
        if (claimMappings != null) {
            Arrays.stream(claimMappings).forEach(claimMapping -> {
                String claimUri = claimMapping.getLocalClaim().getClaimUri();
                if (!Arrays.asList(allLocalClaimUris).contains(claimUri)) {
                    validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, claimUri, tenantDomain));
                }
            });
        }

        String[] spClaimDialects = claimConfig.getSpClaimDialects();
        if (spClaimDialects != null) try {
            ClaimMetadataManagementServiceImpl claimAdminService = new ClaimMetadataManagementServiceImpl();
            List<ClaimDialect> serverClaimMapping = claimAdminService.getClaimDialects(tenantDomain);
            if (serverClaimMapping != null) {
                List<String> serverDialectURIS = serverClaimMapping.stream()
                        .map(ClaimDialect::getClaimDialectURI).collect(Collectors.toList());
                Arrays.stream(spClaimDialects).forEach(spClaimDialect -> {
                    if (!serverDialectURIS.contains(spClaimDialect)) {
                        validationMsg.add(String.format(CLAIM_DIALECT_NOT_AVAILABLE, spClaimDialect, tenantDomain));
                    }
                });
            }
        } catch (ClaimMetadataException e) {
            validationMsg.add(String.format("Error in getting claim dialect for %s. ", tenantDomain));
        }
        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }
}
