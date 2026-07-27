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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.flow.v1;

import org.json.JSONObject;

/**
 * FlowExecutionResponse model class.
 */
public class FlowExecutionResponse {

    private final int statusCode;
    private final JSONObject response;

    /**
     * Constructor to initialize FlowExecutionResponse with status code and response body.
     *
     * @param statusCode HTTP status code from the Flow Execution API response.
     * @param response   JSONObject containing the response body from the Flow Execution API.
     */
    public FlowExecutionResponse(int statusCode, JSONObject response) {

        this.statusCode = statusCode;
        this.response = response;
    }

    /**
     * Get the HTTP status code from the Flow Execution API response.
     *
     * @return HTTP status code as an integer.
     */
    public int getStatusCode() {

        return statusCode;
    }

    /**
     * Get the response as a JSONObject.
     *
     * @return JSONObject containing the response from the Flow Execution API.
     */
    public JSONObject getResponse() {

        return response;
    }
}
