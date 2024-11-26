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

package org.wso2.carbon.identity.action.management.dao.model;

import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action Data Transfer Object.
 */
public class ActionDTO {

    private String id;
    private Action.ActionTypes type;
    private final String name;
    private final String description;
    private final Action.Status status;
    private EndpointConfig endpoint;
    private Map<String, Object> properties;

    public ActionDTO(Builder builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
        this.endpoint = builder.endpoint;
        this.properties = builder.properties;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setType(Action.ActionTypes type) {

        this.type = type;
    }

    public Action.ActionTypes getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }

    public org.wso2.carbon.identity.action.management.model.Action.Status getStatus() {

        return status;
    }

    public EndpointConfig getEndpoint() {

        return endpoint;
    }

    public void setAuthenticationProperties(List<AuthProperty> authProperties) {

        if (this.endpoint != null && this.endpoint.getAuthentication() != null) {
            Map<String, String> propertyMap = authProperties.stream()
                    .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));

            this.endpoint = new EndpointConfig.EndpointConfigBuilder()
                    .uri(this.endpoint.getUri())
                    .authentication(new Authentication.AuthenticationBuilder()
                            .type(this.endpoint.getAuthentication().getType())
                            .properties(propertyMap)
                            .build())
                    .build();
        }
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public Object getProperty(String propertyName) {

        if (properties == null) {
            return null;
        }

        return properties.get(propertyName);
    }

    /**
     * Builder for Extended Action.
     */
    public static class Builder {

        private String id;
        private Action.ActionTypes type;
        private String name;
        private String description;
        private Action.Status status;
        private EndpointConfig endpoint;
        private Map<String, Object> properties;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder type(Action.ActionTypes type) {

            this.type = type;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder description(String description) {

            this.description = description;
            return this;
        }

        public Builder status(Action.Status status) {

            this.status = status;
            return this;
        }

        public Builder endpoint(EndpointConfig endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        public void setEndpointAndProperties(Map<String, String> properties) throws ActionMgtException {

            Authentication authentication;
            Authentication.Type authnType =
                    Authentication.Type.valueOf(properties.remove(ActionMgtConstants.AUTHN_TYPE_PROPERTY));
            switch (authnType) {
                case BASIC:
                    authentication = new Authentication.BasicAuthBuilder(
                            properties.remove(Authentication.Property.USERNAME.getName()),
                            properties.remove(Authentication.Property.PASSWORD.getName())).build();
                    break;
                case BEARER:
                    authentication = new Authentication.BearerAuthBuilder(
                            properties.remove(Authentication.Property.ACCESS_TOKEN.getName())).build();
                    break;
                case API_KEY:
                    authentication = new Authentication.APIKeyAuthBuilder(
                            properties.remove(Authentication.Property.HEADER.getName()),
                            properties.remove(Authentication.Property.VALUE.getName())).build();
                    break;
                case NONE:
                    authentication = new Authentication.NoneAuthBuilder().build();
                    break;
                default:
                    throw new ActionMgtServerException("Authentication type is not defined for the Action Endpoint.");
            }

            this.endpoint = new EndpointConfig.EndpointConfigBuilder()
                    .uri(properties.remove(ActionMgtConstants.URI_PROPERTY))
                    .authentication(authentication)
                    .build();
            // Add remaining properties as action properties.
            this.properties = properties.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public Builder properties(Map<String, Object> properties) {

            this.properties = properties;
            return this;
        }

        public Builder property(String propertyName, Object propertyValue) {

            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.put(propertyName, propertyValue);
            return this;
        }

        public ActionDTO build() {

            return new ActionDTO(this);
        }
    }
}
