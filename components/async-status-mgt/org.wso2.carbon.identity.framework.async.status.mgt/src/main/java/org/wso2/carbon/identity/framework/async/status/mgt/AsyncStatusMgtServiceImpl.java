package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.*;
import org.wso2.carbon.identity.framework.async.status.mgt.queue.AsyncOperationConsumer;
import org.wso2.carbon.identity.framework.async.status.mgt.queue.AsyncOperationQueue;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {
    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private OperationStatusStrategy strategy;
    private static final Logger LOGGER = Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());
    private final BlockingQueue<UnitOperationContext> eventQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, List<UnitOperationContext>> operationSubTasks = new ConcurrentHashMap<>();
    private final AsyncOperationQueue queue;
    private final AsyncOperationConsumer consumer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AsyncStatusMgtServiceImpl() {
        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.strategy = null;
//        new Thread(this::processEvents, "AsyncEventProcessor").start();
        this.queue = new AsyncOperationQueue(asyncStatusMgtDAO, 100, 20);
        this.consumer = new AsyncOperationConsumer(queue);
        // Start consumer in a dedicated thread
        executorService.submit(consumer);
    }
    public AsyncStatusMgtServiceImpl(OperationStatusStrategy strategy) {
        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.strategy = strategy;
        this.queue = new AsyncOperationQueue(asyncStatusMgtDAO, 100, 20);
        this.consumer = new AsyncOperationConsumer(queue);
//        new Thread(this::processEvents, "AsyncEventProcessor").start();
        // Start consumer in a dedicated thread
        executorService.submit(consumer);
    }

    @Override
    public String registerOperationStatus(String operationType, String operationSubjectId, String resourceType, String sharingPolicy, String residentOrgId, String initiatorId) {
        strategy = OperationStatusStrategyFactory.getStrategy(resourceType);
        String operationId = null;

        OperationContext context = new OperationContext();
        context.setOperationType(operationType);
        context.setOperationSubjectId(operationSubjectId);
        context.setResourceType(resourceType);
        context.setSharingPolicy(sharingPolicy);
        context.setResidentOrgId(residentOrgId);
        context.setInitiatorId(initiatorId);

        if (strategy != null) {
            operationId = strategy.register(context);
        } else {
            LOGGER.warning("Strategy is not initialized. Cannot register operation status.");
        }
        return operationId;
    }

    @Override
    public void updateOperationStatus(String operationId, String status) {
        asyncStatusMgtDAO.updateAsyncOperationStatus(operationId, status);
    }

    @Override
    public void registerUnitOperationStatus(String operationId, String operationType, String operationInitiatedResourceId, String sharedOrgId, String unitOperationStatus, String statusMessage) {
        strategy = OperationStatusStrategyFactory.getStrategy(operationType);

        UnitOperationContext context = new UnitOperationContext();
        context.setOperationId(operationId);
        context.setOperationType(operationType);
        context.setOperationInitiatedResourceId(operationInitiatedResourceId);
        context.setTargetOrgId(sharedOrgId);
        context.setUnitOperationStatus(unitOperationStatus);
        context.setStatusMessage(statusMessage);

        if (strategy != null) {
            strategy.registerUnitOperation(context);
        } else {
            LOGGER.warning("Strategy is not initialized. Cannot register operation status.");
        }
    }

    @Override
    public void registerBulkUnitOperationStatus(ResponseUnitOperationContext context) {
//        strategy = OperationStatusStrategyFactory.getStrategy(context.getOperationType());
        if (strategy != null) {
            strategy.registerBulkUnitOperations(context);
        } else {
            LOGGER.warning("Strategy is not initialized. Cannot register operation status.");
        }
    }

    @Override
    public ResponseOperationContext getLatestAsyncOperationStatus(String orgId, String operationSubjectId, String resourceType, String userId) {
        return asyncStatusMgtDAO.getLatestAsyncOperationStatus(operationSubjectId, orgId, resourceType, userId);
    }

    @Override
    public void finalizeOperation(String operationId) {
        List<UnitOperationContext> subOperations = operationSubTasks.remove(operationId);
        if (subOperations != null) {
            LOGGER.info("Processing finalization for operation ID: " + operationId);
            processFinalizedOperation(subOperations);
        } else {
            LOGGER.warning("No sub-operations found for operation ID: " + operationId);
        }
    }

    private void processFinalizedOperation(List<UnitOperationContext> subOperations) {
        LOGGER.info("Finalized " + subOperations.size() + " sub-operations.");
    }
    @Override
    public void addOperation(UnitOperationContext operation) {
        queue.enqueue(operation);
    }

    @Override
    public void shutdown() {
        consumer.stop();
        executorService.shutdown();
    }
}
