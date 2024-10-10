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

package org.wso2.carbon.identity.action.execution.util;

import org.wso2.carbon.identity.action.execution.model.ActionType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class filters the headers and parameters of the request based on the action type.
 * Filtering is performed by validating headers and parameters received against the headers and parameters
 * configured in the system configuration to be excluded in request.
 */
public class RequestFilter {

    public static Map<String, String[]> getFilteredHeaders(Map<String, String[]> headers, ActionType actionType) {

        Map<String, String[]> filteredHeaders = new HashMap<>();
        Set<String> excludedHeaders = ActionExecutorConfig.getInstance()
                .getExcludedHeadersInActionRequestForActionType(actionType);

        headers.forEach((key, value) -> {
            // Headers are case-insensitive. Therefore, convert the key to lowercase before comparing.
            if (!excludedHeaders.contains(key.toLowerCase(Locale.ROOT))) {
                filteredHeaders.put(key, value);
            }
        });

        return filteredHeaders;
    }

    public static Map<String, String[]> getFilteredParams(Map<String, String[]> params, ActionType actionType) {

        Map<String, String[]> filteredParams = new HashMap<>();
        Set<String> excludedParams = ActionExecutorConfig.getInstance()
                .getExcludedParamsInActionRequestForActionType(actionType);

        params.forEach((key, value) -> {
            if (!excludedParams.contains(key)) {
                filteredParams.put(key, value);
            }
        });

        return filteredParams;
    }
}
