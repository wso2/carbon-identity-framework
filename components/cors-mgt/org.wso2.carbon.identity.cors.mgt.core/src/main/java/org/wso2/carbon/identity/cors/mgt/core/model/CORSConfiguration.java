/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.model;

import java.util.HashSet;
import java.util.Set;

/**
 * The CORS configuration of a tenant.
 */
public class CORSConfiguration {

    /**
     * If {@code true} generic HTTP requests must be allowed to pass through the valve, else only valid and accepted
     * CORS requests must be allowed (strict CORS filtering).
     */
    private boolean allowGenericHttpRequests;

    /**
     * If {@code true} the CORS valve must allow requests from any origin, else the origin whitelist must be consulted.
     */
    private boolean allowAnyOrigin;

    /**
     * If {@code true} the CORS valve must allow requests from any origin which is a subdomain origin of the allowed
     * origins.
     */
    private boolean allowSubdomains;

    /**
     * The supported HTTP methods. Requests for methods not included here must be refused by the CORS filter with a
     * HTTP 405 "Method not allowed" response.
     */
    private Set<String> supportedMethods;

    /**
     * If {@code true} the CORS valve must support any requested header, else the supported headers list must be
     * consulted.
     */
    private boolean supportAnyHeader;

    /**
     * The names of the supported author request headers. Applies if {@link #supportAnyHeader} is {@code false}.
     * Never {@code null}.
     */
    private Set<String> supportedHeaders;

    /**
     * The non-simple response headers that the web browser should expose to the author of the CORS request.
     */
    private Set<String> exposedHeaders;

    /**
     * Whether user credentials, such as cookies, HTTP authentication or client-side certificates, are supported.
     */
    private boolean supportsCredentials;

    /**
     * Indicates how long the results of a preflight request can be cached by the web client, in seconds. If {@code
     * -1} unspecified.
     */
    private int maxAge;

    /**
     * Enables HTTP servlet request tagging to provide CORS information to downstream handlers.
     */
    private boolean tagRequests;

    /**
     * Default constructor.
     */
    public CORSConfiguration() {

        this.supportedMethods = new HashSet<>();
        this.supportedHeaders = new HashSet<>();
        this.exposedHeaders = new HashSet<>();
    }

    public boolean isAllowGenericHttpRequests() {

        return allowGenericHttpRequests;
    }

    public void setAllowGenericHttpRequests(boolean allowGenericHttpRequests) {

        this.allowGenericHttpRequests = allowGenericHttpRequests;
    }

    public boolean isAllowAnyOrigin() {

        return allowAnyOrigin;
    }

    public void setAllowAnyOrigin(boolean allowAnyOrigin) {

        this.allowAnyOrigin = allowAnyOrigin;
    }

    public boolean isAllowSubdomains() {

        return allowSubdomains;
    }

    public void setAllowSubdomains(boolean allowSubdomains) {

        this.allowSubdomains = allowSubdomains;
    }

    public Set<String> getSupportedMethods() {

        return supportedMethods;
    }

    public void setSupportedMethods(Set<String> supportedMethods) {

        this.supportedMethods = supportedMethods;
    }

    public boolean isSupportAnyHeader() {

        return supportAnyHeader;
    }

    public void setSupportAnyHeader(boolean supportAnyHeader) {

        this.supportAnyHeader = supportAnyHeader;
    }

    public Set<String> getSupportedHeaders() {

        return supportedHeaders;
    }

    public void setSupportedHeaders(Set<String> supportedHeaders) {

        this.supportedHeaders = supportedHeaders;
    }

    public Set<String> getExposedHeaders() {

        return exposedHeaders;
    }

    public void setExposedHeaders(Set<String> exposedHeaders) {

        this.exposedHeaders = exposedHeaders;
    }

    public boolean isSupportsCredentials() {

        return supportsCredentials;
    }

    public void setSupportsCredentials(boolean supportsCredentials) {

        this.supportsCredentials = supportsCredentials;
    }

    public int getMaxAge() {

        return maxAge;
    }

    public void setMaxAge(int maxAge) {

        this.maxAge = maxAge;
    }

    public boolean isTagRequests() {

        return tagRequests;
    }

    public void setTagRequests(boolean tagRequests) {

        this.tagRequests = tagRequests;
    }
}
