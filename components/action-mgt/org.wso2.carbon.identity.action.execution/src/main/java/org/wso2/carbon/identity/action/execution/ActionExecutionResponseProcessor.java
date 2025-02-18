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
import org.wso2.carbon.identity.action.execution.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Error;
import org.wso2.carbon.identity.action.execution.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.model.Event;
import org.wso2.carbon.identity.action.execution.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.model.Failure;
import org.wso2.carbon.identity.action.execution.model.FlowContext;
import org.wso2.carbon.identity.action.execution.model.Incomplete;
import org.wso2.carbon.identity.action.execution.model.Success;

import java.util.Map;

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
     * @param eventContext    The event context.
     * @param actionEvent     The action event.
     * @param successResponse The success response.
     * @return The success status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Success> processSuccessResponse(Map<String, Object> eventContext,
                                                                  Event actionEvent,
                                                                  ActionInvocationSuccessResponse successResponse)
            throws ActionExecutionResponseProcessorException {

        throw new UnsupportedOperationException(
                "The SUCCESS status is not supported for the action type: " + getSupportedActionType());
    }

    /**
     * This method processes the incomplete response received from the action execution. The default implementation
     * throws an {@link UnsupportedOperationException}. The downstream components can override this method,
     * if INCOMPLETE status is supported for corresponding action type.
     *
     * @param eventContext       The event context.
     * @param actionEvent        The action event.
     * @param incompleteResponse The incomplete response.
     * @return The incomplete status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Incomplete> processIncompleteResponse(Map<String, Object> eventContext,
                                                                        Event actionEvent,
                                                                        ActionInvocationIncompleteResponse
                                                                                incompleteResponse)
            throws ActionExecutionResponseProcessorException {

        throw new UnsupportedOperationException(
                "The INCOMPLETE status is not supported for the action type: " + getSupportedActionType());
    }

    /**
     * This method processes the error response received from the action execution.
     *
     * @param eventContext  The event context.
     * @param actionEvent   The action event.
     * @param errorResponse The error response.
     * @return The error status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Error> processErrorResponse(Map<String, Object> eventContext,
                                                              Event actionEvent,
                                                              ActionInvocationErrorResponse errorResponse)
            throws ActionExecutionResponseProcessorException {

        return new ErrorStatus(new Error(errorResponse.getErrorMessage(), errorResponse.getErrorDescription()));
    }

    /**
     * This method processes the failure response received from the action execution.
     * The default implementation returns a failed status with the failure reason and description.
     *
     * @param eventContext    The event context.
     * @param actionEvent     The action event.
     * @param failureResponse The failure response.
     * @return The failed status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Failure> processFailureResponse(Map<String, Object> eventContext,
                                                                  Event actionEvent,
                                                                  ActionInvocationFailureResponse failureResponse)
            throws ActionExecutionResponseProcessorException {

        return new FailedStatus(new Failure(failureResponse.getFailureReason(),
                failureResponse.getFailureDescription()));
    }

    /**
     * This method processes the success response received from the action execution.
     *
     * @param flowContext     The flow context.
     * @param responseContext The response context.
     * @return The success status.
     * @throws ActionExecutionResponseProcessorException If an error occurs while processing the response.
     */
    default ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
                                                                  ActionExecutionResponseContext
                                                                          <ActionInvocationSuccessResponse>
                                                                          responseContext)
            throws ActionExecutionResponseProcessorException {

        return processSuccessResponse(flowContext.getContextData(), responseContext.getActionEvent(),
                responseContext.getActionInvocationResponse());

    }

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

        return processIncompleteResponse(flowContext.getContextData(), responseContext.getActionEvent(),
                responseContext.getActionInvocationResponse());
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

        return processErrorResponse(flowContext.getContextData(), responseContext.getActionEvent(),
                responseContext.getActionInvocationResponse());
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

        return processFailureResponse(flowContext.getContextData(), responseContext.getActionEvent(),
                responseContext.getActionInvocationResponse());
    }
}
