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

package org.wso2.carbon.identity.policy.management.api.service;

import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;

/**
 * Service interface for evaluating compliance policies against a caller-supplied rule context.
 */
public interface PolicyEvaluationService {

    /**
     * Evaluates the rule matching {@code ruleSelector} within the named policy against the provided
     * {@link FlowContext}.
     *
     * <p>Returns {@code null} when no policy with {@code policyName} exists — the caller interprets
     * this as "no constraint" and decides the appropriate response. Returns a satisfied result with
     * a {@code null} rule ID when a policy exists but no rule matches the selector (compliant by default).
     *
     * @param policyName   Name of the policy to evaluate.
     * @param ruleSelector Value used to select a rule within the policy (e.g. device platform).
     * @param flowContext  Context carrying the data fields for rule evaluation.
     * @param tenantDomain Tenant domain.
     * @return Evaluation result, or {@code null} if the named policy does not exist.
     * @throws PolicyManagementException If policy retrieval fails.
     * @throws RuleEvaluationException   If rule evaluation fails.
     */
    RuleEvaluationResult evaluate(String policyName, String ruleSelector,
                                  FlowContext flowContext, String tenantDomain)
            throws PolicyManagementException, RuleEvaluationException;
}
