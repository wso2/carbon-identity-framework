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

package org.wso2.carbon.identity.framework.async.status.mgt.api.models;

import java.sql.Timestamp;

/**
 * Represents the response record of an asynchronous operation, including its status.
 * This class encapsulates the details of an operation along with its current status.
 */
public class ResponseOperationRecord {

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

    public ResponseOperationRecord() {

    }

    public ResponseOperationRecord(String operationId, String correlationId, String operationType,
                                   String operationSubjectType,
                                   String operationSubjectId, String residentOrgId, String initiatorId,
                                   String operationStatus, String operationPolicy, Timestamp createdTime,
                                   Timestamp modifiedTime) {

        this.operationId = operationId;
        this.correlationId = correlationId;
        this.operationType = operationType;
        this.operationSubjectType = operationSubjectType;
        this.operationSubjectId = operationSubjectId;
        this.residentOrgId = residentOrgId;
        this.initiatorId = initiatorId;
        this.operationStatus = operationStatus;
        this.operationPolicy = operationPolicy;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
    }

    public String getOperationId() {

        return operationId;
    }

    public void setOperationId(String operationId) {

        this.operationId = operationId;
    }

    public String getOperationType() {

        return operationType;
    }

    public void setOperationType(String operationType) {

        this.operationType = operationType;
    }

    public String getOperationSubjectType() {

        return operationSubjectType;
    }

    public void setOperationSubjectType(String operationSubjectType) {

        this.operationSubjectType = operationSubjectType;
    }

    public String getOperationSubjectId() {

        return operationSubjectId;
    }

    public void setOperationSubjectId(String operationSubjectId) {

        this.operationSubjectId = operationSubjectId;
    }

    public String getOperationPolicy() {

        return operationPolicy;
    }

    public void setOperationPolicy(String operationPolicy) {

        this.operationPolicy = operationPolicy;
    }

    public String getResidentOrgId() {

        return residentOrgId;
    }

    public void setResidentOrgId(String residentOrgId) {

        this.residentOrgId = residentOrgId;
    }

    public String getInitiatorId() {

        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {

        this.initiatorId = initiatorId;
    }

    public String getOperationStatus() {

        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {

        this.operationStatus = operationStatus;
    }

    public String getCorrelationId() {

        return correlationId;
    }

    public void setCorrelationId(String correlationId) {

        this.correlationId = correlationId;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {

        this.createdTime = createdTime;
    }

    public Timestamp getModifiedTime() {

        return modifiedTime;
    }

    public void setModifiedTime(Timestamp modifiedTime) {

        this.modifiedTime = modifiedTime;
    }
}
