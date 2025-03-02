package org.wso2.carbon.identity.framework.async.status.mgt.util.strategy;

import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationDBContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;

import java.util.ArrayList;
import java.util.logging.Logger;

public class UserSharingStatusStrategy implements OperationStatusStrategy {
    private static final Logger LOGGER =
            Logger.getLogger(UserSharingStatusStrategy.class.getName());

    @Override
    public String register(OperationContext operationContext) {
        LOGGER.info("Registering User Share Operation For: " + operationContext.getOperationSubjectId() + " Started.");

        OperationDBContext dbContext = new OperationDBContext(operationContext, getAudienceForOperationRegistration());
        AsyncStatusMgtDAO dao = new AsyncStatusMgtDAOImpl();
        String operationId = dao.registerAsyncOperation(dbContext);

        LOGGER.info("Registering User Share Operation For: " + operationContext.getOperationSubjectId() + " Completed");
        return operationId;
    }

    @Override
    public void registerUnitOperation(UnitOperationContext context) {
        LOGGER.info("Registering Unit Operation For: " + context.getOperationId() + " Started.");

        AsyncStatusMgtDAO dao = new AsyncStatusMgtDAOImpl();
        dao.registerUnitAsyncOperation(context.getOperationId(), context.getOperationInitiatedResourceId(), context.getTargetOrgId(), context.getUnitOperationStatus(), context.getStatusMessage());

        LOGGER.info("Registering Unit Operation For: " + context.getOperationId() + " Completed");
    }

    @Override
    public void registerBulkUnitOperations(ResponseUnitOperationContext context) {
        LOGGER.info("Registering Bulk Unit Operations For: " + context.getOperationId() + " Started.");

        AsyncStatusMgtDAO dao = new AsyncStatusMgtDAOImpl();
        dao.registerBulkUnitAsyncOperation(context.getOperationId(), context.getOperationType(), context.getQueue());

        LOGGER.info("Registering Bulk Unit Operations For: " + context.getOperationId() + " Completed");
    }

    private ArrayList<String> getAudienceForOperationRegistration() {
        return new ArrayList<>();
    }

}
