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

package org.wso2.carbon.identity.action.execution.internal.service.impl;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the Action Execution Response Processor Factory.
 * Action Execution Response Processor Factory is the component that is responsible for providing the
 * {@link ActionExecutionResponseProcessor} based on the action type.
 */
public class ActionExecutionResponseProcessorFactory {

    private static final Map<ActionType, ActionExecutionResponseProcessor> actionInvocationResponseProcessors =
            new HashMap<>();

    public static ActionExecutionResponseProcessor getActionExecutionResponseProcessor(ActionType actionType) {

        return actionInvocationResponseProcessors.get(actionType);
    }

    public static void registerActionExecutionResponseProcessor(ActionExecutionResponseProcessor
                                                                        actionExecutionResponseProcessor) {

        actionInvocationResponseProcessors.put(actionExecutionResponseProcessor.getSupportedActionType(),
                actionExecutionResponseProcessor);
    }

    public static void unregisterActionExecutionResponseProcessor(ActionExecutionResponseProcessor
                                                                          actionExecutionResponseProcessor) {

        actionInvocationResponseProcessors.remove(actionExecutionResponseProcessor.getSupportedActionType());
    }

}
