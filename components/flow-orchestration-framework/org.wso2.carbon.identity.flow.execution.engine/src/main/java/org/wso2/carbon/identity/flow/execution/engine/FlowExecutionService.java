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

package org.wso2.carbon.identity.flow.execution.engine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.REGISTRATION_FLOW_TYPE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;

/**
 * Service class to handle the user flow.
 */
public class FlowExecutionService {

    private static final Log LOG = LogFactory.getLog(FlowExecutionService.class);
    private static final FlowExecutionService instance = new FlowExecutionService();

    private FlowExecutionService() {

    }

    public static FlowExecutionService getInstance() {

        return instance;
    }

    /**
     * Execute the flow for the given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @param applicationId Application ID.
     * @param flowId        Flow ID.
     * @param actionId      Action ID.
     * @param inputs        User inputs.
     * @return ExecutionState.
     * @throws FlowEngineException If something goes wrong while executing the flow.
     */
    public FlowExecutionStep executeFlow(String tenantDomain, String applicationId, String flowId, String actionId,
                                         String flowType, Map<String, String> inputs) throws FlowEngineException {

        FlowExecutionStep step;
        FlowExecutionContext context = null;
        try {
            if (StringUtils.isBlank(flowId)) {
                // No flowId present hence initiate the flow.
                context = FlowExecutionEngineUtils.initiateContext(tenantDomain, applicationId, flowType);
            } else {
                context = FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowType, flowId);
            }

            if (inputs != null) {
                context.getUserInputData().putAll(inputs);
            }
            
            context.setCurrentActionId(actionId);
            for (FlowExecutionListener listener :
                    FlowExecutionEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPreExecute(context)) {
                    return null;
                }
            }
            step = FlowExecutionEngine.getInstance().execute(context);
            for (FlowExecutionListener listener :
                    FlowExecutionEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPostExecute(step, context)) {
                    return null;
                }
            }
            if (STATUS_COMPLETE.equals(step.getFlowStatus())) {

                if (REGISTRATION_FLOW_TYPE.equals(context.getFlowType())) {
                    Map<String, String> userClaims =
                            context.getFlowUser() != null ? context.getFlowUser().getClaims() : null;
                    FrameworkUtils.publishEventOnUserRegistrationSuccess(userClaims, tenantDomain);
                }

                FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
            } else {
                FlowExecutionEngineUtils.addFlowContextToCache(context);
            }
            return step;
        } catch (FlowEngineException e) {

            if (context != null && REGISTRATION_FLOW_TYPE.equals(context.getFlowType())) {
                Map<String, String> userClaims =
                        context.getFlowUser() != null ? context.getFlowUser().getClaims() : null;
                FrameworkUtils.publishEventOnUserRegistrationFailure(e.getErrorCode(), e.getDescription(),
                        userClaims, tenantDomain);
            }

            FlowExecutionEngineUtils.rollbackContext(flowType, flowId);
            FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
            throw e;
        }
    }
}
