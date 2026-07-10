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
 * {@link #isSatisfied()} is {@code true} only if every {@link ResourceEvaluationResult} in
 * {@link #getResults()} is satisfied.
 */
public class PolicyEvaluationResult {

    private final boolean satisfied;
    private final List<ResourceEvaluationResult> results;

    /**
     * Creates a policy evaluation result. Overall satisfaction is derived from the results: the result
     * is satisfied only if every outcome is satisfied (an empty outcome list is satisfied by default).
     *
     * @param results Per-resource evaluation results.
     */
    public PolicyEvaluationResult(List<ResourceEvaluationResult> results) {

        this.results = results == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(results);
        this.satisfied = this.results.stream().allMatch(ResourceEvaluationResult::isSatisfied);
    }

    public boolean isSatisfied() {

        return satisfied;
    }

    public List<ResourceEvaluationResult> getResults() {

        return results;
    }
}
