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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
                        resetAuthenticationContext(context);
                        continue;
                    }
                }

                resetAuthenticationContext(context);
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

                    String idpRoleClaimUri = getIdpRoleClaimUri(externalIdPConfig);

                    // Get the mapped user roles according to the mapping in the IDP configuration.
                    // Include the unmapped roles as it is.
                    List<String> identityProviderMappedUserRolesUnmappedInclusive = getIdentityProvideMappedUserRoles(
                            externalIdPConfig, extAttibutesValueMap, idpRoleClaimUri, false);

                    String serviceProviderMappedUserRoles = getServiceProviderMappedUserRoles(sequenceConfig,
                            identityProviderMappedUserRolesUnmappedInclusive);
                    if (StringUtils.isNotBlank(idpRoleClaimUri)
                            && StringUtils.isNotBlank(serviceProviderMappedUserRoles)) {
                        extAttibutesValueMap.put(idpRoleClaimUri, serviceProviderMappedUserRoles);
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

                }

                // Do user provisioning if provisioning is enabled.
                // We should provision the user with the original external subject identifier.
                if (externalIdPConfig.isProvisioningEnabled()) {

                    if (localClaimValues == null) {
                        localClaimValues = new HashMap<>();
                    }

                    String idpRoleClaimUri = getIdpRoleClaimUri(externalIdPConfig);
                    Map<String, String> originalExternalAttributeValueMap = FrameworkUtils.getClaimMappings(
                            extAttrs, false);

                    // Get the mapped user roles according to the mapping in the IDP configuration.
                    // Exclude the unmapped from the returned list.
                    List<String> identityProviderMappedUserRolesUnmappedExclusive = getIdentityProvideMappedUserRoles(
                            externalIdPConfig, originalExternalAttributeValueMap, idpRoleClaimUri, true);

                    localClaimValues.put(FrameworkConstants.ASSOCIATED_ID, originalExternalIdpSubjectValueForThisStep);
                    localClaimValues.put(FrameworkConstants.IDP_ID, stepConfig.getAuthenticatedIdP());

                    handleJitProvisioning(originalExternalIdpSubjectValueForThisStep, context,
                            identityProviderMappedUserRolesUnmappedExclusive, localClaimValues);
                }

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

                            if (MapUtils.isNotEmpty(localClaimValues)) {
                                mappedAttrs = localClaimValues;
                            } else if (MapUtils.isNotEmpty(idpClaimValues)) {
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

                    if (!sequenceConfig.getApplicationConfig().isMappedSubjectIDSelected()) {
                        // if we found the mapped subject - then we do not need to worry about
                        // finding attributes.

                        // if no requested claims are selected, send all local mapped claim values or idp claim values
                        if (context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings() == null ||
                                context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings().isEmpty()) {

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

                    if (StringUtils.isNotBlank(roleAttr)) {
                        //Need to convert multiAttributeSeparator value into a regex literal before calling
                        // split function. Otherwise split can produce misleading results in case
                        // multiAttributeSeparator contains regex special meaning characters like .*
                        String[] roles = roleAttr.split(Pattern.quote(FrameworkUtils.getMultiAttributeSeparator()));
                        mappedAttrs.put(
                                spRoleUri,
                                getServiceProviderMappedUserRoles(sequenceConfig,
                                        Arrays.asList(roles))
                        );
                    }

                    authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                }
            }
        }

        String subjectClaimURI = sequenceConfig.getApplicationConfig().getSubjectClaimUri();
        String subjectValue = (String) context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE);
        if (StringUtils.isNotBlank(subjectClaimURI)) {
            if (subjectValue != null) {
                sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);

                // Check whether the tenant domain should be appended to the subject identifier for this SP and if yes,
                // append it.
                if (sequenceConfig.getApplicationConfig().isUseTenantDomainInLocalSubjectIdentifier()) {
                    String tenantDomain = sequenceConfig.getAuthenticatedUser().getTenantDomain();
                    subjectValue = UserCoreUtil.addTenantDomainToEntry(subjectValue, tenantDomain);
                    sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);
                }

                // Check whether the user store domain should be appended to the subject identifier for this SP and
                // if yes, append it.
                if (sequenceConfig.getApplicationConfig().isUseUserstoreDomainInLocalSubjectIdentifier()) {
                    String userStoreDomain = sequenceConfig.getAuthenticatedUser().getUserStoreDomain();
                    subjectValue = UserCoreUtil.addDomainToName(subjectValue, userStoreDomain);
                    sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);
                }

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
                        spRoleClaimUri = entry.getKey();
                        break;
                    }
                }
            }
        }

        if (StringUtils.isEmpty(spRoleClaimUri)) {
            spRoleClaimUri = FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
            if (log.isDebugEnabled()) {
                String serviceProvider = appConfig.getApplicationName();
                log.debug("Service Provider Role Claim URI not configured for SP: " + serviceProvider +
                        ". Defaulting to " + spRoleClaimUri);
            }
        }

        return spRoleClaimUri;
    }

    /**
     * @param externalIdPConfig
     * @return
     */
    protected String getIdpRoleClaimUri(ExternalIdPConfig externalIdPConfig) throws FrameworkException {
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
     * Map the external IDP roles to local roles.
     * If excludeUnmapped is true exclude unmapped roles.
     * Otherwise include unmapped roles as well.
     *
     * @param externalIdPConfig
     * @param extAttributesValueMap
     * @param idpRoleClaimUri
     * @param excludeUnmapped
     * @return ArrayList<string>
     */
    protected List<String> getIdentityProvideMappedUserRoles(ExternalIdPConfig externalIdPConfig,
                                                             Map<String, String> extAttributesValueMap,
                                                             String idpRoleClaimUri,
                                                             Boolean excludeUnmapped) throws FrameworkException {

        if (idpRoleClaimUri == null) {
            // Since idpRoleCalimUri is not defined cannot do role mapping.
            if (log.isDebugEnabled()) {
                log.debug("Role claim uri is not configured for the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }

        String idpRoleAttrValue = null;
        if (extAttributesValueMap != null) {
            idpRoleAttrValue = extAttributesValueMap.get(idpRoleClaimUri);
        }

        String[] idpRoles;
        if (idpRoleAttrValue != null) {
            idpRoles = idpRoleAttrValue.split(FrameworkUtils.getMultiAttributeSeparator());
        } else {
            // No identity provider role values found.
            if (log.isDebugEnabled()) {
                log.debug("No role attribute value has received from the external IDP: "
                        + externalIdPConfig.getIdPName() + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }

        Map<String, String> idpToLocalRoleMapping = externalIdPConfig.getRoleMappings();

        List<String> idpMappedUserRoles = new ArrayList<>();
        // If no role mapping is configured in the identity provider.
        if (MapUtils.isEmpty(idpToLocalRoleMapping)) {
            if (log.isDebugEnabled()) {
                log.debug("No role mapping is configured in the external IDP: "
                        + externalIdPConfig.getIdPName() + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }

            if (excludeUnmapped) {
                return new ArrayList<>();
            }

            idpMappedUserRoles.addAll(Arrays.asList(idpRoles));
            return idpMappedUserRoles;
        }

        for (String idpRole : idpRoles){
            if (idpToLocalRoleMapping.containsKey(idpRole)) {
                idpMappedUserRoles.add(idpToLocalRoleMapping.get(idpRole));
            } else if (!excludeUnmapped) {
                idpMappedUserRoles.add(idpRole);
            }
        }
        return idpMappedUserRoles;
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
            String provisioningClaimUri = context.getExternalIdP().getProvisioningUserStoreClaimURI();
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
            IdentityApplicationManagementUtil.setThreadLocalProvisioningServiceProvider(serviceProvider);

            FrameworkUtils.getProvisioningHandler().handle(mappedRoles, subjectIdentifier,
                    extAttributesValueMap, userStoreDomain, context.getTenantDomain());

        } catch (FrameworkException e) {
            log.error("User provisioning failed!", e);
        } finally {
            IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();
        }
    }

    /*
       TODO: This needs to be refactored so that there is a separate context object for each authentication step, rather than resetting.
        */
    protected void resetAuthenticationContext(AuthenticationContext context) throws FrameworkException {
        context.setSubject(null);
        context.setStateInfo(null);
        context.setExternalIdP(null);
        context.setAuthenticatorProperties(new HashMap<String, String>());
        context.setRetryCount(0);
        context.setRetrying(false);
        context.setCurrentAuthenticator(null);
    }

}