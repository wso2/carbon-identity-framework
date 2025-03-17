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

package org.wso2.carbon.identity.action.management.internal.dao.impl;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;

import java.util.EnumMap;
import java.util.Map;

/**
 * This class defines the ActionDTO Model Resolver Factory.
 * ActionDTO Model Resolver Factory is the component that is responsible for providing the
 * {@link ActionDTOModelResolver} based on the action type.
 */
public class ActionDTOModelResolverFactory {

    private static final Map<Action.ActionTypes, ActionDTOModelResolver> actionDTOModelResolvers =
            new EnumMap<>(Action.ActionTypes.class);

    private ActionDTOModelResolverFactory() {
    }

    public static ActionDTOModelResolver getActionDTOModelResolver(Action.ActionTypes actionType) {

        switch (actionType) {
            case PRE_UPDATE_PROFILE:
                return actionDTOModelResolvers.get(Action.ActionTypes.PRE_UPDATE_PROFILE);
            case PRE_UPDATE_PASSWORD:
                return actionDTOModelResolvers.get(Action.ActionTypes.PRE_UPDATE_PASSWORD);
            case PRE_ISSUE_ACCESS_TOKEN:
                return actionDTOModelResolvers.get(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
            default:
                return null;
        }
    }

    public static void registerActionDTOModelResolver(ActionDTOModelResolver actionDTOModelResolver) {

        actionDTOModelResolvers.put(actionDTOModelResolver.getSupportedActionType(), actionDTOModelResolver);
    }

    public static void unregisterActionDTOModelResolver(ActionDTOModelResolver actionDTOModelResolver) {

        actionDTOModelResolvers.remove(actionDTOModelResolver.getSupportedActionType());
    }
}
