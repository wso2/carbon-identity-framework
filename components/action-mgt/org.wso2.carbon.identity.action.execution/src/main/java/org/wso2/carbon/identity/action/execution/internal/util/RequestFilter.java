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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class filters the headers and parameters of the request based on the action type.
 * Filtering is performed by validating headers and parameters received against the headers and parameters
 * configured in the system configuration to be allowed or excluded in request.
 */
public class RequestFilter {

    public static List<Header> getFilteredHeaders(List<Header> headers, ActionType actionType) {

        List<Header> filteredHeaders = new ArrayList<>();
        Set<String> allowedHeaders = ActionExecutorConfig.getInstance().getAllowedHeadersForActionType(actionType);
        Set<String> excludedHeaders = ActionExecutorConfig.getInstance()
                .getExcludedHeadersInActionRequestForActionType(actionType);

        boolean isAllowedHeadersConfigured = !allowedHeaders.isEmpty();
        boolean isExcludedHeadersConfigured = !excludedHeaders.isEmpty();

        if (isAllowedHeadersConfigured && isExcludedHeadersConfigured) {
            throw new IllegalStateException(
                    "Both allowed and excluded header configurations cannot be present for action type: " + actionType);
        }

        if (isAllowedHeadersConfigured) {
            headers.stream()
                    .filter(header -> allowedHeaders.contains(header.getName().toLowerCase(Locale.ROOT)))
                    .forEach(filteredHeaders::add);
        } else if (isExcludedHeadersConfigured) {
            headers.stream()
                    .filter(header -> !excludedHeaders.contains(header.getName().toLowerCase(Locale.ROOT)))
                    .forEach(filteredHeaders::add);
        }

        return filteredHeaders;
    }

    public static List<Param> getFilteredParams(List<Param> params, ActionType actionType) {

        List<Param> filteredParams = new ArrayList<>();
        Set<String> allowedParams = ActionExecutorConfig.getInstance().getAllowedParamsForActionType(actionType);
        Set<String> excludedParams = ActionExecutorConfig.getInstance()
                .getExcludedParamsInActionRequestForActionType(actionType);

        boolean isAllowedParamsConfigured = !allowedParams.isEmpty();
        boolean isExcludedParamsConfigured = !excludedParams.isEmpty();

        if (isAllowedParamsConfigured && isExcludedParamsConfigured) {
            throw new IllegalStateException(
                    "Both allowed and excluded param configurations cannot be present for action type: " + actionType);
        }

        if (isAllowedParamsConfigured) {
            params.stream()
                    .filter(param -> allowedParams.contains(param.getName()))
                    .forEach(filteredParams::add);
        } else if (isExcludedParamsConfigured) {
            params.stream()
                    .filter(param -> !excludedParams.contains(param.getName()))
                    .forEach(filteredParams::add);
        }

        return filteredParams;
    }
}
