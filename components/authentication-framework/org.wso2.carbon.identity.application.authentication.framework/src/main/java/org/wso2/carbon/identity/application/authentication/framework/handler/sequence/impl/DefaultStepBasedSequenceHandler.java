/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationFlowHandler;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.MisconfigurationException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CONFIG_ALLOW_SP_REQUESTED_FED_CLAIMS_ONLY;

/**
 * Default implementation of step based sequence handler.
 */
public class DefaultStepBasedSequenceHandler implements StepBasedSequenceHandler {

    private static final Log log = LogFactory.getLog(DefaultStepBasedSequenceHandler.class);
    private static volatile DefaultStepBasedSequenceHandler instance;
    private static boolean allowSPRequestedFedClaimsOnly = true;

    public static DefaultStepBasedSequenceHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultStepBasedSequenceHandler.class) {
                if (instance == null) {
                    instance = new DefaultStepBasedSequenceHandler();
                }
            }
        }

        return instance;
    }

    static {
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(CONFIG_ALLOW_SP_REQUESTED_FED_CLAIMS_ONLY))) {
            allowSPRequestedFedClaimsOnly =
                    Boolean.parseBoolean(IdentityUtil.getProperty(CONFIG_ALLOW_SP_REQUESTED_FED_CLAIMS_ONLY));
        }
    }

    /**
     * Executes the steps
     *
     * @param request
     * @param response
     * @throws FrameworkException
     * @throws FrameworkException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        while (!context.getSequenceConfig().isCompleted()) {

            int currentStep = context.getCurrentStep();

            // let's initialize the step count to 1 if this the beginning of the sequence
            if (currentStep == 0) {
                currentStep++;
                context.setCurrentStep(currentStep);

                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    Map<String, Object> stepMap = new HashMap<>();
                    context.getSequenceConfig().getStepMap().forEach((key, value) -> {
                        List<Map<String, Object>> stepConfigParams = new ArrayList<>();
                        value.getAuthenticatorList().forEach(authenticatorConfig -> {
                            Map<String, Object> authenticatorParams = new HashMap<>();
                            authenticatorParams.put(FrameworkConstants.LogConstants.AUTHENTICATOR_NAME,
                                    authenticatorConfig.getName());
                            authenticatorParams.put(FrameworkConstants.LogConstants.IDP_NAMES,
                                    authenticatorConfig.getIdpNames());
                            stepConfigParams.add(authenticatorParams);
                        });
                        stepMap.put(FrameworkConstants.LogConstants.STEP + " " + key.toString(), stepConfigParams);
                    });
                    LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                            FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                            FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST)
                            .inputParam(LogConstants.InputKeys.AUTHENTICATOR_NAME, context.getServiceProviderName())
                            .inputParam(LogConstants.InputKeys.TENANT_DOMAIN, context.getTenantDomain())
                            .inputParam(FrameworkConstants.LogConstants.STEPS, stepMap)
                            .resultMessage("Executing step-based authentication.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
                }
            }

            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

            // if the current step is completed
            if (stepConfig != null && stepConfig.isCompleted()) {
                stepConfig.setCompleted(false);
                stepConfig.setRetrying(false);

                // if the request didn't fail during the step execution
                if (context.isRequestAuthenticated()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Step " + stepConfig.getOrder() + " is completed. Going to get the next one.");
                    }

                    currentStep = context.getCurrentStep() + 1;
                    context.setCurrentStep(currentStep);
                    stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Authentication has failed in the Step " + (context.getCurrentStep()));
                    }

                    // if the step contains multiple login options, we should give the user to retry
                    // authentication
                    if (stepConfig.isMultiOption() && !context.isPassiveAuthenticate()) {
                        stepConfig.setRetrying(true);
                        context.setRequestAuthenticated(true);
                    } else {
                        context.getSequenceConfig().setCompleted(true);
                        FrameworkUtils.resetAuthenticationContext(context);
                        continue;
                    }
                }

                FrameworkUtils.resetAuthenticationContext(context);
            }

            // if no further steps exists
            if (stepConfig == null) {

                if (log.isDebugEnabled()) {
                    log.debug("There are no more steps to execute.");
                }

                // if no step failed at authentication we should do post authentication work (e.g.
                // claim handling, provision etc)
                if (context.isRequestAuthenticated()) {

                    if (log.isDebugEnabled()) {
                        log.debug("Request is successfully authenticated.");
                    }

                    context.getSequenceConfig().setCompleted(true);
                    handlePostAuthentication(request, response, context);

                }

                // we should get out of steps now.
                if (log.isDebugEnabled()) {
                    log.debug("Step processing is completed.");
                }
                continue;
            }

            // if the sequence is not completed, we have work to do.
            if (log.isDebugEnabled()) {
                log.debug("Starting Step: " + stepConfig.getOrder());
            }

            FrameworkUtils.getStepHandler().handle(request, response, context);

            // if step is not completed, that means step wants to redirect to outside
            if (!stepConfig.isCompleted()) {
                if (log.isDebugEnabled()) {
                    log.debug("Step is not complete yet. Redirecting to outside.");
                }
                return;
            }

            context.setReturning(false);
        }

    }

    @SuppressWarnings("unchecked")
    protected void handlePostAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Handling Post Authentication tasks");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        StringBuilder jsonBuilder = new StringBuilder();

        boolean subjectFoundInStep = false;
        boolean subjectAttributesFoundInStep = false;
        int stepCount = 1;
        Map<String, String> mappedAttrs = new HashMap<>();
        Map<ClaimMapping, String> authenticatedUserAttributes = new HashMap<>();

        boolean isAuthenticatorExecuted = false;
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            if (authenticatorConfig == null) {
                //May have skipped from the script
                //ex: Different authentication sequences evaluated by the script
                continue;
            }
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (!(authenticator instanceof AuthenticationFlowHandler)) {
                isAuthenticatorExecuted = true;
            }

            // build the authenticated idps JWT to send to the calling servlet.
            if (stepCount == 1) {
                jsonBuilder.append("\"idps\":");
                jsonBuilder.append("[");
            }

            // build the JSON object for this step
            jsonBuilder.append("{");
            jsonBuilder.append("\"idp\":\"").append(stepConfig.getAuthenticatedIdP()).append("\",");
            jsonBuilder.append("\"authenticator\":\"").append(authenticator.getName()).append("\"");

            if (stepCount != sequenceConfig.getStepMap().size()) {
                jsonBuilder.append("},");
            } else {
                // wrap up the JSON object
                jsonBuilder.append("}");
                jsonBuilder.append("]");

                sequenceConfig.setAuthenticatedIdPs(IdentityApplicationManagementUtil.getSignedJWT(
                        jsonBuilder.toString(), sequenceConfig.getApplicationConfig()
                                .getServiceProvider()));

                stepConfig.setSubjectIdentifierStep(!subjectFoundInStep);

                stepConfig.setSubjectAttributeStep(!subjectAttributesFoundInStep);
            }

            stepCount++;

            if (authenticator instanceof FederatedApplicationAuthenticator) {

                ExternalIdPConfig externalIdPConfig = null;
                try {
                    externalIdPConfig = ConfigurationFacade.getInstance()
                            .getIdPConfigByName(stepConfig.getAuthenticatedIdP(),
                                    context.getTenantDomain());
                } catch (IdentityProviderManagementException e) {
                    log.error("Exception while getting IdP by name", e);
                }

                context.setExternalIdP(externalIdPConfig);

                String originalExternalIdpSubjectValueForThisStep =
                        stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier();

                if (externalIdPConfig == null) {
                    String errorMsg = "An External IdP cannot be null for a FederatedApplicationAuthenticator";
                    log.error(errorMsg);
                    throw new FrameworkException(errorMsg);
                }

                Map<ClaimMapping, String> extAttrs;
                Map<String, String> extAttibutesValueMap;
                Map<String, String> localClaimValues = null;
                Map<String, String> idpClaimValues = null;

                extAttrs = stepConfig.getAuthenticatedUser().getUserAttributes();
                extAttibutesValueMap = FrameworkUtils.getClaimMappings(extAttrs, false);

                if (stepConfig.isSubjectAttributeStep()) {

                    subjectAttributesFoundInStep = true;

                    if (mappedAttrs == null || mappedAttrs.isEmpty()) {
                        // do claim handling
                        mappedAttrs = handleClaimMappings(stepConfig, context,
                                extAttibutesValueMap, true);
                        // external claim values mapped to local claim uris.
                        localClaimValues = (Map<String, String>) context
                                .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);

                        idpClaimValues = (Map<String, String>) context
                                .getProperty(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES);
                    }
                }

                if (stepConfig.isSubjectIdentifierStep()) {
                    if (!stepConfig.isSubjectAttributeStep()) {
                        /*
                        Do claim mapping inorder to get subject claim uri requested. This is done only if the
                        step is not a subject attribute step. Because it is already done in the previous flow if
                        the step is a subject attribute step.
                        */
                        handleClaimMappings(stepConfig, context, extAttibutesValueMap, true);
                    }
                    subjectFoundInStep = true;
                    sequenceConfig.setAuthenticatedUser(new AuthenticatedUser(stepConfig.getAuthenticatedUser()));
                }
                if (stepConfig.isSubjectAttributeStep()) {

                    if (!sequenceConfig.getApplicationConfig().isMappedSubjectIDSelected()) {
                        // if we found the mapped subject - then we do not need to worry about
                        // finding attributes.

                        // if no requested claims are selected and sp claim dialect is not a standard dialect,
                        // send all local mapped claim values or idp claim values
                        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
                        if (MapUtils.isEmpty(appConfig.getRequestedClaimMappings()) &&
                                (!allowSPRequestedFedClaimsOnly ||
                                        !isSPStandardClaimDialect(context.getRequestType()))) {

                            if (MapUtils.isNotEmpty(localClaimValues)) {
                                mappedAttrs = localClaimValues;
                            } else if (MapUtils.isNotEmpty(idpClaimValues)) {
                                mappedAttrs = idpClaimValues;
                            }
                        }
                        authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                    }

                }

            } else {

                if (stepConfig.isSubjectIdentifierStep()) {
                    if (!stepConfig.isSubjectAttributeStep()) {
                        /*
                        Do claim mapping inorder to get subject claim uri requested. This is done only if the
                        step is not a subject attribute step. Because it is already done in the previous flow if
                        the step is a subject attribute step.
                        */
                        handleClaimMappings(stepConfig, context, null, false);
                    }
                    subjectFoundInStep = true;
                    sequenceConfig.setAuthenticatedUser(new AuthenticatedUser(stepConfig.getAuthenticatedUser()));

                    if (log.isDebugEnabled()) {
                        log.debug("Authenticated User: " + sequenceConfig.getAuthenticatedUser().getLoggableUserId());
                        log.debug("Authenticated User Tenant Domain: " + sequenceConfig.getAuthenticatedUser()
                                .getTenantDomain());
                    }
                }

                if (stepConfig.isSubjectAttributeStep()) {
                    subjectAttributesFoundInStep = true;
                    // local authentications
                    mappedAttrs = handleClaimMappings(stepConfig, context, null, false);
                    handleRoleMapping(context, sequenceConfig, mappedAttrs);
                    authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                }
            }
        }
        if (!isAuthenticatorExecuted) {
            String errorMsg = String.format("No authenticator have been executed in the authentication flow of " +
                    "application: %s in tenant-domain: %s", sequenceConfig.getApplicationConfig().getApplicationName
                    (), context.getTenantDomain());
            log.error(errorMsg);
            throw new MisconfigurationException(errorMsg);
        }

        if (sequenceConfig.getAuthenticatedUser() == null) {
            return;
        }
        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
        List<ClaimMapping> selectedRequestedClaims = FrameworkServiceDataHolder.getInstance()
                .getHighestPriorityClaimFilter().getFilteredClaims(context, appConfig);

        // Reset the user attributes returned from federate IdP if the requested claims are not empty.
        if (!selectedRequestedClaims.isEmpty()) {
            sequenceConfig.getAuthenticatedUser().setUserAttributes(Collections.unmodifiableMap(new HashMap<>()));
        }
        if (isSPStandardClaimDialect(context.getRequestType()) && authenticatedUserAttributes.isEmpty()) {
            sequenceConfig.getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);
        }
        if (!authenticatedUserAttributes.isEmpty()) {
            sequenceConfig.getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);
        }
    }

    /**
     * @param sequenceConfig
     * @param locallyMappedUserRoles String of user roles mapped according to Service Provider role mappings
     *                               seperated by the multi attribute separator
     * @return
     */
    protected String getServiceProviderMappedUserRoles(SequenceConfig sequenceConfig,
                                                       List<String> locallyMappedUserRoles) throws FrameworkException {
        return DefaultSequenceHandlerUtils.getServiceProviderMappedUserRoles(sequenceConfig, locallyMappedUserRoles);
    }

    private boolean isSPStandardClaimDialect(String clientType) {

        return (FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(clientType) ||
                FrameworkConstants.RequestType.CLAIM_TYPE_STS.equals(clientType) ||
                FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(clientType) ||
                FrameworkConstants.RequestType.CLAIM_TYPE_SCIM.equals(clientType));
    }

    private void handleRoleMapping(AuthenticationContext context, SequenceConfig sequenceConfig, Map<String, String>
            mappedAttrs) throws FrameworkException {

        String spRoleUri = getSpRoleClaimUri(sequenceConfig.getApplicationConfig());
        String[] roles = DefaultSequenceHandlerUtils.getRolesFromSPMappedClaims(context, sequenceConfig, mappedAttrs,
                spRoleUri);
        if (!ArrayUtils.isEmpty(roles)) {
            String standardRoleClaimUri = DefaultSequenceHandlerUtils.getStandardRoleClaimUri(context,
                    spRoleUri, sequenceConfig);
            String spMappedAttributes = getServiceProviderMappedUserRoles(sequenceConfig, Arrays.asList(roles));
            mappedAttrs.put(spRoleUri, spMappedAttributes);
            mappedAttrs.put(standardRoleClaimUri, spMappedAttributes);
        }
    }

    /**
     * @param appConfig
     * @return
     */
    protected String getSpRoleClaimUri(ApplicationConfig appConfig) throws FrameworkException {

        return DefaultSequenceHandlerUtils.getSpRoleClaimUri(appConfig);
    }

    /**
     * @param stepConfig
     * @param context
     * @param extAttrs
     * @param isFederatedClaims
     * @return
     */
    protected Map<String, String> handleClaimMappings(StepConfig stepConfig,
                                                      AuthenticationContext context,
                                                      Map<String, String> extAttrs,
                                                      boolean isFederatedClaims) throws FrameworkException {
        Map<String, String> mappedAttrs;
        try {
            mappedAttrs = FrameworkUtils.getClaimHandler().handleClaimMappings(stepConfig, context,
                    extAttrs, isFederatedClaims);
            return mappedAttrs;
        } catch (FrameworkException e) {
            log.error("Claim handling failed!", e);
        }
        // Claim handling failed. So we are returning an empty map.
        return Collections.emptyMap();
    }

    @Deprecated
    @Override
    public void callJitProvisioning(String subjectIdentifier, AuthenticationContext context,
                                    List<String> mappedRoles, Map<String, String> extAttributesValueMap)
            throws FrameworkException {

        handleJitProvisioning(subjectIdentifier, context, mappedRoles, extAttributesValueMap);
    }

    @Override
    public void callJitProvisioningWithV2Roles(String subjectIdentifier, AuthenticationContext context,
                                               List<String> assignedRoleIdList,
                                               Map<String, String> extAttributesValueMap) throws FrameworkException {

        handleJitProvisioningWithV2Roles(subjectIdentifier, context, assignedRoleIdList, extAttributesValueMap);
    }

    /**
     * Handle JIT provisioning with v1 roles.
     *
     * @param context               Authentication context.
     * @param mappedRoles           Mapped roles.
     * @param extAttributesValueMap Attribute value map.
     * @throws FrameworkException If an error occurred while handling JIT provisioning.
     */
    protected void handleJitProvisioning(String subjectIdentifier, AuthenticationContext context,
                                         List<String> mappedRoles, Map<String, String> extAttributesValueMap)
            throws FrameworkException {

        try {
            String userStoreDomain = getProvisioningUserstoreDomain(context.getExternalIdP(), extAttributesValueMap);

            // Setup thread local variable to be consumed by the provisioning framework.
            setThreadLocalProvisioningServiceProvider(context);
            setLocalUnfilteredClaimsForNullValues(context, extAttributesValueMap);
            setIdPRoleToLocalRoleMappingToThreadLocal(context);
            setAttributeSyncMethodToThreadLocal(context);

            FrameworkUtils.getProvisioningHandler()
                    .handle(mappedRoles, subjectIdentifier, extAttributesValueMap, userStoreDomain,
                            context.getTenantDomain());
        } catch (FrameworkException e) {
            log.error("User provisioning failed!", e);
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(FrameworkConstants.IDP_TO_LOCAL_ROLE_MAPPING);
            IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();
        }
    }

    /**
     * Handle JIT provisioning with v2 roles.
     *
     * @param context               Authentication context.
     * @param assignedRoleIdList    Assigned Role ID list.
     * @param extAttributesValueMap Attribute value map.
     * @throws FrameworkException If an error occurred while handling JIT provisioning.
     */
    protected void handleJitProvisioningWithV2Roles(String subjectIdentifier, AuthenticationContext context,
                                                    List<String> assignedRoleIdList,
                                                    Map<String, String> extAttributesValueMap)
            throws FrameworkException {

        try {
            String userStoreDomain = getProvisioningUserstoreDomain(context.getExternalIdP(), extAttributesValueMap);

            // Setup thread local variable to be consumed by the provisioning framework.
            setThreadLocalProvisioningServiceProvider(context);
            setLocalUnfilteredClaimsForNullValues(context, extAttributesValueMap);
            setAttributeSyncMethodToThreadLocal(context);

            FrameworkUtils.getProvisioningHandler()
                    .handleWithV2Roles(assignedRoleIdList, subjectIdentifier, extAttributesValueMap, userStoreDomain,
                            context.getTenantDomain());
        } catch (FrameworkException e) {
            log.error("User provisioning failed!", e);
        } finally {
            IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();
        }
    }

    /**
     * Returns the local claim uri that is mapped for the IdP role claim uri configured.
     * If no role claim uri is configured for the IdP returns the local role claim 'http://wso2.org/claims/role'.
     *
     * @param externalIdPConfig IdP configurations
     * @return local claim uri mapped for the IdP role claim uri.
     * @throws FrameworkException
     */
    protected String getLocalClaimUriMappedForIdPRoleClaim(ExternalIdPConfig externalIdPConfig) throws
            FrameworkException {
        return FrameworkUtils.getLocalClaimUriMappedForIdPRoleClaim(externalIdPConfig);
    }

    /**
     * Returns the provisioning userstore domain.
     *
     * @param externalIdPConfig External IdP config.
     * @param extAttributesValueMap Attributes value map.
     * @return Provisioning userstore domain.
     */
    private String getProvisioningUserstoreDomain(ExternalIdPConfig externalIdPConfig,
                                                  Map<String, String> extAttributesValueMap) {

        String provisioningUserStoreId = externalIdPConfig.getProvisioningUserStoreId();
        String provisioningClaimUri = externalIdPConfig.getProvisioningUserStoreClaimURI();
        String userStoreDomain = null;
        if (provisioningUserStoreId != null) {
            userStoreDomain = provisioningUserStoreId;
        } else if (provisioningClaimUri != null) {
            userStoreDomain = extAttributesValueMap.get(provisioningClaimUri);
        }
        return userStoreDomain;
    }

    /**
     * Set the provisioning service provider to the thread local.
     *
     * @param context Authentication context.
     */
    private void setThreadLocalProvisioningServiceProvider(AuthenticationContext context) {

        String serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();
        ThreadLocalProvisioningServiceProvider serviceProvider = new ThreadLocalProvisioningServiceProvider();
        serviceProvider.setServiceProviderName(serviceProviderName);
        serviceProvider.setJustInTimeProvisioning(true);
        serviceProvider.setClaimDialect(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
        serviceProvider.setTenantDomain(context.getTenantDomain());
        IdentityApplicationManagementUtil.setThreadLocalProvisioningServiceProvider(serviceProvider);
    }

    /**
     * Set the local unfiltered claims for null values.
     *
     * @param context Authentication context.
     * @param extAttributesValueMap Attribute value map.
     */
    private void setLocalUnfilteredClaimsForNullValues(AuthenticationContext context,
                                                       Map<String, String> extAttributesValueMap) {

        Map<String, String> localUnfilteredClaimsForNullValues =
                (Map<String, String>) context
                        .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIMS_FOR_NULL_VALUES);
        if (MapUtils.isNotEmpty(localUnfilteredClaimsForNullValues)) {
            extAttributesValueMap.putAll(localUnfilteredClaimsForNullValues);
        }
    }

    /**
     * Set the IdP to local role mapping to the thread local.
     *
     * @param context Authentication context.
     */
    private void setIdPRoleToLocalRoleMappingToThreadLocal(AuthenticationContext context) {

        List<String> idpToLocalRoleMapping = new ArrayList<String>(
                context.getExternalIdP().getRoleMappings().values());
        IdentityUtil.threadLocalProperties.get().put(FrameworkConstants.IDP_TO_LOCAL_ROLE_MAPPING,
                idpToLocalRoleMapping);
    }

    /**
     * Set attribute sync method to thread local.
     *
     * @param context Authentication context.
     */
    private void setAttributeSyncMethodToThreadLocal(AuthenticationContext context) {

        IdentityUtil.threadLocalProperties.get().put(FrameworkConstants.ATTRIBUTE_SYNC_METHOD,
                context.getExternalIdP().getAttributeSyncMethod());
    }
}
