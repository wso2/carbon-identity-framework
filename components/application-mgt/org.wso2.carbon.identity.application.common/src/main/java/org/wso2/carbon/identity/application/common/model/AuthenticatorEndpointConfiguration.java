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

import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.Authentication.Property;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * The authenticator endpoint configuration model.
 */
public class AuthenticatorEndpointConfiguration {

    private String uri;
    private String authenticationType;
    private HashMap<String, String> authenticationProperties;

    public AuthenticatorEndpointConfiguration() {
    }

    public AuthenticatorEndpointConfiguration(AuthenticatorEndpointConfigurationBuilder builder) {

        this.uri = builder.uri;
        this.authenticationProperties = builder.authenticationProperties;
        this.authenticationType = builder.authenticationType;
    }

    public String getUri() {

        return uri;
    }

    public HashMap<String, String> getAuthenticationProperties() {

        return authenticationProperties;
    }
    
    public String getAuthenticationType() {

        return authenticationType;
    }

    /**
     * AuthenticatorEndpointConfiguration builder.
     */
    public static class AuthenticatorEndpointConfigurationBuilder {

        private String uri;
        private String authenticationType;
        private HashMap<String, String> authenticationProperties;

        public AuthenticatorEndpointConfigurationBuilder() {
        }

        public AuthenticatorEndpointConfigurationBuilder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public AuthenticatorEndpointConfigurationBuilder authenticationProperties(
                HashMap<String, String> authentication) {

            this.authenticationProperties = authentication;
            return this;
        }

        public AuthenticatorEndpointConfigurationBuilder authenticationType(String authenticationType) {

            this.authenticationType = authenticationType;
            return this;
        }
        
        public AuthenticatorEndpointConfiguration build() {

            try {
                EndpointConfig.EndpointConfigBuilder actionEndpointConfigBuilder =
                        new EndpointConfig.EndpointConfigBuilder();
                actionEndpointConfigBuilder.uri(this.uri);
                actionEndpointConfigBuilder.authentication(buildAuthentication(
                        this.authenticationType, this.authenticationProperties));
                actionEndpointConfigBuilder.build();
            } catch (Exception e) {
                throw new IllegalArgumentException("Endpoint configuration of the authenticator is not in expected " +
                        "format.", e);
            }
            return new AuthenticatorEndpointConfiguration(this);
        }
        
        private Authentication buildAuthentication(String authenticationType,
                        HashMap<String, String> properties) throws NoSuchElementException {

            switch (Authentication.Type.valueOf(authenticationType)) {
                case BASIC:
                    return new Authentication.BasicAuthBuilder(
                            getProperty(properties, Property.USERNAME.getName()),
                            getProperty(properties, Property.PASSWORD.getName())).build();
                case BEARER:
                    return new Authentication.BearerAuthBuilder(
                            getProperty(properties, Property.ACCESS_TOKEN.getName())).build();
                case API_KEY:
                    return new Authentication.APIKeyAuthBuilder(
                            getProperty(properties, Property.HEADER.getName()),
                            getProperty(properties, Property.VALUE.getName())).build();
                case NONE:
                    return new Authentication.NoneAuthBuilder().build();
                default:
                    throw new IllegalArgumentException();
            }    
        }

        private String getProperty(HashMap<String, String> actionEndpointProperties, String propertyName) {

            if (actionEndpointProperties.containsKey(propertyName)) {
                return actionEndpointProperties.get(propertyName);
            } else {
                throw new NoSuchElementException("Property " + propertyName +
                        " is not found in the endpoint configuration.");
            }
        }
    }
}
