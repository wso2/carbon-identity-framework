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

package org.wso2.carbon.identity.policy.evaluation.api.service;

import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;

/**
 * Service interface for evaluating compliance policies against a caller-supplied rule context.
 */
public interface PolicyEvaluationService {

    /**
     * Evaluates a policy identified by its immutable ID against the provided {@link FlowContext}.
     *
     * <p>Evaluates every resource matching {@code target} within the policy, dispatching each resource
     * to the evaluator registered for its resource type. Returns {@code null} when no policy with
     * {@code policyId} exists — the caller interprets this as "no constraint". Returns a satisfied result
     * with no outcomes when a policy exists but no resource matches the target (compliant by default).
     * The overall result is satisfied only if every matching resource's outcome is satisfied.
     *
     * @param policyId     ID of the policy to evaluate.
     * @param target       Resource target used to select resources within the policy (e.g. platform or category).
     * @param flowContext  Context carrying the data fields for evaluation.
     * @param tenantDomain Tenant domain.
     * @return Aggregate evaluation result, or {@code null} if the policy does not exist.
     * @throws PolicyEvaluationException If policy retrieval fails, a resource fails to evaluate, or no
     *                                   evaluator is registered for a matching resource's type.
     */
    PolicyEvaluationResult evaluate(String policyId, String target,
                                    FlowContext flowContext, String tenantDomain)
            throws PolicyEvaluationException;
}
