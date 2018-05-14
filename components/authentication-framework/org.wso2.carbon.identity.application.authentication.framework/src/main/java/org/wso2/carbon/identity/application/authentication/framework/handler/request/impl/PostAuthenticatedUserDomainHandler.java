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
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This PostAuthenticationHandler handles user domin related post processing.
 */
public class PostAuthenticatedUserDomainHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(PostAuthenticatedUserDomainHandler.class);
    private static PostAuthenticatedUserDomainHandler instance;

    public static PostAuthenticatedUserDomainHandler getInstance() {

        if (instance == null) {
            synchronized (PostAuthenticatedUserDomainHandler.class) {
                if (instance == null) {
                    instance = new PostAuthenticatedUserDomainHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public int getPriority() {

        return 22;
    }

    @Override
    public String getName() {

        return "PostAuthenticatedUserDomainHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
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
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }
}
