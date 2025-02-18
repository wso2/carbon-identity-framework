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

package org.wso2.carbon.identity.user.action.internal.factory;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.user.action.api.service.UserActionExecutor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class to get the UserActionExecutor based on the action type.
 */
public class UserActionExecutorFactory {

    private static final Map<ActionType, UserActionExecutor> userActionExecutors = new EnumMap<>(ActionType.class);

    private UserActionExecutorFactory() {

    }

    public static UserActionExecutor getUserActionExecutor(ActionType actionType) {

        return userActionExecutors.get(actionType);
    }

    public static void registerUserActionExecutor(UserActionExecutor userActionExecutor) {

        userActionExecutors.put(userActionExecutor.getSupportedActionType(), userActionExecutor);
    }

    public static void unregisterUserActionExecutor(UserActionExecutor userActionExecutor) {

        userActionExecutors.remove(userActionExecutor.getSupportedActionType());
    }
}
