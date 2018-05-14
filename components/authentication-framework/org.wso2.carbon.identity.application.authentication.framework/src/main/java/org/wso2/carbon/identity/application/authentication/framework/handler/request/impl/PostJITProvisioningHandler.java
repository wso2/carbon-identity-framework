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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
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
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.USERNAME_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.*;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.*;

/**
 * This is post authentication handler responsible for JIT provisioning.
 */
public class PostJITProvisioningHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(PostJITProvisioningHandler.class);
    private static volatile PostJITProvisioningHandler instance;

    public static PostJITProvisioningHandler getInstance() {

        if (instance == null) {
            synchronized (PostJITProvisioningHandler.class) {
                if (instance == null) {
                    instance = new PostJITProvisioningHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public int getPriority() {

        return 20;
    }

    @Override
    public String getName() {

        return "JITProvisionHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws PostAuthenticationFailedException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();

        AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
        if (authenticatedUser == null) {
            return UNSUCCESS_COMPLETED;
        }

        Object object = context.getProperty(PASSWORD_PROVISION_REDIRECTION_TRIGGERED);
        boolean passWordProvisioningRedirectionTriggered = false;
        if (object instanceof Boolean) {
            passWordProvisioningRedirectionTriggered = (boolean) object;
        }

        if (passWordProvisioningRedirectionTriggered) {
            return handleResponseFlow(request, context, sequenceConfig);
        } else {
            return handleRequestFlow(request, response, context, sequenceConfig);
        }
    }

    /**
     * This method is used to handle response flow, after going through password provisioning.
     *
     * @param request        HttpServlet request.
     * @param context        Authentication context
     * @param sequenceConfig Relevant sequence config.
     * @return Status of PostAuthnHandler flow.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception
     */
    @SuppressWarnings("unchecked")
    private PostAuthnHandlerFlowStatus handleResponseFlow(HttpServletRequest request, AuthenticationContext context,
            SequenceConfig sequenceConfig) throws PostAuthenticationFailedException {

        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                ExternalIdPConfig externalIdPConfig = null;
                String externalIdPConfigName = stepConfig.getAuthenticatedIdP();
                externalIdPConfig = getExternalIdpConfig(externalIdPConfigName, context);
                context.setExternalIdP(externalIdPConfig);

                if (externalIdPConfig != null && externalIdPConfig.isProvisioningEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug("JIT provisioning response flow has hit for the IDP " + externalIdPConfigName + " "
                                + "for the user, " + sequenceConfig.getAuthenticatedUser().getUserName());
                    }
                    final Map<String, String> localClaimValues;
                    Object unfilteredLocalClaimValues = context
                            .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
                    localClaimValues = unfilteredLocalClaimValues == null ?
                            new HashMap<>() :
                            (Map<String, String>) unfilteredLocalClaimValues;

                    org.wso2.carbon.user.api.ClaimMapping[] claims = getClaimsForTenant(context.getTenantDomain(),
                            externalIdPConfigName);

                    if (claims != null) {
                        for (org.wso2.carbon.user.api.ClaimMapping claimMapping : claims) {
                            String uri = claimMapping.getClaim().getClaimUri();
                            String claimValue = request.getParameter(uri);

                            if (StringUtils.isNotBlank(claimValue) && StringUtils.isEmpty(localClaimValues.get(uri))) {
                                localClaimValues.put(uri, claimValue);
                            }
                        }
                    }

                    localClaimValues
                            .put(FrameworkConstants.PASSWORD, request.getParameter(FrameworkConstants.PASSWORD));
                    String username = sequenceConfig.getAuthenticatedUser().getUserName();

                    if (context.getProperty(CHANGING_USERNAME_ALLOWED) != null) {
                        username = request.getParameter(FrameworkConstants.USERNAME);
                    }

                    callDefaultProvisioniningHandler(username, context, externalIdPConfig, localClaimValues,
                            stepConfig);
                }

            }
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    /**
     * To handle the request flow of the post authentication handler.
     *
     * @param response       HttpServlet response.
     * @param context        Authentication context
     * @param sequenceConfig Sequence Config
     * @return Status of this post authentication handler flow.
     * @throws PostAuthenticationFailedException Exception that will be thrown in case of failure.
     */
    @SuppressWarnings("unchecked")
    private PostAuthnHandlerFlowStatus handleRequestFlow(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig) throws PostAuthenticationFailedException {

        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {

                ExternalIdPConfig externalIdPConfig = null;
                String externalIdPConfigName = stepConfig.getAuthenticatedIdP();
                externalIdPConfig = getExternalIdpConfig(externalIdPConfigName, context);

                context.setExternalIdP(externalIdPConfig);
                Map<String, String> localClaimValues;
                localClaimValues = (Map<String, String>) context
                        .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);

                if (externalIdPConfig != null && externalIdPConfig.isProvisioningEnabled()) {

                    if (localClaimValues == null) {
                        localClaimValues = new HashMap<>();
                    }

                    String username = null;
                    try {
                        UserProfileAdmin userProfileAdmin = UserProfileAdmin.getInstance();
                        username = userProfileAdmin.getNameAssociatedWith(stepConfig.getAuthenticatedIdP(),
                                stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
                    } catch (UserProfileException e) {
                        handleExceptions(String.format(
                                ErrorMessages.ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getMessage(),
                                externalIdPConfigName),
                                ErrorMessages.ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getCode(), e);
                    }

                    if (username == null) {
                        redirectToPasswordProvisioningUI(externalIdPConfig, context, localClaimValues, response,
                                sequenceConfig.getAuthenticatedUser().getUserName());
                        context.setProperty(PASSWORD_PROVISION_REDIRECTION_TRIGGERED, true);
                        return PostAuthnHandlerFlowStatus.INCOMPLETE;
                    }

                    if (StringUtils.isEmpty(username)) {
                        username = stepConfig.getAuthenticatedUser().getUserName();
                    }

                    callDefaultProvisioniningHandler(username, context, externalIdPConfig, localClaimValues, stepConfig);
                }

            }
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
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
        log.error(errorCode + " - " + errorMessage, e);
        throw new PostAuthenticationFailedException(errorCode, errorMessage, e);
    }

    /**
     * Call the relevant URL for the password provisioning.
     *
     * @param externalIdPConfig Relevant external IDP.
     * @param context           Authentication context.
     * @param localClaimValues  Local claim values.
     * @param response          HttpServlet response.
     * @param username          Relevant user name
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private void redirectToPasswordProvisioningUI(ExternalIdPConfig externalIdPConfig, AuthenticationContext context,
            Map<String, String> localClaimValues, HttpServletResponse response, String username)
            throws PostAuthenticationFailedException {

        try {
            URIBuilder uriBuilder;
            if (externalIdPConfig.isModifyUserNameAllowed()) {
                context.setProperty(FrameworkConstants.CHANGING_USERNAME_ALLOWED, true);
                uriBuilder = new URIBuilder(REGISTRATION_ENDPOINT);
                if (log.isDebugEnabled()) {
                    log.debug(externalIdPConfig.getName() + " allow to change the username, redirecting to "
                            + "registration endpoint to provision the user " + username);
                }
            } else {
                uriBuilder = new URIBuilder(SIGN_UP_ENDPOINT);
                if (log.isDebugEnabled()) {
                    log.debug(externalIdPConfig.getName() + " supports password provisioning, redirecting to "
                            + "sign up endpoint to provision the user " + username);
                }
            }

            if (externalIdPConfig.isPasswordProvisioningEnabled()) {
                uriBuilder.addParameter(FrameworkConstants.PASSWORD_PROVISION_ENABLED, String.valueOf(true));
            }
            uriBuilder.addParameter(FrameworkConstants.USERNAME, username);
            uriBuilder.addParameter(FrameworkConstants.SKIP_SIGN_UP_ENABLE_CHECK, String.valueOf(true));
            uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY, context.getContextIdentifier());
            String[] missingClaims = PostAuthnMissingClaimHandler.getInstance().getMissingClaims(context);

            if (StringUtils.isNotEmpty(missingClaims[1])) {
                uriBuilder.addParameter(FrameworkConstants.MISSING_CLAIMS, missingClaims[1]);
                uriBuilder.addParameter(FrameworkConstants.MISSING_CLAIMS_DISPLAY_NAME, missingClaims[0]);
            }
            localClaimValues.forEach(uriBuilder::addParameter);
            response.sendRedirect(uriBuilder.build().toString());
        } catch (URISyntaxException | IOException e) {
            handleExceptions(String.format(
                    ErrorMessages.ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getMessage(),
                    username, externalIdPConfig.getName()),
                    ErrorMessages.ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getCode(), e);
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
    private org.wso2.carbon.user.api.ClaimMapping[] getClaimsForTenant(String tenantDomain, String externalIdPConfigName)
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

        // Getting only the supported claims.
        try {
            claimMappings = realm.getClaimManager().getAllClaimMappings();

        } catch (UserStoreException e) {
            handleExceptions(
                    String.format(ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION.getMessage(),
                            externalIdPConfigName),
                    ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION.getCode(), e);
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
    private void callDefaultProvisioniningHandler(String username, AuthenticationContext context,
            ExternalIdPConfig externalIdPConfig, Map<String, String> localClaimValues, StepConfig stepConfig)
            throws PostAuthenticationFailedException {

        String idpRoleClaimUri = FrameworkUtils.getIdpRoleClaimUri(externalIdPConfig);
        Map<ClaimMapping, String> extAttrs = stepConfig.getAuthenticatedUser().getUserAttributes();
        Map<String, String> originalExternalAttributeValueMap = FrameworkUtils.getClaimMappings(extAttrs, false);

        // Get the mapped user roles according to the mapping in the IDP configuration.
        // Exclude the unmapped from the returned list.
        List<String> identityProviderMappedUserRolesUnmappedExclusive = FrameworkUtils
                .getIdentityProvideMappedUserRoles(externalIdPConfig, originalExternalAttributeValueMap,
                        idpRoleClaimUri, true);

        localClaimValues.put(FrameworkConstants.ASSOCIATED_ID,
                stepConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
        localClaimValues.put(FrameworkConstants.IDP_ID, stepConfig.getAuthenticatedIdP());
        // Remove role claim from local claims as roles are specifically handled.
        localClaimValues.remove(FrameworkUtils.getLocalClaimUriMappedForIdPRoleClaim(externalIdPConfig));
        localClaimValues.remove(USERNAME_CLAIM);

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
}