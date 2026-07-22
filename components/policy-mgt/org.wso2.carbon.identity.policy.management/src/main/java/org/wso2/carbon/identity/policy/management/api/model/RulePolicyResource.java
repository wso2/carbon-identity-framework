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

package org.wso2.carbon.identity.policy.management.api.model;

import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * Policy resource backed by an IS-native rule managed by rule-mgt.
 * Instances are created through {@link Builder}. Field validation is not performed here; user supplied
 * resources are validated by the service layer so that failures surface as client errors.
 */
public class RulePolicyResource extends PolicyResource {

    private final Rule rule;

    private RulePolicyResource(Builder builder) {

        super(builder.id, builder.target, builder.resourceId);
        this.rule = builder.rule;
    }

    @Override
    public ResourceType getResourceType() {

        return ResourceType.RULE;
    }

    /**
     * Returns the hydrated rule. Non-null only after the resource has been hydrated by the service layer.
     *
     * @return Hydrated rule, or {@code null}.
     */
    public Rule getRule() {

        return rule;
    }

    /**
     * Builder for {@link RulePolicyResource}.
     */
    public static class Builder {

        private String id;
        private String target;
        private String resourceId;
        private Rule rule;

        /**
         * Sets the row identifier.
         *
         * @param id Row identifier, or {@code null} if not yet persisted.
         * @return This builder.
         */
        public Builder id(String id) {

            this.id = id;
            return this;
        }

        /**
         * Sets the selector value this resource applies to.
         *
         * @param target Selector value.
         * @return This builder.
         */
        public Builder target(String target) {

            this.target = target;
            return this;
        }

        /**
         * Sets the ID of the rule in rule-mgt.
         *
         * @param resourceId Rule ID, or {@code null} if not yet created.
         * @return This builder.
         */
        public Builder resourceId(String resourceId) {

            this.resourceId = resourceId;
            return this;
        }

        /**
         * Sets the rule payload.
         *
         * @param rule Rule payload, or {@code null} if not hydrated.
         * @return This builder.
         */
        public Builder rule(Rule rule) {

            this.rule = rule;
            return this;
        }

        /**
         * Builds the rule policy resource.
         *
         * @return RulePolicyResource instance.
         */
        public RulePolicyResource build() {

            return new RulePolicyResource(this);
        }
    }
}
