/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.evaluation.api.service;

import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;

/**
 * Rule evaluation service interface.
 * This interface is used to evaluate a rule.
 */
public interface RuleEvaluationService {

    /**
     * Evaluate a rule with the given rule id and flow context.
     *
     * @param ruleId       Rule id.
     * @param flowContext  Flow context.
     * @param tenantDomain Tenant domain.
     * @return Rule evaluation result.
     * @throws RuleEvaluationException If an error occurs while evaluating the rule.
     */
    RuleEvaluationResult evaluate(String ruleId, FlowContext flowContext, String tenantDomain)
            throws RuleEvaluationException;

}
