package org.wso2.carbon.identity.framework.async.status.mgt.util.strategy;

import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtServiceImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;

import java.util.logging.Logger;

public class BulkUserImportStatusStrategy implements OperationStatusStrategy {
    private static final Logger LOGGER =
            Logger.getLogger(BulkUserImportStatusStrategy.class.getName());

    @Override
    public void register(OperationContext operationContext) {
        LOGGER.info("Registering Bulk User Import Operation: " + operationContext.getOperationType());
    }
}
