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

package org.wso2.carbon.identity.core.context.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

/**
 * This class models the Request.
 * Request is the entity that represents headers and parameters sent in the request.
 */
public class Request {

    private final List<Header> headers = new ArrayList<>();
    private final List<Parameter> queryParameters = new ArrayList<>();
    private final String ipAddress;

    private Request(Builder builder) {

        this.headers.addAll(builder.headers);
        this.queryParameters.addAll(builder.queryParameters);
        this.ipAddress = builder.ipAddress;
    }

    public List<Header> getHeaders() {

        return Collections.unmodifiableList(headers);
    }

    public List<Parameter> getQueryParameters() {

        return Collections.unmodifiableList(queryParameters);
    }

    public String getIpAddress() {

        return ipAddress;
    }

    /**
     * Builder for the Request.
     */
    public static class Builder {

        private static final Log LOG = LogFactory.getLog(Builder.class);
        private static final String PARAM_SEPARATOR = "&";
        private static final char NAME_VALUE_SEPARATOR = '=';
        private static final List<String> DEFAULT_EXCLUDED_QUERY_PARAMS = Arrays.asList(
                "password",
                "client_secret",
                "client_assertion",
                "assertion",
                "code",
                "code_verifier",
                "access_token",
                "refresh_token",
                "id_token",
                "id_token_hint",
                "logout_token",
                "token",
                "subject_token",
                "actor_token",
                "device_code",
                "user_code",
                "request",
                "SAMLResponse",
                "confirmation",
                "otp",
                "sessionDataKey",
                "sessionDataKeyConsent"
        );
        private static final String EXCLUDED_QUERY_PARAMS_CONFIG = "IdentityContext.Request.ExcludedQueryParams.Param";
        private static final String MAX_QUERY_PARAMS_CONFIG = "IdentityContext.Request.MaxQueryParams";
        private static final int DEFAULT_MAX_QUERY_PARAMS = 50;

        // Server configurations do not change at runtime. Hence, these are resolved once and cached.
        private static volatile Set<String> excludedQueryParams;
        private static volatile Integer maxQueryParams;

        private final List<Header> headers = new ArrayList<>();
        private final List<Parameter> queryParameters = new ArrayList<>();
        private String ipAddress;

        public Builder fromHttpRequest(HttpServletRequest request) {

            if (request != null) {
                resolveHeaders(request);
                resolveQueryParameters(request);
                this.ipAddress = request.getRemoteAddr();
            }

            return this;
        }

        public Request build() {

            return new Request(this);
        }

        private void resolveHeaders(HttpServletRequest request) {

            Enumeration headerNames = request.getHeaderNames();
            if (headerNames == null) {
                return;
            }

            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                if (headerValues == null) {
                    continue;
                }

                List<String> values = Collections.list(headerValues);
                this.headers.add(new Header(headerName, values));
            }
        }

        private void resolveQueryParameters(HttpServletRequest request) {

            String queryString = request.getQueryString();
            int maxParams = getMaxQueryParams();
            if (StringUtils.isBlank(queryString) || maxParams == 0) {
                return;
            }

            Set<String> excludedParams = getExcludedQueryParams();
            // LinkedHashMap preserves the parameter order and groups repeated parameters (e.g. ?a=1&a=2).
            Map<String, List<String>> params = new LinkedHashMap<>();
            int capturedCount = 0;
            for (String pair : queryString.split(PARAM_SEPARATOR)) {
                if (pair.isEmpty()) {
                    continue;
                }
                if (capturedCount >= maxParams) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Maximum number of query parameters: " + maxParams + " captured for the " +
                                "request. Remaining query parameters are ignored.");
                    }
                    break;
                }
                if (addQueryParameter(pair, excludedParams, params)) {
                    capturedCount++;
                }
            }

            params.forEach((name, values) -> this.queryParameters.add(new Parameter(name, values)));
        }

        private boolean addQueryParameter(String pair, Set<String> excludedParams,
                                          Map<String, List<String>> params) {

            int separatorIndex = pair.indexOf(NAME_VALUE_SEPARATOR);
            String name = decode(separatorIndex > -1 ? pair.substring(0, separatorIndex) : pair);
            if (StringUtils.isEmpty(name) || excludedParams.contains(name)) {
                return false;
            }
            String value = separatorIndex > -1 ? decode(pair.substring(separatorIndex + 1)) : StringUtils.EMPTY;
            params.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return true;
        }

        private String decode(String value) {

            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                LOG.debug("Malformed encoding found in the query string. Using the raw value.");
                return value;
            }
        }

        private static Set<String> getExcludedQueryParams() {

            Set<String> params = excludedQueryParams;
            if (params == null) {
                // Matched case-insensitively so that excluded parameters cannot be captured by changing the case.
                Set<String> resolvedParams = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                resolvedParams.addAll(DEFAULT_EXCLUDED_QUERY_PARAMS);
                for (String param : IdentityUtil.getPropertyAsList(EXCLUDED_QUERY_PARAMS_CONFIG)) {
                    if (StringUtils.isNotBlank(param)) {
                        resolvedParams.add(param.trim());
                    }
                }
                params = Collections.unmodifiableSet(resolvedParams);
                excludedQueryParams = params;
            }
            return params;
        }

        private static int getMaxQueryParams() {

            Integer max = maxQueryParams;
            if (max == null) {
                max = DEFAULT_MAX_QUERY_PARAMS;
                String configuredMax = IdentityUtil.getProperty(MAX_QUERY_PARAMS_CONFIG);
                if (StringUtils.isNotBlank(configuredMax)) {
                    try {
                        int parsedMax = Integer.parseInt(configuredMax.trim());
                        if (parsedMax >= 0) {
                            max = parsedMax;
                        } else {
                            LOG.warn("Invalid value: " + configuredMax + " configured for " + MAX_QUERY_PARAMS_CONFIG
                                    + ". Using the default value: " + max);
                        }
                    } catch (NumberFormatException e) {
                        LOG.warn("Invalid value: " + configuredMax + " configured for " + MAX_QUERY_PARAMS_CONFIG +
                                ". Using the default value: " + max);
                    }
                }
                maxQueryParams = max;
            }
            return max;
        }
    }
}
