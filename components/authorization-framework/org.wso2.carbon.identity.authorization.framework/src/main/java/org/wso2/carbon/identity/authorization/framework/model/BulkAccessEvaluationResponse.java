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
 * The {@code BulkAccessEvaluationResponse} class is a generic model class for a bulk access evaluation response
 * returned in a bulk access evaluation request from an authorization engine. This follows the AuthZen evaluations
 * response format.
 */
public class BulkAccessEvaluationResponse {

    private List<AccessEvaluationResponse> results;

    /**
     * Constructs a {@code BulkAccessEvaluationResponse} object with the results.
     *
     * @param results      The list of results of the bulk access evaluation.
     */
    public BulkAccessEvaluationResponse(List<AccessEvaluationResponse> results) {

        this.results = results;
    }

    /**
     * Returns the results of the bulk access evaluation.
     *
     * @return The results of the bulk access evaluation.
     */
    public List<AccessEvaluationResponse> getResults() {

        return this.results;
    }
}
