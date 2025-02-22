package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

public class UnitOperationContext {
    private String operationId;
    private String operationInitiatedResourceId;
    private String sharedOrgId;
    private String unitOperationStatus;
    private String statusMessage;

    public UnitOperationContext() {
    }

    public UnitOperationContext(String operationId, String operationInitiatedResourceId, String sharedOrgId, String unitOperationStatus, String statusMessage) {
        this.operationId = operationId;
        this.operationInitiatedResourceId = operationInitiatedResourceId;
        this.sharedOrgId = sharedOrgId;
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

    public String getSharedOrgId() {
        return sharedOrgId;
    }

    public void setSharedOrgId(String sharedOrgId) {
        this.sharedOrgId = sharedOrgId;
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
}
