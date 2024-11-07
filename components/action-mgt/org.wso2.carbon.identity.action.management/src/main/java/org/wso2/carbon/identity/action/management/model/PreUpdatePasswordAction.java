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

import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;

import java.util.Map;

/**
 * PreUpdatePasswordAction.
 */
public class PreUpdatePasswordAction extends Action {

    /**
     * Password Format Enum.
     * Defines the category of the password sharing types.
     */
    public enum PasswordFormat {

        PLAIN_TEXT,
        SHA256_HASHED;
    }

    private final PasswordFormat passwordSharingFormat;
    private final String certificate;

    public PreUpdatePasswordAction(ResponseBuilder responseBuilder) {

        super(responseBuilder);
        this.passwordSharingFormat = responseBuilder.passwordSharingFormat;
        this.certificate = responseBuilder.certificate;
    }

    public PreUpdatePasswordAction(RequestBuilder requestBuilder) {

        super(requestBuilder);
        this.passwordSharingFormat = requestBuilder.passwordSharingFormat;
        this.certificate = requestBuilder.certificate;
    }

    public PasswordFormat getPasswordSharingFormat() {

        return passwordSharingFormat;
    }

    public String getCertificate() {

        return certificate;
    }

    /**
     * Retrieves a map of property names and values from the endpoint configuration, along with the password
     * sharing format and certificate if they are set.
     *
     * @return A map containing the endpoint properties, password sharing format, and certificate.
     */
    @Override
    public Map<String, String> getPropertiesMap() {

        Map<String, String> propertiesMap = super.getPropertiesMap();
        if (getPasswordSharingFormat() != null) {
            propertiesMap.put(ActionMgtConstants.PASSWORD_SHARING_FORMAT_PROPERTY, getPasswordSharingFormat().name());
        }
        if (getCertificate() != null) {
            propertiesMap.put(ActionMgtConstants.CERTIFICATE_PROPERTY, getCertificate());
        }

        return propertiesMap;
    }

    /**
     * Response Builder for PreUpdatePasswordAction.
     */
    public static class ResponseBuilder extends ActionResponseBuilder {

        private PasswordFormat passwordSharingFormat;
        private String certificate;

        public ResponseBuilder() {
        }

        public ResponseBuilder passwordSharingFormat(PasswordFormat passwordSharingFormat) {

            this.passwordSharingFormat = passwordSharingFormat;
            return this;
        }

        public ResponseBuilder certificate(String certificate) {

            this.certificate = certificate;
            return this;
        }

        @Override
        public ResponseBuilder id(String id) {

            super.id(id);
            return this;
        }

        @Override
        public ResponseBuilder type(ActionTypes type) {

            super.type(type);
            return this;
        }

        @Override
        public ResponseBuilder name(String name) {

            super.name(name);
            return this;
        }

        @Override
        public ResponseBuilder description(String description) {

            super.description(description);
            return this;
        }

        @Override
        public ResponseBuilder status(Status status) {

            super.status(status);
            return this;
        }

        @Override
        public ResponseBuilder endpoint(EndpointConfig endpoint) {

            super.endpoint(endpoint);
            return this;
        }

        /**
         * Sets properties from a given map to the relevant attributes in the builder.
         * Based on the provided properties, this method configures the {@link EndpointConfig}
         * with the URI and the {@link Authentication} object.
         *
         * @param propertiesMap A map containing the endpoint URI, authentication type, and authentication properties.
         * @return The current {@link ActionResponseBuilder} instance with the configured attributes.
         */
        @Override
        public ActionResponseBuilder setPropertiesToAttributes(Map<String, String> propertiesMap) {

            if (propertiesMap.isEmpty()) {
                return this;
            }

            // Set the endpoint properties to the common attributes.
            super.setPropertiesToAttributes(propertiesMap);
            // Set other properties to the specific attributes of PRE_UPDATE_PASSWORD action type.

            return this.passwordSharingFormat(PasswordFormat
                            .valueOf(propertiesMap.get(ActionMgtConstants.PASSWORD_SHARING_FORMAT_PROPERTY)))
                    .certificate(propertiesMap.get(ActionMgtConstants.CERTIFICATE_PROPERTY));
        }

        @Override
        public PreUpdatePasswordAction build() {

            return new PreUpdatePasswordAction(this);
        }
    }

    /**
     * Request Builder for PreUpdatePasswordAction.
     */
    public static class RequestBuilder extends ActionRequestBuilder {

        private PasswordFormat passwordSharingFormat;
        private String certificate;

        public RequestBuilder() {
        }

        public RequestBuilder passwordSharingFormat(PasswordFormat passwordSharingFormat) {

            this.passwordSharingFormat = passwordSharingFormat;
            return this;
        }

        public RequestBuilder certificate(String certificate) {

            this.certificate = certificate;
            return this;
        }

        @Override
        public RequestBuilder name(String name) {

            super.name(name);
            return this;
        }

        @Override
        public RequestBuilder description(String description) {

            super.description(description);
            return this;
        }

        @Override
        public RequestBuilder endpoint(EndpointConfig endpoint) {

            super.endpoint(endpoint);
            return this;
        }

        @Override
        public PreUpdatePasswordAction build() {

            return new PreUpdatePasswordAction(this);
        }
    }
}
