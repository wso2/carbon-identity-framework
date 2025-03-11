package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponseUnitOperationContext {
    private String operationId;
    private String operationType;
    private ConcurrentLinkedQueue<UnitOperationRecord> queue;
    private String unitOperationStatus;
    private String statusMessage;

    public ResponseUnitOperationContext() {
    }

    public ResponseUnitOperationContext(String operationId, String operationType, ConcurrentLinkedQueue<UnitOperationRecord> queue, String unitOperationStatus, String statusMessage) {
        this.operationId = operationId;
        this.operationType = operationType;
        this.queue = queue;
        this.unitOperationStatus = unitOperationStatus;
        this.statusMessage = statusMessage;
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

    public ConcurrentLinkedQueue<UnitOperationRecord> getQueue() {
        return queue;
    }

    public void setQueue(ConcurrentLinkedQueue<UnitOperationRecord> queue) {
        this.queue = queue;
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
