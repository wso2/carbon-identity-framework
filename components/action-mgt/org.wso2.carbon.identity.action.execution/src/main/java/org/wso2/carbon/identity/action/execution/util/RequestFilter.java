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
import org.wso2.carbon.identity.action.execution.model.Header;
import org.wso2.carbon.identity.action.execution.model.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class filters the headers and parameters of the request based on the action type.
 * Filtering is performed by validating headers and parameters received against the headers and parameters
 * configured in the system configuration to be excluded in request.
 */
public class RequestFilter {

    public static List<Header> getFilteredHeaders(List<Header> headers, ActionType actionType) {

        List<Header> filteredHeaders = new ArrayList<>();
        Set<String> excludedHeaders = ActionExecutorConfig.getInstance()
                .getExcludedHeadersInActionRequestForActionType(actionType);

        headers.forEach(header -> {
            if (!excludedHeaders.contains(header.getName().toLowerCase(Locale.ROOT))) {
                filteredHeaders.add(header);
            }
        });

        return filteredHeaders;
    }

    public static List<Param> getFilteredParams(List<Param> params, ActionType actionType) {

        List<Param> filteredParams = new ArrayList<>();
        Set<String> excludedParams = ActionExecutorConfig.getInstance()
                .getExcludedParamsInActionRequestForActionType(actionType);

        params.forEach(param -> {
            if (!excludedParams.contains(param.getName())) {
                filteredParams.add(param);
            }
        });

        return filteredParams;
    }
}
