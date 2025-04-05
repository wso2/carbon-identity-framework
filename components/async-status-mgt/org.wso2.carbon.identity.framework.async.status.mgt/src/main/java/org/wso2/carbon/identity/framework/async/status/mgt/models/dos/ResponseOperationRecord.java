package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

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

    public ResponseOperationRecord(String operationId, String correlationId, String operationType, String operationSubjectType,
                                   String operationSubjectId, String residentOrgId, String initiatorId,
                                   String operationStatus, String operationPolicy, Timestamp createdTime, Timestamp modifiedTime) {

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
