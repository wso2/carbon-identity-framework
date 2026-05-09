/*
 * Copyright (c) 2024-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementExceptionHandler;
import org.wso2.carbon.identity.action.management.internal.util.ActionSecretProcessor;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
        API_KEY("apiKey", "API_KEY"),
        CLIENT_CREDENTIAL("clientCredential", "CLIENT_CREDENTIAL"),
        PASSWORD_CREDENTIAL("passwordCredential", "PASSWORD_CREDENTIAL");

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

        public static Type valueOfName(String name) {

            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Authentication type cannot be null or empty.");
            }

            for (Type type : Type.values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid authentication type: " + name);
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
        ACCESS_TOKEN("accessToken"),
        CLIENT_ID("clientId"),
        CLIENT_SECRET("clientSecret"),
        TOKEN_ENDPOINT("tokenEndpoint"),
        SCOPES("scopes"),
        INTERNAL_ACCESS_TOKEN("internalAccessToken"),
        INTERNAL_REFRESH_TOKEN("internalRefreshToken");

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
    private List<AuthProperty> internalAuthProperties = null;
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

    public Authentication(ClientCredentialAuthBuilder clientCredentialAuthBuilder) {

        this.type = clientCredentialAuthBuilder.type;
        this.properties = clientCredentialAuthBuilder.properties;
        this.internalAuthProperties = clientCredentialAuthBuilder.internalProperties;
    }

    public Authentication(PasswordCredentialAuthBuilder passwordCredentialAuthBuilder) {

        this.type = passwordCredentialAuthBuilder.type;
        this.properties = passwordCredentialAuthBuilder.properties;
        this.internalAuthProperties = passwordCredentialAuthBuilder.internalProperties;
    }

    public Type getType() {

        return type;
    }

    public List<AuthProperty> getProperties() {

        return properties;
    }

    public List<AuthProperty> getInternalAuthProperties() {

        return internalAuthProperties;
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
                    secretProcessor.decryptAssociatedSecrets(this, actionId);
        } catch (SecretManagementException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_DECRYPTING_ACTION_ENDPOINT_AUTH_PROPERTIES, e);
        }
    }

    public AuthProperty getPropertyWithDecryptedValue(String actionId, String propertyName) {

        for (AuthProperty authProperty : this.getProperties()) {
            if (StringUtils.equalsIgnoreCase(propertyName, authProperty.getName())) {
                try {
                    return secretProcessor.decryptProperty(authProperty, this.getType().name(), actionId);
                } catch (SecretManagementException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public AuthProperty getInternalPropertyWithDecryptedValue(String actionId, String propertyName) {

        if (this.getInternalAuthProperties() == null || this.getInternalAuthProperties().isEmpty()) {
            return null;
        }
        for (AuthProperty authProperty : this.getInternalAuthProperties()) {
            if (StringUtils.equalsIgnoreCase(propertyName, authProperty.getName())) {
                try {
                    return secretProcessor.decryptProperty(authProperty, this.getType().name(), actionId);
                } catch (SecretManagementException e) {
                    return null;
                }
            }
        }
        return null;
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

    /**
     * Client Credential Authentication builder.
     */
    public static class ClientCredentialAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();
        private final List<AuthProperty> internalProperties = new ArrayList<>();

        public ClientCredentialAuthBuilder(String clientId, String clientSecret, String tokenEndpoint, String scopes,
                                           String internalAccessToken, String internalRefreshToken) {

            this.type = Type.CLIENT_CREDENTIAL;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_ID.getName()).value(clientId).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_SECRET.getName()).value(clientSecret).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.TOKEN_ENDPOINT.getName()).value(tokenEndpoint).isConfidential(false).build());
            if (StringUtils.isNotBlank(scopes)) {
                this.properties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(Property.SCOPES.getName()).value(scopes).isConfidential(false).build());
            }
            this.internalProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.INTERNAL_ACCESS_TOKEN.getName()).value(internalAccessToken).isConfidential(true)
                    .build());
            this.internalProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.INTERNAL_REFRESH_TOKEN.getName()).value(internalRefreshToken).isConfidential(true)
                    .build());
        }

        public ClientCredentialAuthBuilder(String clientId, String clientSecret, String tokenEndpoint, String scopes) {

            this.type = Type.CLIENT_CREDENTIAL;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_ID.getName()).value(clientId).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_SECRET.getName()).value(clientSecret).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.TOKEN_ENDPOINT.getName()).value(tokenEndpoint).isConfidential(false).build());
            if (StringUtils.isNotBlank(scopes)) {
                this.properties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(Property.SCOPES.getName()).value(scopes).isConfidential(false).build());
            }
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }

    /**
     * Password Credential Authentication builder.
     */
    public static class PasswordCredentialAuthBuilder {

        private final Type type;
        private final List<AuthProperty> properties = new ArrayList<>();
        private final List<AuthProperty> internalProperties = new ArrayList<>();

        public PasswordCredentialAuthBuilder(String clientId, String clientSecret, String tokenEndpoint, String scopes,
                                             String username, String password,
                                             String internalAccessToken, String internalRefreshToken) {

            this.type = Type.PASSWORD_CREDENTIAL;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_ID.getName()).value(clientId).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_SECRET.getName()).value(clientSecret).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.TOKEN_ENDPOINT.getName()).value(tokenEndpoint).isConfidential(false).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.USERNAME.getName()).value(username).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.PASSWORD.getName()).value(password).isConfidential(true).build());
            if (StringUtils.isNotBlank(scopes)) {
                this.properties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(Property.SCOPES.getName()).value(scopes).isConfidential(false).build());
            }
            this.internalProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.INTERNAL_ACCESS_TOKEN.getName()).value(internalAccessToken).isConfidential(true)
                    .build());
            this.internalProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.INTERNAL_REFRESH_TOKEN.getName()).value(internalRefreshToken).isConfidential(true)
                    .build());
        }

        public PasswordCredentialAuthBuilder(String clientId, String clientSecret, String tokenEndpoint, String scopes,
                                             String username, String password) {

            this.type = Type.PASSWORD_CREDENTIAL;
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_ID.getName()).value(clientId).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.CLIENT_SECRET.getName()).value(clientSecret).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.TOKEN_ENDPOINT.getName()).value(tokenEndpoint).isConfidential(false).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.USERNAME.getName()).value(username).isConfidential(true).build());
            this.properties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(Property.PASSWORD.getName()).value(password).isConfidential(true).build());
            if (StringUtils.isNotBlank(scopes)) {
                this.properties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(Property.SCOPES.getName()).value(scopes).isConfidential(false).build());
            }
        }

        public Authentication build() {

            return new Authentication(this);
        }
    }

    /**
     * This builder build endpoint by taking the authentication type and properties as input.
     */
    public static class AuthenticationBuilder {

        private Type authType;
        private Map<String, String> authPropertiesMap;

        public AuthenticationBuilder type(Type type) {

            this.authType = type;
            return this;
        }

        public AuthenticationBuilder properties(Map<String, String> authPropertiesMap) {

            this.authPropertiesMap = authPropertiesMap;
            return this;
        }

        public Authentication build() {

            switch (authType) {
                case BASIC:
                    return new Authentication.BasicAuthBuilder(
                            getProperty(Type.BASIC, authPropertiesMap, Property.USERNAME.getName()),
                            getProperty(Type.BASIC, authPropertiesMap, Property.PASSWORD.getName())).build();
                case BEARER:
                    return new Authentication.BearerAuthBuilder(
                            getProperty(Type.BEARER, authPropertiesMap, Property.ACCESS_TOKEN.getName())).build();
                case API_KEY:
                    return new Authentication.APIKeyAuthBuilder(
                            getProperty(Type.API_KEY, authPropertiesMap, Property.HEADER.getName()),
                            getProperty(Type.API_KEY, authPropertiesMap, Property.VALUE.getName())).build();
                case CLIENT_CREDENTIAL:
                    return new Authentication.ClientCredentialAuthBuilder(
                            getProperty(Type.CLIENT_CREDENTIAL, authPropertiesMap, Property.CLIENT_ID.getName()),
                            getProperty(Type.CLIENT_CREDENTIAL, authPropertiesMap, Property.CLIENT_SECRET.getName()),
                            getProperty(Type.CLIENT_CREDENTIAL, authPropertiesMap, Property.TOKEN_ENDPOINT.getName()),
                            getOptionalProperty(authPropertiesMap, Property.SCOPES.getName()),
                            getOptionalProperty(authPropertiesMap, Property.INTERNAL_ACCESS_TOKEN.getName()),
                            getOptionalProperty(authPropertiesMap, Property.INTERNAL_REFRESH_TOKEN.getName())
                    ).build();
                case PASSWORD_CREDENTIAL:
                    return new Authentication.PasswordCredentialAuthBuilder(
                            getProperty(Type.PASSWORD_CREDENTIAL, authPropertiesMap, Property.CLIENT_ID.getName()),
                            getProperty(Type.PASSWORD_CREDENTIAL, authPropertiesMap, Property.CLIENT_SECRET.getName()),
                            getProperty(Type.PASSWORD_CREDENTIAL, authPropertiesMap,
                                    Property.TOKEN_ENDPOINT.getName()),
                            getOptionalProperty(authPropertiesMap, Property.SCOPES.getName()),
                            getProperty(Type.PASSWORD_CREDENTIAL, authPropertiesMap, Property.USERNAME.getName()),
                            getProperty(Type.PASSWORD_CREDENTIAL, authPropertiesMap, Property.PASSWORD.getName()),
                            getOptionalProperty(authPropertiesMap, Property.INTERNAL_ACCESS_TOKEN.getName()),
                            getOptionalProperty(authPropertiesMap, Property.INTERNAL_REFRESH_TOKEN.getName())
                    ).build();
                case NONE:
                    return new Authentication.NoneAuthBuilder().build();
                default:
                    throw new IllegalArgumentException(String.format("An invalid authentication type '%s' is " +
                            "provided for the authentication configuration of the endpoint.", authType.name()));
            }
        }

        private String getProperty(Authentication.Type authType,  Map<String, String> actionEndpointProperties,
                                   String propertyName) {

            if (actionEndpointProperties != null && actionEndpointProperties.containsKey(propertyName)) {
                String propValue = actionEndpointProperties.get(propertyName);
                if (StringUtils.isNotBlank(propValue)) {
                    return propValue;
                }
                throw new IllegalArgumentException(String.format("The Property %s cannot be blank.", propertyName));
            }

            throw new NoSuchElementException(String.format("The property %s must be provided as an authentication " +
                    "property for the %s authentication type.", propertyName, authType.name()));
        }

        private String getOptionalProperty(Map<String, String> actionEndpointProperties, String propertyName) {

            if (actionEndpointProperties != null && actionEndpointProperties.containsKey(propertyName)) {
                String propValue = actionEndpointProperties.get(propertyName);
                if (StringUtils.isNotBlank(propValue)) {
                    return propValue;
                }
            }
            return null;
        }
    }
}
