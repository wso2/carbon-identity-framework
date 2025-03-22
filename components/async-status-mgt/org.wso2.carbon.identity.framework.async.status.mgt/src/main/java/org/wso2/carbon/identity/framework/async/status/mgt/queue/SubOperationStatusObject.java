package org.wso2.carbon.identity.framework.async.status.mgt.queue;

/**
 * Represents the status of a single sub-operation within an asynchronous operation.
 * This object holds a status value (e.g., "SUCCESS" or "FAIL") that can be used
 * to evaluate the overall operation's result.
 */
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
