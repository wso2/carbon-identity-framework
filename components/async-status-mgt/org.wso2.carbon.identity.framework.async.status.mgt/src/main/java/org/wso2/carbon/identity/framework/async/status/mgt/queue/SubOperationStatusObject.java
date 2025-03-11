package org.wso2.carbon.identity.framework.async.status.mgt.queue;

public class SubOperationStatusObject {
    private String status;

    public SubOperationStatusObject(String status) {
        this.status = status;
    }

    public SubOperationStatusObject() {
        this.status = null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
