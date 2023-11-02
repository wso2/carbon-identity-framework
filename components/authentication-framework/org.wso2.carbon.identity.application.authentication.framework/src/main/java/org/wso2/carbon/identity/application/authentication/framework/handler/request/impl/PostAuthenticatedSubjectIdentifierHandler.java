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
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;

/**
 * This PostAuthenticationHandler is responsible for setting subject identifier related with authenticated user.
 */
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

            /**
             * Generally SAAS applications (Console and MyAccount) are having a subject claim uri setup at SP_APP.
             * The "if" segment will handle the subject identifier for those applications which distinguish the
             * database scheme at the end.
             *
             * Since the normal business applications does not have a subject claim uri setup at SP_APP, the "else"
             */

            if (StringUtils.isNotBlank(subjectClaimURI) && StringUtils.isNotBlank(subjectValue)) {
                handleUserStoreAndTenantDomain(sequenceConfig, subjectValue);
            } else if (FrameworkUtils.isUserIdFoundAmongClaims(context.getExternalIdP(),
                    sequenceConfig.getStepMap().get(context.getCurrentStep()).getAuthenticatedAutenticator()
                            .getName())) {
                handleUserStoreAndTenantDomain(sequenceConfig, sequenceConfig.getAuthenticatedUser()
                        .getAuthenticatedSubjectIdentifier());
            } else {
                log.warn("Subject claim could not be found.");
                setAuthenticatedSubjectIdentifierBasedOnUserId(sequenceConfig);
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
    private void setAuthenticatedSubjectIdentifierBasedOnUserId(SequenceConfig sequenceConfig)
            throws UserIdNotFoundException {

        boolean isUserstoreDomainInLocalSubjectIdentifier = sequenceConfig.getApplicationConfig()
                .isUseUserstoreDomainInLocalSubjectIdentifier();
        boolean isUseTenantDomainInLocalSubjectIdentifier = sequenceConfig.getApplicationConfig()
                .isUseTenantDomainInLocalSubjectIdentifier();

        boolean useUserIdForDefaultSubject = sequenceConfig.getApplicationConfig().isUseUserIdForDefaultSubject();
        if (useUserIdForDefaultSubject) {
            String authenticatedUserId = sequenceConfig.getAuthenticatedUser().getUserId();

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

}
