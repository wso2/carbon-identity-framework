/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.framework.async.status.mgt;

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.BulkUserImportOperationDO;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationDO;

/**
 * Service that processes the status of
 * asynchronous operations.
 */
public interface AsyncStatusMgtService {

    /**
     * Processing the status on B2B Asynchronous
     * Resource Share Operation.
     *
     * @param sharingOperationDO Sharing operation metadata is shared.
     */
    void processB2BAsyncOperationStatus(SharingOperationDO sharingOperationDO);

    /**
     * Processing the status on B2B Asynchronous
     * Resource Share Operation.
     *
     * @param bulkUserImportOperationDO Sharing operation metadata is shared.
     */
    void processBulkUserImportAsyncOperationStatus(BulkUserImportOperationDO bulkUserImportOperationDO);

    /**
     * Test method for the interface
     *
     */
    void test(String operation);
}
