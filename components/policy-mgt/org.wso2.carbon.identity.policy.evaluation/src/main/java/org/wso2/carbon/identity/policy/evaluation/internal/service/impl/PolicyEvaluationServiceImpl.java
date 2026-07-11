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

package org.wso2.carbon.identity.policy.evaluation.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.policy.evaluation.api.evaluator.PolicyResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.evaluation.internal.component.PolicyEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link PolicyEvaluationService}.
 */
public class PolicyEvaluationServiceImpl implements PolicyEvaluationService {

    private static final Log LOG = LogFactory.getLog(PolicyEvaluationServiceImpl.class);

    @Override
    public PolicyEvaluationResult evaluate(String policyId, String target,
                                           FlowContext flowContext, String tenantDomain)
            throws PolicyEvaluationException {

        Policy policy;
        try {
            policy = PolicyEvaluationComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyById(policyId, tenantDomain);
        } catch (PolicyManagementException e) {
            throw new PolicyEvaluationException(
                    "Error retrieving policy with ID '" + policyId + "' for evaluation.", e);
        }

        if (policy == null) {
            throw new PolicyEvaluationException("Policy not found for the given policyId: " + policyId);
        }
        return evaluate(policy, target, flowContext, tenantDomain);
    }

    private PolicyEvaluationResult evaluate(Policy policy, String target,
                                            FlowContext flowContext, String tenantDomain)
            throws PolicyEvaluationException {

        if (target == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Target is null for policy '" + policy.getName() + "' — treating as compliant.");
            }
            return new PolicyEvaluationResult(Collections.emptyList());
        }

        List<PolicyResource> matchingResources = policy.getResources().stream()
                .filter(r -> r.getTarget() != null && target.equalsIgnoreCase(r.getTarget()))
                .collect(Collectors.toList());

        if (matchingResources.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No resources for target '" + target + "' in policy '" + policy.getName()
                        + "' — treating as compliant.");
            }
            return new PolicyEvaluationResult(Collections.emptyList());
        }

        List<ResourceEvaluationResult> results = new ArrayList<>();
        for (PolicyResource resource : matchingResources) {
            PolicyResourceEvaluator evaluator = PolicyEvaluationComponentServiceHolder.getInstance()
                    .getPolicyResourceEvaluator(resource.getResourceType());
            if (evaluator == null) {
                throw new PolicyEvaluationException(
                        "No evaluator for resource type: " + resource.getResourceType());
            }
            results.add(evaluator.evaluate(resource, flowContext, tenantDomain));
        }

        return new PolicyEvaluationResult(results);
    }
}
