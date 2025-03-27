package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

/**
 * Represents a record of a unit operation, which is a part of a larger asynchronous operation.
 * This class encapsulates the details of a unit operation, including its status and related information.
 */
public class UnitOperationRecord {

    private String operationId;
    private String operationInitiatedResourceId;
    private String targetOrgId;
    private String unitOperationStatus;
    private String statusMessage;

    public UnitOperationRecord() {

    }

    public UnitOperationRecord(String operationId, String operationInitiatedResourceId, String targetOrgId,
                               String unitOperationStatus, String statusMessage) {

        this.operationId = operationId;
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
}
