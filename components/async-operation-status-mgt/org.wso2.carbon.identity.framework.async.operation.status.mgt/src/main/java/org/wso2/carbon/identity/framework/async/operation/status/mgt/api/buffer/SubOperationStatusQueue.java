/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.api.buffer;

import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.PARTIALLY_COMPLETED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.SUCCESS;

/**
 * A thread-safe queue that holds {@link OperationStatus} instances representing
 * the status of individual sub-operations within an asynchronous operation.
 * <p>
 * Provides utility methods to add sub-operation statuses, iterate over them, and
 * compute the overall status of the operation based on individual results.
 */
public class SubOperationStatusQueue {

    private ConcurrentLinkedQueue<OperationStatus> subOperationList = new ConcurrentLinkedQueue<OperationStatus>();

    /**
     * Constructs a new empty {@link SubOperationStatusQueue}.
     */
    public SubOperationStatusQueue() {
    }

    /**
     * Adds a new status representing the result of a sub-operation to the queue.
     *
     * @param status The status of the sub-operation (e.g., SUCCESS, FAILED, PARTIALLY_COMPLETED).
     */
    public void add(OperationStatus status) {

        this.subOperationList.add(status);
    }

    /**
     * Computes and returns the overall status of the operation based on the individual sub-operation statuses
     * in the queue. The operation status can be one of the following:
     * <ul>
     *     <li>{@link OperationStatus#SUCCESS} if all sub-operations succeeded.</li>
     *     <li>{@link OperationStatus#FAILED} if all sub-operations failed.</li>
     *     <li>{@link OperationStatus#PARTIALLY_COMPLETED} if some sub-operations succeeded and others failed or are
     *     incomplete.</li>
     * </ul>
     *
     * @return The overall {@link OperationStatus} of the operation.
     */
    public OperationStatus getOperationStatus() {

        boolean allSuccess = true;
        boolean allFail = true;

        for (OperationStatus status : subOperationList) {
            if (PARTIALLY_COMPLETED.equals(status)) {
                return PARTIALLY_COMPLETED;
            } else if (FAILED.equals(status)) {
                allSuccess = false;
            } else if (SUCCESS.equals(status)) {
                allFail = false;
            }
        }

        if (allSuccess) {
            return SUCCESS;
        } else if (allFail) {
            return FAILED;
        }
        return PARTIALLY_COMPLETED;
    }
}
