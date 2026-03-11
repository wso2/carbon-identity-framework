/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-Flow Extension Action.
 * <p>
 * Extends the base {@link Action} with an {@link AccessConfig} that defines what flow context data
 * is exposed to the external service and which operations are allowed.
 * </p>
 */
public class InFlowExtensionAction extends Action {

    private final AccessConfig accessConfig;
    private final Encryption encryption;
    private final Map<String, AccessConfig> flowTypeOverrides;

    public InFlowExtensionAction(ResponseBuilder responseBuilder) {

        super(responseBuilder);
        this.accessConfig = responseBuilder.accessConfig;
        this.encryption = responseBuilder.encryption;
        this.flowTypeOverrides = responseBuilder.flowTypeOverrides != null
                ? Collections.unmodifiableMap(new HashMap<>(responseBuilder.flowTypeOverrides))
                : Collections.emptyMap();
    }

    public InFlowExtensionAction(RequestBuilder requestBuilder) {

        super(requestBuilder);
        this.accessConfig = requestBuilder.accessConfig;
        this.encryption = requestBuilder.encryption;
        this.flowTypeOverrides = requestBuilder.flowTypeOverrides != null
                ? Collections.unmodifiableMap(new HashMap<>(requestBuilder.flowTypeOverrides))
                : Collections.emptyMap();
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
     * Returns the encryption configuration holding the external service's certificate.
     *
     * @return The encryption config, or {@code null} if not configured.
     */
    public Encryption getEncryption() {

        return encryption;
    }

    /**
     * Returns the per-flow-type access config overrides.
     * Keys are flow type strings (e.g., "REGISTRATION", "LOGIN").
     *
     * @return Unmodifiable map of flow type to AccessConfig overrides.
     */
    public Map<String, AccessConfig> getFlowTypeOverrides() {

        return flowTypeOverrides;
    }

    /**
     * Resolves the effective access config for the given flow type.
     * Returns the flow-type-specific override if present, otherwise falls back to the default access config.
     *
     * @param flowType The flow type (e.g., "REGISTRATION").
     * @return The resolved AccessConfig, or {@code null} if neither override nor default is configured.
     */
    public AccessConfig resolveAccessConfig(String flowType) {

        if (flowType != null && flowTypeOverrides.containsKey(flowType)) {
            return flowTypeOverrides.get(flowType);
        }
        return accessConfig;
    }

    /**
     * Response Builder for InFlowExtensionAction.
     * Used when building from persisted data (DAO → service layer).
     */
    public static class ResponseBuilder extends ActionResponseBuilder {

        private AccessConfig accessConfig;
        private Encryption encryption;
        private Map<String, AccessConfig> flowTypeOverrides;

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

        public ResponseBuilder encryption(Encryption encryption) {

            this.encryption = encryption;
            return this;
        }

        public ResponseBuilder flowTypeOverrides(Map<String, AccessConfig> flowTypeOverrides) {

            this.flowTypeOverrides = flowTypeOverrides;
            return this;
        }

        @Override
        public InFlowExtensionAction build() {

            return new InFlowExtensionAction(this);
        }
    }

    /**
     * Request Builder for InFlowExtensionAction.
     * Used when building from REST API request (API → service layer).
     */
    public static class RequestBuilder extends ActionRequestBuilder {

        private AccessConfig accessConfig;
        private Encryption encryption;
        private Map<String, AccessConfig> flowTypeOverrides;

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

        public RequestBuilder encryption(Encryption encryption) {

            this.encryption = encryption;
            return this;
        }

        public RequestBuilder flowTypeOverrides(Map<String, AccessConfig> flowTypeOverrides) {

            this.flowTypeOverrides = flowTypeOverrides;
            return this;
        }

        @Override
        public InFlowExtensionAction build() {

            return new InFlowExtensionAction(this);
        }
    }
}
