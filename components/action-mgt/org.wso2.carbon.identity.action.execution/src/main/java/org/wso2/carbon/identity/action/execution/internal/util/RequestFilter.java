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

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.Param;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class filters the headers and parameters of the request based on the action type.
 * Filtering is performed by validating headers and parameters received against the headers and parameters
 * configured in the system configuration to be allowed or excluded in request.
 */
public class RequestFilter {

    /**
     * Filters request headers based on the allowedHeaders and excludedHeaders configured.
     *
     * List of allowed headers can be configured per action or globally at server level.
     * If allowed headers per action have been configured, then the server config will be ignored.
     * List of excluded headers can be configured only at server level. These will be filtered out from the
     * list of allowed headers.
     * @param requestHeaders         Request headers.
     * @param allowedHeadersInAction Allowed headers configured for the action.
     * @param actionType             Action type.
     * @return A list of filtered headers.
     */
    public static List<Header> getFilteredHeaders(List<Header> requestHeaders, List<String> allowedHeadersInAction,
                                                  ActionType actionType) {

        Set<String> allowedHeadersInServer = Optional.ofNullable(ActionExecutorConfig.getInstance()
                .getAllowedHeadersForActionType(actionType)).orElse(Collections.emptySet());
        Set<String> excludedHeadersInServer = Optional.ofNullable(ActionExecutorConfig.getInstance()
                .getExcludedHeadersInActionRequestForActionType(actionType)).orElse(Collections.emptySet());

        boolean hasServerAllowedHeaders = CollectionUtils.isNotEmpty(allowedHeadersInServer);
        boolean hasActionAllowedHeaders = CollectionUtils.isNotEmpty(allowedHeadersInAction);

        // Filter out allowed headers at action level and server level.
        Set<String> allowedHeaders = new HashSet<>();
        if (hasActionAllowedHeaders) {
            allowedHeaders.addAll(allowedHeadersInAction);
        } else if (hasServerAllowedHeaders) {
            allowedHeaders.addAll(allowedHeadersInServer);
        } else if (ActionType.PRE_ISSUE_ACCESS_TOKEN.equals(actionType)) {
            // This is to preserve backward compatibility.
            allowedHeaders.addAll(requestHeaders.stream()
                    .map(Header::getName)
                    .collect(Collectors.toSet()));
        }
        // Filter out excluded headers configured at server level.
        allowedHeaders.removeAll(excludedHeadersInServer);

        // Normalize final headers to lower case
        // Header Fields should be case-insensitive - RFC 7230
        Set<String> normalizedAllowedHeaders = allowedHeaders.stream()
                .map(header -> header.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<Header> normalizeRequestHeaders = requestHeaders.stream()
                .map(header -> new Header(header.getName().toLowerCase(Locale.ROOT), header.getValue()))
                .collect(Collectors.toList());

        return normalizeRequestHeaders.stream()
                .filter(header -> normalizedAllowedHeaders.contains(header.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Filters request parameters based on the allowedParameters and excludedParameters configured.

     * List of allowed parameters can be configured per action or globally at server level.
     * If allowed parameters per action have been configured, then the server config will be ignored.
     * List of excluded parameters can be configured only at server level. These will be filtered out from the
     * list of allowed parameters.
     * @param requestParameters     Request parameters.
     * @param allowedParamsInAction Allowed parameters configured for the action.
     * @param actionType            Action type.
     * @return A list of filtered parameters.
     */
    public static List<Param> getFilteredParams(List<Param> requestParameters, List<String> allowedParamsInAction,
                                                ActionType actionType) {

        Set<String> allowedParamsInServer = ActionExecutorConfig.getInstance()
                .getAllowedParamsForActionType(actionType);
        Set<String> excludedParamsInServer = ActionExecutorConfig.getInstance()
                .getExcludedParamsInActionRequestForActionType(actionType);

        boolean hasServerAllowedParams = CollectionUtils.isNotEmpty(allowedParamsInServer);
        boolean hasActionAllowedParams = CollectionUtils.isNotEmpty(allowedParamsInAction);

        Set<String> allAllowedParamsSet = new HashSet<>();
        if (hasActionAllowedParams) {
            allAllowedParamsSet.addAll(allowedParamsInAction);
        } else if (hasServerAllowedParams) {
            allAllowedParamsSet.addAll(allowedParamsInServer);
        } else if (ActionType.PRE_ISSUE_ACCESS_TOKEN.equals(actionType)) {
            // This is to preserve backward compatibility.
            allAllowedParamsSet.addAll(requestParameters.stream()
                    .map(Param::getName)
                    .collect(Collectors.toSet()));
        }
        // Filter out excluded parameters configured at server level.
        allAllowedParamsSet.removeAll(excludedParamsInServer);

        return requestParameters.stream()
                .filter(param -> allAllowedParamsSet.contains(param.getName()))
                .collect(Collectors.toList());
    }
}
