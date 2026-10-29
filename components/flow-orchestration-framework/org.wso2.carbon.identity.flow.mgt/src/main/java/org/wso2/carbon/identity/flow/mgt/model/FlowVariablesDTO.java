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
 * What a flow defines besides its steps: the headers it reads from each request, and the runtime values it keeps.
 */
public class FlowVariablesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FlowVariableDTO> headers = new ArrayList<>();
    private List<FlowRuntimeValueDTO> runtimeValues = new ArrayList<>();

    public List<FlowVariableDTO> getHeaders() {

        return headers;
    }

    public void setHeaders(List<FlowVariableDTO> headers) {

        this.headers = headers == null ? new ArrayList<>() : headers;
    }

    public List<FlowRuntimeValueDTO> getRuntimeValues() {

        return runtimeValues;
    }

    public void setRuntimeValues(List<FlowRuntimeValueDTO> runtimeValues) {

        this.runtimeValues = runtimeValues == null ? new ArrayList<>() : runtimeValues;
    }

    /**
     * Whether the flow defines nothing.
     *
     * @return True when there are no headers or runtime values.
     */
    public boolean isEmpty() {

        return headers.isEmpty() && runtimeValues.isEmpty();
    }
}
