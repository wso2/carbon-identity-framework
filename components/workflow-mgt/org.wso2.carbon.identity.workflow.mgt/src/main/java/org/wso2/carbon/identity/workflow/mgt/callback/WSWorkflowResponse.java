/*
 * Copyright (c) 2015, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.callback;

import org.wso2.carbon.identity.core.model.ParameterDO;

/**
 * Represents the response from WS workflow executors
 */
public class WSWorkflowResponse {

    private String uuid;
    private String status;
    private ParameterDO[] outputParams;

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public ParameterDO[] getOutputParams() {

        return outputParams;
    }

    public void setOutputParams(ParameterDO[] outputParams) {

        this.outputParams = outputParams;
    }
}
