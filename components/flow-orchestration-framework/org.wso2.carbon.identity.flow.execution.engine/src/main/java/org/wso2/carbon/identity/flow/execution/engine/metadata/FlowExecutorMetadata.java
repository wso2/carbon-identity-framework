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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Display metadata declared by an executor, describing how the flow composer should present it as a
 * selectable step.
 *
 * <p>Returned from
 * {@link org.wso2.carbon.identity.flow.execution.engine.graph.Executor#getExecutorMetadata()}.
 * Every field is optional; anything left unset is derived from the executor's name by
 * the engine. An executor that declares nothing at all keeps working exactly as before - see
 * {@link FlowExecutorMetadataService} for the resolution rules.</p>
 *
 * <p>Instances are immutable. Build them with {@link #builder()}.</p>
 */
public class FlowExecutorMetadata {

    private final String displayName;
    private final String description;
    private final String icon;
    private final List<String> tags;
    private final String associatedAuthenticator;
    private final boolean connectionRequired;
    private final boolean visibleInComposer;

    private FlowExecutorMetadata(Builder builder) {

        this.displayName = builder.displayName;
        this.description = builder.description;
        this.icon = builder.icon;
        this.tags = builder.tags == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.tags));
        this.associatedAuthenticator = builder.associatedAuthenticator;
        this.connectionRequired = builder.connectionRequired;
        this.visibleInComposer = builder.visibleInComposer;
    }

    /**
     * Human readable name shown in the composer step palette, e.g. "Daon TrustX Verification".
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
     * Icon path or URL for the step, e.g. "assets/images/logos/daon.svg".
     *
     * @return Icon reference, or null if not declared.
     */
    public String getIcon() {

        return icon;
    }

    /**
     * Reserved tags that alter how a consumer treats this executor. See
     * {@link org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorTags} for the
     * recognised values; declaring anything else has no
     * effect today, so prefer leaving this empty over adding descriptive labels.
     *
     * @return Immutable list of tags. Never null.
     */
    public List<String> getTags() {

        return tags;
    }

    /**
     * Name of the {@code ApplicationAuthenticator} that backs this executor, e.g. "DaonAuthenticator".
     *
     * <p>Declaring this lets a consumer resolve the identity providers configured with that
     * authenticator and offer them as the connections available to this step, without needing a
     * hardcoded authenticator-to-executor mapping.</p>
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

    /**
     * Whether this executor should be offered as a selectable step. Set false for executors that
     * exist only to be inserted into the graph by the engine itself.
     *
     * @return True if the executor should appear in the composer.
     */
    public boolean isVisibleInComposer() {

        return visibleInComposer;
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
        private List<String> tags;
        private String associatedAuthenticator;
        private boolean connectionRequired;
        private boolean visibleInComposer = true;

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

        public Builder tags(List<String> tags) {

            this.tags = tags;
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

        public FlowExecutorMetadata build() {

            return new FlowExecutorMetadata(this);
        }
    }
}
