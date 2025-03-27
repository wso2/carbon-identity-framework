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

