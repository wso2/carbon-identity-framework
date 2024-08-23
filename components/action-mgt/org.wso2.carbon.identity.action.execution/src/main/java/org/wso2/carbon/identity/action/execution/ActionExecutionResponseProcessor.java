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

package org.wso2.carbon.identity.action.execution;

import org.wso2.carbon.identity.action.execution.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Event;

import java.util.Map;

/**
 * This interface defines the Action Execution Response Processor.
 * Action Execution Response Processor is the component that is responsible for processing the response received
 * from the action execution.
 */
public interface ActionExecutionResponseProcessor {

    ActionType getSupportedActionType();

    ActionExecutionStatus processSuccessResponse(Map<String, Object> eventContext,
                                                 Event actionEvent,
                                                 ActionInvocationSuccessResponse successResponse) throws
            ActionExecutionResponseProcessorException;

    ActionExecutionStatus processErrorResponse(Map<String, Object> eventContext,
                                               Event actionEvent,
                                               ActionInvocationErrorResponse errorResponse) throws
            ActionExecutionResponseProcessorException;

    ActionExecutionStatus processFailureResponse(Map<String, Object> eventContext,
                                               Event actionEvent,
                                               ActionInvocationFailureResponse failureResponse) throws
            ActionExecutionResponseProcessorException;
}
