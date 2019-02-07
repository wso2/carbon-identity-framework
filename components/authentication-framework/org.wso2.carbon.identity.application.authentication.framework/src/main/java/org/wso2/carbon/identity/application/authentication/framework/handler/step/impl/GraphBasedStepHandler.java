/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link DefaultStepHandler} does not set the {@link AuthenticatorFlowStatus} during an authentication failure.
 * Also the default step handler does not have context information such as the user who attempted the authentication.
 * These two concerns needs to be addressed for {@link GraphBasedSequenceHandler} to the provide the capability perform
 * actions on an authentication failure in a step.
 * <p>
 * This extended step handler is written to solve the above concerns whilst preserving backward compatibility for anyone
 * who has extended the {@link DefaultStepHandler}
 */
public class GraphBasedStepHandler extends DefaultStepHandler implements StepHandler {

    private static final Log log = LogFactory.getLog(GraphBasedStepHandler.class);

    @Override
    protected void handleFailedAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationContext context, AuthenticatorConfig authenticatorConfig,
                                              User user) {

        super.handleFailedAuthentication(request, response, context, authenticatorConfig, user);

        if (user != null) {
            AuthenticatedUser lastAttemptedUser = buildAuthenticatedUser(user);
            context.setProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER, lastAttemptedUser);
            if (log.isDebugEnabled()) {
                log.debug("Last attempted user : " + lastAttemptedUser.toFullQualifiedUsername() + " is set in the " +
                        "authentication context for failed login attempt to service provider: " +
                        context.getServiceProviderName());
            }
        }

        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.FAIL_COMPLETED);
        if (log.isDebugEnabled()) {
            log.debug("Authentication flow status set to '" + AuthenticatorFlowStatus.FAIL_COMPLETED + "' for " +
                    "authentication attempt made to service provider: " + context.getServiceProviderName());
        }
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName(user.getUserName());
        authenticatedUser.setTenantDomain(user.getTenantDomain());
        authenticatedUser.setUserStoreDomain(user.getUserStoreDomain());
        return authenticatedUser;
    }
}
