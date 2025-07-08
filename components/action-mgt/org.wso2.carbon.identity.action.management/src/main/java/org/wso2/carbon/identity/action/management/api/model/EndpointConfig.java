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

package org.wso2.carbon.identity.action.management.api.model;

import java.util.Collections;
import java.util.List;

/**
 * EndpointConfig.
 */
public class EndpointConfig {

    private String uri;
    private Authentication authentication;
    private List<String> allowedHeaders;
    private List<String> allowedParameters;

    public EndpointConfig() {
    }

    public EndpointConfig(EndpointConfigBuilder endpointConfigBuilder) {

        this.uri = endpointConfigBuilder.uri;
        this.authentication = endpointConfigBuilder.authentication;
        this.allowedHeaders = endpointConfigBuilder.allowedHeaders;
        this.allowedParameters = endpointConfigBuilder.allowedParameters;
    }

    public String getUri() {

        return uri;
    }

    public Authentication getAuthentication() {

        return authentication;
    }

    public List<String> getAllowedHeaders() {

        return (allowedHeaders == null) ? null : Collections.unmodifiableList(allowedHeaders);
    }

    public List<String> getAllowedParameters() {

        return (allowedParameters == null) ? null : Collections.unmodifiableList(allowedParameters);
    }

    /**
     * EndpointConfig builder.
     */
    public static class EndpointConfigBuilder {

        private String uri;
        private Authentication authentication;
        private List<String> allowedHeaders;
        private List<String> allowedParameters;

        public EndpointConfigBuilder() {
        }

        public EndpointConfigBuilder(EndpointConfig endpointConfig) {

            this.uri = endpointConfig.getUri();
            this.authentication = endpointConfig.getAuthentication();
            this.allowedHeaders = endpointConfig.getAllowedHeaders();
            this.allowedParameters = endpointConfig.getAllowedParameters();
        }

        public EndpointConfigBuilder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public EndpointConfigBuilder authentication(Authentication authentication) {

            this.authentication = authentication;
            return this;
        }

        public EndpointConfigBuilder allowedHeaders(List<String> allowedHeaders) {

            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public EndpointConfigBuilder allowedParameters(List<String> allowedParameters) {

            this.allowedParameters = allowedParameters;
            return this;
        }

        public EndpointConfig build() {

            return new EndpointConfig(this);
        }
    }
}
