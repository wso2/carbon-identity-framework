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

package org.wso2.carbon.identity.action.execution.internal.service.impl;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for ActionVersionHandler implementations for action types.
 */
public class ActionVersionHandlerFactory {

    private static final Map<ActionType, ActionVersionHandler> actionVersionHandlers =
            new HashMap<>();

    public static void registerActionVersionHandler(ActionVersionHandler actionVersionHandler) {

        actionVersionHandlers.put(actionVersionHandler.getSupportedActionType(),
                actionVersionHandler);
    }

    public static void unregisterActionVersionHandler(ActionVersionHandler actionVersionHandler) {

        actionVersionHandlers.remove(actionVersionHandler.getSupportedActionType());
    }

    /**
     * Get ActionVersionHandler for the given action type.
     *
     * @param actionType ActionType.
     * @return ActionVersionHandler.
     */
    public static ActionVersionHandler getActionVersionHandler(ActionType actionType) {

        ActionVersionHandler classProvider = actionVersionHandlers.get(actionType);
        if (classProvider != null) {
            return actionVersionHandlers.get(actionType);
        }
        return DefaultActionVersionHandler.getInstance();
    }
}
