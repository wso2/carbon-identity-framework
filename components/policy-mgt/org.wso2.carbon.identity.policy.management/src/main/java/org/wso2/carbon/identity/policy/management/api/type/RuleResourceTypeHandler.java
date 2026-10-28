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

package org.wso2.carbon.identity.policy.management.api.type;

import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.List;

/**
 * Policy type specific behaviour for {@link ResourceType#RULE} resources.
 */
public interface RuleResourceTypeHandler extends PolicyResourceTypeHandler {

    /**
     * Returns the rule flow whose fields, operators and values the rules of the policy type use. Rules are
     * validated against this flow when they are built.
     *
     * @return Rule flow type.
     */
    FlowType getRuleFlowType();

    /**
     * Returns the rule field definitions that apply to a target. Rule metadata is defined per rule flow and has no
     * notion of a target, so a policy type whose fields depend on the target narrows them down here. All fields of
     * the rule flow apply to every target unless this is overridden.
     *
     * @param target           Resource target.
     * @param fieldDefinitions Field definitions of the rule flow.
     * @return Field definitions that rules of the target can use.
     */
    default List<FieldDefinition> filterMetadata(String target, List<FieldDefinition> fieldDefinitions) {

        return fieldDefinitions;
    }

    @Override
    default ResourceType getResourceType() {

        return ResourceType.RULE;
    }
}
