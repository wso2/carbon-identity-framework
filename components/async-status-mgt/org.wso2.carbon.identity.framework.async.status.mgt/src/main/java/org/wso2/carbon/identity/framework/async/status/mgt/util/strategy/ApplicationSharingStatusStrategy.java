package org.wso2.carbon.identity.framework.async.status.mgt.util.strategy;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;

import java.util.logging.Logger;

public class ApplicationSharingStatusStrategy implements OperationStatusStrategy {
    private static final Logger LOGGER =
            Logger.getLogger(ApplicationSharingStatusStrategy.class.getName());

    @Override
    public String register(OperationContext operationContext) {
        LOGGER.info("Registering Application Share Operation: " + operationContext.getOperationType());
        return "";
    }

    @Override
    public void registerUnitOperation(UnitOperationContext unitOperationContext) {

    }

    @Override
    public void registerBulkUnitOperations(ResponseUnitOperationContext bulkUnitOperationContext) {

    }
}
