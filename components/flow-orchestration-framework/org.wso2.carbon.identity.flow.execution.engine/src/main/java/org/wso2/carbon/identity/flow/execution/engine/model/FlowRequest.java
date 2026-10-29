/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The headers and query parameters a flow reads from its requests, kept in its context. Only those the flow defines
 * are kept, each as the last valid value received.
 */
public class FlowRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> queryParameters = new HashMap<>();

    public FlowRequest() {

    }

    /**
     * A copy of a request, with its own maps of headers and query parameters.
     *
     * @param request The request to copy.
     */
    public FlowRequest(FlowRequest request) {

        this.headers = new HashMap<>(request.getHeaders());
        this.queryParameters = new HashMap<>(request.getQueryParameters());
    }

    /**
     * The headers, by name in lower case.
     *
     * @return The headers.
     */
    public Map<String, Object> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {

        this.headers = headers == null ? new HashMap<>() : headers;
    }

    /**
     * The query parameters, by name.
     *
     * @return The query parameters.
     */
    public Map<String, Object> getQueryParameters() {

        return queryParameters;
    }

    public void setQueryParameters(Map<String, Object> queryParameters) {

        this.queryParameters = queryParameters == null ? new HashMap<>() : queryParameters;
    }
}
