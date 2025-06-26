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
import org.wso2.carbon.identity.action.management.api.model.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Filters request headers based on the action type and allowedHeaders and excludedHeaders configured.
     *
     * Only an allowed list of headers can be configured per action. At server level, both allowed headers and
     * excluded headers can be configured. Excluded headers configured at the server level are removed from
     * any allowed headers defined at either the server level or the action level.
     * @param headers Request headers.
     * @param actionType Action type.
     * @param action Action.
     * @return A list of filtered headers.
     * @throws IllegalStateException If both allowed and excluded parameter are configured at server level.
     */
    public static List<Header> getFilteredHeaders(List<Header> headers, ActionType actionType, Action action) {

        Set<String> serverAllowedHeaders = ActionExecutorConfig.getInstance()
                .getAllowedHeadersForActionType(actionType);
        Set<String> serverExcludedHeaders = ActionExecutorConfig.getInstance()
                .getExcludedHeadersInActionRequestForActionType(actionType);
        List<String> actionAllowedHeaders = action.getEndpoint().getAllowedHeaders();

        boolean hasServerAllowedHeaders = !serverAllowedHeaders.isEmpty();
        boolean hasActionAllowedHeaders = !actionAllowedHeaders.isEmpty();

        Set<String> allAllowedHeadersSet = new HashSet<>();
        if (hasActionAllowedHeaders) {
            allAllowedHeadersSet.addAll(actionAllowedHeaders);
        } else if (hasServerAllowedHeaders) {
            allAllowedHeadersSet.addAll(serverAllowedHeaders);
        }

        // Filter out excluded headers configured at server level.
        allAllowedHeadersSet.removeAll(serverExcludedHeaders);
        return headers.stream()
                .filter(header -> allAllowedHeadersSet.contains(header.getName().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
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

    /**
     * Filters request parameters based on the action type and allowedParameters and excludedParameters configured.
     *
     * Only an allowed list of parameters can be configured per action. At server level, both allowed parameters and
     * excluded parameters can be configured. Excluded parameters configured at the server level are removed from
     * any allowed parameters defined at either the server level or the action level.
     * @param params Request parameters.
     * @param actionType Action type.
     * @param action Action.
     * @return A list of filtered parameters.
     * @throws IllegalStateException If both allowed and excluded parameter are configured at server-level.
     */
    public static List<Param> getFilteredParams(List<Param> params, ActionType actionType, Action action) {

        Set<String> serverAllowedParams = ActionExecutorConfig.getInstance().getAllowedParamsForActionType(actionType);
        Set<String> serverExcludedParams = ActionExecutorConfig.getInstance()
                .getExcludedParamsInActionRequestForActionType(actionType);
        List<String> actionAllowedParams = action.getEndpoint().getAllowedParameters();

        boolean hasServerAllowedParams = !serverAllowedParams.isEmpty();
        boolean hasActionAllowedParams = !actionAllowedParams.isEmpty();

        Set<String> allAllowedParamsSet = new HashSet<>();
        if (hasActionAllowedParams) {
            allAllowedParamsSet.addAll(actionAllowedParams);
        } else if (hasServerAllowedParams) {
            allAllowedParamsSet.addAll(serverAllowedParams);
        }

        // Filter out excluded parameters configured at server level.
        allAllowedParamsSet.removeAll(serverExcludedParams);
        return params.stream()
                .filter(header -> allAllowedParamsSet.contains(header.getName().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}
