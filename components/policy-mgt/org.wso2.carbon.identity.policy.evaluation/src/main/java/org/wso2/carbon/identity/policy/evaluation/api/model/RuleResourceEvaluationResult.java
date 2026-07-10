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

    /**
     * Creates a rule resource evaluation result.
     *
     * @param resource     The evaluated rule resource.
     * @param satisfied    Whether the rule evaluation was satisfied.
     * @param failedFields Fields that caused the rule to fail, or empty if satisfied.
     */
    public RuleResourceEvaluationResult(PolicyResource resource, boolean satisfied, List<String> failedFields) {

        super(resource, satisfied);
        this.failedFields = failedFields == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(failedFields);
    }

    public List<String> getFailedFields() {

        return failedFields;
    }
}
