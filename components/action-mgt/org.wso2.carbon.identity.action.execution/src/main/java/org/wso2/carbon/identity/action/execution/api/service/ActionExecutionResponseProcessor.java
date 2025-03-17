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

package org.wso2.carbon.identity.action.execution.api.service;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Incomplete;
import org.wso2.carbon.identity.action.execution.api.model.Success;

/**
 * This interface defines the Action Execution Response Processor.
 * Action Execution Response Processor is the component that is responsible for processing the response received
 * from the action execution.
 */
public interface ActionExecutionResponseProcessor {

    /**
     * This method returns the supported action type for the response processor.
     *
     * @return The supported action type.
     */
    ActionType getSupportedActionType();

    /**
     * This method processes the success response received from the action execution.
     *
     * @param flowContext     The flow context.
     * @param responseContext The response context.
     * @return The success status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
                                                                  ActionExecutionResponseContext
                                                                          <ActionInvocationSuccessResponse>
                                                                          responseContext)
            throws ActionExecutionResponseProcessorException;

    /**
     * This method processes the incomplete response received from the action execution.
     *
     * @param flowContext     The flow context.
     * @param responseContext The response context.
     * @return The incomplete status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Incomplete> processIncompleteResponse(FlowContext flowContext,
                                                                        ActionExecutionResponseContext
                                                                                <ActionInvocationIncompleteResponse>
                                                                                responseContext)
            throws ActionExecutionResponseProcessorException {

        throw new UnsupportedOperationException(
                "The INCOMPLETE status is not supported for the action type: " + getSupportedActionType());
    }

    /**
     * This method processes the error response received from the action execution.
     *
     * @param flowContext     The flow context.
     * @param responseContext The response context.
     * @return The error status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Error> processErrorResponse(FlowContext flowContext,
                                                              ActionExecutionResponseContext
                                                                      <ActionInvocationErrorResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        return new ErrorStatus(new Error(responseContext.getActionInvocationResponse().getErrorMessage(),
                responseContext.getActionInvocationResponse().getErrorDescription()));
    }

    /**
     * This method processes the failure response received from the action execution.
     *
     * @param flowContext     The flow context.
     * @param responseContext The response context.
     * @return The failed status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Failure> processFailureResponse(FlowContext flowContext,
                                                                  ActionExecutionResponseContext
                                                                          <ActionInvocationFailureResponse>
                                                                          responseContext)
            throws ActionExecutionResponseProcessorException {

        return new FailedStatus(new Failure(responseContext.getActionInvocationResponse().getFailureReason(),
                responseContext.getActionInvocationResponse().getFailureDescription()));
    }
}
