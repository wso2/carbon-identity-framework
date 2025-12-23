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

package org.wso2.carbon.identity.action.execution.api.service;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.management.api.model.Action;

/**
 * The Action Versioning Handler that manages action execution based on the corresponding action version.
 */
public interface ActionVersioningHandler {

    /**
     * Get the supported action type.
     *
     * @return ActionType.
     */
    ActionType getSupportedActionType();

    /**
     * Check whether the action version is eligible to be triggered for the action version and the given flow context.
     *
     * @param flowContext FlowContext.
     * @return true if the action version is eligible to be triggered, false otherwise.
     * @throws ActionExecutionException ActionExecutionException.
     */
    boolean canExecute(ActionExecutionRequestContext actionExecutionRequestContext, FlowContext flowContext)
            throws ActionExecutionException;

    /**
     * Check whether the action version is retired.
     *
     * @param actionType ActionType.
     * @param action     Action.
     * @return true if the action version is retired, false otherwise.
     * @throws ActionExecutionException ActionExecutionException.
     */
    boolean isRetiredActionVersion(ActionType actionType, Action action)
            throws ActionExecutionException;
}
