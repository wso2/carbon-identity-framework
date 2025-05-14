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
package org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models;

import java.sql.Timestamp;

/**
 * Unit Operation Response Data Transfer Object.
 */
public class UnitOperationResponseDTO {

    private final String unitOperationId;
    private final String operationId;
    private final String operationInitiatedResourceId;
    private final String targetOrgId;
    private final String targetOrgName;
    private final String unitOperationStatus;
    private final String statusMessage;
    private final Timestamp createdTime;

    private UnitOperationResponseDTO(Builder builder) {

        this.unitOperationId = builder.unitOperationId;
        this.operationId = builder.operationId;
        this.operationInitiatedResourceId = builder.operationInitiatedResourceId;
        this.targetOrgId = builder.targetOrgId;
        this.targetOrgName = builder.targetOrgName;
        this.unitOperationStatus = builder.unitOperationStatus;
        this.statusMessage = builder.statusMessage;
        this.createdTime = builder.createdTime;
    }

    public String getUnitOperationId() {

        return unitOperationId;
    }

    public String getOperationId() {

        return operationId;
    }

    public String getOperationInitiatedResourceId() {

        return operationInitiatedResourceId;
    }

    public String getTargetOrgId() {

        return targetOrgId;
    }

    public String getTargetOrgName() {

        return targetOrgName;
    }

    public String getUnitOperationStatus() {

        return unitOperationStatus;
    }

    public String getStatusMessage() {

        return statusMessage;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    /**
     * Unit Operation Response Builder.
     */
    public static class Builder {

        private String unitOperationId;
        private String operationId;
        private String operationInitiatedResourceId;
        private String targetOrgId;
        private String targetOrgName;
        private String unitOperationStatus;
        private String statusMessage;
        private Timestamp createdTime;

        public Builder unitOperationId(String unitOperationId) {

            this.unitOperationId = unitOperationId;
            return this;
        }

        public Builder operationId(String operationId) {

            this.operationId = operationId;
            return this;
        }

        public Builder operationInitiatedResourceId(String operationInitiatedResourceId) {

            this.operationInitiatedResourceId = operationInitiatedResourceId;
            return this;
        }

        public Builder targetOrgId(String targetOrgId) {

            this.targetOrgId = targetOrgId;
            return this;
        }

        public Builder targetOrgName(String targetOrgName) {

            this.targetOrgName = targetOrgName;
            return this;
        }

        public Builder unitOperationStatus(String unitOperationStatus) {

            this.unitOperationStatus = unitOperationStatus;
            return this;
        }

        public Builder statusMessage(String statusMessage) {

            this.statusMessage = statusMessage;
            return this;
        }

        public Builder createdTime(Timestamp createdTime) {

            this.createdTime = createdTime;
            return this;
        }

        public UnitOperationResponseDTO build() {

            return new UnitOperationResponseDTO(this);
        }
    }
}
