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

package org.wso2.carbon.identity.flow.inflow.extensions.config;

/**
 * Immutable per-flow-type policy controlling which {@code FlowExecutionContext} fields the
 * In-Flow Extension executor hands over to the action framework, plus whether REDIRECT is
 * advertised in {@code allowedOperations}.
 *
 * <p>Sourced from {@code [identity.in_flow_extension.context.{flowType}]} in
 * {@code deployment.toml}. A flow-type-specific policy {@link Builder#startingFrom inherits}
 * from the {@code default} policy for unspecified keys.</p>
 */
public final class FlowContextHandoverPolicy {

    private final boolean redirectionEnabled;

    private final boolean userEnabled;
    private final boolean userUserId;
    private final boolean userUserStoreDomain;
    private final boolean userClaims;
    private final boolean userCredentials;

    private final boolean flowEnabled;
    private final boolean flowTenantDomain;
    private final boolean flowApplicationId;
    private final boolean flowFlowType;
    private final boolean flowCallbackUrl;
    private final boolean flowPortalUrl;

    private final boolean propertiesEnabled;

    private FlowContextHandoverPolicy(Builder b) {

        this.redirectionEnabled = b.redirectionEnabled;
        this.userEnabled = b.userEnabled;
        this.userUserId = b.userUserId;
        this.userUserStoreDomain = b.userUserStoreDomain;
        this.userClaims = b.userClaims;
        this.userCredentials = b.userCredentials;
        this.flowEnabled = b.flowEnabled;
        this.flowTenantDomain = b.flowTenantDomain;
        this.flowApplicationId = b.flowApplicationId;
        this.flowFlowType = b.flowFlowType;
        this.flowCallbackUrl = b.flowCallbackUrl;
        this.flowPortalUrl = b.flowPortalUrl;
        this.propertiesEnabled = b.propertiesEnabled;
    }

    public boolean isRedirectionEnabled() {

        return redirectionEnabled;
    }

    public boolean isUserEnabled() {

        return userEnabled;
    }

    public boolean isUserUserId() {

        return userUserId;
    }

    public boolean isUserUserStoreDomain() {

        return userUserStoreDomain;
    }

    public boolean isUserClaims() {

        return userClaims;
    }

    public boolean isUserCredentials() {

        return userCredentials;
    }

    public boolean isFlowEnabled() {

        return flowEnabled;
    }

    public boolean isFlowTenantDomain() {

        return flowTenantDomain;
    }

    public boolean isFlowApplicationId() {

        return flowApplicationId;
    }

    public boolean isFlowFlowType() {

        return flowFlowType;
    }

    public boolean isFlowCallbackUrl() {

        return flowCallbackUrl;
    }

    public boolean isFlowPortalUrl() {

        return flowPortalUrl;
    }

    public boolean isPropertiesEnabled() {

        return propertiesEnabled;
    }

    /**
     * Permissive default — every field allowed. Used as the seed for building the
     * deployment-configured "default" policy and as the safety net when no config is loaded.
     */
    public static FlowContextHandoverPolicy permissive() {

        return new Builder().allowAll().build();
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link FlowContextHandoverPolicy}.
     *
     * <p>Defaults: every field disabled. Call {@link #allowAll()} to start from the permissive
     * baseline, or {@link #startingFrom(FlowContextHandoverPolicy)} to inherit from another
     * policy (used for per-flow-type overrides that fill unspecified keys from {@code default}).</p>
     */
    public static final class Builder {

        private boolean redirectionEnabled;
        private boolean userEnabled;
        private boolean userUserId;
        private boolean userUserStoreDomain;
        private boolean userClaims;
        private boolean userCredentials;
        private boolean flowEnabled;
        private boolean flowTenantDomain;
        private boolean flowApplicationId;
        private boolean flowFlowType;
        private boolean flowCallbackUrl;
        private boolean flowPortalUrl;
        private boolean propertiesEnabled;

        public Builder allowAll() {

            this.redirectionEnabled = true;
            this.userEnabled = true;
            this.userUserId = true;
            this.userUserStoreDomain = true;
            this.userClaims = true;
            this.userCredentials = true;
            this.flowEnabled = true;
            this.flowTenantDomain = true;
            this.flowApplicationId = true;
            this.flowFlowType = true;
            this.flowCallbackUrl = true;
            this.flowPortalUrl = true;
            this.propertiesEnabled = true;
            return this;
        }

        public Builder startingFrom(FlowContextHandoverPolicy base) {

            this.redirectionEnabled = base.redirectionEnabled;
            this.userEnabled = base.userEnabled;
            this.userUserId = base.userUserId;
            this.userUserStoreDomain = base.userUserStoreDomain;
            this.userClaims = base.userClaims;
            this.userCredentials = base.userCredentials;
            this.flowEnabled = base.flowEnabled;
            this.flowTenantDomain = base.flowTenantDomain;
            this.flowApplicationId = base.flowApplicationId;
            this.flowFlowType = base.flowFlowType;
            this.flowCallbackUrl = base.flowCallbackUrl;
            this.flowPortalUrl = base.flowPortalUrl;
            this.propertiesEnabled = base.propertiesEnabled;
            return this;
        }

        public Builder redirectionEnabled(boolean v) {

            this.redirectionEnabled = v;
            return this;
        }

        public Builder userEnabled(boolean v) {

            this.userEnabled = v;
            return this;
        }

        public Builder userUserId(boolean v) {

            this.userUserId = v;
            return this;
        }

        public Builder userUserStoreDomain(boolean v) {

            this.userUserStoreDomain = v;
            return this;
        }

        public Builder userClaims(boolean v) {

            this.userClaims = v;
            return this;
        }

        public Builder userCredentials(boolean v) {

            this.userCredentials = v;
            return this;
        }

        public Builder flowEnabled(boolean v) {

            this.flowEnabled = v;
            return this;
        }

        public Builder flowTenantDomain(boolean v) {

            this.flowTenantDomain = v;
            return this;
        }

        public Builder flowApplicationId(boolean v) {

            this.flowApplicationId = v;
            return this;
        }

        public Builder flowFlowType(boolean v) {

            this.flowFlowType = v;
            return this;
        }

        public Builder flowCallbackUrl(boolean v) {

            this.flowCallbackUrl = v;
            return this;
        }

        public Builder flowPortalUrl(boolean v) {

            this.flowPortalUrl = v;
            return this;
        }

        public Builder propertiesEnabled(boolean v) {

            this.propertiesEnabled = v;
            return this;
        }

        public FlowContextHandoverPolicy build() {

            return new FlowContextHandoverPolicy(this);
        }
    }
}
