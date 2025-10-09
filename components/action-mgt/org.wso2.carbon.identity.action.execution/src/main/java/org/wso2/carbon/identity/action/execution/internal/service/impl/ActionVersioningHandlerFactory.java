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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.api.service.impl.DefaultActionVersioningHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for ActionVersioningHandler implementations for action types.
 */
public class ActionVersioningHandlerFactory {

    private static final Map<ActionType, ActionVersioningHandler> actionVersioningHandlers = new HashMap<>();

    private static final Log LOG = LogFactory.getLog(ActionVersioningHandlerFactory.class);

    public static void registerActionVersioningHandler(ActionVersioningHandler versioningHandler) {

        LOG.debug("Registering ActionVersioningHandler for action type: " + versioningHandler.getSupportedActionType());
        actionVersioningHandlers.put(versioningHandler.getSupportedActionType(),
                versioningHandler);
    }

    public static void unregisterActionVersioningHandler(ActionVersioningHandler versioningHandler) {

        LOG.debug("Unregistering ActionVersioningHandler for action type: " +
                versioningHandler.getSupportedActionType());
        actionVersioningHandlers.remove(versioningHandler.getSupportedActionType());
    }

    /**
     * Get ActionVersioningHandler for the given action type.
     *
     * @param actionType ActionType.
     * @return ActionVersioningHandler.
     */
    public static ActionVersioningHandler getActionVersioningHandler(ActionType actionType) {

        ActionVersioningHandler classProvider = actionVersioningHandlers.get(actionType);
        if (classProvider != null) {
            return actionVersioningHandlers.get(actionType);
        }
        return DefaultActionVersioningHandler.getInstance();
    }
}
