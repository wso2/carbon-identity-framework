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

import java.util.HashMap;

/**
 * The {@code BulkAccessEvaluationResponse} class is a generic model class for a bulk access evaluation response
 * returned in a bulk access evaluation request from an authorization engine. This follows the AuthZen evaluations
 * response format.
 */
public class BulkAccessEvaluationResponse {

    private HashMap<AccessEvaluationRequest, AccessEvaluationResponse> results;
    private HashMap<AccessEvaluationRequest, ErrorResponse> errorResults;

    /**
     * Constructs a {@code BulkAccessEvaluationResponse} object with the results and error results.
     *
     * @param results      The results of the bulk access evaluation.
     * @param errorResults The error results of the bulk access evaluation.
     */
    public BulkAccessEvaluationResponse(HashMap<AccessEvaluationRequest, AccessEvaluationResponse> results, HashMap<AccessEvaluationRequest,
            ErrorResponse> errorResults) {

        this.results = results;
        this.errorResults = errorResults;
    }

    /**
     * Returns the results of the bulk access evaluation.
     *
     * @return The results of the bulk access evaluation.
     */
    public HashMap<AccessEvaluationRequest, AccessEvaluationResponse> getResults() {

        return this.results;
    }

    /**
     * Returns the error results of the bulk access evaluation.
     *
     * @return The error results of the bulk access evaluation.
     */
    public HashMap<AccessEvaluationRequest, ErrorResponse> getErrorResults() {

        return this.errorResults;
    }
}
