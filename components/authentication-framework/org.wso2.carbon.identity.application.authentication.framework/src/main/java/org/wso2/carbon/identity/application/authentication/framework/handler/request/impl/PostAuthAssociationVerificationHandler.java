/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_GETTING_ASSOCIATION_FOR_USER;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_NO_ASSOCIATED_LOCAL_USER_FOUND;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS;

/**
 * Post authentication handler to handle the association of federated user with local user.
 */
public class PostAuthAssociationVerificationHandler extends AbstractPostAuthnHandler {

    private static final Log LOG = LogFactory.getLog(PostAuthAssociationVerificationHandler.class);
    private static final String SSO = "SSO";

    private static final PostAuthAssociationVerificationHandler instance = new PostAuthAssociationVerificationHandler();

    protected PostAuthAssociationVerificationHandler() {
    }

    public static PostAuthAssociationVerificationHandler getInstance() {

        return instance;
    }

    @Override
    public int getPriority() {

        int priority = super.getPriority();
        if (priority == -1) {
            // This should execute after JIT provisioning handler.
            priority = 40;
        }
        return priority;
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing PostAuthAssociationVerificationHandler");
        }

        // Skip if the user is not federated or the external IdP is Organization SSO authenticator.
        if (context.getExternalIdP() == null ||
                (context.getExternalIdP() != null &&
                        SSO.equals(context.getExternalIdP().getIdentityProvider().getIdentityProviderName()))) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        String tenantDomain = context.getTenantDomain();
        String spName = context.getServiceProviderName();

        try {
            AuthenticatedUser authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();
            ServiceProvider serviceProvider = FrameworkServiceDataHolder.getInstance().getApplicationManagementService()
                    .getServiceProvider(spName, tenantDomain);
            ClaimConfig serviceProviderClaimConfig = serviceProvider.getClaimConfig();
            UserLinkStrategy userLinkStrategy = resolveLocalUserLinkingStrategy(serviceProviderClaimConfig);
            if (userLinkStrategy == UserLinkStrategy.MANDATORY) {
                User localUser = FrameworkServiceDataHolder.getInstance()
                        .getFederatedAssociationManager().getAssociatedLocalUser(
                                tenantDomain,
                                context.getExternalIdP().getIdentityProvider().getResourceId(),
                                authenticatedUser.getUserName());
                if (localUser == null) {
                    throw new PostAuthenticationFailedException(ERROR_NO_ASSOCIATED_LOCAL_USER_FOUND.getErrorCode(),
                            "Federated user is not associated with any local user.");
                }
            }
        } catch (IdentityApplicationManagementException e) {
            throw new PostAuthenticationFailedException(ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS.getErrorCode(),
                    "Error while retrieving service provider.", e);
        } catch (FederatedAssociationManagerException e) {
            throw new PostAuthenticationFailedException(ERROR_GETTING_ASSOCIATION_FOR_USER.getErrorCode(),
                    "Error while retrieving federated associations.", e);
        }

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    /**
     * Method to get the assert local user behaviour based on the service provider claim configuration.
     *
     * @param claimConfig Claim configuration of the service provider.
     * @return Assert local user behaviour.
     */
    private static UserLinkStrategy resolveLocalUserLinkingStrategy(ClaimConfig claimConfig) {

        if (claimConfig == null) {
            return UserLinkStrategy.DISABLED;
        }

        if (claimConfig.isMappedLocalSubjectMandatory()) {
            return UserLinkStrategy.MANDATORY;
        } else if (claimConfig.isAlwaysSendMappedLocalSubjectId()) {
            return UserLinkStrategy.OPTIONAL;
        } else {
            return UserLinkStrategy.DISABLED;
        }
    }

    /**
     * Enum to represent the user link strategy.
     */
    public enum UserLinkStrategy {

        DISABLED,
        OPTIONAL,
        MANDATORY
    }
}


