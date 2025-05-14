package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.BulkUserImportOperationDO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationDO;

import java.util.logging.Logger;

@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());

    @Override
    public void processB2BAsyncOperationStatus(SharingOperationDO sharingOperationDO) {

    }

    @Override
    public void processBulkUserImportAsyncOperationStatus(BulkUserImportOperationDO bulkUserImportOperationDO) {

    }

    @Override
    public void test(String operation) {
        LOGGER.info("Process Started: "+operation);
    }
}
