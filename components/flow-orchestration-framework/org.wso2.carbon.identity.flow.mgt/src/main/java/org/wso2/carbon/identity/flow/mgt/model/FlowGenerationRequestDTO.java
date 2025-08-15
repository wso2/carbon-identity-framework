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

package org.wso2.carbon.identity.flow.mgt.model;

/**
 * DTO for Flow Generation Request.
 */
public class FlowGenerationRequestDTO {

    private final String flowType;
    private final String userQuery;

    private FlowGenerationRequestDTO(String flowType, String userQuery) {

        this.flowType = flowType;
        this.userQuery = userQuery;
    }

    public String getFlowType() {

        return flowType;
    }

    public String getUserQuery() {

        return userQuery;
    }

    /**
     * Builder class for FlowGenerationRequestDTO.
     */
    public static class Builder {

        private String flowType;
        private String userQuery;

        public Builder flowType(String flowType) {

            this.flowType = flowType;
            return this;
        }

        public Builder userQuery(String userQuery) {

            this.userQuery = userQuery;
            return this;
        }

        public FlowGenerationRequestDTO build() {

            return new FlowGenerationRequestDTO(flowType, userQuery);
        }
    }
}
