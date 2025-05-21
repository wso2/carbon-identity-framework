/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngine;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils;

import java.util.Map;

import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_COMPLETE;

/**
 * Service class to handle the user registration flow.
 */
public class UserRegistrationFlowService {

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowService.class);
    private static final UserRegistrationFlowService instance = new UserRegistrationFlowService();

    private UserRegistrationFlowService() {

    }

    public static UserRegistrationFlowService getInstance() {

        return instance;
    }

    /**
     * Initiates the registration flow for the given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @param callbackUrl   Callback URL.
     * @param applicationId Application ID.
     * @return RegistrationStep.
     * @throws RegistrationEngineException if something goes wrong while initiating the registration flow.
     */
    public RegistrationStep initiateDefaultRegistrationFlow(String tenantDomain, String applicationId,
                                                            String callbackUrl) throws RegistrationEngineException {

        try {
            RegistrationContext context =
                    RegistrationFlowEngineUtils.initiateContext(tenantDomain, callbackUrl, applicationId);
            for (FlowExecutionListener listener :
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationExecutionListeners()) {
                if (listener.isEnabled() && !listener.doPreInitiate(context)) {
                    return null;
                }
            }
            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);
            for (FlowExecutionListener listener :
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationExecutionListeners()) {
                if (listener.isEnabled() && !listener.doPostInitiate(step, context)) {
                    return null;
                }
            }
            RegistrationFlowEngineUtils.addRegContextToCache(context);
            return step;
        } catch (RegistrationEngineException e) {
            RegistrationFlowEngineUtils.removeRegContextFromCache(tenantDomain);
            throw e;
        }
    }

    /**
     * Continues the registration flow for the given flow ID with the provided user inputs.
     *
     * @param flowId Flow ID.
     * @param inputs User inputs.
     * @return ExecutionState.
     * @throws RegistrationEngineException if something goes wrong while continuing the registration flow.
     */
    public RegistrationStep continueFlow(String flowId, String actionId, Map<String, String> inputs)
            throws RegistrationEngineException {

        try {
            RegistrationContext context = RegistrationFlowEngineUtils.retrieveRegContextFromCache(flowId);
            context.getUserInputData().putAll(inputs);
            context.setCurrentActionId(actionId);
            for (FlowExecutionListener listener :
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationExecutionListeners()) {
                if (listener.isEnabled() && !listener.doPreContinue(context)) {
                    return null;
                }
            }
            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);
            for (FlowExecutionListener listener :
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationExecutionListeners()) {
                if (listener.isEnabled() && !listener.doPostContinue(step, context)) {
                    return null;
                }
            }
            if (STATUS_COMPLETE.equals(step.getFlowStatus())) {
                RegistrationFlowEngineUtils.removeRegContextFromCache(flowId);
            } else {
                RegistrationFlowEngineUtils.addRegContextToCache(context);
            }
            return step;
        } catch (RegistrationEngineException e) {
            RegistrationFlowEngineUtils.removeRegContextFromCache(flowId);
            throw e;
        }
    }
}
