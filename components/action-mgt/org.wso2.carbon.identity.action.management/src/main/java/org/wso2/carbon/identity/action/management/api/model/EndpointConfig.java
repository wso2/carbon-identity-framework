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

/**
 * EndpointConfig.
 */
public class EndpointConfig {

    private String uri;
    private Authentication authentication;

    public EndpointConfig() {
    }

    public EndpointConfig(EndpointConfigBuilder endpointConfigBuilder) {

        this.uri = endpointConfigBuilder.uri;
        this.authentication = endpointConfigBuilder.authentication;
    }

    public String getUri() {

        return uri;
    }

    public Authentication getAuthentication() {

        return authentication;
    }

    /**
     * EndpointConfig builder.
     */
    public static class EndpointConfigBuilder {

        private String uri;
        private Authentication authentication;

        public EndpointConfigBuilder() {
        }

        public EndpointConfigBuilder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public EndpointConfigBuilder authentication(Authentication authentication) {

            this.authentication = authentication;
            return this;
        }

        public EndpointConfig build() {

            return new EndpointConfig(this);
        }
    }
}
