package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

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
            if ("FAIL".equals(status)) {
                allSuccess = false;
            } else if ("SUCCESS".equals(status)) {
                allFail = false;
            }
        }
        if (allSuccess) {
            return "SUCCESS";
        } else if (allFail)
            return "FAIL";
        return "PARTIAL";
    }
}
