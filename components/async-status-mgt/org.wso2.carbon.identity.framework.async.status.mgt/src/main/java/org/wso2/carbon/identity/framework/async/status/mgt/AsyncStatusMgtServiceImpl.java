package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.AsyncStatusMgtDataHolder;
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
        this.asyncStatusMgtDAO = AsyncStatusMgtDataHolder.getInstance().getAsyncStatusMgtDAO();
//        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.operationDataBuffer = new AsyncOperationDataBuffer(asyncStatusMgtDAO, 100, 5);
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatus(String operationType, String operationSubjectId) {
        return asyncStatusMgtDAO.getLatestAsyncOperationStatus(operationType, operationSubjectId);
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatusByInitiatorId(String operationType, String operationSubjectId, String initiatorId) {
        return asyncStatusMgtDAO.getLatestAsyncOperationStatusByInitiatorId(operationType, operationSubjectId, initiatorId);
    }

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithinDays(String operationType, String operationSubjectId, int days) {
        return asyncStatusMgtDAO.getAsyncOperationStatusWithinDays(operationType, operationSubjectId, days);
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

    @Override
    public AsyncStatusMgtDAO getAsyncStatusMgtDAO() {
        return asyncStatusMgtDAO;
    }
}
