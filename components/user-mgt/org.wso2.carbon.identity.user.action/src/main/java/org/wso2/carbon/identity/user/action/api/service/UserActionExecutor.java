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

package org.wso2.carbon.identity.user.action.api.service;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;

/**
 * Interface for User Action Executor.
 * This interface is implemented by the classes that execute user actions.
 */
public interface UserActionExecutor {

    /**
     * Get the supported action type.
     *
     * @return Supported action type.
     */
    ActionType getSupportedActionType();

    /**
     * Executes the user action.
     *
     * @param userActionContext User action context.
     * @param tenantDomain      Tenant domain.
     * @return Action execution status.
     */
    ActionExecutionStatus<?> execute(UserActionContext userActionContext, String tenantDomain)
            throws ActionExecutionException;
}
