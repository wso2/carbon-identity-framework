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

import org.wso2.carbon.identity.action.management.api.model.Action;

/**
 * This class models the Action Execution Request Context.
 * The context includes the action type, action and any action execution request related data.
 */
public class ActionExecutionRequestContext {

    private final Action action;

    private ActionExecutionRequestContext(Action action) {

        this.action = action;
    }

    public ActionType getActionType() {

        return ActionType.valueOf(action.getType().getActionType());
    }

    public String getActionId() {

        return action.getId();
    }

    public Action getAction() {

        return action;
    }

    public static ActionExecutionRequestContext create(Action action) {

        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null.");
        }
        return new ActionExecutionRequestContext(action);
    }
}
