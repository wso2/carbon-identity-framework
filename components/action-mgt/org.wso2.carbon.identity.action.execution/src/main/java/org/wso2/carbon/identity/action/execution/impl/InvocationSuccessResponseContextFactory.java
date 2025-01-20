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

package org.wso2.carbon.identity.action.execution.impl;

import org.wso2.carbon.identity.action.execution.model.Context;
import org.wso2.carbon.identity.action.execution.model.ActionType;

import java.util.HashMap;
import java.util.Map;

public class InvocationSuccessResponseContextFactory {

    private static final Map<ActionType, Class<? extends Context>> contextClassMap = new HashMap<>();

    public static Class<? extends Context> getInvocationSuccessResponseContextClass(ActionType actionType) {

        Class<? extends Context> responseClass = contextClassMap.get(actionType);
        if (responseClass != null) {
            return responseClass;
        }
        return Context.class;
    }

    public static void registerInvocationSuccessResponseContextClass(
            Class<? extends Context> invocationSuccessResponse) throws NoSuchFieldException, IllegalAccessException {

        ActionType type = (ActionType) invocationSuccessResponse.getDeclaredField("ACTION_TYPE").get(null);
        contextClassMap.put(type, invocationSuccessResponse);
    }

    public static void unregisterInvocationSuccessResponse(Class<? extends Context> invocationSuccessResponse)
            throws NoSuchFieldException, IllegalAccessException {

        ActionType type = (ActionType) invocationSuccessResponse.getDeclaredField("ACTION_TYPE").get(null);
        contextClassMap.remove(type);
    }
}
