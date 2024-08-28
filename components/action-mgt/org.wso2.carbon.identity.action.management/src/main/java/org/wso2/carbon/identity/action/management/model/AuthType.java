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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AuthType.
 */
public class AuthType {

    public static final String AUTH_PROPERTY_USERNAME = "username";
    public static final String AUTH_PROPERTY_PASSWORD = "password";
    public static final String AUTH_PROPERTY_HEADER = "header";
    public static final String AUTH_PROPERTY_VALUE = "value";
    public static final String AUTH_PROPERTY_ACCESS_TOKEN = "accessToken";

    /**
     * Authentication Type.
     */
    public enum AuthenticationType {

        NONE("none", "NONE", Collections.emptyList()),
        BEARER("bearer", "BEARER", Arrays.asList(AuthenticationProperty.ACCESS_TOKEN)),
        BASIC("basic", "BASIC",
                Arrays.asList(AuthenticationProperty.USERNAME, AuthenticationProperty.PASSWORD)),
        API_KEY("apiKey", "API_KEY",
                Arrays.asList(AuthenticationProperty.HEADER, AuthenticationProperty.VALUE));

        private final String pathParam;
        private final String type;
        private final List<AuthenticationProperty> properties;

        AuthenticationType(String pathParam, String type, List<AuthenticationProperty>  properties) {

            this.pathParam = pathParam;
            this.type = type;
            this.properties = properties;
        }

        public String getPathParam() {

            return pathParam;
        }

        public String getType() {

            return type;
        }

        public List<AuthenticationProperty> getProperties() {

            return properties;
        }

        /**
         * Authentication Property.
         */
        public enum AuthenticationProperty {

            ACCESS_TOKEN(AUTH_PROPERTY_ACCESS_TOKEN, true),
            USERNAME(AUTH_PROPERTY_USERNAME, true),
            PASSWORD(AUTH_PROPERTY_PASSWORD, true),
            HEADER(AUTH_PROPERTY_HEADER, false),
            VALUE(AUTH_PROPERTY_VALUE, true);

            private final String name;
            private final boolean isConfidential;

            AuthenticationProperty(String name, boolean isConfidential) {
                this.name = name;
                this.isConfidential = isConfidential;
            }

            public String getName() {
                return name;
            }

            public boolean getIsConfidential() {
                return isConfidential;
            }
        }
    }

    private AuthenticationType type;
    private List<AuthProperty> properties = null;
    private final ActionSecretProcessor secretProcessor = new ActionSecretProcessor();

    public AuthType() {
    }

    public AuthType(AuthTypeBuilder authTypeBuilder) {

        this.type = authTypeBuilder.type;
        this.properties = authTypeBuilder.properties;
    }

    public AuthenticationType getType() {

        return type;
    }

    public List<AuthProperty> getProperties() {

        return properties;
    }

    public List<AuthProperty> getPropertiesWithDecryptedValues(String actionId) throws ActionMgtException {

        try {
            return CollectionUtils.isEmpty(properties) ? properties :
                    secretProcessor.decryptAssociatedSecrets(properties, type.getType(), actionId);
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
     * AuthType builder.
     */
    public static class AuthTypeBuilder {

        private AuthenticationType type;
        private List<AuthProperty> properties = null;

        public AuthTypeBuilder() {
        }

        public AuthTypeBuilder type(AuthenticationType type) {

            this.type = type;
            return this;
        }

        public AuthTypeBuilder properties(List<AuthProperty> properties) {

            this.properties = properties;
            return this;
        }

        public AuthTypeBuilder addProperty(AuthProperty authProperty) {

            if (this.properties == null) {
                this.properties = new ArrayList<>();
            }
            this.properties.add(authProperty);
            return this;
        }

        public AuthType build() {

            return new AuthType(this);
        }
    }
}
