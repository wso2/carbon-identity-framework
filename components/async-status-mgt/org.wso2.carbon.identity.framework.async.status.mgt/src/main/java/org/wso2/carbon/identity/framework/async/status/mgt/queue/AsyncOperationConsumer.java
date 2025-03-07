package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;
import java.util.logging.Logger;

/**
 * Consumer that processes operations when available.
 */
public class AsyncOperationConsumer implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(AsyncOperationConsumer.class.getName());
    private final AsyncOperationQueue queue;
    private volatile boolean running = true;

    public AsyncOperationConsumer(AsyncOperationQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (running) {
            UnitOperationContext operation = queue.dequeue(); // Blocks until new data
            processOperation(operation);
        }
    }

    private void processOperation(UnitOperationContext operation) {
        LOGGER.info("Processing operation: " + operation.getOperationId());
        // Process the operation (e.g., update status, notify users)
    }

    public void stop() {
        running = false;
    }
}

