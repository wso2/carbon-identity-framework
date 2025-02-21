package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import java.time.LocalDateTime;

public class OperationContext {

    private String operationType;
    private String operationSubjectId;
    private String resourceType;
    private String sharingPolicy;
    private String residentOrgId;
    private String initiatorId;

    // Constructors
    public OperationContext() {
    }

    public OperationContext(String operationType, String operationSubjectId,
                            String resourceType, String sharingPolicy, String residentOrgId, String initiatorId) {
        this.operationType = operationType;
        this.operationSubjectId = operationSubjectId;
        this.resourceType = resourceType;
        this.sharingPolicy = sharingPolicy;
        this.residentOrgId = residentOrgId;
        this.initiatorId = initiatorId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationSubjectId() {
        return operationSubjectId;
    }

    public void setOperationSubjectId(String operationSubjectId) {
        this.operationSubjectId = operationSubjectId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSharingPolicy() {
        return sharingPolicy;
    }

    public void setSharingPolicy(String sharingPolicy) {
        this.sharingPolicy = sharingPolicy;
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

}