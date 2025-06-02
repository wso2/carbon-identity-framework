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

package org.wso2.carbon.identity.flow.engine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.engine.core.FlowEngine;
import org.wso2.carbon.identity.flow.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.engine.internal.FlowEngineDataHolder;
import org.wso2.carbon.identity.flow.engine.listener.FlowListener;
import org.wso2.carbon.identity.flow.engine.model.FlowContext;
import org.wso2.carbon.identity.flow.engine.model.FlowStep;
import org.wso2.carbon.identity.flow.engine.util.FlowEngineUtils;

import java.util.Map;
import java.util.Optional;

import static org.wso2.carbon.identity.flow.engine.Constants.STATUS_COMPLETE;

/**
 * Service class to handle the user flow.
 */
public class FlowService {

    private static final Log LOG = LogFactory.getLog(FlowService.class);
    private static final FlowService instance = new FlowService();

    private FlowService() {

    }

    public static FlowService getInstance() {

        return instance;
    }

    /**
     * Execute the flow for the given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @param applicationId Application ID.
     * @param callbackUrl   Callback URL.
     * @param flowId        Flow ID.
     * @param actionId      Action ID.
     * @param inputs        User inputs.
     * @return ExecutionState.
     * @throws FlowEngineException If something goes wrong while executing the flow.
     */
    public FlowStep executeFlow(String tenantDomain, String applicationId,
                                String callbackUrl, String flowId, String actionId, String flowType,
                                Map<String, String> inputs) throws FlowEngineException {

        FlowStep step;
        try {
            FlowContext context;
            if (StringUtils.isBlank(flowId)) {
                // No flowId present hence initiate the flow.
                context = FlowEngineUtils.initiateContext(tenantDomain, callbackUrl, applicationId,
                        flowType);
            } else {
                context = FlowEngineUtils.retrieveFlowContextFromCache(flowType, flowId);
            }
            Optional.ofNullable(inputs).ifPresent(inputs1 -> context.getUserInputData().putAll(inputs1));
            context.setCurrentActionId(actionId);
            for (FlowListener listener :
                    FlowEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPreExecute(context)) {
                    return null;
                }
            }
            step = FlowEngine.getInstance().execute(context);
            for (FlowListener listener :
                    FlowEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPostExecute(step, context)) {
                    return null;
                }
            }
            if (STATUS_COMPLETE.equals(step.getFlowStatus())) {
                FlowEngineUtils.removeRegContextFromCache(flowId);
            } else {
                FlowEngineUtils.addRegContextToCache(context);
            }
            return step;
        } catch (FlowEngineException e) {
            FlowEngineUtils.rollbackContext(flowType, flowId);
            FlowEngineUtils.removeRegContextFromCache(flowId);
            throw e;
        }
    }
}
