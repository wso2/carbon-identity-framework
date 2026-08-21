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

/**
 * Display metadata declared by an executor. Describes how flow composer presents it as a selectable step.
 */
public class FlowExecutorMetadata {

    private final String displayName;
    private final String description;
    private final String icon;
    private final String associatedAuthenticator;
    private final boolean connectionRequired;

    private FlowExecutorMetadata(Builder builder) {

        this.displayName = builder.displayName;
        this.description = builder.description;
        this.icon = builder.icon;
        this.associatedAuthenticator = builder.associatedAuthenticator;
        this.connectionRequired = builder.connectionRequired;
    }

    /**
     * Human-readable name shown in the composer step palette.
     *
     * @return Display name, or null to let the engine derive one from the executor name.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Short explanation of what this step does, shown alongside the display name.
     *
     * @return Description, or null if not declared.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Icon path or URL for the step.
     *
     * @return Icon reference, or null if not declared.
     */
    public String getIcon() {

        return icon;
    }

    /**
     * Name of the {@code ApplicationAuthenticator} that backs this executor.
     *
     * @return Authenticator name, or null if this executor is not backed by one.
     */
    public String getAssociatedAuthenticator() {

        return associatedAuthenticator;
    }

    /**
     * Whether a connection must be selected before a step using this executor is valid.
     *
     * @return True if a connection is required.
     */
    public boolean isConnectionRequired() {

        return connectionRequired;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link FlowExecutorMetadata}.
     */
    public static final class Builder {

        private String displayName;
        private String description;
        private String icon;
        private String associatedAuthenticator;
        private boolean connectionRequired;

        private Builder() {

        }

        public Builder displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {

            this.description = description;
            return this;
        }

        public Builder icon(String icon) {

            this.icon = icon;
            return this;
        }

        public Builder associatedAuthenticator(String associatedAuthenticator) {

            this.associatedAuthenticator = associatedAuthenticator;
            return this;
        }

        public Builder connectionRequired(boolean connectionRequired) {

            this.connectionRequired = connectionRequired;
            return this;
        }

        public FlowExecutorMetadata build() {

            return new FlowExecutorMetadata(this);
        }
    }
}
