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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     * @param parameters Request parameters.
     * @param actionType Action type.
     * @param action Action.
     * @return A list of filtered parameters.
     * @throws IllegalStateException If both allowed and excluded parameter configurations are present at the server level.
     */
    public static List<Header> getFilteredHeaders(List<Header> headers, ActionType actionType, Action action) {

        Set<String> allowedHeadersInServer = ActionExecutorConfig.getInstance().
                getAllowedHeadersForActionType(actionType);
        Set<String> excludedHeadersInServer = ActionExecutorConfig.getInstance().
                getExcludedHeadersInActionRequestForActionType(actionType);
        List<String> allowedHeadersInAction = action.getEndpoint().getAllowedHeaders();

        boolean isServerLevelAllowedHeadersConfigured = !allowedHeadersInServer.isEmpty();
        boolean isServerLevelExcludedHeadersConfigured = !excludedHeadersInServer.isEmpty();
        if (isServerLevelAllowedHeadersConfigured && isServerLevelExcludedHeadersConfigured) {
            throw new IllegalStateException(
                    "Both allowed and excluded header configurations cannot be present for action type: " + actionType);
        }

        // Action level allowed headers should be validated against server level excluded headers.
        // When allowed headers are configured at both server level and action level, action level allowed headers
        // are prioritized.
        Collection<Header> filteredHeaders = headers.stream()
                .map(header -> new AbstractMap.SimpleEntry<>(header.getName().toLowerCase(Locale.ROOT), header))
                .filter(entry -> {
                    String name = entry.getKey();
                    if (allowedHeadersInAction != null) {
                        return allowedHeadersInAction.contains(name)
                                && !excludedHeadersInServer.contains(name);
                    }
                    if (isServerLevelAllowedHeadersConfigured) {
                        return allowedHeadersInServer.contains(name);
                    }
                    if (isServerLevelExcludedHeadersConfigured) {
                        return !excludedHeadersInServer.contains(name);
                    }
                    return false;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (first, second) -> first, LinkedHashMap::new))
                .values();

        return new ArrayList<>(filteredHeaders);
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
     * @param parameters Request parameters.
     * @param actionType Action type.
     * @param action Action.
     * @return A list of filtered parameters.
     * @throws IllegalStateException If both allowed and excluded parameter configurations are present at the server level.
     */
    public static List<Param> getFilteredParams(List<Param> parameters, ActionType actionType, Action action) {

        Set<String> allowedParamsInServer = ActionExecutorConfig.getInstance().
                getAllowedParamsForActionType(actionType);
        Set<String> excludedParametersInServer = ActionExecutorConfig.getInstance().
                getExcludedParamsInActionRequestForActionType(actionType);
        List<String> allowedParametersInAction = action.getEndpoint().getAllowedParameters();

        boolean isServerLevelAllowedParamsConfigured = !allowedParamsInServer.isEmpty();
        boolean isServerLevelExcludedParamsConfigured = !excludedParametersInServer.isEmpty();
        if (isServerLevelAllowedParamsConfigured && isServerLevelExcludedParamsConfigured) {
            throw new IllegalStateException(
                    "Both allowed and excluded parameters configurations cannot be present for action type: "
                            + actionType);
        }

        // Action level allowed parameters should be validated against server level excluded parameters.
        // When allowed parameters are configured at both server level and action level, action level allowed
        // parameters are prioritized.
        Collection<Param> filteredParams = parameters.stream()
                .map(param -> new AbstractMap.SimpleEntry<>(param.getName().toLowerCase(Locale.ROOT), param))
                .filter(entry -> {
                    String name = entry.getKey();
                    if (allowedParametersInAction != null) {
                        return allowedParametersInAction.contains(name)
                                && !excludedParametersInServer.contains(name);
                    }
                    if (isServerLevelAllowedParamsConfigured) {
                        return allowedParamsInServer.contains(name);
                    }
                    if (isServerLevelExcludedParamsConfigured) {
                        return !excludedParametersInServer.contains(name);
                    }
                    return false;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (first, second) -> first, LinkedHashMap::new))
                .values();

        return new ArrayList<>(filteredParams);
    }
}
