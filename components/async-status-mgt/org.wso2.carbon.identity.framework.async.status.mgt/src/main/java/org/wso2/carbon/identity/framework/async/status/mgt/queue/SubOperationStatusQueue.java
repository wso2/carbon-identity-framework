package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus.PARTIAL;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus.SUCCESS;

/**
 * A thread-safe queue that holds {@link SubOperationStatusObject} instances representing
 * the status of individual sub-operations within an asynchronous operation.
 * <p>
 * Provides utility methods to add sub-operation statuses, iterate over them, and
 * compute the overall status of the operation based on individual results.
 */
public class SubOperationStatusQueue {

    private ConcurrentLinkedQueue<SubOperationStatusObject> subOperationList = new ConcurrentLinkedQueue<>();

    public SubOperationStatusQueue() {

    }

    public void add(SubOperationStatusObject subOperationStatusObject) {

        this.subOperationList.add(subOperationStatusObject);
    }

    public Iterator<SubOperationStatusObject> iterator() {

        return this.subOperationList.iterator();
    }

    public String getOperationStatus() {

        boolean allSuccess = true;
        boolean allFail = true;

        for (SubOperationStatusObject statusObject : subOperationList) {
            String status = statusObject.getStatus();
            if (PARTIAL.toString().equals(status)) {
                return PARTIAL.toString();
            } else if (FAILED.toString().equals(status)) {
                allSuccess = false;
            } else if (SUCCESS.toString().equals(status)) {
                allFail = false;
            }
        }

        if (allSuccess) {
            return SUCCESS.toString();
        } else if (allFail) {
            return FAILED.toString();
        }
        return SUCCESS.toString();
    }
}
