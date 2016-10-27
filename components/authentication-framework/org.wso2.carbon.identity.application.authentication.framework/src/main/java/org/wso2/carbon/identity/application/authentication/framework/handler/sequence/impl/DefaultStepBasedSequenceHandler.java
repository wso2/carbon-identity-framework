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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultStepBasedSequenceHandler implements StepBasedSequenceHandler {

    public static final String USER_TENANT_DOMAIN = "user-tenant-domain";
    private static final Log log = LogFactory.getLog(DefaultStepBasedSequenceHandler.class);
    private static volatile DefaultStepBasedSequenceHandler instance;

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
            }

            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

            // if the current step is completed
            if (stepConfig != null && stepConfig.isCompleted()) {
                stepConfig.setCompleted(false);
                stepConfig.setRetrying(false);

                // if the request didn't fail during the step execution
                if (context.isRequestAuthenticated()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Step " + stepConfig.getOrder()
                                + " is completed. Going to get the next one.");
                    }

                    currentStep = context.getCurrentStep() + 1;
                    context.setCurrentStep(currentStep);
                    stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Authentication has failed in the Step "
                                + (context.getCurrentStep()));
                    }

                    // if the step contains multiple login options, we should give the user to retry
                    // authentication
                    if (stepConfig.isMultiOption() && !context.isPassiveAuthenticate()) {
                        stepConfig.setRetrying(true);
                        context.setRequestAuthenticated(true);
                    } else {
                        context.getSequenceConfig().setCompleted(true);
                        resetAuthenticationContext(context);
                        continue;
                    }
                }

                resetAuthenticationContext(context);
            }

            // if no further steps exists
            if (stepConfig == null) {

                if (log.isDebugEnabled()) {
                    log.debug("There are no more steps to execute");
                }

                // if no step failed at authentication we should do post authentication work (e.g.
                // claim handling, provision etc)
                if (context.isRequestAuthenticated()) {

                    if (log.isDebugEnabled()) {
                        log.debug("Request is successfully authenticated");
                    }

                    context.getSequenceConfig().setCompleted(true);
                    handlePostAuthentication(request, response, context);

                    // if step is not completed, that means step wants to redirect to outside
                    if (!context.getSequenceConfig().isCompleted()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Post authentication is not complete yet. Redirecting to outside.");
                        }
                        return;
                    }
                }

                // we should get out of steps now.
                if (log.isDebugEnabled()) {
                    log.debug("Step processing is completed");
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

    private void handleResponseWithMissingClaims(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws FrameworkException {

        Map<String, String> claims = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        boolean persistClaims = false;

        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();

        for (String key : requestParams.keySet()) {
            if (key.startsWith("claim_mand_")) {
                String claim = key.substring("claim_mand_".length());
                claims.put(claim, requestParams.get(key)[0]);
            }
        }

        Map<ClaimMapping, String> authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(claims);
        authenticatedUserAttributes.putAll(user.getUserAttributes());

        for (Map.Entry<Integer, StepConfig> entry : context.getSequenceConfig().getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            if (stepConfig.isSubjectAttributeStep()) {

                user = stepConfig.getAuthenticatedUser();
                if (!user.isFederatedUser()) {
                    persistClaims = true;
                } else {
                    String associatedID = null;
                    UserProfileAdmin userProfileAdmin = UserProfileAdmin.getInstance();
                    String subject = user.getAuthenticatedSubjectIdentifier();
                    try {
                        associatedID = userProfileAdmin.getNameAssociatedWith(stepConfig.getAuthenticatedIdP(),
                                subject);
                        if (StringUtils.isNotBlank(associatedID)) {
                            String fullQualifiedAssociatedUserId = FrameworkUtils.prependUserStoreDomainToName(
                                    associatedID + UserCoreConstants.TENANT_DOMAIN_COMBINER + context.getTenantDomain());
                            user = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(
                                            fullQualifiedAssociatedUserId);
                            persistClaims = true;
                        }
                    } catch (UserProfileException e) {
                        throw new FrameworkException("Error while getting association for " + subject, e);
                    }
                }
                break;
            }
        }

        if (persistClaims) {

            try {
                String tenantDomain =
                        context.getSequenceConfig().getApplicationConfig().getServiceProvider().getOwner().getTenantDomain();
                String spName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

                ApplicationManagementServiceImpl applicationManagementService =
                        ApplicationManagementServiceImpl.getInstance();
                Map<String, String> claimMapping =
                        applicationManagementService.getServiceProviderToLocalIdPClaimMapping(spName, tenantDomain);

                Map<String, String> localIdpClaims = new HashMap<>();
                for (Map.Entry<String, String> entry : claims.entrySet()) {
                    String localClaim = claimMapping.get(entry.getKey());
                    localIdpClaims.put(localClaim, entry.getValue());
                }

                UserRealm realm = getUserRealm(user.getTenantDomain());
                UserStoreManager userStoreManager =
                        realm.getUserStoreManager().getSecondaryUserStoreManager(user.getUserStoreDomain());
                userStoreManager.setUserClaimValues(user.getUserName(), localIdpClaims, null);
            } catch (UserStoreException e) {
                throw new FrameworkException(
                        "Error while updating claims for local user. Could not update profile", e);
            } catch (IdentityApplicationManagementException e) {
                throw new FrameworkException(
                        "Error while retrieving application claim mapping. Could not update profile", e);
            }
        }

        context.getSequenceConfig().getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);
        context.setProperty(FrameworkConstants.REQUEST_MISSING_CLAIMS_TRIGGERED, false);

    }

    @SuppressWarnings("unchecked")
    protected void handlePostAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Handling Post Authentication tasks");
        }

        if (!isPostAuthenticateExtensionTriggered(context)) {

            SequenceConfig sequenceConfig = context.getSequenceConfig();
            StringBuilder jsonBuilder = new StringBuilder();

            boolean subjectFoundInStep = false;
            boolean subjectAttributesFoundInStep = false;
            int stepCount = 1;
            Map<String, String> mappedAttrs = new HashMap<>();
            Map<ClaimMapping, String> authenticatedUserAttributes = new HashMap<>();

            for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = entry.getValue();
                AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
                ApplicationAuthenticator authenticator = authenticatorConfig
                        .getApplicationAuthenticator();

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

                    if (!subjectFoundInStep) {
                        stepConfig.setSubjectIdentifierStep(true);
                    }

                    if (!subjectAttributesFoundInStep) {
                        stepConfig.setSubjectAttributeStep(true);
                    }
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
                    List<String> locallyMappedUserRoles = null;

                    extAttrs = stepConfig.getAuthenticatedUser().getUserAttributes();
                    extAttibutesValueMap = FrameworkUtils.getClaimMappings(extAttrs, false);

                    if (stepConfig.isSubjectIdentifierStep()) {
                        // there can be only step for subject attributes.

                        subjectFoundInStep = true;
                        String associatedID = null;

                        // now we know the value of the subject - from the external identity provider.

                        if (sequenceConfig.getApplicationConfig().isAlwaysSendMappedLocalSubjectId()) {

                            // okay - now we need to find out the corresponding mapped local subject
                            // identifier.

                            UserProfileAdmin userProfileAdmin = UserProfileAdmin.getInstance();
                            try {
                                // start tenant flow
                                FrameworkUtils.startTenantFlow(context.getTenantDomain());
                                associatedID = userProfileAdmin.getNameAssociatedWith(stepConfig.getAuthenticatedIdP(),
                                        originalExternalIdpSubjectValueForThisStep);
                                if (StringUtils.isNotBlank(associatedID)) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("User " + stepConfig.getAuthenticatedUser() +
                                                " has an associated account as " + associatedID + ". Hence continuing as " +
                                                associatedID);
                                    }
                                    stepConfig.getAuthenticatedUser().setUserName(associatedID);
                                    stepConfig.getAuthenticatedUser().setTenantDomain(context.getTenantDomain());
                                    stepConfig.setAuthenticatedUser(stepConfig.getAuthenticatedUser());
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("User " + stepConfig.getAuthenticatedUser() +
                                                " doesn't have an associated" +
                                                " account. Hence continuing as the same user.");
                                    }
                                }
                            } catch (UserProfileException e) {
                                throw new FrameworkException("Error while getting associated local user ID for "
                                        + originalExternalIdpSubjectValueForThisStep, e);
                            } finally {
                                // end tenant flow
                                FrameworkUtils.endTenantFlow();
                            }
                        }


                        if (associatedID != null && associatedID.trim().length() > 0) {

                            handleClaimMappings(stepConfig, context, extAttibutesValueMap, true);
                            localClaimValues = (Map<String, String>) context
                                    .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);

                            idpClaimValues = (Map<String, String>) context
                                    .getProperty(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES);
                            // we found an associated user identifier
                            // build the full qualified user id for the associated user
                            String fullQualifiedAssociatedUserId = FrameworkUtils.prependUserStoreDomainToName(
                                    associatedID + UserCoreConstants.TENANT_DOMAIN_COMBINER + context.getTenantDomain());
                            sequenceConfig.setAuthenticatedUser(AuthenticatedUser
                                    .createLocalAuthenticatedUserFromSubjectIdentifier(
                                            fullQualifiedAssociatedUserId));

                            sequenceConfig.getApplicationConfig().setMappedSubjectIDSelected(true);

                            // if we found a local mapped user - then we will also take attributes from
                            // that user - this will load local claim values for the user.
                            mappedAttrs = handleClaimMappings(stepConfig, context, null, false);

                            // if no requested claims are selected, send all local mapped claim values or idp claim values
                            if (context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings() == null ||
                                    context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings()
                                            .isEmpty()) {

                                if (localClaimValues != null && !localClaimValues.isEmpty()) {
                                    mappedAttrs = localClaimValues;
                                } else if (idpClaimValues != null && !idpClaimValues.isEmpty()) {
                                    mappedAttrs = idpClaimValues;
                                }
                            }

                            authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);

                            // in this case associatedID is a local user name - belongs to a tenant in IS.
                            String tenantDomain = MultitenantUtils.getTenantDomain(associatedID);
                            Map<String, Object> authProperties = context.getProperties();

                            if (authProperties == null) {
                                authProperties = new HashMap<>();
                                context.setProperties(authProperties);
                            }

                            //TODO: user tenant domain has to be an attribute in the AuthenticationContext
                            authProperties.put(USER_TENANT_DOMAIN, tenantDomain);

                            if (log.isDebugEnabled()) {
                                log.debug("Authenticated User: " +
                                        sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
                                log.debug("Authenticated User Tenant Domain: " + tenantDomain);
                            }

                        } else {

                            sequenceConfig.setAuthenticatedUser(new AuthenticatedUser(stepConfig.getAuthenticatedUser()));

                            // Only place we do not set the setAuthenticatedUserTenantDomain into the sequenceConfig
                            // TODO : Check whether not setting setAuthenticatedUserTenantDomain is correct

                        }

                    }

                    if (stepConfig.isSubjectAttributeStep()) {

                        subjectAttributesFoundInStep = true;

                        String idpRoleClaimUri = getIdpRoleClaimUri(externalIdPConfig);

                        locallyMappedUserRoles = getLocallyMappedUserRoles(sequenceConfig,
                                externalIdPConfig, extAttibutesValueMap, idpRoleClaimUri);

                        if (idpRoleClaimUri != null && getServiceProviderMappedUserRoles(sequenceConfig,
                                locallyMappedUserRoles) != null) {
                            extAttibutesValueMap.put(idpRoleClaimUri, getServiceProviderMappedUserRoles(sequenceConfig,
                                    locallyMappedUserRoles));
                        }

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

                        if (!sequenceConfig.getApplicationConfig().isMappedSubjectIDSelected()) {
                            // if we found the mapped subject - then we do not need to worry about
                            // finding attributes.

                            // if no requested claims are selected, send all local mapped claim values or idp claim values
                            if (context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings() == null ||
                                    context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings().isEmpty()) {

                                if (localClaimValues != null && !localClaimValues.isEmpty()) {
                                    mappedAttrs = localClaimValues;
                                } else if (idpClaimValues != null && !idpClaimValues.isEmpty()) {
                                    mappedAttrs = idpClaimValues;
                                }
                            }
                            authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                        }

                    }

                    // do user provisioning. we should provision the user with the original external
                    // subject identifier.
                    if (externalIdPConfig.isProvisioningEnabled()) {

                        if (localClaimValues == null) {
                            localClaimValues = new HashMap<>();
                        }

                        handleJitProvisioning(originalExternalIdpSubjectValueForThisStep, context,
                                locallyMappedUserRoles, localClaimValues);
                    }

                } else {

                    if (stepConfig.isSubjectIdentifierStep()) {
                        subjectFoundInStep = true;
                        sequenceConfig.setAuthenticatedUser(new AuthenticatedUser(stepConfig.getAuthenticatedUser()));

                        if (log.isDebugEnabled()) {
                            log.debug("Authenticated User: " + sequenceConfig.getAuthenticatedUser().getUserName());
                            log.debug("Authenticated User Tenant Domain: " + sequenceConfig.getAuthenticatedUser()
                                    .getTenantDomain());
                        }
                    }

                    if (stepConfig.isSubjectAttributeStep()) {
                        subjectAttributesFoundInStep = true;
                        // local authentications
                        mappedAttrs = handleClaimMappings(stepConfig, context, null, false);

                        String spRoleUri = getSpRoleClaimUri(sequenceConfig.getApplicationConfig());

                        String roleAttr = mappedAttrs.get(spRoleUri);

                        if (roleAttr != null && roleAttr.trim().length() > 0) {

                            String[] roles = roleAttr.split(",");
                            mappedAttrs.put(
                                    spRoleUri,
                                    getServiceProviderMappedUserRoles(sequenceConfig,
                                            Arrays.asList(roles)));
                        }

                        authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                    }
                }
            }

            String subjectClaimURI = sequenceConfig.getApplicationConfig().getSubjectClaimUri();
            String subjectValue = (String) context.getProperty("ServiceProviderSubjectClaimValue");
            if (StringUtils.isNotBlank(subjectClaimURI)) {
                if (subjectValue != null) {
                    sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);

                    if (log.isDebugEnabled()) {
                        log.debug("Authenticated User: " +
                                sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
                        log.debug("Authenticated User Tenant Domain: " + sequenceConfig.getAuthenticatedUser()
                                .getTenantDomain());
                    }
                } else {
                    log.warn("Subject claim could not be found. Defaulting to Name Identifier.");
                    if (StringUtils.isNotBlank(sequenceConfig.getAuthenticatedUser().getUserName())) {
                        sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(sequenceConfig
                                .getAuthenticatedUser().getUsernameAsSubjectIdentifier(sequenceConfig.getApplicationConfig()
                                        .isUseUserstoreDomainInLocalSubjectIdentifier(), sequenceConfig
                                        .getApplicationConfig().isUseTenantDomainInLocalSubjectIdentifier()));
                    }
                }

            } else {
                if (StringUtils.isNotBlank(sequenceConfig.getAuthenticatedUser().getUserName())) {
                    sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(sequenceConfig
                            .getAuthenticatedUser().getUsernameAsSubjectIdentifier(sequenceConfig.getApplicationConfig()
                                    .isUseUserstoreDomainInLocalSubjectIdentifier(), sequenceConfig.getApplicationConfig
                                    ().isUseTenantDomainInLocalSubjectIdentifier()));
                }

            }

            sequenceConfig.getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);

            request.setAttribute(FrameworkConstants.MAPPED_ATTRIBUTES, mappedAttrs);
        }

        handlePostAuthenticateExtensions(request, response, context);
    }

    /**
     * @param sequenceConfig
     * @param locallyMappedUserRoles
     * @return
     */
    protected String getServiceProviderMappedUserRoles(SequenceConfig sequenceConfig,
                                                       List<String> locallyMappedUserRoles) throws FrameworkException {

        if (locallyMappedUserRoles != null && !locallyMappedUserRoles.isEmpty()) {

            Map<String, String> localToSpRoleMapping = sequenceConfig.getApplicationConfig()
                    .getRoleMappings();

            boolean roleMappingDefined = false;

            if (localToSpRoleMapping != null && !localToSpRoleMapping.isEmpty()) {
                roleMappingDefined = true;
            }

            StringBuilder spMappedUserRoles = new StringBuilder();

            for (String role : locallyMappedUserRoles) {
                if (roleMappingDefined) {
                    if (localToSpRoleMapping.containsKey(role)) {
                        spMappedUserRoles.append(localToSpRoleMapping.get(role) + ",");
                    } else {
                        spMappedUserRoles.append(role + ",");
                    }
                } else {
                    spMappedUserRoles.append(role + ",");
                }
            }

            return spMappedUserRoles.length() > 0 ? spMappedUserRoles.toString().substring(0,
                                                                                           spMappedUserRoles.length() - 1) : null;
        }

        return null;
    }

    /**
     * @param appConfig
     * @return
     */
    protected String getSpRoleClaimUri(ApplicationConfig appConfig) throws FrameworkException {
        // get external identity provider role claim uri.
        String spRoleClaimUri = appConfig.getRoleClaim();

        if (spRoleClaimUri == null || spRoleClaimUri.isEmpty()) {
            // no role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            Map<String, String> spToLocalClaimMapping = appConfig.getClaimMappings();

            if (spToLocalClaimMapping != null && !spToLocalClaimMapping.isEmpty()) {

                for (Entry<String, String> entry : spToLocalClaimMapping.entrySet()) {
                    if (FrameworkConstants.LOCAL_ROLE_CLAIM_URI.equals(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }

        return spRoleClaimUri;
    }

    /**
     * @param externalIdPConfig
     * @return
     */
    protected String getIdpRoleClaimUri(ExternalIdPConfig externalIdPConfig)
            throws FrameworkException {
        // get external identity provider role claim uri.
        String idpRoleClaimUri = externalIdPConfig.getRoleClaimUri();

        if (idpRoleClaimUri == null || idpRoleClaimUri.isEmpty()) {
            // no role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            ClaimMapping[] idpToLocalClaimMapping = externalIdPConfig.getClaimMappings();

            if (idpToLocalClaimMapping != null && idpToLocalClaimMapping.length > 0) {

                for (ClaimMapping mapping : idpToLocalClaimMapping) {
                    if (FrameworkConstants.LOCAL_ROLE_CLAIM_URI.equals(
                            mapping.getLocalClaim().getClaimUri()) && mapping.getRemoteClaim() != null) {
                        return mapping.getRemoteClaim().getClaimUri();
                    }
                }
            }
        }

        return idpRoleClaimUri;
    }

    /**
     * @param sequenceConfig
     * @param externalIdPConfig
     * @param extAttributesValueMap
     * @return
     */
    protected List<String> getLocallyMappedUserRoles(SequenceConfig sequenceConfig,
                                                     ExternalIdPConfig externalIdPConfig,
                                                     Map<String, String> extAttributesValueMap,
                                                     String idpRoleClaimUri) throws FrameworkException {


        if (idpRoleClaimUri == null) {
            // we cannot do role mapping.
            log.debug("Role claim uri not found for the identity provider");
            return new ArrayList<>();
        }

        String idpRoleAttrValue = extAttributesValueMap.get(idpRoleClaimUri);

        String[] idpRoles;

        if (idpRoleAttrValue != null) {
            idpRoles = idpRoleAttrValue.split(",");
        } else {
            // no identity provider role values found.
            return new ArrayList<>();
        }

        // identity provider role to local role.
        Map<String, String> idpToLocalRoleMapping = externalIdPConfig.getRoleMappings();

        boolean roleMappingDefined = false;

        if (idpToLocalRoleMapping != null && !idpToLocalRoleMapping.isEmpty()) {
            // if no role mapping defined in the identity provider configuration
            // - we will just
            // pass-through the roles.
            roleMappingDefined = true;
        }

        List<String> locallyMappedUserRoles = new ArrayList<>();

        if (idpRoles.length > 0) {
            for (String idpRole : idpRoles) {
                if (roleMappingDefined) {
                    if (idpToLocalRoleMapping.containsKey(idpRole)) {
                        locallyMappedUserRoles.add(idpToLocalRoleMapping.get(idpRole));
                    } else {
                        locallyMappedUserRoles.add(idpRole);
                    }
                } else {
                    locallyMappedUserRoles.add(idpRole);
                }
            }
        }

        return locallyMappedUserRoles;
    }

    /**
     * @param stepConfig
     * @param context
     * @param extAttrs
     * @param isFederatedClaims
     * @return
     */
    protected Map<String, String> handleClaimMappings(StepConfig stepConfig,
                                                      AuthenticationContext context, Map<String, String> extAttrs,
                                                      boolean isFederatedClaims)
            throws FrameworkException {

        Map<String, String> mappedAttrs = new HashMap<String, String>();

        try {
            mappedAttrs = FrameworkUtils.getClaimHandler().handleClaimMappings(stepConfig, context,
                                                                               extAttrs, isFederatedClaims);
        } catch (FrameworkException e) {
            log.error("Claim handling failed!", e);
        }
        if(mappedAttrs == null){
            mappedAttrs = new HashMap<>();
        }
        return mappedAttrs;
    }

    /**
     * @param context
     * @param mappedRoles
     * @param extAttributesValueMap
     */
    protected void handleJitProvisioning(String subjectIdentifier, AuthenticationContext context,
                                         List<String> mappedRoles, Map<String, String> extAttributesValueMap)
            throws FrameworkException {

        try {
            @SuppressWarnings("unchecked")
            String userStoreDomain = null;
            String provisioningClaimUri = context.getExternalIdP()
                    .getProvisioningUserStoreClaimURI();
            String provisioningUserStoreId = context.getExternalIdP().getProvisioningUserStoreId();

            if (provisioningUserStoreId != null) {
                userStoreDomain = provisioningUserStoreId;
            } else if (provisioningClaimUri != null) {
                userStoreDomain = extAttributesValueMap.get(provisioningClaimUri);
            }

            // setup thread local variable to be consumed by the provisioning
            // framework.
            ThreadLocalProvisioningServiceProvider serviceProvider = new ThreadLocalProvisioningServiceProvider();
            serviceProvider.setServiceProviderName(context.getSequenceConfig()
                                                           .getApplicationConfig().getApplicationName());
            serviceProvider.setJustInTimeProvisioning(true);
            serviceProvider.setClaimDialect(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
            serviceProvider.setTenantDomain(context.getTenantDomain());
            IdentityApplicationManagementUtil
                    .setThreadLocalProvisioningServiceProvider(serviceProvider);

            FrameworkUtils.getProvisioningHandler().handle(mappedRoles, subjectIdentifier,
                                                           extAttributesValueMap, userStoreDomain, context.getTenantDomain());

        } catch (FrameworkException e) {
            log.error("User provisioning failed!", e);
        } finally {
            IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();
        }
    }

    protected void resetAuthenticationContext(AuthenticationContext context)
            throws FrameworkException {

        context.setSubject(null);
        context.setStateInfo(null);
        context.setExternalIdP(null);
        context.setAuthenticatorProperties(new HashMap<String, String>());
        context.setRetryCount(0);
        context.setRetrying(false);
        context.setCurrentAuthenticator(null);
    }

    protected boolean isPostAuthenticateExtensionTriggered(AuthenticationContext context) {

        Object object = context.getProperty(FrameworkConstants.REQUEST_MISSING_CLAIMS_TRIGGERED);
        if (object != null) {
            return (Boolean) object;
        } else {
            return false;
        }
    }

    private String getMissingClaims(Map<String,String> mappedAttrs, Map<String,String> requestedClaims) {

        String missingClaims = "";
        for (Map.Entry<String, String> entry : requestedClaims.entrySet())
        {
            if (mappedAttrs.get(entry.getKey()) == null){
                if (StringUtils.isNotBlank(missingClaims)) {
                    missingClaims += ",";
                }
                missingClaims += entry.getKey();
            }
        }
        return missingClaims;
    }

    private UserRealm getUserRealm(String tenantDomain) throws FrameworkException {
        UserRealm realm;
        try {
            realm = AnonymousSessionUtil.getRealmByTenantDomain(
                    FrameworkServiceComponent.getRegistryService(),
                    FrameworkServiceComponent.getRealmService(), tenantDomain);
        } catch (CarbonException e) {
            throw new FrameworkException("Error occurred while retrieving the Realm for " +
                    tenantDomain + " to handle local claims", e);
        }
        return realm;
    }

    private void handleIncompletePostAuthenticationTasks(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws FrameworkException {

        Map<String,String> mandatoryClaims =
                context.getSequenceConfig().getApplicationConfig().getMandatoryClaimMappings();
        Object object = request.getAttribute(FrameworkConstants.MAPPED_ATTRIBUTES);
        Map<String, String> mappedAttrs = (Map<String, String>) object;
        String missingClaims = getMissingClaims(mappedAttrs, mandatoryClaims);

        if (StringUtils.isNotBlank(missingClaims)) {
            //need to request for the missing claims before completing authentication
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
            context.getSequenceConfig().setCompleted(false);
            context.setProperty(FrameworkConstants.REQUEST_MISSING_CLAIMS_TRIGGERED, true);

            try {
                String queryString = FrameworkConstants.MISSING_CLAIMS + "=" + missingClaims;
                queryString += "&" + FrameworkConstants.SESSION_DATA_KEY + "=" + context.getContextIdentifier();
                queryString += "&" + "spName" + "=" + context.getSequenceConfig().getApplicationConfig().getApplicationName();
                //queryString = URLEncoder.encode(queryString);
                response.sendRedirect("/authenticationendpoint/claims.do?" + queryString);
            } catch (IOException e) {
                throw new FrameworkException("Error while redirecting to request claims", e);
            }
        }
    }

    protected void handlePostAuthenticateExtensions(HttpServletRequest request, HttpServletResponse response,
                                                    AuthenticationContext context) throws FrameworkException {

        // if post authentication extension for missing claims is not already triggered, trigger it
        if (!isPostAuthenticateExtensionTriggered(context)) {
            handleIncompletePostAuthenticationTasks(request, response, context);
        } else {
            handleResponseWithMissingClaims(request, response, context);
        }
    }

}
