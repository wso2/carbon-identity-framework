package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;

import java.util.logging.Logger;

/**
 * Consumer that processes operations when available.
 */
public class AsyncOperationConsumer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AsyncOperationConsumer.class.getName());
    private final AsyncOperationDataBuffer queue;
    private volatile boolean running = true;

    public AsyncOperationConsumer(AsyncOperationDataBuffer queue) {

        this.queue = queue;
    }

    @Override
    public void run() {

        LOGGER.info("HERE");
        while (running) {
            LOGGER.info("HERE");
            UnitOperationRecord operation = queue.dequeue();
            processOperation(operation);
        }
    }

    private void processOperation(UnitOperationRecord operation) {

        LOGGER.info("Processing operation: " + operation.getOperationId());
        LOGGER.info("HERE");
    }

    public void stop() {

        running = false;
    }
}

