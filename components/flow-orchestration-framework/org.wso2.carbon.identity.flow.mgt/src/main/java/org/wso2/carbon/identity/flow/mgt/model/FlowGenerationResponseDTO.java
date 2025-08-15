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
 * DTO for Flow Generation Response.
 */
public class FlowGenerationResponseDTO {

    private final String operationId;

    public FlowGenerationResponseDTO(String operationId) {

        this.operationId = operationId;
    }

    public String getOperationId() {

        return operationId;
    }

    /**
     * Builder class for FlowGenerationResponseDTO.
     */
    public static class Builder {

        private String operationId;

        public Builder operationId(String operationId) {

            this.operationId = operationId;
            return this;
        }

        public FlowGenerationResponseDTO build() {

            return new FlowGenerationResponseDTO(operationId);
        }
    }
}
