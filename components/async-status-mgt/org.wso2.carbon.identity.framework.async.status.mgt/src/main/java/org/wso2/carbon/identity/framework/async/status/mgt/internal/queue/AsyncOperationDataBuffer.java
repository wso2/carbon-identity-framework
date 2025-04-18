/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.internal.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.AsyncStatusMgtDAO;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory queue to store async operations, with database fallback.
 */
public class AsyncOperationDataBuffer {

    private static final Log LOG = LogFactory.getLog(AsyncOperationDataBuffer.class);
    private final ConcurrentLinkedQueue<UnitOperationRecord> queue = new ConcurrentLinkedQueue<>();
    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private final int threshold;
    private final int flushIntervalSeconds;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public AsyncOperationDataBuffer(AsyncStatusMgtDAO asyncStatusMgtDAO, int threshold, int flushIntervalSeconds) {

        this.asyncStatusMgtDAO = asyncStatusMgtDAO;
        this.threshold = threshold;
        this.flushIntervalSeconds = flushIntervalSeconds;

        startPeriodicFlushTask();
    }

    /**
     * Add an operation to the queue. If queue exceeds threshold, persist to DB.
     *
     * @param operation The operation to add.
     */
    public synchronized void add(UnitOperationRecord operation) throws AsyncStatusMgtException {

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
    public synchronized UnitOperationRecord dequeue() {

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
    private synchronized void persistToDatabase() throws AsyncStatusMgtException {

        if (!queue.isEmpty()) {
            asyncStatusMgtDAO.registerAsyncStatusUnit(queue);
            queue.clear();
        }
    }

    /**
     * Periodically flushes the queue to avoid long delays for small workloads.
     */
    private void startPeriodicFlushTask() {

        scheduler.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {
                LOG.debug("Periodic flush triggered.");
                try {
                    persistToDatabase();
                } catch (AsyncStatusMgtException e) {  //TODO: to rethrow or handle here?
                    LOG.error("Error while flushing to the database.", e);
                }
            }
        }, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
    }

}
