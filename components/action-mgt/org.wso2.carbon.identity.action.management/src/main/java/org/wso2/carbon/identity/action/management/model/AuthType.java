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

import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AuthType.
 */
public class AuthType {

    /**
     * Authentication Type.
     */
    public enum AuthenticationType {

        NONE(
                "none",
                "NONE",
                new ArrayList<>()
        ),
        BEARER(
                "bearer",
                "BEARER",
                Arrays.asList(
                        new AuthProperty.AuthPropertyBuilder()
                                .name("accessToken")
                                .isConfidential(true).build())
        ),
        BASIC(
                "basic",
                "BASIC",
                Arrays.asList(
                        new AuthProperty.AuthPropertyBuilder()
                                .name("username")
                                .isConfidential(true).build(),
                        new AuthProperty.AuthPropertyBuilder()
                                .name("password")
                                .isConfidential(true).build())
        ),
        API_KEY(
                "apiKey",
                "API_KEY",
                Arrays.asList(
                        new AuthProperty.AuthPropertyBuilder()
                                .name("header")
                                .isConfidential(false).build(),
                        new AuthProperty.AuthPropertyBuilder()
                                .name("value")
                                .isConfidential(true).build())
        );

        private final String pathParam;
        private final String type;
        private final List<AuthProperty> properties;

        AuthenticationType(String pathParam, String type, List<AuthProperty>  properties) {

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

        public List<AuthProperty> getProperties() {

            return properties;
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

    public void setProperties(List<AuthProperty> properties) {

        this.properties = properties;
    }

    public List<AuthProperty> getProperties() {

        return properties;
    }

    public List<AuthProperty> getPropertiesWithDecryptedValues(String actionId) throws SecretManagementException {

        if (properties != null) {

            return secretProcessor.decryptAssociatedSecrets(properties, actionId, type.name());
        }
        return null;
    }

    public List<AuthProperty> getPropertiesWithSecretReferences(String actionId) throws SecretManagementException {

        if (properties != null) {

            return secretProcessor.getPropertiesWithSecretReferences(properties, actionId, type.name());
        }
        return null;
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
