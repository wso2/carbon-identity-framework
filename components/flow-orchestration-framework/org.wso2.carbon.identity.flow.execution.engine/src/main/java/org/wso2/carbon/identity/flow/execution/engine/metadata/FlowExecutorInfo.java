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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Fully resolved view of one registered executor: what the executor declared, with engine derived
 * defaults filled in for everything it left out.
 *
 * <p>This is the type served by {@link FlowExecutorMetadataService} and consumed by callers such as
 * the flow management REST API. Instances are immutable.</p>
 */
public class FlowExecutorInfo {

    private final String name;
    private final String displayName;
    private final String description;
    private final String icon;
    private final List<String> tags;
    private final Set<FlowTypes> supportedFlowTypes;
    private final String associatedAuthenticator;
    private final boolean connectionRequired;
    private final boolean visibleInComposer;
    private final boolean idpRequired;
    private final boolean metadataDeclared;

    private FlowExecutorInfo(Builder builder) {

        this.name = builder.name;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.icon = builder.icon;
        this.tags = builder.tags == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.tags));
        /*
         * EnumSet.noneOf(..) + addAll rather than EnumSet.copyOf(..): copyOf throws
         * IllegalArgumentException on an empty non-EnumSet, and an empty set is a legitimate value
         * meaning "this executor is not offered in any flow".
         */
        Set<FlowTypes> flowTypes = EnumSet.noneOf(FlowTypes.class);
        if (builder.supportedFlowTypes != null) {
            flowTypes.addAll(builder.supportedFlowTypes);
        }
        this.supportedFlowTypes = Collections.unmodifiableSet(flowTypes);
        this.associatedAuthenticator = builder.associatedAuthenticator;
        this.connectionRequired = builder.connectionRequired;
        this.visibleInComposer = builder.visibleInComposer;
        this.idpRequired = builder.idpRequired;
        this.metadataDeclared = builder.metadataDeclared;
    }

    /**
     * Unique executor name, as returned by
     * {@link org.wso2.carbon.identity.flow.execution.engine.graph.Executor#getName()}. This is the
     * value a flow step references.
     *
     * @return Executor name.
     */
    public String getName() {

        return name;
    }

    /**
     * Display name. Never null: falls back to {@link #getName()} when the executor declared none.
     *
     * @return Display name.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * @return Description, or null if the executor declared none.
     */
    public String getDescription() {

        return description;
    }

    /**
     * @return Icon path or URL, or null if the executor declared none.
     */
    public String getIcon() {

        return icon;
    }

    /**
     * Reserved tags declared by the executor. See
     * {@link org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorTags} for the values
     * that carry behaviour.
     *
     * @return Immutable list of tags. Never null.
     */
    public List<String> getTags() {

        return tags;
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

        return associatedAuthenticator;
    }

    /**
     * @return True if a connection must be selected for a step using this executor.
     */
    public boolean isConnectionRequired() {

        return connectionRequired;
    }

    /**
     * @return True if this executor should be offered as a selectable step.
     */
    public boolean isVisibleInComposer() {

        return visibleInComposer;
    }

    /**
     * Whether the executor resolves identity provider configuration from its step metadata, which
     * means the composer has to ask the admin to pick a connection. Derived from
     * {@code instanceof AuthenticationExecutor} when not declared.
     *
     * @return True if an identity provider must be bound to the step.
     */
    public boolean isIdpRequired() {

        return idpRequired;
    }

    /**
     * Whether the executor declared any metadata of its own. False means every value above was
     * derived by the engine, which is the normal state for executors written before this SPI existed.
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
        private String displayName;
        private String description;
        private String icon;
        private List<String> tags;
        private Set<FlowTypes> supportedFlowTypes;
        private String associatedAuthenticator;
        private boolean connectionRequired;
        private boolean visibleInComposer = true;
        private boolean idpRequired;
        private boolean metadataDeclared;

        private Builder() {

        }

        public Builder name(String name) {

            this.name = name;
            return this;
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

        public Builder tags(List<String> tags) {

            this.tags = tags;
            return this;
        }

        public Builder supportedFlowTypes(Set<FlowTypes> supportedFlowTypes) {

            this.supportedFlowTypes = supportedFlowTypes;
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

        public Builder visibleInComposer(boolean visibleInComposer) {

            this.visibleInComposer = visibleInComposer;
            return this;
        }

        public Builder idpRequired(boolean idpRequired) {

            this.idpRequired = idpRequired;
            return this;
        }

        public Builder metadataDeclared(boolean metadataDeclared) {

            this.metadataDeclared = metadataDeclared;
            return this;
        }

        public FlowExecutorInfo build() {

            return new FlowExecutorInfo(this);
        }
    }
}
