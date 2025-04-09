/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.framework.async.status.mgt.api.buffer;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.wso2.carbon.identity.framework.async.status.mgt.api.constants.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.status.mgt.api.constants.OperationStatus.PARTIAL;
import static org.wso2.carbon.identity.framework.async.status.mgt.api.constants.OperationStatus.SUCCESS;

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
