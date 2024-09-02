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

package org.wso2.carbon.identity.action.management.model;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication class which hold supported authentication types and their properties.
 */
public class Authentication {

    /**
     * Authentication Type.
     */
    public enum Type {

        NONE("none", "NONE"),
        BEARER("bearer", "BEARER"),
        BASIC("basic", "BASIC"),
        API_KEY("apiKey", "API_KEY");

        private final String pathParam;
        private final String name;

        Type(String pathParam, String name) {

            this.pathParam = pathParam;
            this.name = name;
        }

        public String getPathParam() {

            return pathParam;
        }

        public String getName() {

            return name;
        }
    }

    /**
     * Authentication Property Enum.
     */
    public enum Property {

        USERNAME("username"),
        PASSWORD("password"),
        HEADER("header"),
        VALUE("value"),
        ACCESS_TOKEN("accessToken");

        private final String name;

        Property(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }

    private final Type type;
    private List<AuthProperty> properties = null;
    private final ActionSecretProcessor secretProcessor = new ActionSecretProcessor();

    public Authentication(BasicAuthBuilder basicAuthBuilder) {

        this.type = basicAuthBuilder.type;
        this.properties = basicAuthBuilder.properties;
    }

    public Authentication(BearerAuthBuilder bearerAuthBuilder) {

        this.type = bearerAuthBuilder.type;
        this.properties = bearerAuthBuilder.properties;
    }

    public Authentication(APIKeyAuthBuilder apiKeyAuthBuilder) {

        this.type = apiKeyAuthBuilder.type;
        this.properties = apiKeyAuthBuilder.properties;
    }

    public Authentication(NoneAuthBuilder noneAuthBuilder) {

        this.type = noneAuthBuilder.type;
        this.properties = noneAuthBuilder.properties;
    }

    public Type getType() {

        return type;
    }

    public List<AuthProperty> getProperties() {

        return properties;
    }

    public AuthProperty getProperty(Property propertyName) {

        return this.properties.stream()
                .filter(property -> propertyName.getName().equals(property.getName()))
                .findFirst()
                .orElse(null);
    }

    public List<AuthProperty> getPropertiesWithDecryptedValues(String actionId) throws ActionMgtException {

        try {
            return CollectionUtils.isEmpty(properties) ? properties :
                    secretProcessor.decryptAssociatedSecrets(properties, type.getName(), actionId);
        } catch (SecretManagementException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_DECRYPTING_ACTION_ENDPOINT_AUTH_PROPERTIES, e);
        }
    }

    public List<AuthProperty> getPropertiesWithSecretReferences(String actionId) throws SecretManagementException {

        return CollectionUtils.isEmpty(properties) ? properties :
                secretProcessor.getPropertiesWithSecretReferences(properties, actionId, type.name());
    }

    /**
     * Basic Authentication builder.
     */
    public static class BasicAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();

        public BasicAuthBuilder(String username, String password) {
            this.type = Type.BASIC;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.USERNAME.getName()).value(username).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.PASSWORD.getName()).value(password).isConfidential(true).build());
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }
    
    /**
     * Bearer Authentication builder.
     */
    public static class BearerAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();

        public BearerAuthBuilder(String accessToken) {
            this.type = Type.BEARER;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.ACCESS_TOKEN.getName()).value(accessToken).isConfidential(true).build());
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }

    /**
     * API Key Authentication builder.
     */
    public static class APIKeyAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();

        public APIKeyAuthBuilder(String header, String value) {

            this.type = Type.API_KEY;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.HEADER.getName()).value(header).isConfidential(false).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.VALUE.getName()).value(value).isConfidential(true).build());
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }

    /**
     * None Authentication builder.
     */
    public static class NoneAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();

        public NoneAuthBuilder() {

            this.type = Type.NONE;
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }
}
