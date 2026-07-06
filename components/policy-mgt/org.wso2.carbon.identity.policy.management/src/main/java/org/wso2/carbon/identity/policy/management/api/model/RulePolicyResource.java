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
 */
public class RulePolicyResource extends PolicyResource {

    private final Rule rule;

    /**
     * Creates a rule-backed policy resource.
     *
     * @param id         Row identifier, or {@code null} if not yet persisted.
     * @param target     Selector value this resource applies to.
     * @param resourceId ID of the rule in rule-mgt, or {@code null} if not yet created.
     * @param rule       Rule payload, or {@code null} if not hydrated.
     */
    public RulePolicyResource(String id, String target, String resourceId, Rule rule) {

        super(id, target, resourceId);
        this.rule = rule;
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
}
