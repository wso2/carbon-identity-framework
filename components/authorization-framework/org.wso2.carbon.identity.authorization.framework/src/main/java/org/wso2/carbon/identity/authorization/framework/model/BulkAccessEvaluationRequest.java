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
 * The {@code BulkAccessEvaluationRequest} class is a generic model class for the bulk authorization check request body
 * which contains a list of access evaluation requests. This follows the AuthZen evaluations request format.
 */
public class BulkAccessEvaluationRequest {

    private ArrayList<AccessEvaluationRequest> requestItems;
    private Map<String, Object> options;

    /**
     * Constructs a {@code BulkAccessEvaluationRequest} object with the list of access evaluation requests.
     *
     * @param requestItems The list of access evaluation requests.
     */
    public BulkAccessEvaluationRequest(ArrayList<AccessEvaluationRequest> requestItems) {

        this.requestItems = requestItems;
    }

    /**
     * Returns the list of access evaluation requests.
     *
     * @return The list of access evaluation requests.
     */
    public ArrayList<AccessEvaluationRequest> getRequestItems() {

        return requestItems;
    }

    /**
     * Returns the options of the bulk access evaluation request.
     * @return The options of the bulk access evaluation request.
     */
    public Map<String, Object> getOptions() {

        return options;
    }

    /**
     * Sets the options of the bulk access evaluation request.
     * @param options The options of the bulk access evaluation request.
     */
    public void setOptions(Map<String, Object> options) {

        this.options = options;
    }
}
