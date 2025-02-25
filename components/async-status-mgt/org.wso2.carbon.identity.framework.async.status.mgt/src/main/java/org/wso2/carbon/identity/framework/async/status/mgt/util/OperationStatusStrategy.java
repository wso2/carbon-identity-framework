package org.wso2.carbon.identity.framework.async.status.mgt.util;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;

public interface OperationStatusStrategy {
    /**
     * Registers the operation status based on the specific sharing strategy.
     *
     * @param operationContext The context of the operation.
     */
    String register(OperationContext operationContext);

    /**
     * Processes individual unit operations within the async process.
     *
     * @param unitOperationContext The context of the unit operation.
     */
    void registerUnitOperation(UnitOperationContext unitOperationContext);

    /**
     * Processes a bulk of unit operations within the async process.
     *
     * @param bulkUnitOperationContext The context of bulk of unit operations.
     */
    void registerBulkUnitOperations(ResponseUnitOperationContext bulkUnitOperationContext);
}
