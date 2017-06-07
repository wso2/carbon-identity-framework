/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationStepsSelector;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Step handler performs adaptive authentication.
 * Selects next step based on Authentication Class References.
 *
 */
public class AdaptiveStepHandler extends DefaultStepHandler implements StepHandler {

    private static final Log log = LogFactory.getLog(AdaptiveStepHandler.class);

    private AuthenticationStepsSelector authenticationStepsSelector;

    public AdaptiveStepHandler(AuthenticationStepsSelector authenticationStepsSelector) {
        this.authenticationStepsSelector = authenticationStepsSelector;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {
        super.handle(request, response, context);
    }

    @Override
    protected void doAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, AuthenticatorConfig authenticatorConfig) throws FrameworkException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        int currentStep = context.getCurrentStep();
        StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
        ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

        if (authenticator == null) {
            log.error("Authenticator is null on the step: " + currentStep + " for Application: " + sequenceConfig
                    .getApplicationId());
            return;
        }

        if (authenticationStepsSelector.isAuthenticationSatisfied(authenticatorConfig, context)) {
            stepConfig.setCompleted(true);
        } else {
            super.doAuthentication(request, response, context, authenticatorConfig);
        }
    }
}
