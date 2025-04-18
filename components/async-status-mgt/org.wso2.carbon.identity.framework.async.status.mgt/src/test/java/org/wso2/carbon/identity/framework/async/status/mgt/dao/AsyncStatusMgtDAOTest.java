package org.wso2.carbon.identity.framework.async.status.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.impl.AsyncStatusMgtDAOImpl;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_2;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.INITIATOR_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.POLICY_SELECTIVE_SHARE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_3;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_4;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_FAIL;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_ONGOING;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_PARTIAL;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_SUCCESS;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_USER;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_SHARE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_1;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = { "dbScripts/async_operation_status.sql" })
@WithCarbonHome
public class AsyncStatusMgtDAOTest {

    private AsyncStatusMgtDAO DAO;

    @BeforeClass
    public void initTest() throws Exception {

        DAO = new AsyncStatusMgtDAOImpl();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        cleanUpDB();
    }

    @DataProvider(name = "asyncOperationDetailProvider")
    public Object[][] asyncOperationDetailProvider() throws Exception {

        return new Object[][] {
                {new OperationRecord(
                        "56565656565655",
                        TYPE_USER_SHARE,
                        "B2B_APPLICATION",
                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
                        "10084a8d-113f-4211-a0d5-efe36b082211",
                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
                        "SHARE_WITH_ALL"
                )},
//                {new OperationRecord(
//                        OPERATION_TYPE_2,
//                        "B2B_APPLICATION",
//                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
//                        "10084a8d-113f-4211-a0d5-efe36b082211",
//                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
//                        "SHARE_WITH_ALL"
//                )},
//                {new OperationRecord(
//                        OPERATION_TYPE_1,
//                        "B2B_APPLICATION",
//                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
//                        "10084a8d-113f-4211-a0d5-efe36b082211",
//                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
//                        "SHARE_WITH_ALL"
//                )}
        };
    }

    @Test(priority = 1)
    public void testRegisterOperationWithoutUpdateSuccess() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            String insertedOperationId_1 = DAO.registerAsyncStatusWithoutUpdate(operation1);
            assertTrue(StringUtils.isNotBlank(insertedOperationId_1), "Async Op_1 Status Addition Failed.");
            assertTrue(Integer.parseInt(insertedOperationId_1) > 0, "Expected a positive non-zero " +
                    "integer as the result for a clean record insertion to the database.");

            DAO.registerAsyncStatusWithoutUpdate(operation2);
            assertEquals(2, getOperationTableSize());
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(dataProvider = "asyncOperationDetailProvider", priority = 2)
    public void testRegisterOperationWithUpdateSuccess(OperationRecord testData) {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            String insertedOperationId_1 = DAO.registerAsyncStatusWithUpdate(operation1);
            assertTrue(StringUtils.isNotBlank(insertedOperationId_1), "Async Op_1 Status Addition Failed.");
            assertTrue(Integer.parseInt(insertedOperationId_1) > 0, "Expected a positive non-zero " +
                    "integer as the result for a clean record insertion to the database.");

            DAO.registerAsyncStatusWithUpdate(operation2);
            assertEquals(1, getOperationTableSize());
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 3)
    public void testUpdateAsyncStatus() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            DAO.registerAsyncStatusWithUpdate(operation1);

            ResponseOperationRecord fetchedOperation = DAO.getOperations(1000, null).get(0);
            String initialStatus = fetchedOperation.getOperationStatus();
            assertEquals(STATUS_ONGOING, initialStatus);

            String initialOperationId = fetchedOperation.getOperationId();
            DAO.updateAsyncStatus(initialOperationId, STATUS_SUCCESS);
            assertEquals(1, getOperationTableSize());

            String fetchedUpdatedStatus = DAO.getOperations(1000, null).get(0).getOperationStatus();
            assertEquals(STATUS_SUCCESS, fetchedUpdatedStatus);
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 4)
    public void testRegisterAsyncStatusUnit() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = DAO.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = DAO.getOperations(1000, null).get(0).getOperationId();

            UnitOperationRecord unit1 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, STATUS_SUCCESS, StringUtils.EMPTY);
            UnitOperationRecord unit2 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_3, STATUS_FAIL, "Invalid User Id.");
            ConcurrentLinkedQueue<UnitOperationRecord> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            list.add(unit2);
            DAO.registerAsyncStatusUnit(list);
            DAO.updateAsyncStatus(returnedId, STATUS_PARTIAL);

            assertEquals(2, DAO.getUnitOperationRecordsForOperationId(fetchedOperationId, 10, null).size());
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 5)
    public void testGetOperationRecords() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            assertEquals(0, DAO.getOperations(100, null).size());

            DAO.registerAsyncStatusWithoutUpdate(operation1);
            DAO.registerAsyncStatusWithoutUpdate(operation2);
            assertEquals(2, DAO.getOperations(100, null).size());

        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 6)
    public void testGetOperation() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

            DAO.registerAsyncStatusWithoutUpdate(operation1);
            String fetchedOperationId = DAO.getOperations(100, null).get(0).getOperationId();
            ResponseOperationRecord record = DAO.getOperation(fetchedOperationId);

            assertEquals(TYPE_USER_SHARE, record.getOperationType());
            assertEquals(SUBJECT_ID_1, record.getOperationSubjectId());
            assertEquals(RESIDENT_ORG_ID_1, record.getResidentOrgId());
            assertEquals(INITIATOR_ID_1, record.getInitiatorId());
            assertEquals(POLICY_SELECTIVE_SHARE, record.getOperationPolicy());
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 7)
    public void testGetUnitOperationRecordsForOperationId() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = DAO.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = DAO.getOperations(1000, null).get(0).getOperationId();

            UnitOperationRecord unit1 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, STATUS_SUCCESS, StringUtils.EMPTY);
            UnitOperationRecord unit2 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_3, STATUS_FAIL, "Invalid User Id.");
            ConcurrentLinkedQueue<UnitOperationRecord> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            list.add(unit2);
            DAO.registerAsyncStatusUnit(list);
            DAO.updateAsyncStatus(returnedId, STATUS_PARTIAL);

            assertEquals(2, DAO.getUnitOperationRecordsForOperationId(fetchedOperationId, 100, null).size());
        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 8)
    public void testGetUnitOperation() {

        try {
            OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                    RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
            String returnedId = DAO.registerAsyncStatusWithUpdate(operation1);
            String fetchedOperationId = DAO.getOperations(1000, null).get(0).getOperationId();

            UnitOperationRecord unit1 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                    RESIDENT_ORG_ID_4, STATUS_SUCCESS, StringUtils.EMPTY);
            ConcurrentLinkedQueue<UnitOperationRecord> list = new ConcurrentLinkedQueue<>();
            list.add(unit1);
            DAO.registerAsyncStatusUnit(list);
            DAO.updateAsyncStatus(returnedId, STATUS_PARTIAL);

            String addedUnitOpId = DAO.getUnitOperationRecordsForOperationId(fetchedOperationId, 100, null).get(0).getUnitOperationId();

            ResponseUnitOperationRecord record = DAO.getUnitOperation(addedUnitOpId);
            assertEquals(returnedId, record.getOperationId());
            assertEquals(RESIDENT_ORG_ID_1, record.getOperationInitiatedResourceId());
            assertEquals(RESIDENT_ORG_ID_4, record.getTargetOrgId());
            assertEquals(STATUS_SUCCESS, record.getUnitOperationStatus());
            assertEquals(StringUtils.EMPTY, record.getStatusMessage());

        } catch (AsyncStatusMgtException e) {
            throw new RuntimeException(e);
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

    private int getOperationTableSize() throws AsyncStatusMgtException {
        return DAO.getOperations(1000, null).size();
    }

}
