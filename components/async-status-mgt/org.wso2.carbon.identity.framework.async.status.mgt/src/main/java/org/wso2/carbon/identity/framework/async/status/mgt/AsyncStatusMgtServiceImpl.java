package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.queue.AsyncOperationDataBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of the {@link AsyncStatusMgtService} interface that manages asynchronous operation statuses.
 * This service interacts with the {@link AsyncStatusMgtDAO} to perform persistence operations and
 * uses an in-memory buffer to temporarily store unit operation records before batch processing.
 */
@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {

    private final AsyncStatusMgtDAO asyncStatusMgtDAO;
    private static final Logger LOGGER = Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());
    private final AsyncOperationDataBuffer operationDataBuffer;

    public AsyncStatusMgtServiceImpl() {
//        this.asyncStatusMgtDAO = AsyncStatusMgtDataHolder.getInstance().getAsyncStatusMgtDAO();
        this.asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
        this.operationDataBuffer = new AsyncOperationDataBuffer(asyncStatusMgtDAO, 100, 5);
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatus(String operationType, String operationSubjectId) {

        return asyncStatusMgtDAO.getLatestAsyncOperationStatus(operationType, operationSubjectId);
    }

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithCurser(String operationType,
                                                                           String operationSubjectId) {

        return asyncStatusMgtDAO.getOperationStatusByOperationTypeAndOperationSubjectId(operationType,
                operationSubjectId);
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatusByInitiatorId(String operationType,
                                                                              String operationSubjectId,
                                                                              String initiatorId) {

        return asyncStatusMgtDAO.getLatestAsyncOperationStatusByInitiatorId(operationType, operationSubjectId,
                initiatorId);
    }

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithinDays(String operationType,
                                                                           String operationSubjectId, int days) {

        return asyncStatusMgtDAO.getAsyncOperationStatusWithinDays(operationType, operationSubjectId, days);
    }

    @Override
    public String registerOperationStatus(OperationRecord record, boolean updateIfExists) {

        if (updateIfExists) {
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

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithCurser(String operationSubjectType,
                                                                           String operationSubjectId,
                                                                           String operationType, String after,
                                                                           String before,
                                                                           Integer limit, String filter,
                                                                           Boolean latest) {

        return new ArrayList<>();
    }

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithoutCurser(String operationSubjectType,
                                                                              String operationSubjectId,
                                                                              String operationType) {

        return asyncStatusMgtDAO.getOperationStatusByOperationSubjectTypeAndOperationSubjectIdAndOperationType(
                operationSubjectType, operationSubjectId, operationType);
    }
}
