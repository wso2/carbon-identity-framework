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

package org.wso2.carbon.identity.rule.evaluation.api.model;

import java.util.Collections;
import java.util.List;

/**
 * Rule evaluation result.
 * This class is used to represent the result of a rule evaluation.
 */
public class RuleEvaluationResult {

    private final String ruleId;
    private final boolean ruleSatisfied;
    private final List<String> failedFields;

    public RuleEvaluationResult(String ruleId, boolean ruleSatisfied) {

        this(ruleId, ruleSatisfied, Collections.emptyList());
    }

    public RuleEvaluationResult(String ruleId, boolean ruleSatisfied, List<String> failedFields) {

        this.ruleId = ruleId;
        this.ruleSatisfied = ruleSatisfied;
        this.failedFields = failedFields != null ? failedFields : Collections.emptyList();
    }

    public String getRuleId() {

        return ruleId;
    }

    public boolean isRuleSatisfied() {

        return ruleSatisfied;
    }

    /**
     * Returns the list of fields that failed evaluation.
     * Empty when the rule is satisfied.
     */
    public List<String> getFailedFields() {

        return Collections.unmodifiableList(failedFields);
    }
}
