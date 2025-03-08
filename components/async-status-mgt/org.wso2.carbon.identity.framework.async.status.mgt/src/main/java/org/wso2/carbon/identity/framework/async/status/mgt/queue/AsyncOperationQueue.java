package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;

import java.util.concurrent.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * In-memory queue to store async operations, with database fallback.
 */
public class AsyncOperationQueue {
    private static final Logger LOGGER = Logger.getLogger(AsyncOperationQueue.class.getName());
    private final ConcurrentLinkedQueue<UnitOperationContext> queue = new ConcurrentLinkedQueue<>();
    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private final int threshold;
    private final int flushIntervalSeconds;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public AsyncOperationQueue(AsyncStatusMgtDAO asyncStatusMgtDAO, int threshold, int flushIntervalSeconds) {
        this.asyncStatusMgtDAO = asyncStatusMgtDAO;
        this.threshold = threshold;
        this.flushIntervalSeconds = flushIntervalSeconds;

        startPeriodicFlushTask();
    }

    /**
     * Add an operation to the queue. If queue exceeds threshold, persist to DB.
     * @param operation The operation to add.
     */
    public synchronized void enqueue(UnitOperationContext operation) {
        queue.offer(operation);
        LOGGER.info("Queue size: " + queue.size());
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
     * @return The next operation, or null if the queue is empty.
     */
    public synchronized UnitOperationContext dequeue() {
        return queue.poll();
    }

    /**
     * Check if the queue is empty.
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
    }/**
     * Shuts down the scheduled task.
     */
    public void shutdown() {
        scheduler.shutdown();
    }

}
