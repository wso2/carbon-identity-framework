/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

/**
 * Represents a record of an asynchronous operation.
 * This class encapsulates the details of an operation, such as its type, subject, organization, initiator, and policy.
 */
public class OperationRecord {

    private String correlationId;
    private String operationType;
    private String operationSubjectType;
    private String operationSubjectId;
    private String residentOrgId;
    private String initiatorId;
    private String operationPolicy;

    public OperationRecord(String correlationId, String operationType, String operationSubjectType, String operationSubjectId,
                           String residentOrgId, String initiatorId, String operationPolicy) {

        this.correlationId = correlationId;
        this.operationType = operationType;
        this.operationSubjectType = operationSubjectType;
        this.operationSubjectId = operationSubjectId;
        this.residentOrgId = residentOrgId;
        this.initiatorId = initiatorId;
        this.operationPolicy = operationPolicy;
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

    public String getOperationPolicy() {

        return operationPolicy;
    }

    public String getResidentOrgId() {

        return residentOrgId;
    }

    public String getInitiatorId() {

        return initiatorId;
    }

    public void setOperationType(String operationType) {

        this.operationType = operationType;
    }

    public void setOperationSubjectType(String operationSubjectType) {

        this.operationSubjectType = operationSubjectType;
    }

    public void setOperationSubjectId(String operationSubjectId) {

        this.operationSubjectId = operationSubjectId;
    }

    public void setOperationPolicy(String operationPolicy) {

        this.operationPolicy = operationPolicy;
    }

    public void setResidentOrgId(String residentOrgId) {

        this.residentOrgId = residentOrgId;
    }

    public void setInitiatorId(String initiatorId) {

        this.initiatorId = initiatorId;
    }

    public String getCorrelationId() {

        return correlationId;
    }

    public void setCorrelationId(String correlationId) {

        this.correlationId = correlationId;
    }

    @Override
    public String toString() {

        return "OperationRecord{" +
                "operationType='" + operationType + '\'' +
                ", operationSubjectType='" + operationSubjectType + '\'' +
                ", operationSubjectId='" + operationSubjectId + '\'' +
                ", residentOrgId='" + residentOrgId + '\'' +
                ", initiatorId='" + initiatorId + '\'' +
                ", sharingPolicy='" + operationPolicy + '\'' +
                '}';
    }
}

