package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

public class AsyncOperationThreadData {
    private final String operationId;
    private final String operationStatus;

    public AsyncOperationThreadData(String operationId, String operationStatus) {
        this.operationStatus = operationStatus;
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String operationStatus() {
        return operationStatus;
    }
}
