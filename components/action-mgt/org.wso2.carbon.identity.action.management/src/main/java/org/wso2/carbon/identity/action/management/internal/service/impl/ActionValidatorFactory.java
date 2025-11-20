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

package org.wso2.carbon.identity.action.management.internal.service.impl;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.service.ActionValidator;
import org.wso2.carbon.identity.action.management.api.service.impl.DefaultActionValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for ActionValidator implementations for action types.
 */
public class ActionValidatorFactory {

    private static final Map<Action.ActionTypes, ActionValidator> actionValidatorMap = new HashMap<>();

    private ActionValidatorFactory() {
    }

    public static ActionValidator getActionValidator(Action.ActionTypes actionType) {

        ActionValidator validator = actionValidatorMap.get(actionType);
        if (validator != null) {
            return validator;
        }
        return DefaultActionValidator.getInstance();
    }

    public static void registerActionValidatorFactory(ActionValidator actionValidator) {

        actionValidatorMap.put(actionValidator.getSupportedActionType(), actionValidator);
    }

    public static void unregisterActionValidatorFactory(ActionValidator actionValidator) {

        actionValidatorMap.remove(actionValidator.getSupportedActionType());
    }
}
