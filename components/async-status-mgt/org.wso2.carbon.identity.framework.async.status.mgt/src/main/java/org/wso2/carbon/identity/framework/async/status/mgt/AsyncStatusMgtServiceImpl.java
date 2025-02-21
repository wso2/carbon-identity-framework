package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.BulkUserImportOperationDO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationDO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationUnitDO;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;

import java.util.ArrayList;
import java.util.logging.Logger;

@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());

    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private final OperationStatusStrategy strategy;

    public AsyncStatusMgtServiceImpl(OperationStatusStrategy strategy) {
        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.strategy = strategy;
    }

    @Override
    public void processB2BAsyncOperationStatus(SharingOperationDO sharingOperationDO) {
        LOGGER.info("processB2BAsyncOperationStatus Started...");
        if (sharingOperationDO == null) {
            LOGGER.warning("Received null SharingOperationDO.");
            return;
        }
        String operationType = sharingOperationDO.getOperationType();
        String residentResourceId = sharingOperationDO.getResidentResourceId();
        String resourceType = String.valueOf(sharingOperationDO.getResourceType());
        String sharingPolicy = sharingOperationDO.getSharingPolicy();
        String residentOrgId = sharingOperationDO.getResidentOrganizationId();
        String initiatorId = sharingOperationDO.getInitiatorId();
        String operationStatus = sharingOperationDO.getOperationStatus();
        ArrayList<SharingOperationUnitDO> unitSharingList = sharingOperationDO.getUnitSharingList();

        try {
            asyncStatusMgtDAO.createB2BResourceSharingOperation(
                    operationType,
                    residentResourceId,
                    resourceType,
                    sharingPolicy,
                    residentOrgId,
                    initiatorId,
                    operationStatus
            );
            LOGGER.info("B2B async operation status processed successfully.");
        } catch (Exception e) {
            LOGGER.severe("Error while processing B2B async operation status: " + e.getMessage());
        }
    }

    @Override
    public void testCheckDatabaseConnection() {
        try {
            asyncStatusMgtDAO.createB2BResourceSharingOperation(
                    "share",
                    "123",
                    "user",
                    "all-orgs",
                    "456",
                    "789",
                    "complete with success."
            );
            LOGGER.info("B2B async operation status processed successfully.");
        } catch (Exception e) {
            LOGGER.severe("Error while processing B2B async operation status: " + e.getMessage());
        }
    }

    @Override
    public void registerOperationStatus(OperationContext operationContext) {
        strategy.register(operationContext);
    }
}
