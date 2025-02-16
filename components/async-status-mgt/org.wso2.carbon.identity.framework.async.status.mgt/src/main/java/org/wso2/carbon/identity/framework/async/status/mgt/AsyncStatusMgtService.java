package org.wso2.carbon.identity.framework.async.status.mgt;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationDO;

public interface AsyncStatusMgtService {
    void processB2BAsyncOperation(SharingOperationDO sharingOperationDO);
    void test(String operation);
}
