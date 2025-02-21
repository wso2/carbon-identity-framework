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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;

/**
 * This class resolves the API version based on the action type.
 * The API version is used to determine the version of the API to be invoked.
 */
public class ActionAPIVersionResolver {

    public static final String API_VERSION_HEADER = "X-WSO2-API-Version";

    private ActionAPIVersionResolver() {

    }

    /**
     * Resolves the API version based on the action type.
     *
     * @param actionType Action type.
     * @return API version.
     */
    public static String resolveAPIVersion(ActionType actionType) {

        switch (actionType) {
            case PRE_UPDATE_PASSWORD:
            case AUTHENTICATION:
            case PRE_ISSUE_ACCESS_TOKEN:
            default:
                return "v1";
        }
    }
}
