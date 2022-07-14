/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.handler.event.account.lock.constants.AccountConstants;
import org.wso2.carbon.identity.handler.event.account.lock.exception.AccountLockServiceException;
import org.wso2.carbon.identity.handler.event.account.lock.service.AccountLockService;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_IDP_BY_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_TRYING_TO_PROVISION_USER_WITHOUT_PASSWORD_PROVISIONING;

/**
 * This is post authentication handler responsible for JIT provisioning.
 */
public class JITProvisioningPostAuthenticationHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(JITProvisioningPostAuthenticationHandler.class);
    private static volatile JITProvisioningPostAuthenticationHandler instance
            = new JITProvisioningPostAuthenticationHandler();

    /**
     * To get an instance of {@link JITProvisioningPostAuthenticationHandler}.
     *
     * @return an instance of PostJITProvisioningHandler.
     */
    public static JITProvisioningPostAuthenticationHandler getInstance() {

        return instance;
    }

    /**
     * To avoid creation of multiple instances of this handler.
     */
    protected JITProvisioningPostAuthenticationHandler() { }

    @Override
    public int getPriority() {

        int priority = super.getPriority();
        if (priority == -1) {
            priority = 20;
        }
        return priority;
    }

    @Override
    public String getName() {

        return "JITProvisionHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws PostAuthenticationFailedException {

        if (!FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)) {
            return SUCCESS_COMPLETED;
        }

        if (log.isDebugEnabled()) {
            AuthenticatedUser authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();
            log.debug("Continuing with JIT flow for the user: " + authenticatedUser);
        }
        Object isProvisionUIRedirectionTriggered = context
                .getProperty(FrameworkConstants.PASSWORD_PROVISION_REDIRECTION_TRIGGERED);
        if (isProvisionUIRedirectionTriggered != null && (boolean) isProvisionUIRedirectionTriggered) {
            if (log.isDebugEnabled()) {
                AuthenticatedUser authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();
                log.debug("The request has hit the response flow of JIT provisioning flow for the user: "
                        + authenticatedUser.getLoggableUserId());
            }
            return handleResponseFlow(request, context);
        } else {
            return handleRequestFlow(request, response, context);
        }
    }

    /**
     * This method is used to handle response flow, after going through password provisioning.
     *
     * @param request        HttpServlet request.
     * @param context        Authentication context
     * @return Status of PostAuthnHandler flow.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception
     */
    @SuppressWarnings("unchecked")
    private PostAuthnHandlerFlowStatus handleResponseFlow(HttpServletRequest request, AuthenticationContext context)
            throws PostAuthenticationFailedException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                String externalIdPConfigName = stepConfig.getAuthenticatedIdP();
                ExternalIdPConfig externalIdPConfig = getExternalIdpConfig(externalIdPConfigName, context);
                context.setExternalIdP(externalIdPConfig);

                if (externalIdPConfig != null && externalIdPConfig.isProvisioningEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug("JIT provisioning response flow has hit for the IDP " + externalIdPConfigName + " "
                                + "for the user, " + sequenceConfig.getAuthenticatedUser().getLoggableUserId());
                    }
                    final Map<String, String> localClaimValues;
                    Object unfilteredLocalClaimValues = context
                            .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
                    localClaimValues = unfilteredLocalClaimValues == null ?
                            new HashMap<>() :
                            (Map<String, String>) unfilteredLocalClaimValues;
                    Map<String, String> combinedLocalClaims = getCombinedClaims(request, localClaimValues, context);
                    if (externalIdPConfig.isPasswordProvisioningEnabled()) {
                        combinedLocalClaims
                                .put(FrameworkConstants.PASSWORD, request.getParameter(FrameworkConstants.PASSWORD));
                    }
                    String username = getUsernameFederatedUser(stepConfig, sequenceConfig,
                            externalIdPConfigName, context, localClaimValues, externalIdPConfig);
                    if (context.getProperty(FrameworkConstants.CHANGING_USERNAME_ALLOWED) != null) {
                        username = request.getParameter(FrameworkConstants.USERNAME);
                        try {
                            /*
                            Checks whether the provided user is already existing in the system. If so an exception
                            will be thrown.
                            */
                            UserRealm realm = getUserRealm(context.getTenantDomain());
                            UserStoreManager userStoreManager = getUserStoreManager(context.getExternalIdP()
                                    .getProvisioningUserStoreId(), realm, username);
                            String sanitizedUserName = UserCoreUtil.removeDomainFromName(
                                    MultitenantUtils.getTenantAwareUsername(username));
                            if (userStoreManager.isExistingUser(sanitizedUserName)) {
                                // Logging the error because the thrown exception is handled in the UI.
                                log.error(ErrorMessages.USER_ALREADY_EXISTS_ERROR.getCode() + " - "
                                        + ErrorMessages.USER_ALREADY_EXISTS_ERROR.getMessage());
                                handleExceptions(ErrorMessages.USER_ALREADY_EXISTS_ERROR.getMessage(),
                                        "provided.username.already.exists", null);
                            }
                        } catch (UserStoreException e) {
                            handleExceptions(ErrorMessages.ERROR_WHILE_CHECKING_USERNAME_EXISTENCE.getMessage(),
                                    "error.user.existence", e);
                        }
                    }
                    callDefaultProvisioningHandler(username, context, externalIdPConfig, combinedLocalClaims,
                            stepConfig);
                   handleConsents(request, stepConfig, context.getTenantDomain());
                }
            }
        }
        return SUCCESS_COMPLETED;
    }

    /**
     * To get the final claims that need to be stored against user by combining the claims from IDP as well as from
     * User entered claims.
     *
     * @param request          Http servlet request.
     * @param localClaimValues Relevant local claim values from IDP.
     * @param context          AuthenticationContext.
     * @return combination of claims came from IDP and the claims user has filed.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private Map<String, String> getCombinedClaims(HttpServletRequest request, Map<String, String> localClaimValues,
            AuthenticationContext context) throws PostAuthenticationFailedException {

        String externalIdPConfigName = context.getExternalIdP().getIdPName();
        org.wso2.carbon.user.api.ClaimMapping[] claims = getClaimsForTenant(context.getTenantDomain(),
                externalIdPConfigName);
        Map<String, String> missingClaims = new HashMap<>();
        if (claims != null) {
            for (org.wso2.carbon.user.api.ClaimMapping claimMapping : claims) {
                String uri = claimMapping.getClaim().getClaimUri();
                String claimValue = request.getParameter(uri);

                if (StringUtils.isNotBlank(claimValue) && StringUtils.isEmpty(localClaimValues.get(uri))) {
                    localClaimValues.put(uri, claimValue);
                } else {
                    /* Claims that are mandatory from service provider level will pre-appended with "missing-" in
                     there name.
                     */
                    claimValue = request.getParameter("missing-" + uri);
                    if (StringUtils.isNotEmpty(claimValue)) {
                        localClaimValues.put(uri, claimValue);
                        missingClaims.put(uri, claimValue);
                    }
                }
            }
        }
        // Handle the missing claims.
        if (MapUtils.isNotEmpty(missingClaims)) {
            AuthenticatedUser authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();
            Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();
            userAttributes.putAll(FrameworkUtils.buildClaimMappings(missingClaims));
            authenticatedUser.setUserAttributes(userAttributes);
            context.getSequenceConfig().setAuthenticatedUser(authenticatedUser);
        }
        return localClaimValues;
    }

    /**
     * To handle the request flow of the post authentication handler.
     *
     * @param response       HttpServlet response.
     * @param context        Authentication context
     * @return Status of this post authentication handler flow.
     * @throws PostAuthenticationFailedException Exception that will be thrown in case of failure.
     */
    @SuppressWarnings("unchecked")
    private PostAuthnHandlerFlowStatus handleRequestFlow(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws PostAuthenticationFailedException {

        String retryURL = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            if (authenticatorConfig == null) {
                //May have skipped from the script
                //ex: Different authentication sequences evaluated by the script
                continue;
            }
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                String externalIdPConfigName = stepConfig.getAuthenticatedIdP();
                ExternalIdPConfig externalIdPConfig = getExternalIdpConfig(externalIdPConfigName, context);
                context.setExternalIdP(externalIdPConfig);
                Map<String, String> localClaimValues;
                if (stepConfig.isSubjectAttributeStep()) {
                    localClaimValues = (Map<String, String>) context
                            .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
                } else {
                    localClaimValues = getLocalClaimValuesOfIDPInNonAttributeSelectionStep(context, stepConfig,
                            externalIdPConfig);
                }
                if (localClaimValues == null || localClaimValues.size() == 0) {
                    Map<ClaimMapping, String> userAttributes = stepConfig.getAuthenticatedUser().getUserAttributes();
                    localClaimValues = FrameworkUtils.getClaimMappings(userAttributes, false);
                }

                if (externalIdPConfig != null && externalIdPConfig.isProvisioningEnabled()) {
                    if (localClaimValues == null) {
                        localClaimValues = new HashMap<>();
                    }

                    String associatedLocalUser =
                            getLocalUserAssociatedForFederatedIdentifier(stepConfig.getAuthenticatedIdP(),
                                    stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier(),
                                    context.getTenantDomain());

                    String username = associatedLocalUser;
                    // If associatedLocalUser is null, that means relevant association not exist already.
                    if (StringUtils.isEmpty(associatedLocalUser)) {
                        if (log.isDebugEnabled()) {
                            log.debug(sequenceConfig.getAuthenticatedUser().getLoggableUserId() + " coming from "
                                    + externalIdPConfig.getIdPName() + " do not have a local account, hence redirecting"
                                    + " to the UI to sign up.");
                        }

                        if (externalIdPConfig.isPromptConsentEnabled()) {
                            username = getUsernameFederatedUser(stepConfig, sequenceConfig,
                                    externalIdPConfigName, context, localClaimValues, externalIdPConfig);
                            redirectToAccountCreateUI(externalIdPConfig, context, localClaimValues, response,
                                    username, request);
                            // Set the property to make sure the request is a returning one.
                            context.setProperty(FrameworkConstants.PASSWORD_PROVISION_REDIRECTION_TRIGGERED, true);
                            return PostAuthnHandlerFlowStatus.INCOMPLETE;
                        }
                    }
                    if (StringUtils.isEmpty(username)) {
                        username = getUsernameFederatedUser(stepConfig, sequenceConfig, externalIdPConfigName,
                                context, localClaimValues, externalIdPConfig);
                    }
                    if (StringUtils.isNotBlank(associatedLocalUser)) {
                        // Check if the associated local account is locked.
                        if (isAccountLocked(username, context.getTenantDomain())) {
                            if (log.isDebugEnabled()) {
                                log.debug(String.format("The account is locked for the user: %s in the " +
                                        "tenant domain: %s ", username, context.getTenantDomain()));
                            }
                            String retryParam =
                                    "&authFailure=true&authFailureMsg=error.user.account.locked&errorCode=" +
                                            UserCoreConstants.ErrorCode.USER_IS_LOCKED;
                            handleAccountLockLoginFailure(retryURL, context, response, retryParam);
                            return PostAuthnHandlerFlowStatus.INCOMPLETE;
                        }
                        // Check if the associated local account is disabled.
                        if (isAccountDisabled(associatedLocalUser, context.getTenantDomain())) {
                            if (log.isDebugEnabled()) {
                                log.debug(String.format("The account is disabled for the user: %s in the " +
                                        "tenant domain: %s ", username, context.getTenantDomain()));
                            }
                            String retryParam =
                                    "&authFailure=true&authFailureMsg=error.user.account.disabled&errorCode=" +
                                            IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE;
                            handleAccountLockLoginFailure(retryURL, context, response, retryParam);
                            return PostAuthnHandlerFlowStatus.INCOMPLETE;
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("User : " + sequenceConfig.getAuthenticatedUser().getLoggableUserId()
                                + " coming from " + externalIdPConfig.getIdPName()
                                + " do have a local account, with the username " + username);
                    }
                    callDefaultProvisioningHandler(username, context, externalIdPConfig, localClaimValues,
                            stepConfig);
                }
            }
        }
        return SUCCESS_COMPLETED;
    }

    private String getUsernameFederatedUser(StepConfig stepConfig, SequenceConfig sequenceConfig,
                                            String externalIdPConfigName, AuthenticationContext context,
                                            Map<String, String> localClaimValues, ExternalIdPConfig externalIdPConfig)
            throws PostAuthenticationFailedException {

        String username;
        String userIdClaimUriInLocalDialect = getUserIdClaimUriInLocalDialect(externalIdPConfig);
        if (isUserNameFoundFromUserIDClaimURI(localClaimValues, userIdClaimUriInLocalDialect)) {
            username = localClaimValues.get(userIdClaimUriInLocalDialect);
        } else {
            if (FrameworkUtils.isJITProvisionEnhancedFeatureEnabled()) {
                username = getFederatedUsername(stepConfig.getAuthenticatedUser().getUserName(),
                        externalIdPConfigName, context);
            } else {
                username = sequenceConfig.getAuthenticatedUser().getUserName();
            }
        }
        return username;
    }

    private boolean isUserNameFoundFromUserIDClaimURI(Map<String, String> localClaimValues, String
            userIdClaimUriInLocalDialect) {

        return StringUtils.isNotBlank(userIdClaimUriInLocalDialect) && StringUtils.isNotBlank
                (localClaimValues.get(userIdClaimUriInLocalDialect));
    }

    private String getFederatedUsername(String username, String idpName, AuthenticationContext context)
            throws PostAuthenticationFailedException {

        String federatedUsername = null;
        try {
            int tenantId = IdentityTenantUtil.getTenantId(context.getTenantDomain());
            int idpId = UserSessionStore.getInstance().getIdPId(idpName, tenantId);
            federatedUsername = UserSessionStore.getInstance().getFederatedUserId(username, tenantId, idpId);
        } catch (UserSessionException e) {
            handleExceptions(
                    String.format(ErrorMessages.ERROR_WHILE_GETTING_FEDERATED_USERNAME.getMessage(), username, idpName),
                    ErrorMessages.ERROR_WHILE_GETTING_FEDERATED_USERNAME.getCode(), e);
        }
        return federatedUsername;
    }

    private boolean isAccountLocked(String username, String tenantDomain)
            throws PostAuthenticationFailedException {

        AccountLockService accountLockService = FrameworkServiceDataHolder.getInstance().getAccountLockService();
        try {
            return accountLockService.isAccountLocked(username, tenantDomain);
        } catch (AccountLockServiceException e) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_CHECKING_ACCOUNT_LOCK_STATUS.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_CHECKING_ACCOUNT_LOCK_STATUS.getMessage(), username), e);
        }
    }

    /**
     * Uses to check whether associated users account is disabled or not.
     *
     * @param username Username of the associated user.
     * @return Whether user is disabled or not.
     * @throws PostAuthenticationFailedException When getting claim value.
     */
    private boolean isAccountDisabled(String username, String tenantDomain) throws PostAuthenticationFailedException {

        try {
            UserRealm realm = (UserRealm) FrameworkServiceDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
            UserStoreManager userStoreManager = realm.getUserStoreManager();
            Map<String, String> claimValues = userStoreManager.getUserClaimValues(username, new String[]{
                    AccountConstants.ACCOUNT_DISABLED_CLAIM}, UserCoreConstants.DEFAULT_PROFILE);
            if (claimValues != null && claimValues.size() > 0) {
                String accountDisabledClaim = claimValues.get(AccountConstants.ACCOUNT_DISABLED_CLAIM);
                return Boolean.parseBoolean(accountDisabledClaim);
            }
        } catch (UserStoreException e) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_CHECKING_ACCOUNT_DISABLE_STATUS.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_CHECKING_ACCOUNT_DISABLE_STATUS.getMessage(), username), e);
        }
        return false;
    }

    private void handleAccountLockLoginFailure(String retryPage, AuthenticationContext context,
                                               HttpServletResponse response, String retryParam)
            throws PostAuthenticationFailedException {

        try {
            // ToDo: Add support to configure enable/disable authentication failure reason.
            boolean showAuthFailureReason = true;
            retryPage = FrameworkUtils.appendQueryParamsStringToUrl(retryPage,
                    "sp=" + context.getServiceProviderName());
            retryPage = FrameworkUtils.appendQueryParamsStringToUrl(retryPage,
                    String.format("%s=", FrameworkConstants.REQUEST_PARAM_AUTH_FLOW_ID)
                            + context.getContextIdentifier());
            if (!showAuthFailureReason) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }
            retryPage = FrameworkUtils.appendQueryParamsStringToUrl(retryPage, retryParam);
            context.setRetrying(false);
            response.sendRedirect(retryPage);
        } catch (IOException e) {
            handleExceptions(ErrorMessages.ERROR_WHILE_HANDLING_ACCOUNT_LOCK_FAILURE_FED_USERS.getMessage(),
                    ErrorMessages.ERROR_WHILE_HANDLING_ACCOUNT_LOCK_FAILURE_FED_USERS.getCode(), e);
        }
    }

    /**
     * Builds consent receipt input according to consent API.
     *
     * @param piiPrincipalId P11 Principal ID
     * @param consent        Consent String which contains services.
     * @param policyURL      Policy URL.
     * @return Consent string which contains above facts.
     */
    private ReceiptInput buildConsentForResidentIDP(String piiPrincipalId, String consent, String policyURL) {

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setJurisdiction("USA");
        receiptInput.setCollectionMethod(FrameworkConstants.Consent.COLLECTION_METHOD_JIT);
        receiptInput.setLanguage(FrameworkConstants.Consent.LANGUAGE_ENGLISH);
        receiptInput.setPiiPrincipalId(piiPrincipalId);
        receiptInput.setPolicyUrl(policyURL);
        JSONObject receipt = new JSONObject(consent);
        receiptInput.setServices(getReceiptServiceInputs(receipt));
        if (log.isDebugEnabled()) {
            log.debug("Built consent from endpoint util : " + consent);
        }
        return receiptInput;
    }

    /**
     * To build ReceiptServices from the incoming receipt.
     *
     * @param receipt Relevant incoming receipt send from the client side.
     * @return Set of the receipt services.
     */
    private List<ReceiptServiceInput> getReceiptServiceInputs(JSONObject receipt) {

        JSONArray services = receipt.getJSONArray(FrameworkConstants.Consent.SERVICES);
        List<ReceiptServiceInput> receiptServiceInputs = new ArrayList<>();
        for (int serviceIndex = 0; serviceIndex < services.length(); serviceIndex++) {
            JSONObject service = services.getJSONObject(serviceIndex);
            ReceiptServiceInput receiptServiceInput = new ReceiptServiceInput();

            JSONArray purposes = service.getJSONArray(FrameworkConstants.Consent.PURPOSES);
            List<ReceiptPurposeInput> receiptPurposeInputs = new ArrayList<>();
            for (int purposeIndex = 0; purposeIndex < purposes.length(); purposeIndex++) {
                receiptPurposeInputs.add(getReceiptPurposeInputs((JSONObject) purposes.get(purposeIndex)));
            }
            receiptServiceInput.setPurposes(receiptPurposeInputs);
            receiptServiceInputs.add(receiptServiceInput);
        }
        return receiptServiceInputs;
    }

    /**
     * To get the receive purpose inputs from json object from the client side.
     *
     * @param receiptPurpose Relevant receipt purpose.
     * @return receipt purpose input, based on receipt purpose object.
     */
    private ReceiptPurposeInput getReceiptPurposeInputs(JSONObject receiptPurpose) {

        ReceiptPurposeInput receiptPurposeInput = new ReceiptPurposeInput();
        receiptPurposeInput.setConsentType(FrameworkConstants.Consent.EXPLICIT_CONSENT_TYPE);
        receiptPurposeInput.setPrimaryPurpose(true);
        receiptPurposeInput.setThirdPartyDisclosure(false);
        receiptPurposeInput.setPurposeId(receiptPurpose.getInt("purposeId"));
        JSONArray purposeCategoryId = receiptPurpose.getJSONArray("purposeCategoryId");
        List<Integer> purposeCategoryIdArray = new ArrayList<>();
        for (int index = 0; index < purposeCategoryId.length(); index++) {
            purposeCategoryIdArray.add(purposeCategoryId.getInt(index));
        }
        receiptPurposeInput.setTermination(FrameworkConstants.Consent.INFINITE_TERMINATION);
        receiptPurposeInput.setPurposeCategoryId(purposeCategoryIdArray);
        receiptPurposeInput.setTermination(FrameworkConstants.Consent.INFINITE_TERMINATION);
        List<PIICategoryValidity> piiCategoryValidities = new ArrayList<>();
        JSONArray piiCategories = (JSONArray) receiptPurpose.get(FrameworkConstants.Consent.PII_CATEGORY);
        for (int categoryIndex = 0; categoryIndex < piiCategories.length(); categoryIndex++) {
            JSONObject piiCategory = (JSONObject) piiCategories.get(categoryIndex);
            PIICategoryValidity piiCategoryValidity = new PIICategoryValidity(piiCategory.getInt("piiCategoryId"),
                    FrameworkConstants.Consent.INFINITE_TERMINATION);
            piiCategoryValidity.setConsented(true);
            piiCategoryValidities.add(piiCategoryValidity);
        }
        receiptPurposeInput.setPiiCategory(piiCategoryValidities);
        return receiptPurposeInput;
    }

    /**
     * To get the associated username for the current step.
     *
     * @param idpName                        Name of IDP related with current step.
     * @param authenticatedSubjectIdentifier Authenticated subject identifier.
     * @return username associated locally.
     */
    private String getLocalUserAssociatedForFederatedIdentifier(String idpName, String authenticatedSubjectIdentifier,
                                                                String tenantDomain)
            throws PostAuthenticationFailedException {

        String username = null;
        try {
            FederatedAssociationManager federatedAssociationManager = FrameworkUtils.getFederatedAssociationManager();
            username = federatedAssociationManager.getUserForFederatedAssociation(tenantDomain, idpName,
                    authenticatedSubjectIdentifier);
        } catch (FederatedAssociationManagerException | FrameworkException e) {
            handleExceptions(
                    String.format(ErrorMessages.ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getMessage(), idpName),
                    ErrorMessages.ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getCode(), e);
        }
        return username;
    }

    /**
     * To handle exceptions.
     *
     * @param errorMessage Error Message
     * @param errorCode    Error Code.
     * @param e            Exception that is thrown during a failure.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void handleExceptions(String errorMessage, String errorCode, Exception e)
            throws PostAuthenticationFailedException {
        throw new PostAuthenticationFailedException(errorCode, errorMessage, e);
    }

    /**
     * Call the relevant URL to add the new user.
     *
     * @param externalIdPConfig Relevant external IDP.
     * @param context           Authentication context.
     * @param localClaimValues  Local claim values.
     * @param response          HttpServlet response.
     * @param username          Relevant user name
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void redirectToAccountCreateUI(ExternalIdPConfig externalIdPConfig, AuthenticationContext context,
            Map<String, String> localClaimValues, HttpServletResponse response, String username,
            HttpServletRequest request) throws PostAuthenticationFailedException {

        try {
            ServiceURLBuilder uriBuilder = ServiceURLBuilder.create();
            if (externalIdPConfig.isModifyUserNameAllowed()) {
                context.setProperty(FrameworkConstants.CHANGING_USERNAME_ALLOWED, true);
                uriBuilder = uriBuilder.addPath(FrameworkUtils.getUserNameProvisioningUIUrl());
                uriBuilder.addParameter(FrameworkConstants.ALLOW_CHANGE_USER_NAME, String.valueOf(true));
                if (log.isDebugEnabled()) {
                    log.debug(externalIdPConfig.getName() + " allow to change the username, redirecting to "
                            + "registration endpoint to provision the user: " + username);
                }
            } else {
                    uriBuilder = uriBuilder.addPath(FrameworkUtils.getPasswordProvisioningUIUrl());
                if (log.isDebugEnabled()) {
                    if (externalIdPConfig.isPasswordProvisioningEnabled()) {
                        log.debug(externalIdPConfig.getName() + " supports password provisioning, redirecting to "
                                + "sign up endpoint to provision the user : " + username);
                    }
                }
            }
            if (externalIdPConfig.isPasswordProvisioningEnabled()) {
                uriBuilder.addParameter(FrameworkConstants.PASSWORD_PROVISION_ENABLED, String.valueOf(true));
            }
            if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                uriBuilder.addParameter(MultitenantConstants.TENANT_DOMAIN_HEADER_NAME, context.getTenantDomain());
            }
            uriBuilder.addParameter(FrameworkConstants.SERVICE_PROVIDER, context.getSequenceConfig()
                    .getApplicationConfig().getApplicationName());
            uriBuilder.addParameter(FrameworkConstants.USERNAME, username);
            uriBuilder.addParameter(FrameworkConstants.SKIP_SIGN_UP_ENABLE_CHECK, String.valueOf(true));
            uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY, context.getContextIdentifier());
            addMissingClaims(uriBuilder, context);
            localClaimValues.forEach(uriBuilder::addParameter);
            response.sendRedirect(uriBuilder.build().getRelativePublicURL());
        } catch (IOException | URLBuilderException e) {
            handleExceptions(String.format(
                    ErrorMessages.ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getMessage(),
                    username, externalIdPConfig.getName()),
                    ErrorMessages.ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getCode(), e);
        }
    }

    /**
     * To add the missing claims.
     *
     * @param uriBuilder Relevant URI builder.
     * @param context    Authentication context.
     */
    private void addMissingClaims(ServiceURLBuilder uriBuilder, AuthenticationContext context) {

        String[] missingClaims = FrameworkUtils.getMissingClaims(context);
        if (StringUtils.isNotEmpty(missingClaims[1])) {
            if (log.isDebugEnabled()) {
                String username = context.getSequenceConfig().getAuthenticatedUser()
                        .getAuthenticatedSubjectIdentifier();
                String idPName = context.getExternalIdP().getIdPName();
                log.debug("Mandatory claims for SP, " + missingClaims[1] + " is missing for the user : " + username
                        + " from the IDP " + idPName);
            }
            uriBuilder.addParameter(FrameworkConstants.MISSING_CLAIMS, missingClaims[1]);
            uriBuilder.addParameter(FrameworkConstants.MISSING_CLAIMS_DISPLAY_NAME, missingClaims[0]);
        }
    }

    /**
     * To get the external IDP Config.
     *
     * @param externalIdPConfigName Name of the external IDP Config.
     * @param context               Authentication Context.
     * @return relevant external IDP config.
     * @throws PostAuthenticationFailedException Post AuthenticationFailedException.
     */
    private ExternalIdPConfig getExternalIdpConfig(String externalIdPConfigName, AuthenticationContext context)
            throws PostAuthenticationFailedException {
        ExternalIdPConfig externalIdPConfig = null;
        try {
            externalIdPConfig = ConfigurationFacade.getInstance()
                    .getIdPConfigByName(externalIdPConfigName, context.getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            handleExceptions(String.format(ERROR_WHILE_GETTING_IDP_BY_NAME.getMessage(), externalIdPConfigName,
                    context.getTenantDomain()), ERROR_WHILE_GETTING_IDP_BY_NAME.getCode(), e);
        }
        return externalIdPConfig;
    }

    /**
     * To get the list of claims available in tenant.
     *
     * @param tenantDomain          Relevant tenant domain.
     * @param externalIdPConfigName External IDP config name.
     * @return list of cliams available in the tenant.
     * @throws PostAuthenticationFailedException PostAuthentication Failed Exception.
     */
    private org.wso2.carbon.user.api.ClaimMapping[] getClaimsForTenant(String tenantDomain,
                                                                       String externalIdPConfigName)
            throws PostAuthenticationFailedException {

        RealmService realmService = FrameworkServiceComponent.getRealmService();
        UserRealm realm = null;
        try {
            int usersTenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            realm = (UserRealm) realmService.getTenantUserRealm(usersTenantId);
        } catch (UserStoreException e) {
            handleExceptions(String.format(ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION.getMessage(), tenantDomain),
                    ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION.getCode(), e);
        }
        org.wso2.carbon.user.api.ClaimMapping[] claimMappings = null;
        try {
            if (realm != null) {
                ClaimManager claimManager = realm.getClaimManager();
                if (claimManager != null) {
                    claimMappings = claimManager.getAllClaimMappings();
                }
            }
        } catch (UserStoreException e) {
            handleExceptions(
                    String.format(ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION.getMessage(),
                            externalIdPConfigName),
                    ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION.getCode(), e);
        }
        if (log.isDebugEnabled()) {
            if (!ArrayUtils.isEmpty(claimMappings)) {
                StringBuilder claimMappingString = new StringBuilder();
                for (org.wso2.carbon.user.api.ClaimMapping claimMapping : claimMappings) {
                    claimMappingString.append(claimMapping.getClaim().getClaimUri()).append(" ");
                }
                log.debug("Claims in tenant " + tenantDomain + " : " + claimMappingString.toString());
            }
        }
        return claimMappings;
    }

    /**
     * To call the default provisioning handler.
     *
     * @param username          Name of the user to be provisioning.
     * @param context           Authentication Context.
     * @param externalIdPConfig Relevant external IDP Config.
     * @param localClaimValues  Local Claim Values.
     * @param stepConfig        Step Config.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void callDefaultProvisioningHandler(String username, AuthenticationContext context,
                                                ExternalIdPConfig externalIdPConfig,
                                                Map<String, String> localClaimValues, StepConfig stepConfig)
            throws PostAuthenticationFailedException {

        boolean useDefaultIdpDialect = externalIdPConfig.useDefaultLocalIdpDialect();
        ApplicationAuthenticator authenticator
                = stepConfig.getAuthenticatedAutenticator().getApplicationAuthenticator();
        String idPStandardDialect = authenticator.getClaimDialectURI();
        String idpRoleClaimUri = FrameworkUtils.getIdpRoleClaimUri(externalIdPConfig);
        Map<ClaimMapping, String> extAttrs = stepConfig.getAuthenticatedUser().getUserAttributes();
        Map<String, String> originalExternalAttributeValueMap = FrameworkUtils.getClaimMappings(extAttrs, false);
        Map<String, String> claimMapping = null;
        boolean excludeUnmappedRoles = false;
        if (useDefaultIdpDialect && StringUtils.isNotBlank(idPStandardDialect)) {
            try {
                claimMapping = ClaimMetadataHandler.getInstance()
                        .getMappingsMapFromOtherDialectToCarbon(idPStandardDialect,
                                originalExternalAttributeValueMap.keySet(), context.getTenantDomain(), true);
            } catch (ClaimMetadataException e) {
                throw new PostAuthenticationFailedException(ErrorMessages.ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getCode
                        (), ErrorMessages.ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getMessage(), e);
            }
        }

        if (claimMapping != null) {
            //Ex. Standard dialects like OIDC.
            idpRoleClaimUri = claimMapping.get(IdentityUtil.getLocalGroupsClaimURI());
        } else if (idPStandardDialect == null && !useDefaultIdpDialect) {
            //Ex. SAML custom claims.
            idpRoleClaimUri = FrameworkUtils.getIdpRoleClaimUri(externalIdPConfig);
        }

        /* Get the mapped user roles according to the mapping in the IDP configuration. Exclude the unmapped from the
         returned list.
         */
        if (StringUtils.isNotEmpty(IdentityUtil.getProperty(SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP))) {
            excludeUnmappedRoles = Boolean
                    .parseBoolean(IdentityUtil.getProperty(SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP));
        }
        List<String> identityProviderMappedUserRolesUnmappedExclusive = FrameworkUtils
                .getIdentityProvideMappedUserRoles(externalIdPConfig, originalExternalAttributeValueMap,
                        idpRoleClaimUri, excludeUnmappedRoles);
        localClaimValues.put(FrameworkConstants.ASSOCIATED_ID,
                stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
        localClaimValues.put(FrameworkConstants.IDP_ID, stepConfig.getAuthenticatedIdP());

        /*
        If TOTP is enabled for federated users, the initial federated user login will be identified with the following
        check and will set the secret key claim for the federated user who is going to be provisioned.
         */
        if (context.getProperty(FrameworkConstants.SECRET_KEY_CLAIM_URL) != null) {
            localClaimValues.put(FrameworkConstants.SECRET_KEY_CLAIM_URL,
                    context.getProperty(FrameworkConstants.SECRET_KEY_CLAIM_URL).toString());
        }

        // Remove role claim from local claims as roles are specifically handled.
        localClaimValues.remove(FrameworkUtils.getLocalClaimUriMappedForIdPRoleClaim(externalIdPConfig));
        localClaimValues.remove(UserCoreConstants.USER_STORE_GROUPS_CLAIM);
        try {
            FrameworkUtils.getStepBasedSequenceHandler()
                    .callJitProvisioning(username, context, identityProviderMappedUserRolesUnmappedExclusive,
                            localClaimValues);
        } catch (FrameworkException e) {
            handleExceptions(
                    String.format(ERROR_WHILE_TRYING_TO_PROVISION_USER_WITHOUT_PASSWORD_PROVISIONING.getMessage(),
                            username, externalIdPConfig.getName()),
                    ERROR_WHILE_TRYING_TO_PROVISION_USER_WITHOUT_PASSWORD_PROVISIONING.getCode(), e);
        }
    }

    /**
     * This method is responsible for handling consents.
     *
     * @param request      Relevant http servlet request.
     * @param stepConfig   Step Configuration for the particular configuration step.
     * @param tenantDomain Specific tenant domain.
     * @throws PostAuthenticationFailedException Post Authentication failed exception.
     */
    private void handleConsents(HttpServletRequest request, StepConfig stepConfig, String tenantDomain)
            throws PostAuthenticationFailedException {

        String userName = getLocalUserAssociatedForFederatedIdentifier(stepConfig.getAuthenticatedIdP(),
                stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier(), tenantDomain);
        String consent = request.getParameter("consent");
        String policyURL = request.getParameter("policy");
        if (StringUtils.isNotEmpty(consent)) {
            ReceiptInput receiptInput = buildConsentForResidentIDP(userName, consent, policyURL);
            addConsent(receiptInput, tenantDomain);
        }
    }

    /**
     * Persist the consents received from the user, while user creation.
     *
     * @param receiptInput Relevant receipt input representing consent data.
     * @param tenantDomain Relevant tenant domain.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void addConsent(ReceiptInput receiptInput, String tenantDomain) throws PostAuthenticationFailedException {

        ConsentManager consentManager = FrameworkServiceDataHolder.getInstance().getConsentManager();
        if (receiptInput.getServices().size() == 0) {
            throw new PostAuthenticationFailedException(ErrorMessages.ERROR_WHILE_ADDING_CONSENT.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_ADDING_CONSENT.getMessage(), tenantDomain));
        }
        // There should be one receipt
        ReceiptServiceInput receiptServiceInput = receiptInput.getServices().get(0);
        receiptServiceInput.setTenantDomain(tenantDomain);
        try {
            setIDPData(tenantDomain, receiptServiceInput);
            receiptInput.setTenantDomain(tenantDomain);
            consentManager.addConsent(receiptInput);
        } catch (ConsentManagementException e) {
            handleExceptions(String.format(ErrorMessages.ERROR_WHILE_ADDING_CONSENT.getMessage(), tenantDomain),
                    ErrorMessages.ERROR_WHILE_ADDING_CONSENT.getCode(), e);
        }
    }

    /**
     * Set the IDP releated data in the receipt service input.
     *
     * @param tenantDomain        Tenant domain.
     * @param receiptServiceInput Relevant receipt service input which the
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void setIDPData(String tenantDomain, ReceiptServiceInput receiptServiceInput)
            throws PostAuthenticationFailedException {

        String resideIdpDescription = "Resident IDP";
        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
        IdentityProvider residentIdP = null;
        try {
            residentIdP = idpManager.getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            handleExceptions(String.format(ErrorMessages.ERROR_WHILE_SETTING_IDP_DATA.getMessage(), tenantDomain),
                    ErrorMessages.ERROR_WHILE_SETTING_IDP_DATA.getCode(), e);
        }
        if (residentIdP == null) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL.getMessage(), tenantDomain));
        }
        if (StringUtils.isEmpty(receiptServiceInput.getService())) {
            if (log.isDebugEnabled()) {
                log.debug("No service name found. Hence adding resident IDP home realm ID");
            }
            receiptServiceInput.setService(residentIdP.getHomeRealmId());
        }
        if (StringUtils.isEmpty(receiptServiceInput.getTenantDomain())) {
            receiptServiceInput.setTenantDomain(tenantDomain);
        }
        if (StringUtils.isEmpty(receiptServiceInput.getSpDescription())) {
            if (StringUtils.isNotEmpty(residentIdP.getIdentityProviderDescription())) {
                receiptServiceInput.setSpDescription(residentIdP.getIdentityProviderDescription());
            } else {
                receiptServiceInput.setSpDescription(resideIdpDescription);
            }
        }
        if (StringUtils.isEmpty(receiptServiceInput.getSpDisplayName())) {
            if (StringUtils.isNotEmpty(residentIdP.getDisplayName())) {
                receiptServiceInput.setSpDisplayName(residentIdP.getDisplayName());
            } else {
                receiptServiceInput.setSpDisplayName(resideIdpDescription);
            }
        }
    }

    private String getUserIdClaimUriInLocalDialect(ExternalIdPConfig idPConfig) {
        // get external identity provider user id claim URI.
        String userIdClaimUri = idPConfig.getUserIdClaimUri();

        if (StringUtils.isBlank(userIdClaimUri)) {
            return null;
        }

        boolean useDefaultLocalIdpDialect = idPConfig.useDefaultLocalIdpDialect();
        if (useDefaultLocalIdpDialect) {
            return userIdClaimUri;
        } else {
            ClaimMapping[] claimMappings = idPConfig.getClaimMappings();
            if (!ArrayUtils.isEmpty(claimMappings)) {
                for (ClaimMapping claimMapping : claimMappings) {
                    if (userIdClaimUri.equals(claimMapping.getRemoteClaim().getClaimUri())) {
                        return claimMapping.getLocalClaim().getClaimUri();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Uses to get local claim values of an authenticated user from an IDP in non attribute selection steps.
     *
     * @param context           Authentication Context.
     * @param stepConfig        Current step configuration.
     * @param externalIdPConfig Identity providers config.
     * @return Mapped federated user values to local claims.
     * @throws PostAuthenticationFailedException Post Authentication failed exception.
     */
    private Map<String, String> getLocalClaimValuesOfIDPInNonAttributeSelectionStep(AuthenticationContext context,
                                                                                    StepConfig stepConfig,
                                                                                    ExternalIdPConfig externalIdPConfig)
            throws PostAuthenticationFailedException {

        boolean useDefaultIdpDialect = externalIdPConfig.useDefaultLocalIdpDialect();
        ApplicationAuthenticator authenticator =
                stepConfig.getAuthenticatedAutenticator().getApplicationAuthenticator();
        String idPStandardDialect = authenticator.getClaimDialectURI();
        Map<ClaimMapping, String> extAttrs = stepConfig.getAuthenticatedUser().getUserAttributes();
        Map<String, String> originalExternalAttributeValueMap = FrameworkUtils.getClaimMappings(extAttrs, false);
        Map<String, String> claimMapping = new HashMap<>();
        Map<String, String> localClaimValues = new HashMap<>();
        if (useDefaultIdpDialect && StringUtils.isNotBlank(idPStandardDialect)) {
            try {
                claimMapping = ClaimMetadataHandler.getInstance()
                        .getMappingsMapFromOtherDialectToCarbon(idPStandardDialect,
                                originalExternalAttributeValueMap.keySet(), context.getTenantDomain(),
                                true);
            } catch (ClaimMetadataException e) {
                throw new PostAuthenticationFailedException(ErrorMessages.ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getCode(),
                        ErrorMessages.ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getMessage(), e);
            }
        } else {
            ClaimMapping[] customClaimMapping = context.getExternalIdP().getClaimMappings();
            for (ClaimMapping externalClaim : customClaimMapping) {
                if (originalExternalAttributeValueMap.containsKey(externalClaim.getRemoteClaim().getClaimUri())) {
                    claimMapping.put(externalClaim.getLocalClaim().getClaimUri(),
                            externalClaim.getRemoteClaim().getClaimUri());
                }
            }
        }

        if (claimMapping != null && claimMapping.size() > 0) {
            for (Map.Entry<String, String> entry : claimMapping.entrySet()) {
                if (originalExternalAttributeValueMap.containsKey(entry.getValue()) &&
                        originalExternalAttributeValueMap.get(entry.getValue()) != null) {
                    localClaimValues.put(entry.getKey(), originalExternalAttributeValueMap.get(entry.getValue()));
                }
            }
        }
        return localClaimValues;
    }

    private UserRealm getUserRealm(String tenantDomain) throws UserStoreException {

        RealmService realmService = FrameworkServiceComponent.getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return (UserRealm) realmService.getTenantUserRealm(tenantId);
    }

    private UserStoreManager getUserStoreManager(String provisioningUserStoreId, UserRealm realm, String username)
            throws UserStoreException {

        String userStoreDomain = getUserStoreDomain(provisioningUserStoreId, realm, username);

        UserStoreManager userStoreManager;
        try {
            if (userStoreDomain != null && !userStoreDomain.isEmpty()) {
                userStoreManager = realm.getUserStoreManager().getSecondaryUserStoreManager(
                        userStoreDomain);
            } else {
                userStoreManager = realm.getUserStoreManager();
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new UserStoreException(ErrorMessages.ERROR_WHILE_GETTING_USER_STORE_MANAGER.getMessage(), e);
        }

        if (userStoreManager == null) {
            throw new UserStoreException(ErrorMessages.ERROR_INVALID_USER_STORE.getMessage(), null);
        }
        return userStoreManager;
    }

    private String getUserStoreDomain(String provisioningUserStoreId, UserRealm realm, String subject)
            throws UserStoreException {

        String userStoreDomain;
        if (IdentityApplicationConstants.AS_IN_USERNAME_USERSTORE_FOR_JIT
                .equalsIgnoreCase(provisioningUserStoreId)) {
            userStoreDomain = UserCoreUtil.extractDomainFromName(subject);
        } else {
            userStoreDomain = provisioningUserStoreId;
        }

        try {
            if (userStoreDomain != null
                    && realm.getUserStoreManager().getSecondaryUserStoreManager(userStoreDomain) == null) {
                throw new UserStoreException(String.format(ErrorMessages.ERROR_INVALID_USER_STORE_DOMAIN
                                .getMessage(), userStoreDomain), null);
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        return userStoreDomain;
    }
}
