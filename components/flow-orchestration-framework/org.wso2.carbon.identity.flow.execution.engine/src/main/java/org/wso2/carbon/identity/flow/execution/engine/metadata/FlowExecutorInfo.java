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

package org.wso2.carbon.identity.flow.execution.engine.metadata;

import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fully resolved view of a registered executor including what the executor declared, with engine derived defaults.
 */
public class FlowExecutorInfo {

    private static final FlowExecutorMetadata NO_DECLARED_METADATA = FlowExecutorMetadata.builder().build();

    private final String name;
    private final FlowExecutorMetadata declaredMetadata;
    private final Set<FlowTypes> supportedFlowTypes;
    private final boolean authenticationExecutor;
    private final boolean metadataDeclared;

    private FlowExecutorInfo(Builder builder) {

        this.name = builder.name;
        this.declaredMetadata = builder.metadata == null ? NO_DECLARED_METADATA : builder.metadata;
        /*
         * Empty set means the executor is not offered as a step in any flow.
         */
        Set<FlowTypes> flowTypes = EnumSet.noneOf(FlowTypes.class);
        if (builder.supportedFlowTypes != null) {
            flowTypes.addAll(builder.supportedFlowTypes);
        }
        this.supportedFlowTypes = Collections.unmodifiableSet(flowTypes);
        this.authenticationExecutor = builder.authenticationExecutor;
        this.metadataDeclared = builder.metadata != null || !this.supportedFlowTypes.isEmpty();
    }

    /**
     * Unique executor name referenced by flow steps.
     *
     * @return Executor name.
     */
    public String getName() {

        return name;
    }

    /**
     * Executor display name. Falls back to {@link #getName()} when the executor declared none.
     *
     * @return Display name.
     */
    public String getDisplayName() {

        String declaredDisplayName = declaredMetadata.getDisplayName();
        return declaredDisplayName == null || declaredDisplayName.trim().isEmpty() ? name : declaredDisplayName;
    }

    /**
     * @return Description, or null if the executor declared none.
     */
    public String getDescription() {

        return declaredMetadata.getDescription();
    }

    /**
     * @return Icon path or URL, or null if the executor declared none.
     */
    public String getIcon() {

        return declaredMetadata.getIcon();
    }

    /**
     * Flow types this executor declared support for. Empty means the executor is not offered as a
     * step in any flow.
     *
     * @return Immutable set of supported flow types. Never null.
     */
    public Set<FlowTypes> getSupportedFlowTypes() {

        return supportedFlowTypes;
    }

    /**
     * @return Backing authenticator name, or null if this executor is not backed by one.
     */
    public String getAssociatedAuthenticator() {

        return declaredMetadata.getAssociatedAuthenticator();
    }

    /**
     * @return True if a connection must be selected for a step using this executor.
     */
    public boolean isConnectionRequired() {

        return declaredMetadata.isConnectionRequired();
    }

    /**
     * Whether the executor authenticates the user, meaning it extends
     * {@code AuthenticationExecutor}. Such an executor resolves IDP configuration from its step
     * metadata, so an IDP must be bound to the step, and it qualifies as an authentication factor
     * wherever a flow requires one.
     *
     * @return True if the executor authenticates the user.
     */
    public boolean isAuthenticationExecutor() {

        return authenticationExecutor;
    }

    /**
     * Whether the executor declared any metadata of its own.
     *
     * @return True if the executor declared metadata.
     */
    public boolean isMetadataDeclared() {

        return metadataDeclared;
    }

    /**
     * Whether this executor may be used as a step in the given flow type.
     *
     * @param flowType Flow type to check.
     * @return True if supported.
     */
    public boolean supportsFlowType(FlowTypes flowType) {

        return flowType != null && supportedFlowTypes.contains(flowType);
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link FlowExecutorInfo}.
     */
    public static final class Builder {

        private String name;
        private FlowExecutorMetadata metadata;
        private Set<FlowTypes> supportedFlowTypes;
        private boolean authenticationExecutor;

        private Builder() {

        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        /**
         * @param metadata Metadata the executor declared, or null if it declared none, in
         *                         which case engine derived defaults apply.
         * @return This builder.
         */
        public Builder metadata(FlowExecutorMetadata metadata) {

            this.metadata = metadata;
            return this;
        }

        public Builder supportedFlowTypes(Set<FlowTypes> supportedFlowTypes) {

            this.supportedFlowTypes = supportedFlowTypes;
            return this;
        }

        public Builder authenticationExecutor(boolean authenticationExecutor) {

            this.authenticationExecutor = authenticationExecutor;
            return this;
        }

        public FlowExecutorInfo build() {

            return new FlowExecutorInfo(this);
        }
    }
}
