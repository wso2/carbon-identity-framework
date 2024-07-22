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

package org.wso2.carbon.identity.action.execution.util;

import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.identity.action.management.model.AuthProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public final class AuthMethods {

    private AuthMethods() {

    }

    public interface AuthMethod {

        void applyAuth(HttpPost httpPost);

        String getAuthType();
    }

    public static final class BearerAuth implements AuthMethod {

        private String token;

        public BearerAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.stream()
                    .filter(authProperty -> "ACCESS_TOKEN".equals(authProperty.getName()))
                    .findFirst()
                    .ifPresent(authProperty -> this.token = authProperty.getValue());
        }

        @Override
        public void applyAuth(HttpPost httpPost) {

            httpPost.setHeader("Authorization", "Bearer " + token);
        }

        @Override
        public String getAuthType() {

            return "BEARER";
        }
    }

    public static final class BasicAuth implements AuthMethod {

        private String username;
        private String password;

        public BasicAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.forEach(authProperty -> {
                switch (authProperty.getName()) {
                    case "USERNAME":
                        this.username = authProperty.getValue();
                        break;
                    case "PASSWORD":
                        this.password = authProperty.getValue();
                        break;
                    default:
                        break;
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

            return "BASIC";
        }
    }

    public static final class APIKeyAuth implements AuthMethod {

        private String apiHeader;
        private String apiKey;

        public APIKeyAuth(List<AuthProperty> authPropertyList) {

            authPropertyList.forEach(authProperty -> {
                switch (authProperty.getName()) {
                    case "HEADER":
                        this.apiHeader = authProperty.getValue();
                        break;
                    case "VALUE":
                        this.apiKey = authProperty.getValue();
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public void applyAuth(HttpPost httpPost) {

            httpPost.setHeader(apiHeader, apiKey);
        }

        @Override
        public String getAuthType() {

            return "API-KEY";
        }
    }
}
