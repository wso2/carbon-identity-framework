/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import org.wso2.carbon.identity.flow.mgt.model.DataDTO;

/**
 * Model class to represent the response from the flow engine.
 */
public class FlowExecutionStep {

    private String flowId;
    private String flowType;
    private String flowStatus;
    private String stepType;
    private DataDTO data;

    public FlowExecutionStep() {

    }

    private FlowExecutionStep(Builder builder) {

        this.flowId = builder.flowId;
        this.flowType = builder.flowType;
        this.flowStatus = builder.flowStatus;
        this.stepType = builder.stepType;
        this.data = builder.data;
    }

    public String getFlowId() {

        return flowId;
    }

    public void setFlowId(String flowId) {

        this.flowId = flowId;
    }

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public String getFlowStatus() {

        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {

        this.flowStatus = flowStatus;
    }

    public String getStepType() {

        return stepType;
    }

    public void setStepType(String stepType) {

        this.stepType = stepType;
    }

    public DataDTO getData() {

        return data;
    }

    public void setData(DataDTO data) {

        this.data = data;
    }

    /**
     * Builder class to build {@link FlowExecutionStep} objects.
     */
    public static class Builder {

        private String flowId;
        private String flowType;
        private String flowStatus;
        private String stepType;
        private DataDTO data;

        public Builder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        public Builder flowType(String flowType) {

            this.flowType = flowType;
            return this;
        }

        public Builder flowStatus(String flowStatus) {

            this.flowStatus = flowStatus;
            return this;
        }

        public Builder stepType(String stepType) {

            this.stepType = stepType;
            return this;
        }

        public Builder data(DataDTO dataDTO) {

            this.data = dataDTO;
            return this;
        }

        public FlowExecutionStep build() {

            return new FlowExecutionStep(this);
        }
    }
}
