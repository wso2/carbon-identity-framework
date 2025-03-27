package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * In-memory queue to store async operations, with database fallback.
 */
public class AsyncOperationDataBuffer {

    private static final Logger LOGGER = Logger.getLogger(AsyncOperationDataBuffer.class.getName());
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
    public synchronized void add(UnitOperationRecord operation) {

        queue.offer(operation);
        if (queue.size() >= threshold) {
            persistToDatabase();
        }
    }

    /**
     * Periodically flushes the queue to avoid long delays for small workloads.
     */
    private void startPeriodicFlushTask() {

        scheduler.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {
                LOGGER.info("Periodic flush triggered.");
                persistToDatabase();
            }
        }, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
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
     * Check if the queue is empty.
     *
     * @return True if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {

        return queue.isEmpty();
    }

    /**
     * Persist queued operations to the database in batch.
     */
    private synchronized void persistToDatabase() {

        LOGGER.info("Queue size exceeded threshold, persisting operations to DB...");
        if (!queue.isEmpty()) {
            asyncStatusMgtDAO.saveOperationsBatch(queue);
            queue.clear();
        }
    }

    /**
     * Shuts down the scheduled task.
     */
    public void shutdown() {

        scheduler.shutdown();
    }

}
