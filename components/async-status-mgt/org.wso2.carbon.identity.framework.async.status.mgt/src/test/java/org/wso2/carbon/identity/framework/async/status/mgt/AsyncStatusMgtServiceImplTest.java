package org.wso2.carbon.identity.framework.async.status.mgt;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.AsyncStatusMgtDataHolder;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.util.TestUtils;

import java.nio.file.Paths;
import java.sql.Timestamp;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AsyncStatusMgtServiceImpl.
 */
public class AsyncStatusMgtServiceImplTest {

    @Mock
    private AsyncStatusMgtDAO asyncStatusMgtDAO;

    private AsyncStatusMgtService asyncStatusMgtService;

    private static final String OPERATION_TYPE = "B2B_APPLICATION_SHARE";
    private static final String OPERATION_SUBJECT_TYPE = "APPLICATION";
    private static final String OPERATION_SUBJECT_ID = "23d7ab3f-023e-43ba-980b-c0fd59aeacf9";
    private static final String RESIDENT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String INITIATOR_ID = "53c191dd-3f9f-454b-8a56-9ad72b5e4f30";
    private static final String OPERATION_POLICY = "SHARE_WITH_ALL";

    private static final OperationRecord operationRecord = new OperationRecord(
            OPERATION_TYPE, OPERATION_SUBJECT_TYPE, OPERATION_SUBJECT_ID, RESIDENT_ORG_ID,
            INITIATOR_ID, OPERATION_POLICY
    );

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        AsyncStatusMgtDataHolder.getInstance().setAsyncStatusMgtDAO(asyncStatusMgtDAO);
        asyncStatusMgtService = new AsyncStatusMgtServiceImpl();
//        asyncStatusMgtDAO = asyncStatusMgtService.getAsyncStatusMgtDAO();

        TestUtils.initiateH2Base();
        TestUtils.mockDataSource();

        startTenantFlow();
    }

    @AfterClass
    public void tearDown() throws Exception {

//        TestUtils.closeH2Base();
    }

    private void startTenantFlow() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "src/test/resources").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        PrivilegedCarbonContext.startTenantFlow();
    }

    @Test
    public void testRegisterOperationStatusWithoutUpdate() throws Exception {

        when(asyncStatusMgtDAO.registerAsyncOperationWithoutUpdate(operationRecord)).thenReturn("3");
        String resolvedOperationIdWithoutUpdate = asyncStatusMgtService
                .registerOperationStatus(operationRecord, false);
        verify(asyncStatusMgtDAO, times(1))
                .registerAsyncOperationWithoutUpdate(operationRecord);
        Assert.assertEquals(resolvedOperationIdWithoutUpdate, "3");
    }

    @Test
    public void testRegisterOperationStatusWithUpdate() throws Exception {

        when(asyncStatusMgtDAO.registerAsyncOperationWithUpdate(operationRecord)).thenReturn("3");
        String resolvedOperationIdWithUpdate = asyncStatusMgtService
                .registerOperationStatus(operationRecord, true);
        verify(asyncStatusMgtDAO, times(1)).registerAsyncOperationWithUpdate(operationRecord);
        Assert.assertEquals(resolvedOperationIdWithUpdate, "3");
    }

    @Test
    public void testGetLatestAsyncOperationStatusInvocation() {

        ResponseOperationRecord resolvedOperationResponseRecord = asyncStatusMgtService.getLatestAsyncOperationStatus(
                "B2B_APPLICATION_UNSHARE", "23d7ab3f-023e-43ba-980b-c0fd59aeacf9");

        verify(asyncStatusMgtDAO, times(1)).getLatestAsyncOperationStatus(
                "B2B_APPLICATION_UNSHARE", "23d7ab3f-023e-43ba-980b-c0fd59aeacf9");
    }

    @Test
    public void testGetLatestAsyncOperationStatus() {

        ResponseOperationRecord expectedResponse = new ResponseOperationRecord("6",
                "e3dd3718-b6fe-446c-a78d-9251b972b23c",
                "B2B_APPLICATION_UNSHARE", "B2B_APPLICATION",
                "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
                "10084a8d-113f-4211-a0d5-efe36b082211", "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
                "SUCCESS", "DO_NOT_SHARE", Timestamp.valueOf("2025-04-03 14:50:37.871"), Timestamp.valueOf("2025-04-03 14:47:38.034")
        );

        when(asyncStatusMgtDAO.getLatestAsyncOperationStatus("B2B_APPLICATION_UNSHARE",
                "23d7ab3f-023e-43ba-980b-c0fd59aeacf9")).thenReturn(expectedResponse);

        ResponseOperationRecord resolvedOperationResponseRecord = asyncStatusMgtService
                .getLatestAsyncOperationStatus("B2B_APPLICATION_UNSHARE",
                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9");

        Assert.assertEquals(resolvedOperationResponseRecord, expectedResponse);
    }

    @DataProvider(name = "asyncOperationDetailProvider")
    public Object[][] asyncOperationDetailProvider() throws Exception {

        return new Object[][]{
                {new OperationRecord(
                        "B2B_APPLICATION_SHARE",
                        "B2B_APPLICATION",
                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
                        "10084a8d-113f-4211-a0d5-efe36b082211",
                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
                        "SHARE_WITH_ALL"
                )}
        };
    }

    @Test(dataProvider = "asyncOperationDetailProvider", priority = 0)
    public void testRegisterOperationStatusSuccess(OperationRecord testData) {

        OperationRecord operationRecord = new OperationRecord(
                testData.getOperationType(),
                testData.getOperationSubjectType(),
                testData.getOperationSubjectId(),
                testData.getResidentOrgId(),
                testData.getInitiatorId(),
                testData.getOperationPolicy()
        );
        String addedOperationId = asyncStatusMgtService.registerOperationStatus(operationRecord, false);
        Assert.assertTrue(Integer.parseInt(addedOperationId) > 0,
                "Expected a positive non-zero integer as the result for a record insertion to the database.");
    }
}
