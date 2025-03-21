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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.AsyncOperationStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.impl.AsyncOperationOperationStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.dos.UnitOperationDO;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.IN_PROGRESS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.PARTIALLY_COMPLETED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.SUCCESS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_2;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.INITIATOR_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.POLICY_SELECTIVE_SHARE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_3;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_4;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_USER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_SHARE;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = { "dbScripts/async_operation_status.sql" })
@WithCarbonHome
public class AsyncOperationStatusMgtDAOTest {

    private AsyncOperationStatusMgtDAO dao;

    @BeforeClass
    public void initTest() {

        dao = new AsyncOperationOperationStatusMgtDAOImpl();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        cleanUpDB();
    }

    @DataProvider(name = "asyncOperationDetailProvider")
    public Object[][] asyncOperationDetailProvider() {

        OperationInitDTO
                operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO
                operation2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        return new Object[][] {
                {Arrays.asList(operation1, operation2)},
        };
    }

    @Test(priority = 1)
    public void testRegisterOperationWithoutUpdateSuccess() {

        try {
            OperationInitDTO
                    operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            OperationInitDTO
                    operation2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            String insertedOperationId1 = dao.registerAsyncStatusWithoutUpdate(operation1);
            assertTrue(StringUtils.isNotBlank(insertedOperationId1), "Async Op_1 Status Addition Failed.");

            dao.registerAsyncStatusWithoutUpdate(operation2);
            assertEquals(2, getOperationTableSize());
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(dataProvider = "asyncOperationDetailProvider", priority = 2)
    public void testRegisterOperationWithUpdateSuccess(List<OperationInitDTO> dtoList) {

        try {
            String insertedOperationId1 = dao.registerAsyncStatusWithUpdate(dtoList.get(0));
            assertTrue(StringUtils.isNotBlank(insertedOperationId1), "Async Op_1 Status Addition Failed.");

            dao.registerAsyncStatusWithUpdate(dtoList.get(1));
            assertEquals(1, getOperationTableSize());
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 3)
    public void testUpdateAsyncStatus() {

        try {
            OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                    SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            String initialOperationId = dao.registerAsyncStatusWithUpdate(operation1);

            OperationResponseDTO fetchedOperation = dao.getOperations(RESIDENT_ORG_ID_1, 1000, null).get(0);
            String initialStatus = fetchedOperation.getOperationStatus();
            assertEquals(IN_PROGRESS.toString(), initialStatus);

            dao.updateAsyncStatus(initialOperationId, SUCCESS);
            assertEquals(1, getOperationTableSize());

            String fetchedUpdatedStatus = dao.getOperations(RESIDENT_ORG_ID_1, 1000,
                    null).get(0).getOperationStatus();
            assertEquals(SUCCESS.toString(), fetchedUpdatedStatus);
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 4)
    public void testRegisterAsyncStatusUnit() {

        try {
            OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                    SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = dao.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = dao.getOperations(RESIDENT_ORG_ID_1, 1000,
                    null).get(0).getOperationId();

            UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, SUCCESS, StringUtils.EMPTY);
            UnitOperationInitDTO unit2 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_3, FAILED, "Invalid User Id.");
            ConcurrentLinkedQueue<UnitOperationInitDTO> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            list.add(unit2);
            dao.registerAsyncStatusUnit(list);
            dao.updateAsyncStatus(returnedId, PARTIALLY_COMPLETED);

            assertEquals(2, dao.getUnitOperations(fetchedOperationId, RESIDENT_ORG_ID_1,
                    10, null).size());
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 5)
    public void testGetOperationRecords() {

        try {
            OperationInitDTO
                    operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            OperationInitDTO
                    operation2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            assertEquals(0, dao.getOperations(RESIDENT_ORG_ID_1, 100, null).size());

            dao.registerAsyncStatusWithoutUpdate(operation1);
            dao.registerAsyncStatusWithoutUpdate(operation2);
            assertEquals(2, dao.getOperations(RESIDENT_ORG_ID_1, 100, null).size());

        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 6)
    public void testGetOperation() {

        try {
            OperationInitDTO
                    operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            dao.registerAsyncStatusWithoutUpdate(operation1);
            String fetchedOperationId = dao.getOperations(RESIDENT_ORG_ID_1, 100,
                    null).get(0).getOperationId();
            OperationResponseDTO record = dao.getOperation(fetchedOperationId, RESIDENT_ORG_ID_1);

            assertEquals(TYPE_USER_SHARE, record.getOperationType());
            assertEquals(SUBJECT_ID_1, record.getOperationSubjectId());
            assertEquals(RESIDENT_ORG_ID_1, record.getResidentOrgId());
            assertEquals(INITIATOR_ID_1, record.getInitiatorId());
            assertEquals(POLICY_SELECTIVE_SHARE, record.getOperationPolicy());
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 7)
    public void testGetUnitOperations() {

        try {
            OperationInitDTO
                    operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = dao.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = dao.getOperations(RESIDENT_ORG_ID_1, 1000,
                    null).get(0).getOperationId();

            UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, SUCCESS, StringUtils.EMPTY);
            UnitOperationInitDTO unit2 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_3, FAILED, "Invalid User Id.");
            ConcurrentLinkedQueue<UnitOperationInitDTO> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            list.add(unit2);
            dao.registerAsyncStatusUnit(list);
            dao.updateAsyncStatus(returnedId, PARTIALLY_COMPLETED);

            assertEquals(2, dao.getUnitOperations(fetchedOperationId, RESIDENT_ORG_ID_1,
                    100, null).size());
        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    @Test(priority = 8)
    public void testGetUnitOperation() {

        try {
            OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                    SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = dao.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = dao.getOperations(RESIDENT_ORG_ID_1, 1000,
                    null).get(0).getOperationId();

            UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, SUCCESS, StringUtils.EMPTY);
            ConcurrentLinkedQueue<UnitOperationInitDTO> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            dao.registerAsyncStatusUnit(list);
            dao.updateAsyncStatus(returnedId, PARTIALLY_COMPLETED);

            String addedUnitOpId = dao.getUnitOperations(fetchedOperationId, RESIDENT_ORG_ID_1,
                    100, null).get(0).getUnitOperationId();

            UnitOperationDO record = dao.getUnitOperation(addedUnitOpId, RESIDENT_ORG_ID_1);
            assertEquals(returnedId, record.getOperationId());
            assertEquals(RESIDENT_ORG_ID_1, record.getOperationInitiatedResourceId());
            assertEquals(RESIDENT_ORG_ID_4, record.getTargetOrgId());
            assertEquals(SUCCESS.toString(), record.getUnitOperationStatus());
            assertEquals(StringUtils.EMPTY, record.getStatusMessage());

        } catch (AsyncOperationStatusMgtException e) {
            Assert.fail();
        }
    }

    private void cleanUpDB() throws Exception {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM IDN_ASYNC_OPERATION_STATUS");
                statement.executeUpdate("DELETE FROM IDN_ASYNC_OPERATION_STATUS_UNIT");
            }
            connection.commit();
        }
    }

    private int getOperationTableSize() throws AsyncOperationStatusMgtException {
        
        return dao.getOperations(RESIDENT_ORG_ID_1, 1000, null).size();
    }
}
