package org.wso2.carbon.identity.framework.async.status.mgt.util;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;

public interface OperationStatusStrategy {
    /**
     * Registers the operation status based on the specific sharing strategy.
     *
     * @param operationContext The context of the operation.
     */
    void register(OperationContext operationContext);
}
