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

package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * What a flow defines besides its steps: the headers and query parameters it reads from each request, and the
 * variables it keeps.
 */
public class FlowInputsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FlowInputDTO> headers = new ArrayList<>();
    private List<FlowInputDTO> queryParams = new ArrayList<>();
    private List<FlowVariableInputDTO> variables = new ArrayList<>();

    public List<FlowInputDTO> getHeaders() {

        return headers;
    }

    public void setHeaders(List<FlowInputDTO> headers) {

        this.headers = headers == null ? new ArrayList<>() : headers;
    }

    public List<FlowInputDTO> getQueryParams() {

        return queryParams;
    }

    public void setQueryParams(List<FlowInputDTO> queryParams) {

        this.queryParams = queryParams == null ? new ArrayList<>() : queryParams;
    }

    public List<FlowVariableInputDTO> getVariables() {

        return variables;
    }

    public void setVariables(List<FlowVariableInputDTO> variables) {

        this.variables = variables == null ? new ArrayList<>() : variables;
    }

    /**
     * Whether the flow defines nothing.
     *
     * @return True when there are no headers, query parameters or variables.
     */
    public boolean isEmpty() {

        return headers.isEmpty() && queryParams.isEmpty() && variables.isEmpty();
    }
}
