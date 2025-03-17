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

import java.util.ArrayList;
import java.util.Map;

/**
 * The {@code BulkAccessEvaluationRequest} class is a model class for a Bulk Access Evaluation request body
 * which contains a list of Access Evaluation requests. This follows the AuthZEN Evaluations request format.
 */
public class BulkAccessEvaluationRequest {

    private ArrayList<AccessEvaluationRequest> requestItems;
    // Options field is an optional field for sending meta information on how the requests should be executed.
    private Map<String, Object> options;

    /**
     * Constructs a {@code BulkAccessEvaluationRequest} object with the list of Access Evaluation requests.
     *
     * @param requestItems The list of Access Evaluation requests.
     */
    public BulkAccessEvaluationRequest(ArrayList<AccessEvaluationRequest> requestItems) {

        this.requestItems = requestItems;
    }

    /**
     * Returns the list of Access Evaluation requests.
     *
     * @return The list of Access Evaluation requests.
     */
    public ArrayList<AccessEvaluationRequest> getRequestItems() {

        return requestItems;
    }

    /**
     * Returns the options of the Bulk Access Evaluation request.
     * @return The options of the Bulk Access Evaluation request.
     */
    public Map<String, Object> getOptions() {

        return options;
    }

    /**
     * Sets the options of the Bulk Access Evaluation request.
     * @param options The options of the Bulk Access Evaluation request.
     */
    public void setOptions(Map<String, Object> options) {

        this.options = options;
    }
}
