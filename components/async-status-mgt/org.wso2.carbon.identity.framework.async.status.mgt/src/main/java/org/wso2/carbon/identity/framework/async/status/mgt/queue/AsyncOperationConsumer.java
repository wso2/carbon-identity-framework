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
            UnitOperationContext operation = queue.dequeue();
            processOperation(operation);
        }
    }

    private void processOperation(UnitOperationContext operation) {
        LOGGER.info("Processing operation: " + operation.getOperationId());
    }

    public void stop() {
        running = false;
    }
}

