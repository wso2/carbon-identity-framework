package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

public class UnitOperationRecord {
    private String operationId;
    private String operationType;
    private String operationInitiatedResourceId;
    private String targetOrgId;
    private String unitOperationStatus;
    private String statusMessage;

    public UnitOperationRecord() {
    }

    public UnitOperationRecord(String operationId, String operationType, String operationInitiatedResourceId, String targetOrgId, String unitOperationStatus, String statusMessage) {
        this.operationId = operationId;
        this.operationType = operationType;
        this.operationInitiatedResourceId = operationInitiatedResourceId;
        this.targetOrgId = targetOrgId;
        this.unitOperationStatus = unitOperationStatus;
        this.statusMessage = statusMessage;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationInitiatedResourceId() {
        return operationInitiatedResourceId;
    }

    public void setOperationInitiatedResourceId(String operationInitiatedResourceId) {
        this.operationInitiatedResourceId = operationInitiatedResourceId;
    }

    public String getTargetOrgId() {
        return targetOrgId;
    }

    public void setTargetOrgId(String targetOrgId) {
        this.targetOrgId = targetOrgId;
    }

    public String getUnitOperationStatus() {
        return unitOperationStatus;
    }

    public void setUnitOperationStatus(String unitOperationStatus) {
        this.unitOperationStatus = unitOperationStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
