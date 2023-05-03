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

package org.wso2.carbon.identity.application.mgt.validator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.WORKFLOW_DOMAIN;
import static org.wso2.carbon.user.mgt.UserMgtConstants.APPLICATION_DOMAIN;

/**
 * Validator class to be used to validate the consistency of the Application/Service Provider, before it is persisted.
 */
public class DefaultApplicationValidator implements ApplicationValidator {

    private static Log log = LogFactory.getLog(DefaultApplicationValidator.class);

    private static final String AUTHENTICATOR_NOT_AVAILABLE = "Authenticator %s is not available in the server.";
    private static final String AUTHENTICATOR_NOT_CONFIGURED =
            "Authenticator %s is not configured for %s identity Provider.";
    private static final String PROVISIONING_CONNECTOR_NOT_CONFIGURED = "No Provisioning connector configured for %s.";
    private static final String FEDERATED_IDP_NOT_AVAILABLE =
            "Federated Identity Provider %s is not available in the server.";
    private static final String CLAIM_DIALECT_NOT_AVAILABLE = "Claim Dialect %s is not available in the server " +
            "for tenantDomain:%s.";
    private static final String CLAIM_NOT_AVAILABLE = "Local claim %s is not available in the server " +
            "for tenantDomain:%s.";
    private static final String SP_CLAIM_NOT_AVAILABLE = "Application Claim URI '%s' is not defined " +
            "for application:%s.";
    private static final String ROLE_NOT_AVAILABLE = "Local Role %s is not available in the server.";
    private static final String GROUPS_ARE_PROHIBITED_FOR_ROLE_MAPPING = "Groups including: %s, are " +
            "prohibited for role mapping. Use roles instead.";
    public static final String IS_HANDLER = "IS_HANDLER";
    private static Pattern loopPattern;
    private static final int MODE_DEFAULT = 1;
    private static final int MODE_ESCAPE = 2;
    private static final int MODE_STRING = 3;
    private static final int MODE_SINGLE_LINE = 4;
    private static final int MODE_MULTI_LINE = 5;

    public DefaultApplicationValidator() {

        loopPattern = Pattern.compile("\\b(for|while|forEach)\\b");
    }

    @Override
    public int getOrderId() {

        return 0;
    }

    @Override
    public List<String> validateApplication(ServiceProvider serviceProvider, String tenantDomain,
                                            String username) throws IdentityApplicationManagementException {

        List<String> validationErrors = new ArrayList<>();
        validateDiscoverabilityConfigs(validationErrors, serviceProvider);
        validateInboundAuthenticationConfig(serviceProvider.getInboundAuthenticationConfig(), tenantDomain,
                serviceProvider.getApplicationID());
        validateLocalAndOutBoundAuthenticationConfig(validationErrors,
                serviceProvider.getLocalAndOutBoundAuthenticationConfig(),
                tenantDomain);
        validateRequestPathAuthenticationConfig(validationErrors, serviceProvider.getRequestPathAuthenticatorConfigs(),
                tenantDomain);
        validateOutBoundProvisioning(validationErrors, serviceProvider.getOutboundProvisioningConfig(), tenantDomain);
        validateClaimsConfigs(validationErrors, serviceProvider.getClaimConfig(),
                serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null ? serviceProvider
                        .getLocalAndOutBoundAuthenticationConfig().getSubjectClaimUri() : null,
                tenantDomain, serviceProvider.getApplicationName());
        validateRoleConfigs(validationErrors, serviceProvider.getPermissionAndRoleConfig(), tenantDomain);
        if (isAuthenticationScriptAvailableConfig(serviceProvider)) {
            validateAdaptiveAuthScript(validationErrors,
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig());
        }

        return validationErrors;
    }

    private void validateDiscoverabilityConfigs(List<String> validationErrors,
                                                ServiceProvider serviceProvider) {

        String validationErrorFormat = "A valid %s needs to be defined if an application is marked as discoverable.";
        if (serviceProvider.isDiscoverable()) {
            if (StringUtils.isBlank(serviceProvider.getAccessUrl())) {
                validationErrors.add(String.format(validationErrorFormat, "accessURL"));
            }
        }
    }

    /**
     * @param inboundAuthenticationConfig Inbound authentication configuration.
     * @param tenantDomain                Tenant domain of application.
     * @param appId                       Application ID.
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException.
     */
    private void validateInboundAuthenticationConfig(InboundAuthenticationConfig inboundAuthenticationConfig, String
            tenantDomain, int appId) throws IdentityApplicationManagementException {

        if (inboundAuthenticationConfig == null) {
            return;
        }
        InboundAuthenticationRequestConfig[] inboundAuthRequestConfigs = inboundAuthenticationConfig
                .getInboundAuthenticationRequestConfigs();
        if (ArrayUtils.isNotEmpty(inboundAuthRequestConfigs)) {
            for (InboundAuthenticationRequestConfig inboundAuthRequestConfig : inboundAuthRequestConfigs) {
                validateInboundAuthKey(inboundAuthRequestConfig, appId, tenantDomain);
            }
        }
    }

    /**
     * Validate whether the configured inbound authentication key is already being used by another application.
     *
     * @param inboundConfig Inbound authentication request configuration.
     * @param appId         Application ID.
     * @param tenantDomain  Application tenant domain.
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException.
     */
    private void validateInboundAuthKey(InboundAuthenticationRequestConfig inboundConfig, int appId, String
            tenantDomain) throws IdentityApplicationManagementException {

        if (inboundConfig == null) {
            return;
        }

        /*
         * We need to directly retrieve the application from DB since {@link ServiceProviderByInboundAuthCache} cache
         * can have inconsistent applications stored against the <inbound-auth-key, inbound-auth-type, tenant-domain>
         * cache key which is not unique.
         */
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        String existingAppName = applicationDAO.getServiceProviderNameByClientId
                (inboundConfig.getInboundAuthKey(), inboundConfig.getInboundAuthType(), CarbonContext
                        .getThreadLocalCarbonContext().getTenantDomain());

        if (StringUtils.isBlank(existingAppName)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find application name for the inbound auth key: " + inboundConfig
                        .getInboundAuthKey() + " of inbound auth type: " + inboundConfig.getInboundAuthType());
            }
            return;
        }
        ServiceProvider existingApp = applicationDAO.getApplication(existingAppName, tenantDomain);
        if (existingApp != null && existingApp.getApplicationID() != appId) {
            String msg = "Inbound key: '" + inboundConfig.getInboundAuthKey() + "' of inbound auth type: '" +
                    inboundConfig.getInboundAuthType() + "' is already configured for the application :'" +
                    existingApp.getApplicationName() + "'";
            /*
             * Since this is a conflict scenario, we need to use a different error code. Hence throwing an
             * 'IdentityApplicationManagementClientException' here with the correct error code.
             */
            throw buildClientException(IdentityApplicationConstants.Error.INBOUND_KEY_ALREADY_EXISTS, msg);
        }
    }

    private IdentityApplicationManagementClientException buildClientException(IdentityApplicationConstants.Error
                                                                                      errorMessage, String message) {

        return new IdentityApplicationManagementClientException(errorMessage.getCode(), message);
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
              LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig,
              String tenantDomain)
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
                if (!allLocalAuthenticators.containsKey(localAuth.getName())) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_AVAILABLE, localAuth.getName()));
                } else if (!isAuthenticatorIncluded.get()) {
                    Property[] properties = allLocalAuthenticators.get(localAuth.getName());
                    if (properties.length == 0) {
                        isAuthenticatorIncluded.set(true);
                    } else {
                        for (Property property : properties) {
                            if (!(IS_HANDLER.equals(property.getName()) && Boolean.parseBoolean(property.getValue()))) {
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
     * @param validationMsg                   validation error messages
     * @param requestPathAuthenticatorConfigs request path authentication config
     * @param tenantDomain                    tenant domain
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
                if (!allRequestPathAuthenticators.containsKey(config.getName())) {
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
     * @param validationMsg              validation error messages
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
     * @param validationMsg   validation error messages
     * @param claimConfig     claim config
     * @param subjectClaimUri Subject claim Uri
     * @param tenantDomain    tenant domain
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private void validateClaimsConfigs(List<String> validationMsg, ClaimConfig claimConfig, String subjectClaimUri,
                                       String tenantDomain, String serviceProviderName)
            throws IdentityApplicationManagementException {

        if (claimConfig == null) {
            return;
        }

        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        String[] allLocalClaimUris = applicationMgtService.getAllLocalClaimUris(tenantDomain);
        ArrayList<String> remoteClaimUris = new ArrayList<>();

        ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
        if (claimMappings != null) {
            for (ClaimMapping claimMapping : claimMappings) {
                String claimUri = claimMapping.getLocalClaim().getClaimUri();
                remoteClaimUris.add(claimMapping.getRemoteClaim().getClaimUri());
                if (!Arrays.asList(allLocalClaimUris).contains(claimUri)) {
                    validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, claimUri, tenantDomain));
                }
            }
        }

        String roleClaimUri = claimConfig.getRoleClaimURI();
        String userClaimUri = claimConfig.getUserClaimURI();
        if (claimConfig.isLocalClaimDialect()) {
            if (StringUtils.isNotBlank(roleClaimUri) && !Arrays.asList(allLocalClaimUris).contains(roleClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, roleClaimUri, tenantDomain));
            }
            if (StringUtils.isNotBlank(userClaimUri) && !Arrays.asList(allLocalClaimUris).contains(userClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, userClaimUri, tenantDomain));
            }
            if (StringUtils.isNotBlank(subjectClaimUri) && !Arrays.asList(allLocalClaimUris).contains(
                    subjectClaimUri)) {
                validationMsg.add(String.format(CLAIM_NOT_AVAILABLE, subjectClaimUri, tenantDomain));
            }
        } else {
            if (StringUtils.isNotBlank(roleClaimUri) && !(remoteClaimUris).contains(roleClaimUri)) {
                validationMsg.add(String.format(SP_CLAIM_NOT_AVAILABLE, roleClaimUri, serviceProviderName));
            }
            if (StringUtils.isNotBlank(userClaimUri) && !(remoteClaimUris).contains(userClaimUri)) {
                validationMsg.add(String.format(SP_CLAIM_NOT_AVAILABLE, userClaimUri, serviceProviderName));
            }
            if (StringUtils.isNotBlank(subjectClaimUri) && !(remoteClaimUris).contains(
                    subjectClaimUri)) {
                validationMsg.add(String.format(SP_CLAIM_NOT_AVAILABLE, subjectClaimUri, serviceProviderName));
            }
        }

        String[] spClaimDialects = claimConfig.getSpClaimDialects();
        if (spClaimDialects != null) {
            try {
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
    }

    /**
     * Validate local roles in role mapping configuration.
     *
     * @param validationMsg            validation error messages
     * @param permissionsAndRoleConfig permission and role configurations
     * @param tenantDomain             tenant domain
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
                if (IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled()) {
                    if (isGroup(roleMapping.getLocalRole().getLocalRoleName())) {
                        validationMsg.add(String.format(GROUPS_ARE_PROHIBITED_FOR_ROLE_MAPPING, roleMapping
                                .getLocalRole().getLocalRoleName()));
                        break;
                    }
                }
                if (!userStoreManager.isExistingRole(roleMapping.getLocalRole().getLocalRoleName())) {
                    validationMsg.add(String.format(ROLE_NOT_AVAILABLE, roleMapping.getLocalRole().getLocalRoleName()));
                    break;
                }
            }
        } catch (UserStoreException e) {
            validationMsg.add(String.format("Error when checking the existence of local roles in %s.", tenantDomain));
        }
    }

    private boolean isGroup(String localRoleName) {

        return !Stream.of(INTERNAL_DOMAIN, APPLICATION_DOMAIN, WORKFLOW_DOMAIN).anyMatch(domain -> localRoleName
                .toUpperCase().startsWith((domain + UserCoreConstants.DOMAIN_SEPARATOR).toUpperCase()));
    }

    private void validateAdaptiveAuthScript(List<String> validationErrors,
                                            AuthenticationScriptConfig authenticationScriptConfig) {

        String script = getAdaptiveAuthScript(authenticationScriptConfig);
        if (StringUtils.isBlank(script)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided authentication script is empty.");
            }
            return;
        }

        if (!IdentityApplicationManagementUtil.isLoopsInAdaptiveAuthScriptAllowed()) {
            if (log.isDebugEnabled()) {
                log.debug("Loops are not allowed in the authentication script. " +
                        "Therefore checking whether loops are present in the provided script.");
            }
            if (IdentityApplicationManagementUtil.isLoopsPresentInAdaptiveAuthScript(script)) {
                validationErrors.add("Loops are not allowed in the adaptive authentication script, " +
                        "but loops are available in the provided script.");
            }
        }
    }

    private String getAdaptiveAuthScript(AuthenticationScriptConfig scriptConfig) {

        if (scriptConfig != null) {
            return scriptConfig.getContent();
        }

        return null;
    }

    private boolean isAuthenticationScriptAvailableConfig(ServiceProvider serviceProvider) {

        return serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null &&
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig() != null;
    }
}
