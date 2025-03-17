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

package org.wso2.carbon.identity.authorization.framework.model;

import java.util.List;

/**
 * The {@code BulkAccessEvaluationResponse} class is a model class for a Bulk Access Evaluation response returned from a
 * Bulk Access Evaluation request sent to an Authorization Engine. This response should consist of evaluation decisions
 * for each request in the Bulk Access Evaluation request. This follows the AuthZEN Evaluations response format.
 */
public class BulkAccessEvaluationResponse {

    private List<AccessEvaluationResponse> results;

    /**
     * Constructs a {@code BulkAccessEvaluationResponse} object with the results.
     *
     * @param results      The list of results of the Bulk Access Evaluation.
     */
    public BulkAccessEvaluationResponse(List<AccessEvaluationResponse> results) {

        this.results = results;
    }

    /**
     * Returns the results of the Bulk Access Evaluation.
     *
     * @return The results of the Bulk Access Evaluation.
     */
    public List<AccessEvaluationResponse> getResults() {

        return this.results;
    }
}
