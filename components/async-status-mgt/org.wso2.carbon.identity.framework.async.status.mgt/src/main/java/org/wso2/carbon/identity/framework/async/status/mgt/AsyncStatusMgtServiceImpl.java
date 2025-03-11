package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.*;
import org.wso2.carbon.identity.framework.async.status.mgt.queue.AsyncOperationDataBuffer;

import java.util.List;
import java.util.logging.Logger;

@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {
    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private static final Logger LOGGER = Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());
    private final AsyncOperationDataBuffer operationDataBuffer;

    public AsyncStatusMgtServiceImpl() {
        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.operationDataBuffer = new AsyncOperationDataBuffer(asyncStatusMgtDAO, 100, 5);
    }

    @Override
    public ResponseOperationContext getLatestAsyncOperationStatus(String resourceType, String operationSubjectId) {
        return asyncStatusMgtDAO.getLatestAsyncOperationStatus(resourceType, operationSubjectId);
    }

    @Override
    public ResponseOperationContext getLatestAsyncOperationStatusByInitiatorId(String resourceType, String operationSubjectId, String initiatorId) {
        return asyncStatusMgtDAO.getLatestAsyncOperationStatusByInitiatorId(resourceType, operationSubjectId, initiatorId);
    }

    @Override
    public List<ResponseOperationContext> getAsyncOperationStatusWithinDays(String resourceType, String operationSubjectId, int days) {
        return asyncStatusMgtDAO.getAsyncOperationStatusWithinDays(resourceType, operationSubjectId, days);
    }

    @Override
    public String registerOperationStatus(OperationRecord record, boolean updateIfExists) {
        if (updateIfExists){
            return asyncStatusMgtDAO.registerAsyncOperationWithUpdate(record);
        }
        return asyncStatusMgtDAO.registerAsyncOperationWithoutUpdate(record);
    }

    @Override
    public void updateOperationStatus(String operationId, String status) {
        asyncStatusMgtDAO.updateAsyncOperationStatus(operationId, status);
    }

    @Override
    public void registerUnitOperationStatus(UnitOperationRecord unitOperationRecord) {
        operationDataBuffer.add(unitOperationRecord);
    }
}
