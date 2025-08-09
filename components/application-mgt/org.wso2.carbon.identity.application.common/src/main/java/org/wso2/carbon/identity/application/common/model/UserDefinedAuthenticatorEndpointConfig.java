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

package org.wso2.carbon.identity.application.common.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The authenticator endpoint configuration model for the user defined authenticator configurations.
 */
public class UserDefinedAuthenticatorEndpointConfig {

    private static final Log LOG = LogFactory.getLog(UserDefinedAuthenticatorEndpointConfig.class);
    private final EndpointConfig endpointConfig;

    private UserDefinedAuthenticatorEndpointConfig(UserDefinedAuthenticatorEndpointConfigBuilder builder) {

        endpointConfig = builder.endpointConfig;
    }
    
    public EndpointConfig getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * Get the URI of the authenticator endpoint of the user defined authenticator.
     *
     * @return URI of the authenticator endpoint.
     */
    public String getAuthenticatorEndpointUri() {

        return endpointConfig.getUri();
    }

    /**
     * Get the authentication type of the authenticator endpoint of the user defined authenticator.
     *
     * @return Authentication type of the authenticator endpoint.
     */
    public String getAuthenticatorEndpointAuthenticationType() {

        return endpointConfig.getAuthentication().getType().getName();
    }

    /**
     * Get the authentication properties of the authenticator endpoint of the user defined authenticator.
     *
     * @return Authentication properties of the authenticator endpoint.
     */
    public Map<String, String> getAuthenticatorEndpointAuthenticationProperties() {

        Map<String, String> propertyMap = new HashMap<>();
        for (AuthProperty prop: endpointConfig.getAuthentication().getProperties()) {
            propertyMap.put(prop.getName(), prop.getValue());
        }
        return propertyMap;
    }

    /**
     * Get the allowed headers of the authenticator endpoint of the user defined authenticator.
     *
     * @return Allowed headers of the authenticator endpoint.
     */
    public List<String> getAuthenticatorEndpointAllowedHeaders() {

        return endpointConfig.getAllowedHeaders();
    }

    /**
     * Get the allowed parameters of the authenticator endpoint of the user defined authenticator.
     *
     * @return Allowed parameters of the authenticator endpoint.
     */
    public List<String> getAuthenticatorEndpointAllowedParameters() {

        return endpointConfig.getAllowedParameters();
    }

    /**
     * UserDefinedAuthenticatorEndpointConfig builder.
     */
    public static class UserDefinedAuthenticatorEndpointConfigBuilder {

        private String uri;
        private String authenticationType;
        private Map<String, String> authenticationProperties;
        private List<String> allowedHeaders;
        private List<String> allowedParameters;
        private EndpointConfig endpointConfig;

        public UserDefinedAuthenticatorEndpointConfigBuilder() {
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder authenticationProperties(
                Map<String, String> authentication) {

            this.authenticationProperties = authentication;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder authenticationType(String authenticationType) {

            this.authenticationType = authenticationType;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder allowedHeaders(List<String> allowedHeaders) {

            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder allowedParameters(List<String> allowedParameters) {

            this.allowedParameters = allowedParameters;
            return this;
        }
        
        public UserDefinedAuthenticatorEndpointConfig build() {

            EndpointConfig.EndpointConfigBuilder endpointConfigBuilder = new EndpointConfig.EndpointConfigBuilder();
            endpointConfigBuilder.uri(uri);
            endpointConfigBuilder.authentication(resolveAuthentication());
            endpointConfigBuilder.allowedHeaders(allowedHeaders);
            endpointConfigBuilder.allowedParameters(allowedParameters);
            endpointConfig = endpointConfigBuilder.build();

            return new UserDefinedAuthenticatorEndpointConfig(this);
        }

        private Authentication resolveAuthentication() {

            if (Objects.equals(authenticationType, Authentication.Type.NONE.getName())) {
                return new Authentication.NoneAuthBuilder().build();
            }

            if (authenticationProperties == null || authenticationProperties.isEmpty()) {
                if (authenticationType != null) {
                    LOG.debug("Ignoring the authentication type: " + authenticationType +
                            " since the authentication properties are not provided.");
                }
                return null;
            }

            // Both authenticationType and authenticationProperties is required to build the authentication.
            return new Authentication.AuthenticationBuilder()
                    .type(Authentication.Type.valueOfName(authenticationType))
                    .properties(authenticationProperties)
                    .build();
        }
    }
}
