/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.api.model;

/**
 * This class models the Action Execution Response Context.
 * The context includes the action event, action invocation response and any action response related data.
 *
 * @param <T> The type of the action invocation response.
 *            {@link ActionInvocationSuccessResponse} or {@link ActionInvocationIncompleteResponse} or
 *            {@link ActionInvocationErrorResponse} or {@link ActionInvocationFailureResponse}
 */
public class ActionExecutionResponseContext<T extends ActionInvocationResponse.APIResponse> {

    Event actionEvent;
    T actionInvocationResponse;

    private ActionExecutionResponseContext(Event actionEvent, T actionInvocationResponse) {

        this.actionEvent = actionEvent;
        this.actionInvocationResponse = actionInvocationResponse;
    }

    public Event getActionEvent() {

        return actionEvent;
    }

    public T getActionInvocationResponse() {

        return actionInvocationResponse;
    }

    public static <T extends ActionInvocationResponse.APIResponse> ActionExecutionResponseContext<T> create(
            Event actionEvent, T actionInvocationResponse) {

        return new ActionExecutionResponseContext<>(actionEvent, actionInvocationResponse);
    }
}
