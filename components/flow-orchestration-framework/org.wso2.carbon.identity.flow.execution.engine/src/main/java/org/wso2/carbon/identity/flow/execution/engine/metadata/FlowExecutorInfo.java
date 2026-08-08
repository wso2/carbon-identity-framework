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
import java.util.List;
import java.util.Set;

/**
 * Fully resolved view of one registered executor: what the executor declared, with engine derived
 * defaults filled in for everything it left out.
 *
 * <p>The declared half is held as a {@link FlowExecutorMetadata} and read through delegating getters
 * rather than copied field by field, so the two types cannot drift apart as the SPI grows: adding a
 * property to {@link FlowExecutorMetadata} only needs a getter here. Everything this type adds on top
 * ({@link #getName()}, {@link #getSupportedFlowTypes()}, {@link #isIdpRequired()},
 * {@link #isMetadataDeclared()}) is derived from the executor instance, not declared by it.</p>
 *
 * <p>This is the type served by {@link FlowExecutorMetadataService} and consumed by callers such as
 * the flow management REST API. Instances are immutable.</p>
 */
public class FlowExecutorInfo {

    /**
     * Stands in for an executor that declared nothing, so the delegating getters below never have to
     * null check. Its builder defaults are exactly the engine derived defaults: no description, no
     * icon, no tags, no backing authenticator, no connection needed, visible in the composer.
     */
    private static final FlowExecutorMetadata NO_DECLARED_METADATA = FlowExecutorMetadata.builder().build();

    private final String name;
    private final FlowExecutorMetadata declaredMetadata;
    private final Set<FlowTypes> supportedFlowTypes;
    private final boolean idpRequired;
    private final boolean metadataDeclared;

    private FlowExecutorInfo(Builder builder) {

        this.name = builder.name;
        this.declaredMetadata = builder.declaredMetadata == null ? NO_DECLARED_METADATA : builder.declaredMetadata;
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
        this.idpRequired = builder.idpRequired;
        this.metadataDeclared = builder.declaredMetadata != null || !this.supportedFlowTypes.isEmpty();
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
     * Reserved tags declared by the executor. See
     * {@link org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorTags} for the values
     * that carry behaviour.
     *
     * @return Immutable list of tags. Never null.
     */
    public List<String> getTags() {

        return declaredMetadata.getTags();
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
     * @return True if this executor should be offered as a selectable step.
     */
    public boolean isVisibleInComposer() {

        return declaredMetadata.isVisibleInComposer();
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
     * Whether the executor declared any metadata of its own, either a
     * {@link FlowExecutorMetadata} or a supported flow type. False means every value above was
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
     * Builder for {@link FlowExecutorInfo}. Takes the declared metadata as a whole and the values the
     * engine derives from the executor instance; everything else is read off the declared metadata.
     */
    public static final class Builder {

        private String name;
        private FlowExecutorMetadata declaredMetadata;
        private Set<FlowTypes> supportedFlowTypes;
        private boolean idpRequired;

        private Builder() {

        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        /**
         * @param declaredMetadata Metadata the executor declared, or null if it declared none, in
         *                         which case engine derived defaults apply.
         * @return This builder.
         */
        public Builder declaredMetadata(FlowExecutorMetadata declaredMetadata) {

            this.declaredMetadata = declaredMetadata;
            return this;
        }

        public Builder supportedFlowTypes(Set<FlowTypes> supportedFlowTypes) {

            this.supportedFlowTypes = supportedFlowTypes;
            return this;
        }

        public Builder idpRequired(boolean idpRequired) {

            this.idpRequired = idpRequired;
            return this;
        }

        public FlowExecutorInfo build() {

            return new FlowExecutorInfo(this);
        }
    }
}
