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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.ALLOW_AUTHENTICATED_SUB_UPDATE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.PROP_USERNAME_UPDATED_EXTERNALLY;

/**
 * This PostAuthenticationHandler is responsible for setting subject identifier related with authenticated user.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework.handler.request." +
                        "PostAuthenticationHandler",
                "service.scope=singleton"
        }
)
public class PostAuthenticatedSubjectIdentifierHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(PostAuthenticatedSubjectIdentifierHandler.class);
    private static PostAuthenticatedSubjectIdentifierHandler instance = new PostAuthenticatedSubjectIdentifierHandler();

    /**
     * To avoid creation of multiple instances of this handler.
     */
    protected PostAuthenticatedSubjectIdentifierHandler() { }


    /**
     * To get the singleton instance of post authentication handler.
     *
     * @return instance of {@link PostAuthenticatedSubjectIdentifierHandler}.
     */
    public static PostAuthenticatedSubjectIdentifierHandler getInstance() {

        return instance;
    }

    @Override
    public int getPriority() {

        int priority = super.getPriority();
        if (priority == -1) {
           /*
        The priority of this authentication handler should be greater than JIT and association post authentication
        handler as subject identifier need to be set after all these handling.
         */
            priority = 30;
        }
        return priority;
    }

    @Override
    public String getName() {

        return "PostAuthenticatedSubjectIdentifierHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) {

        if (!FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)) {
            return SUCCESS_COMPLETED;
        }
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        String subjectClaimURI = sequenceConfig.getApplicationConfig().getSubjectClaimUri();
        String subjectValue = (String) context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE);
        try {
            if (StringUtils.isNotBlank(subjectClaimURI)) {
                if (subjectValue != null) {
                    if (FrameworkConstants.USER_ID_CLAIM.equals(subjectClaimURI) && StringUtils.equals(subjectValue,
                            sequenceConfig.getAuthenticatedUser().getUserId())) {
                        String sharedUserId =
                                getSharedUserIdForSubject(sequenceConfig.getAuthenticatedUser(), context);
                        if (StringUtils.isNotEmpty(sharedUserId)) {
                            subjectValue = sharedUserId;
                        }
                    }
                    handleUserStoreAndTenantDomain(sequenceConfig, subjectValue);
                } else {
                    log.warn("Subject claim could not be found. Defaulting to Name Identifier.");
                    setAuthenticatedSubjectIdentifierBasedOnUserId(sequenceConfig, context);
                }
            } else {
                setAuthenticatedSubjectIdentifierBasedOnUserId(sequenceConfig, context);
            }
        } catch (UserIdNotFoundException e) {
            return UNSUCCESS_COMPLETED;
        }
        return SUCCESS_COMPLETED;
    }

    /**
     * Handle userstore domain and tenant domain with subjects identifier.
     *
     * @param sequenceConfig Relevant sequence config.
     * @param subjectValue   Subject value.
     */
    private void handleUserStoreAndTenantDomain(SequenceConfig sequenceConfig, String subjectValue) {

        sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);
        /* Check whether the tenant domain should be appended to the subject identifier for this SP and if yes,
         append it. */
        if (sequenceConfig.getApplicationConfig().isUseTenantDomainInLocalSubjectIdentifier()) {
            String tenantDomain = sequenceConfig.getAuthenticatedUser().getTenantDomain();
            subjectValue = UserCoreUtil.addTenantDomainToEntry(subjectValue, tenantDomain);
            sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);
        }
        /* Check whether the user store domain should be appended to the subject identifier for this SP and
         if yes, append it. */
        if (sequenceConfig.getApplicationConfig().isUseUserstoreDomainInLocalSubjectIdentifier()) {
            String userStoreDomain = sequenceConfig.getAuthenticatedUser().getUserStoreDomain();
            subjectValue = UserCoreUtil.addDomainToName(subjectValue, userStoreDomain);
            sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(subjectValue);
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Authenticated User: " + sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
            log.debug("Authenticated User Tenant Domain: " + sequenceConfig.getAuthenticatedUser().getTenantDomain());
        }
    }

    /**
     * To set authenticated subject identifier based on user id.
     *
     * @param sequenceConfig Relevant sequence config.
     */
    private void setAuthenticatedSubjectIdentifierBasedOnUserId(SequenceConfig sequenceConfig,
                                                                AuthenticationContext context)
            throws UserIdNotFoundException {

        boolean isUserstoreDomainInLocalSubjectIdentifier = sequenceConfig.getApplicationConfig()
                .isUseUserstoreDomainInLocalSubjectIdentifier();
        boolean isUseTenantDomainInLocalSubjectIdentifier = sequenceConfig.getApplicationConfig()
                .isUseTenantDomainInLocalSubjectIdentifier();

        boolean useUserIdForDefaultSubject = sequenceConfig.getApplicationConfig().isUseUserIdForDefaultSubject();
        if (Boolean.parseBoolean(IdentityUtil.getProperty(ALLOW_AUTHENTICATED_SUB_UPDATE)) &&
                Boolean.parseBoolean((String) context.getProperty(PROP_USERNAME_UPDATED_EXTERNALLY))) {
            String authenticatedUserName = sequenceConfig.getAuthenticatedUser().getUserName();

            if (StringUtils.isNotEmpty(authenticatedUserName)) {
                sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(
                        sequenceConfig.getAuthenticatedUser()
                                .getUsernameAsSubjectIdentifier(isUserstoreDomainInLocalSubjectIdentifier,
                                        isUseTenantDomainInLocalSubjectIdentifier));
            }
        } else if (useUserIdForDefaultSubject) {
            String authenticatedUserId = sequenceConfig.getAuthenticatedUser().getUserId();
            String sharedUserId = getSharedUserIdForSubject(sequenceConfig.getAuthenticatedUser(), context);
            if (StringUtils.isNotEmpty(sharedUserId)) {
                authenticatedUserId = sharedUserId;
            }

            if (StringUtils.isNotEmpty(authenticatedUserId)) {
                if (isUseTenantDomainInLocalSubjectIdentifier) {
                    authenticatedUserId = UserCoreUtil.addTenantDomainToEntry(authenticatedUserId,
                            sequenceConfig.getAuthenticatedUser().getTenantDomain());
                }
                if (isUserstoreDomainInLocalSubjectIdentifier) {
                    authenticatedUserId = UserCoreUtil.addDomainToName(authenticatedUserId,
                            sequenceConfig.getAuthenticatedUser().getUserStoreDomain());
                }

                sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(authenticatedUserId);
            }
        } else {
            String authenticatedUserName = sequenceConfig.getAuthenticatedUser().getUserName();

            if (StringUtils.isNotEmpty(authenticatedUserName)) {
                sequenceConfig.getAuthenticatedUser().setAuthenticatedSubjectIdentifier(
                        sequenceConfig.getAuthenticatedUser()
                                .getUsernameAsSubjectIdentifier(isUserstoreDomainInLocalSubjectIdentifier,
                                        isUseTenantDomainInLocalSubjectIdentifier));
            }
        }
    }

    /**
     * Get the shared user id of the accessing organization to use as the subject identifier, if the authenticated
     * user is a shared user and the accessing organization enables the shared user subject compatibility setting.
     * Only the subject identifier changes. The authenticated user keeps the resident user details.
     *
     * @param authenticatedUser Authenticated user.
     * @param context           Authentication context.
     * @return Shared user id, or null if the resident user id must be used.
     */
    private String getSharedUserIdForSubject(AuthenticatedUser authenticatedUser, AuthenticationContext context) {

        if (!authenticatedUser.isSharedUser() || authenticatedUser.isFederatedUser()) {
            return null;
        }
        String accessingOrganization = authenticatedUser.getAccessingOrganization();
        if (StringUtils.isBlank(accessingOrganization) ||
                StringUtils.equals(accessingOrganization, authenticatedUser.getUserResidentOrganization())) {
            return null;
        }
        if (!FrameworkUtils.isSharedUserIdAsSubjectEnabled(context.getTenantDomain())) {
            return null;
        }
        try {
            String sharedUserId = FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(authenticatedUser);
            if (StringUtils.isBlank(sharedUserId) && log.isDebugEnabled()) {
                log.debug("Shared user id could not be resolved for the user: " +
                        authenticatedUser.getLoggableMaskedUserId() + " in the organization: " +
                        accessingOrganization + ". Resident user id will be used as the subject identifier.");
            }
            return sharedUserId;
        } catch (OrganizationManagementException e) {
            log.error("Error while resolving the shared user id of the user: " +
                    authenticatedUser.getLoggableMaskedUserId() + " in the organization: " + accessingOrganization +
                    ". Resident user id will be used as the subject identifier.", e);
            return null;
        }
    }
}
