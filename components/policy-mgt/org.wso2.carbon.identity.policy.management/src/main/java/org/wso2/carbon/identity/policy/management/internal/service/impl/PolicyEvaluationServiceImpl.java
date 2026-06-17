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

package org.wso2.carbon.identity.policy.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;

/**
 * Default implementation of {@link PolicyEvaluationService}.
 */
public class PolicyEvaluationServiceImpl implements PolicyEvaluationService {

    private static final Log LOG = LogFactory.getLog(PolicyEvaluationServiceImpl.class);

    @Override
    public RuleEvaluationResult evaluate(String policyName, String ruleSelector,
                                         FlowContext flowContext, String tenantDomain)
            throws PolicyManagementException, RuleEvaluationException {

        Policy policy = PolicyMgtComponentServiceHolder.getInstance()
                .getPolicyManagementService()
                .getPolicyByName(policyName, tenantDomain);

        if (policy == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy not found: " + policyName + " for tenant: " + tenantDomain);
            }
            return null;
        }

        if (ruleSelector == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rule selector is null for policy '" + policyName + "' — treating as compliant.");
            }
            return new RuleEvaluationResult(null, true);
        }

        PolicyResource matchingResource = policy.getResources().stream()
                .filter(r -> r.getResourceType() == ResourceType.RULE
                        && r.getTarget() != null
                        && ruleSelector.equalsIgnoreCase(r.getTarget()))
                .findFirst()
                .orElse(null);

        if (matchingResource == null || matchingResource.getRule() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No rule for selector '" + ruleSelector + "' in policy '" + policyName
                        + "' — treating as compliant.");
            }
            return new RuleEvaluationResult(null, true);
        }

        return PolicyMgtComponentServiceHolder.getInstance()
                .getRuleEvaluationService()
                .evaluate(matchingResource.getRule().getId(), flowContext, tenantDomain);
    }
}
