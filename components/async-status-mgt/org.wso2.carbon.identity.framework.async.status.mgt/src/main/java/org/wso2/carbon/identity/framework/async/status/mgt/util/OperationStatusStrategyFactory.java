package org.wso2.carbon.identity.framework.async.status.mgt.util;

import org.wso2.carbon.identity.framework.async.status.mgt.util.strategy.ApplicationSharingStatusStrategy;
import org.wso2.carbon.identity.framework.async.status.mgt.util.strategy.BulkUserImportStatusStrategy;
import org.wso2.carbon.identity.framework.async.status.mgt.util.strategy.UserSharingStatusStrategy;

public class OperationStatusStrategyFactory {
    /**
     * Returns the appropriate strategy based on resource type.
     *
     * @param resourceType The type of resource (user, application, bulk_import)
     * @return Corresponding OperationStatusStrategy
     */
    public static OperationStatusStrategy getStrategy(String resourceType) {
        switch (resourceType.toLowerCase()) {
            case "user":
                return new UserSharingStatusStrategy();
            case "application":
                return new ApplicationSharingStatusStrategy();
            case "bulk_import":
                return new BulkUserImportStatusStrategy();
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }
    }
}
