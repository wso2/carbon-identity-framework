/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

import java.sql.Timestamp;

/**
 * Flow Extension Action.
 * <p>
 * Extends the base {@link Action} with an {@link AccessConfig} that defines what flow context data
 * is exposed to the external service and which operations are allowed.
 * </p>
 */
public class FlowExtensionAction extends Action {

    private final AccessConfig accessConfig;
    private final Certificate certificate;
    private final String iconUrl;

    public FlowExtensionAction(ResponseBuilder responseBuilder) {

        super(responseBuilder);
        this.accessConfig = responseBuilder.accessConfig;
        this.certificate = responseBuilder.certificate;
        this.iconUrl = responseBuilder.iconUrl;
    }

    public FlowExtensionAction(RequestBuilder requestBuilder) {

        super(requestBuilder);
        this.accessConfig = requestBuilder.accessConfig;
        this.certificate = requestBuilder.certificate;
        this.iconUrl = requestBuilder.iconUrl;
    }

    /**
     * Returns the default access configuration for this In-Flow Extension action.
     *
     * @return The access config, or {@code null} if not configured.
     */
    public AccessConfig getAccessConfig() {

        return accessConfig;
    }

    /**
     * Returns the external service's X.509 public certificate used for outbound JWE encryption
     * of expose-path values marked {@code encrypted: true}.
     *
     * @return The certificate, or {@code null} if not configured.
     */
    public Certificate getCertificate() {

        return certificate;
    }

    /**
     * Returns the icon URL for this In-Flow Extension action.
     *
     * @return The icon URL, or {@code null} if not configured.
     */
    public String getIconUrl() {

        return iconUrl;
    }

    /**
     * Response Builder for FlowExtensionAction.
     * Used when building from persisted data (DAO → service layer).
     */
    public static class ResponseBuilder extends ActionResponseBuilder {

        private AccessConfig accessConfig;
        private Certificate certificate;
        private String iconUrl;

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
        public ResponseBuilder actionVersion(String actionVersion) {

            super.actionVersion(actionVersion);
            return this;
        }

        @Override
        public ResponseBuilder createdAt(Timestamp createdAt) {

            super.createdAt(createdAt);
            return this;
        }

        @Override
        public ResponseBuilder updatedAt(Timestamp updatedAt) {

            super.updatedAt(updatedAt);
            return this;
        }

        @Override
        public ResponseBuilder endpoint(EndpointConfig endpoint) {

            super.endpoint(endpoint);
            return this;
        }

        @Override
        public ResponseBuilder rule(org.wso2.carbon.identity.action.management.api.model.ActionRule rule) {

            super.rule(rule);
            return this;
        }

        public ResponseBuilder accessConfig(AccessConfig accessConfig) {

            this.accessConfig = accessConfig;
            return this;
        }

        public ResponseBuilder certificate(Certificate certificate) {

            this.certificate = certificate;
            return this;
        }

        public ResponseBuilder iconUrl(String iconUrl) {

            this.iconUrl = iconUrl;
            return this;
        }

        @Override
        public FlowExtensionAction build() {

            return new FlowExtensionAction(this);
        }
    }

    /**
     * Request Builder for FlowExtensionAction.
     * Used when building from REST API request (API → service layer).
     */
    public static class RequestBuilder extends ActionRequestBuilder {

        private AccessConfig accessConfig;
        private Certificate certificate;
        private String iconUrl;

        public RequestBuilder(Action action) {

            name(action.getName());
            description(action.getDescription());
            actionVersion(action.getActionVersion());
            endpoint(action.getEndpoint());
            rule(action.getActionRule());
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

        public RequestBuilder accessConfig(AccessConfig accessConfig) {

            this.accessConfig = accessConfig;
            return this;
        }

        public RequestBuilder certificate(Certificate certificate) {

            this.certificate = certificate;
            return this;
        }

        public RequestBuilder iconUrl(String iconUrl) {

            this.iconUrl = iconUrl;
            return this;
        }

        @Override
        public FlowExtensionAction build() {

            return new FlowExtensionAction(this);
        }
    }
}
