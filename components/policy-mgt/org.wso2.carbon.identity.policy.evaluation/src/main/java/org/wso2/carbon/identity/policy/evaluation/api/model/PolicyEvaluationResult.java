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

import java.util.Collections;
import java.util.List;

/**
 * Aggregate result of evaluating a policy against a resource target.
 * {@link #isSatisfied()} is {@code true} only if every {@link ResourceEvaluationOutcome} in
 * {@link #getOutcomes()} is satisfied.
 */
public class PolicyEvaluationResult {

    private final boolean satisfied;
    private final List<ResourceEvaluationOutcome> outcomes;

    /**
     * Creates a policy evaluation result.
     *
     * @param satisfied Overall satisfaction across all evaluated resources.
     * @param outcomes  Per-resource evaluation outcomes.
     */
    public PolicyEvaluationResult(boolean satisfied, List<ResourceEvaluationOutcome> outcomes) {

        this.satisfied = satisfied;
        this.outcomes = outcomes == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(outcomes);
    }

    public boolean isSatisfied() {

        return satisfied;
    }

    public List<ResourceEvaluationOutcome> getOutcomes() {

        return outcomes;
    }
}
