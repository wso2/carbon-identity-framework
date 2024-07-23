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

import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionType;

import java.util.Map;

/**
 * This interface defines the Action Execution Request Builder.
 * Action Execution Request Builder is the component that is responsible for building the Action Execution Request
 * based on the action type and the event context.
 */
public interface ActionExecutionRequestBuilder {

    ActionType getSupportedActionType();

    ActionExecutionRequest buildActionExecutionRequest(ActionType actionType, Map<String, Object> eventContext) throws
            ActionExecutionRequestBuilderException;

}
