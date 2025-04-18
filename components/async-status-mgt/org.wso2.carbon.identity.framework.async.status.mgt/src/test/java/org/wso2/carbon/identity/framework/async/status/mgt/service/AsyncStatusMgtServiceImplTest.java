package org.wso2.carbon.identity.framework.async.status.mgt.service;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.service.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.service.impl.AsyncStatusMgtServiceImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.util.TestUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.wso2.carbon.identity.framework.async.status.mgt.api.constants.ErrorMessage.ERROR_CODE_INVALID_LIMIT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1_PREFIX;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_2;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_3;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_4;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.INITIATOR_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.POLICY_SELECTIVE_SHARE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_2;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_3;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_4;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_FAIL;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_ONGOING;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_PARTIAL;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.STATUS_SUCCESS;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1_PREFIX;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_2;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_3;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_4;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_APPLICATION;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_USER;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.TYPE_APP_SHARE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_BULK_IMPORT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_SHARE;

/**
 * Unit tests for AsyncStatusMgtServiceImpl.
 */
@WithCarbonHome
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = { "dbScripts/async_operation_status.sql" })
public class AsyncStatusMgtServiceImplTest {

    private AsyncStatusMgtService service;

    @BeforeClass
    public void initTest() throws Exception {

        service = new AsyncStatusMgtServiceImpl();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        cleanUpDB();
    }

    @AfterClass
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @Test(priority = 0)
    public void testRegisterOperationStatusWithoutUpdate() throws AsyncStatusMgtException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_APP_SHARE, SUBJECT_TYPE_APPLICATION,
                SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_2,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        service.registerOperationStatus(operation1, false);
        service.registerOperationStatus(operation2, false);

        // limit testing
        assertEquals(2, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                StringUtils.EMPTY).size());
    }

    @Test(priority = 1)
    public void testRegisterOperationStatusWithUpdate() throws AsyncStatusMgtException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        service.registerOperationStatus(operation1, true);
        service.registerOperationStatus(operation2, true);

        // limit testing
        assertEquals(1, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                StringUtils.EMPTY).size());
    }

    @Test(priority = 2)
    public void testPagination() throws AsyncStatusMgtException {

        for (int i = 0; i < 10; i++) {
            OperationRecord operation = new OperationRecord(CORR_ID_1_PREFIX + i, TYPE_USER_SHARE,
                    SUBJECT_TYPE_USER, SUBJECT_ID_1_PREFIX + i, RESIDENT_ORG_ID_1, INITIATOR_ID_1,
                    POLICY_SELECTIVE_SHARE);
            service.registerOperationStatus(operation, false);
        }

        // paginating in descending order by timestamp.
        assertEquals(10, service.getOperations(StringUtils.EMPTY, getStartOfCurrentYearBase64(),
                100, StringUtils.EMPTY).size());
        assertEquals(10, service.getOperations(getStartOfNextYearBase64(), StringUtils.EMPTY,
                100, StringUtils.EMPTY).size());
        assertEquals(10, service.getOperations(getStartOfNextYearBase64(), getStartOfCurrentYearBase64(),
                100, StringUtils.EMPTY).size());
        assertEquals(10, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY,
                100, StringUtils.EMPTY).size());
        assertEquals(10, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 100,
                StringUtils.EMPTY).size());
        assertEquals(5, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 5,
                StringUtils.EMPTY).size());
        assertEquals(0, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 0,
                StringUtils.EMPTY).size());

        int count = service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY).size();
        assertTrue(count <= IdentityUtil.getMaximumItemPerPage());

        try {
            service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, -1, StringUtils.EMPTY);
        } catch (AsyncStatusMgtException e) {
            assertEquals(ERROR_CODE_INVALID_LIMIT.getCode(), e.getErrorCode());
        }
    }

    @Test(priority = 3)
    public void testFiltering() throws AsyncStatusMgtException, InterruptedException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_APP_SHARE, SUBJECT_TYPE_APPLICATION,
                SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationRecord operation2 = new OperationRecord(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_2,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationRecord operation3 = new OperationRecord(CORR_ID_3, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_3,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationRecord operation4 = new OperationRecord(CORR_ID_4, TYPE_USER_BULK_IMPORT, SUBJECT_TYPE_USER, SUBJECT_ID_3,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        service.registerOperationStatus(operation1, false);
        service.registerOperationStatus(operation2, false);
        service.registerOperationStatus(operation3, false);
        Thread.sleep(500);
        String currentTime = new Timestamp(new Date().getTime()).toString();
        Thread.sleep(500);
        service.registerOperationStatus(operation4, false);

        assertEquals(1, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "( operationType EQ " + TYPE_APP_SHARE + " AND subjectId EQ )" + SUBJECT_ID_1).size());
        assertEquals(2, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "operationType EQ " + TYPE_USER_SHARE + " OR operationType EQ" + TYPE_USER_BULK_IMPORT).size());

        assertEquals(3, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "operationType SW B2B").size());
        assertEquals(4, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "subjectId NE " + SUBJECT_ID_4).size());
        assertEquals(1, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "createdTime GT " + currentTime).size());
        assertEquals(3, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "createdTime LT " + currentTime).size());
        assertEquals(1, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "createdTime GE " + currentTime).size());
        assertEquals(3, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 10,
                "createdTime LE " + currentTime).size());
    }

    @Test(priority = 4)
    public void testUpdateOperationStatus() throws AsyncStatusMgtException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        service.registerOperationStatus(operation1, false);

        ResponseOperationRecord fetchedOperation = service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY,
                null, StringUtils.EMPTY).get(0);
        String initialStatus = fetchedOperation.getOperationStatus();
        assertEquals(STATUS_ONGOING, initialStatus);

        String initialOperationId = fetchedOperation.getOperationId();
        service.updateOperationStatus(initialOperationId, STATUS_SUCCESS);
        assertEquals(STATUS_SUCCESS, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, null,
                StringUtils.EMPTY).get(0).getOperationStatus());
        assertEquals(1, service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, null,
                StringUtils.EMPTY).size());
    }

    @Test(priority = 5)
    public void testRegisterUnitOperationStatus() throws AsyncStatusMgtException, InterruptedException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        String returnedId = service.registerOperationStatus(operation1, false);
        String fetchedOperationId = service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 5, StringUtils.EMPTY).get(0).getOperationId();

        UnitOperationRecord unit1 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_4, STATUS_SUCCESS, StringUtils.EMPTY);
        service.registerUnitOperationStatus(unit1);
        Thread.sleep(4000);

        String returnedOperationId = service.getUnitOperationStatusRecords(fetchedOperationId, StringUtils.EMPTY, StringUtils.EMPTY, 10, StringUtils.EMPTY).get(0).getOperationId();
        assertEquals(returnedId, returnedOperationId);
    }

    @Test(priority = 6)
    public void testGetUnitOperationStatusRecords() throws AsyncStatusMgtException, InterruptedException {

        OperationRecord operation1 = new OperationRecord(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        String returnedId = service.registerOperationStatus(operation1, false);
        String fetchedOperationId = service.getOperations(StringUtils.EMPTY, StringUtils.EMPTY, 5, StringUtils.EMPTY).get(0).getOperationId();

        UnitOperationRecord unit1 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_2, STATUS_SUCCESS, StringUtils.EMPTY);
        UnitOperationRecord unit2 = new UnitOperationRecord(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_3, STATUS_FAIL, StringUtils.EMPTY);
        service.registerUnitOperationStatus(unit1);
        service.registerUnitOperationStatus(unit2);
        Thread.sleep(4000);

        List<ResponseUnitOperationRecord> unitOperations = service.getUnitOperationStatusRecords(fetchedOperationId, StringUtils.EMPTY, StringUtils.EMPTY, 10, StringUtils.EMPTY);

        assertEquals(2, unitOperations.size());
        assertEquals(returnedId, unitOperations.get(0).getOperationId());
    }

    public static String getStartOfCurrentYearBase64() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return Base64.getEncoder().encodeToString(timestamp.toString().getBytes());
    }

    public static String getStartOfNextYearBase64() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.YEAR, 1);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return Base64.getEncoder().encodeToString(timestamp.toString().getBytes());
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
}
