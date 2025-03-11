package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

public class OperationRecord {
    private String operationType;
    private String operationSubjectId;
    private String resourceType;
    private String operationPolicy;
    private String residentOrgId;
    private String initiatorId;

    public OperationRecord(String operationType, String operationSubjectId, String resourceType,
                           String operationPolicy, String residentOrgId, String initiatorId) {
        this.operationType = operationType;
        this.operationSubjectId = operationSubjectId;
        this.resourceType = resourceType;
        this.operationPolicy = operationPolicy;
        this.residentOrgId = residentOrgId;
        this.initiatorId = initiatorId;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getOperationSubjectId() {
        return operationSubjectId;
    }

    public String getResourceType() {
        return resourceType;
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

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
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
                ", resourceType='" + resourceType + '\'' +
                ", sharingPolicy='" + operationPolicy + '\'' +
                ", residentOrgId='" + residentOrgId + '\'' +
                ", initiatorId='" + initiatorId + '\'' +
                '}';
    }
}

