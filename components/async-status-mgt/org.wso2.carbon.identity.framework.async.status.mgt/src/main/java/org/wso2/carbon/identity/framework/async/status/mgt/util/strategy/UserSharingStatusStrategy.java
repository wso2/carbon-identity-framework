package org.wso2.carbon.identity.framework.async.status.mgt.util.strategy;

import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtServiceImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationDBContext;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;

import java.util.ArrayList;
import java.util.logging.Logger;

public class UserSharingStatusStrategy implements OperationStatusStrategy {
    private static final Logger LOGGER =
            Logger.getLogger(UserSharingStatusStrategy.class.getName());

    @Override
    public void register(OperationContext operationContext) {
        LOGGER.info("Registering User Share Operation For: " + operationContext.getOperationSubjectId() + " Started.");

        OperationDBContext dbContext = new OperationDBContext(operationContext, getAudienceForOperationRegistration());
        AsyncStatusMgtDAO dao = new AsyncStatusMgtDAOImpl();
        dao.registerB2BUserSharingAsyncOperation(dbContext);

        LOGGER.info("Registering User Share Operation For: " + operationContext.getOperationSubjectId() + " Completed.");
    }

    private ArrayList<String> getAudienceForOperationRegistration() {
        return new ArrayList<>();
    }

}
