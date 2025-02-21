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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * This class contains the authentication methods.
 * The authentication methods are used to authenticate the HTTP requests.
 */
public final class AuthMethods {

    private AuthMethods() {

    }

    /**
     * This interface defines how authentication methods are applied to http request.
     */
    public interface AuthMethod {

        void applyAuth(HttpPost httpPost);

        String getAuthType();
    }

    /**
     * This class applies bearer authentication to the http request.
     */
    public static final class BearerAuth implements AuthMethod {

        private String token;

        public BearerAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.stream()
                    .filter(authProperty -> Authentication.Property.ACCESS_TOKEN.getName().
                            equals(authProperty.getName()))
                    .findFirst()
                    .ifPresent(authProperty -> this.token = authProperty.getValue());
        }

        @Override
        public void applyAuth(HttpPost httpPost) {

            httpPost.setHeader("Authorization", "Bearer " + token);
        }

        @Override
        public String getAuthType() {

            return Authentication.Type.BEARER.getName();
        }
    }

    /**
     * This class applies basic authentication to the http request.
     */
    public static final class BasicAuth implements AuthMethod {

        private String username;
        private String password;

        public BasicAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.forEach(authProperty -> {
                if (Authentication.Property.USERNAME.getName().equals(authProperty.getName())) {
                    this.username = authProperty.getValue();
                } else if (Authentication.Property.PASSWORD.getName().equals(authProperty.getName())) {
                    this.password = authProperty.getValue();
                }
            });
        }

        @Override
        public void applyAuth(HttpPost httpPost) {

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
            httpPost.setHeader("Authorization", authHeader);
        }

        @Override
        public String getAuthType() {

            return Authentication.Type.BASIC.getName();
        }
    }

    /**
     * This class applies API Key based authentication to the http request.
     */
    public static final class APIKeyAuth implements AuthMethod {

        private String apiHeader;
        private String apiKey;

        public APIKeyAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.forEach(authProperty -> {
                if (Authentication.Property.HEADER.getName().equals(authProperty.getName())) {
                    this.apiHeader = authProperty.getValue();
                } else if (Authentication.Property.VALUE.getName().equals(authProperty.getName())) {
                    this.apiKey = authProperty.getValue();
                }
            });
        }

        @Override
        public void applyAuth(HttpPost httpPost) {

            httpPost.setHeader(apiHeader, apiKey);
        }

        @Override
        public String getAuthType() {

            return Authentication.Type.API_KEY.getName();
        }
    }
}
