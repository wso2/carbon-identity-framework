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

package org.wso2.carbon.identity.policy.evaluation.internal.evaluator;

import org.wso2.carbon.identity.policy.evaluation.api.evaluator.PolicyResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationOutcome;
import org.wso2.carbon.identity.policy.evaluation.internal.component.PolicyEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;

/**
 * {@link PolicyResourceEvaluator} for {@link ResourceType#RULE} resources.
 */
public class RuleResourceEvaluator implements PolicyResourceEvaluator {

    @Override
    public ResourceType getSupportedResourceType() {

        return ResourceType.RULE;
    }

    @Override
    public ResourceEvaluationOutcome evaluate(PolicyResource resource, FlowContext flowContext,
                                              String tenantDomain) throws PolicyEvaluationException {

        RulePolicyResource ruleResource = (RulePolicyResource) resource;

        if (ruleResource.getRule() == null) {
            return new ResourceEvaluationOutcome(ruleResource.getResourceId(), ResourceType.RULE, true);
        }

        try {
            RuleEvaluationResult result = PolicyEvaluationComponentServiceHolder.getInstance()
                    .getRuleEvaluationService()
                    .evaluate(ruleResource.getRule().getId(), flowContext, tenantDomain);
            return new ResourceEvaluationOutcome(ruleResource.getResourceId(), ResourceType.RULE,
                    result.isRuleSatisfied());
        } catch (RuleEvaluationException e) {
            throw new PolicyEvaluationException(
                    "Error evaluating rule resource '" + ruleResource.getResourceId() + "'.", e);
        }
    }
}
