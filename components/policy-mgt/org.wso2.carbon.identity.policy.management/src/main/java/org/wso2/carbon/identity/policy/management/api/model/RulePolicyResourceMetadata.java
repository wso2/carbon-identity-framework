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

import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata of {@link ResourceType#RULE} resources: the rule flow that rules are built and validated against, and
 * the fields, operators and values a rule can use.
 */
public class RulePolicyResourceMetadata extends PolicyResourceMetadata {

    private final FlowType ruleFlowType;
    private final List<FieldDefinition> fieldDefinitions;

    public RulePolicyResourceMetadata(FlowType ruleFlowType, List<FieldDefinition> fieldDefinitions) {

        this.ruleFlowType = ruleFlowType;
        this.fieldDefinitions = fieldDefinitions != null
                ? Collections.unmodifiableList(new ArrayList<>(fieldDefinitions)) : Collections.emptyList();
    }

    @Override
    public ResourceType getResourceType() {

        return ResourceType.RULE;
    }

    /**
     * Returns the rule flow that rules of the policy type are built and validated against.
     *
     * @return Rule flow type.
     */
    public FlowType getRuleFlowType() {

        return ruleFlowType;
    }

    /**
     * Returns the field definitions rules can use.
     *
     * @return Field definitions.
     */
    public List<FieldDefinition> getFieldDefinitions() {

        return fieldDefinitions;
    }
}
