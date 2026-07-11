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

package org.wso2.carbon.identity.policy.evaluation.api.model;

import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;

import java.util.Collections;
import java.util.List;

/**
 * Result of evaluating a rule-typed policy resource. Adds the rule-specific list of fields that
 * caused the rule to fail.
 */
public class RuleResourceEvaluationResult extends ResourceEvaluationResult {

    private final List<String> failedFields;

    private RuleResourceEvaluationResult(PolicyResource resource, boolean satisfied, List<String> failedFields) {

        super(resource, satisfied);
        this.failedFields = failedFields == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(failedFields);
    }

    /**
     * Creates a satisfied result for a rule resource (no failed fields).
     *
     * @param resource The evaluated rule resource.
     * @return A satisfied rule resource evaluation result.
     */
    public static RuleResourceEvaluationResult satisfied(PolicyResource resource) {

        return new RuleResourceEvaluationResult(resource, true, Collections.emptyList());
    }

    /**
     * Creates an unsatisfied result for a rule resource, carrying the fields that caused the failure.
     *
     * @param resource     The evaluated rule resource.
     * @param failedFields Fields that caused the rule to fail.
     * @return An unsatisfied rule resource evaluation result.
     */
    public static RuleResourceEvaluationResult unsatisfied(PolicyResource resource, List<String> failedFields) {

        return new RuleResourceEvaluationResult(resource, false, failedFields);
    }

    public List<String> getFailedFields() {

        return failedFields;
    }
}
