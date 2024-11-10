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

import org.wso2.carbon.identity.certificate.management.model.Certificate;

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
    private final Certificate certificate;

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

    public Certificate getCertificate() {

        return certificate;
    }

    /**
     * Response Builder for PreUpdatePasswordAction.
     */
    public static class ResponseBuilder extends ActionResponseBuilder {

        private PasswordFormat passwordSharingFormat;
        private Certificate certificate;

        public ResponseBuilder passwordSharingFormat(PasswordFormat passwordSharingFormat) {

            this.passwordSharingFormat = passwordSharingFormat;
            return this;
        }

        public ResponseBuilder certificate(Certificate certificate) {

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
        private Certificate certificate;

        public RequestBuilder passwordSharingFormat(PasswordFormat passwordSharingFormat) {

            this.passwordSharingFormat = passwordSharingFormat;
            return this;
        }

        public RequestBuilder certificate(Certificate certificate) {

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
