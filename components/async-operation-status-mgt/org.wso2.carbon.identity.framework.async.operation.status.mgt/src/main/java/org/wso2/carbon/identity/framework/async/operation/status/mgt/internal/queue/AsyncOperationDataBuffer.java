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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.AsyncOperationStatusMgtDAO;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory queue to store async operations, with database fallback.
 */
public class AsyncOperationDataBuffer {

    private static final Log LOG = LogFactory.getLog(AsyncOperationDataBuffer.class);
    private final ConcurrentLinkedQueue<UnitOperationInitDTO> queue = new ConcurrentLinkedQueue<>();
    private final AsyncOperationStatusMgtDAO asyncOperationStatusMgtDAO;
    private final int threshold;
    private final int flushIntervalSeconds;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public AsyncOperationDataBuffer(AsyncOperationStatusMgtDAO asyncOperationStatusMgtDAO, int threshold,
                                    int flushIntervalSeconds) {

        this.asyncOperationStatusMgtDAO = asyncOperationStatusMgtDAO;
        this.threshold = threshold;
        this.flushIntervalSeconds = flushIntervalSeconds;

        startPeriodicFlushTask();
    }

    /**
     * Add an operation to the queue. If queue exceeds threshold, persist to DB.
     *
     * @param operation The operation to add.
     */
    public synchronized void add(UnitOperationInitDTO operation) throws AsyncOperationStatusMgtException {

        queue.offer(operation);
        if (queue.size() >= threshold) {
            persistToDatabase();
        }
    }

    /**
     * Check if the queue is empty.
     *
     * @return True if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {

        return queue.isEmpty();
    }

    /**
     * Retrieve and remove an operation from the queue.
     *
     * @return The next operation, or null if the queue is empty.
     */
    public synchronized UnitOperationInitDTO dequeue() {

        return queue.poll();
    }

    /**
     * Shuts down the scheduled task.
     */
    public void shutdown() {

        scheduler.shutdown();
    }

    /**
     * Persist queued operations to the database in batch.
     */
    private synchronized void persistToDatabase() throws AsyncOperationStatusMgtException {

        if (!queue.isEmpty()) {
            asyncOperationStatusMgtDAO.registerAsyncStatusUnit(queue);
            queue.clear();
        }
    }

    /**
     * Periodically flushes the queue to avoid long delays for small workloads.
     */
    private void startPeriodicFlushTask() {

        scheduler.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {
                try {
                    persistToDatabase();
                } catch (AsyncOperationStatusMgtException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.error("Error while flushing unit operation records to the database.", e);
                    }
                }
            }
        }, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
    }

}
