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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler.USER_TENANT_DOMAIN;

public class PostAuthAssociationHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(PostAuthAssociationHandler.class);
    private static PostAuthAssociationHandler instance;

    public static PostAuthAssociationHandler getInstance() {

        if (instance == null) {
            synchronized (PostAuthAssociationHandler.class) {
                if (instance == null) {
                    instance = new PostAuthAssociationHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public int getPriority() {

        return 21;
    }

    @Override
    public String getName() {

        return "PostAuthAssociationHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws PostAuthenticationFailedException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        Map<ClaimMapping, String> authenticatedUserAttributes = null;

        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                Map<String, String> localClaimValues;

                ExternalIdPConfig externalIdPConfig = null;
                try {
                    externalIdPConfig = ConfigurationFacade.getInstance()
                            .getIdPConfigByName(stepConfig.getAuthenticatedIdP(), context.getTenantDomain());
                } catch (IdentityProviderManagementException e) {
                    log.error("Exception while getting IdP by name", e);
                }

                context.setExternalIdP(externalIdPConfig);

                String originalExternalIdpSubjectValueForThisStep = stepConfig.getAuthenticatedUser()
                        .getAuthenticatedSubjectIdentifier();

                if (stepConfig.isSubjectIdentifierStep()) {
                    // there can be only step for subject attributes.
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
                                    log.debug("User " + stepConfig.getAuthenticatedUser()
                                            + " has an associated account as " + associatedID + ". Hence continuing as "
                                            + associatedID);
                                }
                                stepConfig.getAuthenticatedUser().setUserName(associatedID);
                                stepConfig.getAuthenticatedUser().setTenantDomain(context.getTenantDomain());
                                stepConfig.setAuthenticatedUser(stepConfig.getAuthenticatedUser());
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug(
                                            "User " + stepConfig.getAuthenticatedUser() + " doesn't have an associated"
                                                    + " account. Hence continuing as the same user.");
                                }
                            }
                        } catch (UserProfileException e) {
                            throw new PostAuthenticationFailedException(
                                    FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_LOCAL_USER_ID.getCode(),
                                    String.format(FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_IDP_BY_NAME
                                            .getMessage(), originalExternalIdpSubjectValueForThisStep), e);
                        } finally {
                            // end tenant flow
                            FrameworkUtils.endTenantFlow();
                        }
                    }

                    if (associatedID != null && associatedID.trim().length() > 0) {
                        // we found an associated user identifier
                        // build the full qualified user id for the associated user
                        String fullQualifiedAssociatedUserId = FrameworkUtils.prependUserStoreDomainToName(
                                associatedID + UserCoreConstants.TENANT_DOMAIN_COMBINER + context.getTenantDomain());
                        sequenceConfig.setAuthenticatedUser(AuthenticatedUser
                                .createLocalAuthenticatedUserFromSubjectIdentifier(fullQualifiedAssociatedUserId));

                        sequenceConfig.getApplicationConfig().setMappedSubjectIDSelected(true);

                        // if we found a local mapped user - then we will also take attributes from
                        // that user - this will load local claim values for the user.
                        Map<String, String> mappedAttrs = null;
                        try {
                            mappedAttrs = FrameworkUtils.getClaimHandler()
                                    .handleClaimMappings(stepConfig, context, null, false);
                        } catch (FrameworkException e) {
                            throw new PostAuthenticationFailedException(FrameworkErrorConstants.ErrorMessages.
                                    ERROR_WHILE_GETTING_CLAIM_MAPPINGS.getCode(),
                                    String.format(FrameworkErrorConstants.ErrorMessages.
                                                    ERROR_WHILE_GETTING_CLAIM_MAPPINGS.getMessage(),
                                            sequenceConfig.getAuthenticatedUser().getUserName()), e);
                        }

                        localClaimValues = (Map<String, String>) context
                                .getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);

                        Map<String, String> idpClaimValues = (Map<String, String>) context
                                .getProperty(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES);

                        // if no requested claims are selected, send all local mapped claim values or idp claim values
                        if (context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings() == null
                                || context.getSequenceConfig().getApplicationConfig().getRequestedClaimMappings()
                                .isEmpty()) {

                            if (MapUtils.isNotEmpty(localClaimValues)) {
                                mappedAttrs = localClaimValues;
                            } else if (MapUtils.isNotEmpty(idpClaimValues)) {
                                mappedAttrs = idpClaimValues;
                            }
                        }

                        if (mappedAttrs != null) {
                            authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(mappedAttrs);
                        }

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
                            log.debug("Authenticated User: " + sequenceConfig.getAuthenticatedUser()
                                    .getAuthenticatedSubjectIdentifier());
                            log.debug("Authenticated User Tenant Domain: " + tenantDomain);
                        }

                    } else {

                        sequenceConfig.setAuthenticatedUser(new AuthenticatedUser(stepConfig.getAuthenticatedUser()));

                        // Only place we do not set the setAuthenticatedUserTenantDomain into the sequenceConfig
                        // TODO : Check whether not setting setAuthenticatedUserTenantDomain is correct

                    }

                }

            }

        }
        if (authenticatedUserAttributes != null) {
            sequenceConfig.getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }
}
