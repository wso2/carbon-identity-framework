package org.wso2.carbon.identity.framework.async.status.mgt.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.service.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.service.impl.AsyncStatusMgtServiceImpl;

import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_INITIATOR_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_POLICY_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_POLICY_2;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_SUBJECT_ID_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_SUBJECT_TYPE_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_OPERATION_TYPE_1;
import static org.wso2.carbon.identity.framework.async.status.mgt.constants.TestAsyncOperationConstants.IDN_RESIDENT_ORG_ID_1;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = { "dbScripts/async_operation_status.sql" })
@WithCarbonHome
public class AsyncStatusMgtDAOTest {

    private AsyncStatusMgtService asyncStatusMgtService;

    @BeforeClass
    public void initTest() throws Exception {
        asyncStatusMgtService = new AsyncStatusMgtServiceImpl();
    }

    @DataProvider(name = "asyncOperationDetailProvider")
    public Object[][] asyncOperationDetailProvider() throws Exception {
        return new Object[][] {
                {new OperationRecord(
                        "56565656565655",
                        IDN_OPERATION_TYPE_1,
                        "B2B_APPLICATION",
                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
                        "10084a8d-113f-4211-a0d5-efe36b082211",
                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
                        "SHARE_WITH_ALL"
                )},
//                {new OperationRecord(
//                        IDN_OPERATION_TYPE_2,
//                        "B2B_APPLICATION",
//                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
//                        "10084a8d-113f-4211-a0d5-efe36b082211",
//                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
//                        "SHARE_WITH_ALL"
//                )},
//                {new OperationRecord(
//                        IDN_OPERATION_TYPE_1,
//                        "B2B_APPLICATION",
//                        "23d7ab3f-023e-43ba-980b-c0fd59aeacf9",
//                        "10084a8d-113f-4211-a0d5-efe36b082211",
//                        "53c191dd-3f9f-454b-8a56-9ad72b5e4f30",
//                        "SHARE_WITH_ALL"
//                )}
        };
    }

    @Test(dataProvider = "asyncOperationDetailProvider", priority = 1)
    public void testRegisterOperationWithoutUpdateSuccess(OperationRecord testData) {

        String addedOperationId = asyncStatusMgtService.registerOperationStatus(testData, false);

//        String fetchedOperationIdFromDB = asyncStatusMgtService.getLatestAsyncOperationStatus(
//                testData.getOperationType(), testData.getOperationSubjectId()).getOperationId();
//
//        assertTrue(Integer.parseInt(addedOperationId) > 0,
//            "Expected a positive non-zero integer as the result for a clean record insertion to the database.");
//        assertEquals(fetchedOperationIdFromDB, addedOperationId);
    }

    @Test
    public void testRegisterOperationWithUpdateSuccess() {

        OperationRecord testRecord1 = new OperationRecord(
                "56565656565655",
                IDN_OPERATION_TYPE_1,
                IDN_OPERATION_SUBJECT_TYPE_1,
                IDN_OPERATION_SUBJECT_ID_1,
                IDN_RESIDENT_ORG_ID_1,
                IDN_OPERATION_INITIATOR_ID_1,
                IDN_OPERATION_POLICY_1
        );
        OperationRecord testRecord2 = new OperationRecord(
                "56565656565655",
                IDN_OPERATION_TYPE_1,
                IDN_OPERATION_SUBJECT_TYPE_1,
                IDN_OPERATION_SUBJECT_ID_1,
                IDN_RESIDENT_ORG_ID_1,
                IDN_OPERATION_INITIATOR_ID_1,
                IDN_OPERATION_POLICY_2
        );
        asyncStatusMgtService.registerOperationStatus(testRecord1, true);
        asyncStatusMgtService.registerOperationStatus(testRecord2, true);
//        List<ResponseOperationRecord> fetchedOperationListFromDB = asyncStatusMgtService
//                .getOperationStatusRecords(IDN_OPERATION_TYPE_1, IDN_OPERATION_SUBJECT_ID_1);
//
//        assertEquals(fetchedOperationListFromDB.size(), 1);
//        assertEquals(fetchedOperationListFromDB.get(0).getOperationPolicy(), IDN_OPERATION_POLICY_2);
    }

}
