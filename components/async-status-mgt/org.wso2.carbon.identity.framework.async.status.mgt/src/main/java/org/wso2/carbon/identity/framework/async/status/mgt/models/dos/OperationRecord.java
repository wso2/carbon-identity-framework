package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

public class OperationRecord {
    private String operationType;
    private String operationSubjectId;
    private String residentOrgId;
    private String initiatorId;
    private String operationPolicy;

    public OperationRecord(String operationType, String operationSubjectId, String residentOrgId, String initiatorId, String operationPolicy) {
        this.operationType = operationType;
        this.operationSubjectId = operationSubjectId;
        this.residentOrgId = residentOrgId;
        this.initiatorId = initiatorId;
        this.operationPolicy = operationPolicy;
    }

    public String getOperationType() {
        return operationType;
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

    @Override
    public String toString() {
        return "OperationRecord{" +
                "operationType='" + operationType + '\'' +
                ", operationSubjectId='" + operationSubjectId + '\'' +
                ", residentOrgId='" + residentOrgId + '\'' +
                ", initiatorId='" + initiatorId + '\'' +
                ", sharingPolicy='" + operationPolicy + '\'' +
                '}';
    }
}

