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
 * DTO representing the response of an asynchronous operation.
 */
public class OperationResponseDTO {

    private final String operationId;
    private final String correlationId;
    private final String operationType;
    private final String operationSubjectType;
    private final String operationSubjectId;
    private final String residentOrgId;
    private final String initiatorId;
    private final String operationStatus;
    private final String operationPolicy;
    private final Timestamp createdTime;
    private final Timestamp modifiedTime;
    private final UnitOperationStatusCount unitStatusCount;

    private OperationResponseDTO(Builder builder) {

        this.operationId = builder.operationId;
        this.correlationId = builder.correlationId;
        this.operationType = builder.operationType;
        this.operationSubjectType = builder.operationSubjectType;
        this.operationSubjectId = builder.operationSubjectId;
        this.residentOrgId = builder.residentOrgId;
        this.initiatorId = builder.initiatorId;
        this.operationStatus = builder.operationStatus;
        this.operationPolicy = builder.operationPolicy;
        this.createdTime = builder.createdTime;
        this.modifiedTime = builder.modifiedTime;
        this.unitStatusCount = builder.unitStatusCount;
    }

    public String getOperationId() {

        return operationId;
    }

    public String getCorrelationId() {

        return correlationId;
    }

    public String getOperationType() {

        return operationType;
    }

    public String getOperationSubjectType() {

        return operationSubjectType;
    }

    public String getOperationSubjectId() {

        return operationSubjectId;
    }

    public String getResidentOrgId() {

        return residentOrgId;
    }

    public String getInitiatorId() {

        return initiatorId;
    }

    public String getOperationStatus() {

        return operationStatus;
    }

    public String getOperationPolicy() {

        return operationPolicy;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    public Timestamp getModifiedTime() {

        return modifiedTime;
    }

    public UnitOperationStatusCount getUnitStatusCount() {

        return unitStatusCount;
    }

    /**
     *
     * Operation Response Builder.
     */
    public static class Builder {

        private String operationId;
        private String correlationId;
        private String operationType;
        private String operationSubjectType;
        private String operationSubjectId;
        private String residentOrgId;
        private String initiatorId;
        private String operationStatus;
        private String operationPolicy;
        private Timestamp createdTime;
        private Timestamp modifiedTime;
        private UnitOperationStatusCount unitStatusCount;

        public Builder operationId(String operationId) {

            this.operationId = operationId;
            return this;
        }

        public Builder correlationId(String correlationId) {

            this.correlationId = correlationId;
            return this;
        }

        public Builder operationType(String operationType) {

            this.operationType = operationType;
            return this;
        }

        public Builder operationSubjectType(String operationSubjectType) {

            this.operationSubjectType = operationSubjectType;
            return this;
        }

        public Builder operationSubjectId(String operationSubjectId) {

            this.operationSubjectId = operationSubjectId;
            return this;
        }

        public Builder residentOrgId(String residentOrgId) {

            this.residentOrgId = residentOrgId;
            return this;
        }

        public Builder initiatorId(String initiatorId) {

            this.initiatorId = initiatorId;
            return this;
        }

        public Builder operationStatus(String operationStatus) {

            this.operationStatus = operationStatus;
            return this;
        }

        public Builder operationPolicy(String operationPolicy) {

            this.operationPolicy = operationPolicy;
            return this;
        }

        public Builder createdTime(Timestamp createdTime) {

            this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(Timestamp modifiedTime) {

            this.modifiedTime = modifiedTime;
            return this;
        }

        public Builder unitStatusCount(UnitOperationStatusCount unitStatusCount) {

            this.unitStatusCount = unitStatusCount;
            return this;
        }

        public OperationResponseDTO build() {

            return new OperationResponseDTO(this);
        }
    }
}
