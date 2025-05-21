/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.buffer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.buffer.SubOperationStatusQueue;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.PARTIALLY_COMPLETED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.SUCCESS;

public class SubOperationStatusQueueTest {

    @DataProvider(name = "operationStatusProvider")
    public Object[][] operationStatusProvider() {

        return new Object[][]{
                {Collections.emptyList(), SUCCESS},
                {Collections.singletonList(SUCCESS), SUCCESS},
                {Collections.singletonList(FAILED), FAILED},
                {Collections.singletonList(PARTIALLY_COMPLETED), PARTIALLY_COMPLETED},
                {Arrays.asList(SUCCESS, FAILED), PARTIALLY_COMPLETED},
                {Arrays.asList(SUCCESS, PARTIALLY_COMPLETED), PARTIALLY_COMPLETED},
                {Arrays.asList(FAILED, PARTIALLY_COMPLETED), PARTIALLY_COMPLETED},
        };
    }

    @Test(dataProvider = "operationStatusProvider")
    public void testRegisterOperationStatusWithoutUpdate(List<OperationStatus> statusList,
                                                         OperationStatus expectedStatus) {

        SubOperationStatusQueue subOperationList = new SubOperationStatusQueue();
        for (OperationStatus obj : statusList) {
            subOperationList.add(obj);
        }
        assertEquals(expectedStatus, subOperationList.getOperationStatus());
    }
}
