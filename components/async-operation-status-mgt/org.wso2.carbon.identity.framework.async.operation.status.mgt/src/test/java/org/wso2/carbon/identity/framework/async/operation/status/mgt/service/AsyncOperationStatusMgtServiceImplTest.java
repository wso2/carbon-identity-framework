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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.service;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.service.AsyncOperationStatusMgtService;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.component.AsyncOperationStatusMgtDataHolder;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.service.impl.AsyncOperationStatusMgtServiceImpl;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.util.TestUtils;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.FAILED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.IN_PROGRESS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus.SUCCESS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_1_PREFIX;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_2;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_3;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.CORR_ID_4;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.INITIATOR_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.POLICY_SELECTIVE_SHARE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_2;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_3;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_ID_4;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_NAME_2;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_NAME_3;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.RESIDENT_ORG_NAME_4;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_1_PREFIX;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_2;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_3;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_ID_4;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_APPLICATION;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.SUBJECT_TYPE_USER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.TENANT_DOMAIN_1;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.TYPE_APP_SHARE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_BULK_IMPORT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.constants.TestAsyncOperationConstants.TYPE_USER_SHARE;

/**
 * Unit tests for AsyncStatusMgtServiceImpl.
 */
@WithCarbonHome
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = { "dbScripts/async_operation_status.sql" })
public class AsyncOperationStatusMgtServiceImplTest {

    private AsyncOperationStatusMgtService service;
    private OrganizationManager organizationManager;
    private final Map<String, BasicOrganization> mockedOrgMap = new HashMap<>();

    @BeforeClass
    public void setUpClass() throws Exception {

        organizationManager = mock(OrganizationManager.class);
        AsyncOperationStatusMgtDataHolder.getInstance().setOrganizationManager(organizationManager);
        service = new AsyncOperationStatusMgtServiceImpl();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        mockedOrgMap.clear();
        cleanUpDB();
    }

    @AfterClass
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @DataProvider(name = "operationStatusProvider")
    public Object[][] operationStatusProvider() {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_APP_SHARE, SUBJECT_TYPE_APPLICATION,
                SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationInitDTO operation2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                SUBJECT_ID_2, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        return new Object[][]{
                {TENANT_DOMAIN_1, Arrays.asList(operation1, operation2), 2}
        };
    }

    @Test(dataProvider = "operationStatusProvider")
    public void testRegisterOperationStatusWithoutUpdate(String tenantDomain, List<OperationInitDTO> operations,
                                                         int expectedOperationCount)
            throws AsyncOperationStatusMgtException, OrganizationManagementException {

        for (OperationInitDTO op : operations) {
            service.registerOperationStatus(op, false);
        }

        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        int actualCount = service.getOperations(tenantDomain, StringUtils.EMPTY, StringUtils.EMPTY, 10,
                StringUtils.EMPTY).size();
        assertEquals(actualCount, expectedOperationCount);
    }

    @DataProvider(name = "operationStatusWithUpdateProvider")
    public Object[][] operationStatusWithUpdateProvider() {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        OperationInitDTO operation2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        return new Object[][]{
                {TENANT_DOMAIN_1, Arrays.asList(operation1, operation2), true, 1}
        };
    }

    @Test(dataProvider = "operationStatusWithUpdateProvider", priority = 1)
    public void testRegisterOperationStatusWithUpdate(String tenantDomain, List<OperationInitDTO> operations,
                                                      boolean isUpdate, int expectedSize)
            throws AsyncOperationStatusMgtException, OrganizationManagementException {

        for (OperationInitDTO operation : operations) {
            service.registerOperationStatus(operation, isUpdate);
        }
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        int actualSize = service.getOperations(tenantDomain, StringUtils.EMPTY, StringUtils.EMPTY, 10,
                StringUtils.EMPTY).size();
        assertEquals(actualSize, expectedSize);
    }

    @DataProvider(name = "paginationTestDataProvider")
    public Object[][] paginationTestDataProvider() {

        return new Object[][]{
                // after, before, limit, expectedCount
                {StringUtils.EMPTY, getStartOfCurrentYearBase64(), 100, 10},
                {StringUtils.EMPTY, getStartOfCurrentYearBase64(), 100000, 10},
                {getStartOfNextYearBase64(), StringUtils.EMPTY, 100, 10},
                {getStartOfNextYearBase64(), getStartOfCurrentYearBase64(), 100, 10},
                {StringUtils.EMPTY, StringUtils.EMPTY, 100, 10},
                {StringUtils.EMPTY, StringUtils.EMPTY, 5, 5},
                {StringUtils.EMPTY, StringUtils.EMPTY, 0, 0}
        };
    }

    @Test(dataProvider = "paginationTestDataProvider", priority = 2)
    public void testPagination(String after, String before, Integer limit, int expectedCount)
            throws AsyncOperationStatusMgtException, OrganizationManagementException {

        for (int i = 0; i < 10; i++) {
            OperationInitDTO operation = new OperationInitDTO(CORR_ID_1_PREFIX + i, TYPE_USER_SHARE,
                    SUBJECT_TYPE_USER, SUBJECT_ID_1_PREFIX + i, RESIDENT_ORG_ID_1, INITIATOR_ID_1,
                    POLICY_SELECTIVE_SHARE);
            service.registerOperationStatus(operation, false);
        }
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        List<OperationResponseDTO> operations = service.getOperations(TENANT_DOMAIN_1, after, before, limit,
                StringUtils.EMPTY);
        assertEquals(operations.size(), expectedCount);
    }

    @DataProvider(name = "filteringTestDataWithOperations")
    public Object[][] filteringTestDataWithOperations() {

        OperationInitDTO op1 = new OperationInitDTO(CORR_ID_1, TYPE_APP_SHARE, SUBJECT_TYPE_APPLICATION,
                SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                SUBJECT_ID_2, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op3 = new OperationInitDTO(CORR_ID_3, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                SUBJECT_ID_3, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op4 = new OperationInitDTO(CORR_ID_4, TYPE_USER_BULK_IMPORT, SUBJECT_TYPE_USER,
                SUBJECT_ID_3, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        List<OperationInitDTO> ops = Arrays.asList(op1, op2, op3, op4);

        return new Object[][]{
                { ops, "( operationType EQ " + TYPE_APP_SHARE + " AND subjectId EQ " + SUBJECT_ID_1 + " )", 1 },
                { ops, "operationType EQ " + TYPE_USER_SHARE + " OR operationType EQ " + TYPE_USER_BULK_IMPORT, 2 },
                { ops, "operationType SW B2B", 3 },
                { ops, "subjectId NE " + SUBJECT_ID_4, 4 },
        };
    }


    @Test(dataProvider = "filteringTestDataWithOperations", priority = 3)
    public void testFiltering(List<OperationInitDTO> operations, String filter, int expectedCount)
            throws AsyncOperationStatusMgtException, OrganizationManagementException {

        for (OperationInitDTO operation : operations) {
            service.registerOperationStatus(operation, false);
        }
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        int actual = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY,
                10, filter).size();
        assertEquals(actual, expectedCount);
    }

    @DataProvider(name = "createdTimeFilterTestData")
    public Object[][] createdTimeFilterTestData() throws InterruptedException {

        OperationInitDTO op1 = new OperationInitDTO(CORR_ID_1, TYPE_APP_SHARE, SUBJECT_TYPE_APPLICATION,
                SUBJECT_ID_1, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op2 = new OperationInitDTO(CORR_ID_2, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                SUBJECT_ID_2, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op3 = new OperationInitDTO(CORR_ID_3, TYPE_USER_SHARE, SUBJECT_TYPE_USER,
                SUBJECT_ID_3, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        OperationInitDTO op4 = new OperationInitDTO(CORR_ID_4, TYPE_USER_BULK_IMPORT, SUBJECT_TYPE_USER,
                SUBJECT_ID_3, RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        List<OperationInitDTO> allOps = Arrays.asList(op1, op2, op3, op4);
        List<String> filters = Arrays.asList("createdTime GT %s", "createdTime LT %s", "createdTime GE %s",
                "createdTime LE %s");
        List<Integer> expectedCount = Arrays.asList(1, 3, 1, 3);

        return new Object[][]{
                { allOps, filters, expectedCount, 4000 },
        };
    }

    @Test(dataProvider = "createdTimeFilterTestData", priority = 4)
    public void testCreatedTimeFiltering(List<OperationInitDTO> operations, List<String> filters,
                                         List<Integer> expectedCount, int waitTime)
            throws AsyncOperationStatusMgtException, OrganizationManagementException, InterruptedException {

        String currentTime = StringUtils.EMPTY;
        int i = 0;
        for (OperationInitDTO operation : operations) {
            Thread.sleep(1000);
            if (i == 3) {
                currentTime = new Timestamp(new Date().getTime()).toString();
                Thread.sleep(waitTime);
            }
            i++;
            service.registerOperationStatus(operation, false);
        }
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        int j = 0;
        for (String filter : filters) {
            int actual = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY, 10,
                    String.format(filter, currentTime)).size();
            assertEquals((int) expectedCount.get(j++), actual);
        }
    }

    @Test(priority = 4)
    public void testUpdateOperationStatus() throws AsyncOperationStatusMgtException, OrganizationManagementException {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        String initialOperationId = service.registerOperationStatus(operation1, false);

        OperationResponseDTO fetchedOperation = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY,
                StringUtils.EMPTY, null, StringUtils.EMPTY).get(0);
        String initialStatus = fetchedOperation.getOperationStatus();
        assertEquals(IN_PROGRESS.toString(), initialStatus);

        service.updateOperationStatus(initialOperationId, SUCCESS);
        assertEquals(SUCCESS.toString(), service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY,
                null, StringUtils.EMPTY).get(0).getOperationStatus());
        assertEquals(1, service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY, null,
                StringUtils.EMPTY).size());
    }

    @DataProvider(name = "registerUnitOperationStatusTestData")
    public Object[][] registerUnitOperationStatusTestData() throws InterruptedException {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        BasicOrganization basicOrganization = new BasicOrganization();
        basicOrganization.setId(RESIDENT_ORG_ID_4);
        basicOrganization.setName(RESIDENT_ORG_NAME_4);
        basicOrganization.setStatus(StringUtils.EMPTY);
        basicOrganization.setCreated(StringUtils.EMPTY);
        basicOrganization.setOrganizationHandle(StringUtils.EMPTY);

        String residentOrgId = RESIDENT_ORG_ID_4;
        return new Object[][]{
                { operation1, residentOrgId, basicOrganization },
        };
    }

    @Test(dataProvider = "registerUnitOperationStatusTestData", priority = 5)
    public void testRegisterUnitOperationStatus(OperationInitDTO initDTO, String residentOrgId,
                                                BasicOrganization basicOrganization)
            throws AsyncOperationStatusMgtException, InterruptedException, OrganizationManagementException {


        String returnedId = service.registerOperationStatus(initDTO, false);
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);

        String fetchedOperationId = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY,
                5, StringUtils.EMPTY).get(0).getOperationId();
        UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_4, SUCCESS, StringUtils.EMPTY);
        service.registerUnitOperationStatus(unit1);
        mockedOrgMap.put(residentOrgId, basicOrganization);
        Thread.sleep(4000);
        when(organizationManager.getBasicOrganizationDetailsByOrgIDs(anyList())).thenReturn(mockedOrgMap);

        String returnedOperationId = service.getUnitOperationStatusRecords(fetchedOperationId, TENANT_DOMAIN_1,
                StringUtils.EMPTY, StringUtils.EMPTY, 10, StringUtils.EMPTY).get(0).getOperationId();
        assertEquals(returnedId, returnedOperationId);
    }

    @DataProvider(name = "getUnitOperationStatusTestData")
    public Object[][] getUnitOperationStatusTestData() throws InterruptedException {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        BasicOrganization basicOrganization1 = new BasicOrganization();
        basicOrganization1.setId(RESIDENT_ORG_ID_2);
        basicOrganization1.setName(RESIDENT_ORG_NAME_2);
        basicOrganization1.setStatus(StringUtils.EMPTY);
        basicOrganization1.setCreated(StringUtils.EMPTY);
        basicOrganization1.setOrganizationHandle(StringUtils.EMPTY);

        BasicOrganization basicOrganization2 = new BasicOrganization();
        basicOrganization2.setId(RESIDENT_ORG_ID_3);
        basicOrganization2.setName(RESIDENT_ORG_NAME_3);
        basicOrganization2.setStatus(StringUtils.EMPTY);
        basicOrganization2.setCreated(StringUtils.EMPTY);
        basicOrganization2.setOrganizationHandle(StringUtils.EMPTY);

        return new Object[][]{
                { operation1, Arrays.asList(RESIDENT_ORG_ID_2, RESIDENT_ORG_ID_3),
                        Arrays.asList(basicOrganization1, basicOrganization2) },
        };
    }

    @Test(dataProvider = "getUnitOperationStatusTestData", priority = 6)
    public void testGetUnitOperationStatusRecords(OperationInitDTO initDTO, List<String> residentOrgIds,
                                                  List<BasicOrganization> basicOrgs)
            throws AsyncOperationStatusMgtException, InterruptedException, OrganizationManagementException {

        String returnedId = service.registerOperationStatus(initDTO, false);
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);
        String fetchedOperationId = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY,
                5, StringUtils.EMPTY).get(0).getOperationId();

        UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_2, SUCCESS, StringUtils.EMPTY);
        UnitOperationInitDTO unit2 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_3, FAILED, StringUtils.EMPTY);
        service.registerUnitOperationStatus(unit1);
        service.registerUnitOperationStatus(unit2);

        mockedOrgMap.put(residentOrgIds.get(0), basicOrgs.get(0));
        mockedOrgMap.put(residentOrgIds.get(1), basicOrgs.get(1));
        Thread.sleep(4000);

        when(organizationManager.getBasicOrganizationDetailsByOrgIDs(anyList())).thenReturn(mockedOrgMap);
        List<UnitOperationResponseDTO> unitOperations = service.getUnitOperationStatusRecords(fetchedOperationId,
                TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY, 10, StringUtils.EMPTY);
        when(organizationManager.getOrganizationNameById(RESIDENT_ORG_ID_2)).thenReturn(basicOrgs.get(0).getName());
        UnitOperationResponseDTO unitOperation = service.getUnitOperation(unitOperations.get(1).getUnitOperationId(),
                TENANT_DOMAIN_1);

        assertEquals(RESIDENT_ORG_ID_1, unitOperation.getOperationInitiatedResourceId());
        assertEquals(RESIDENT_ORG_ID_2, unitOperation.getTargetOrgId());
        assertEquals(basicOrgs.get(0).getName(), unitOperation.getTargetOrgName());
        assertEquals(SUCCESS.toString(), unitOperation.getUnitOperationStatus());
        assertEquals(StringUtils.EMPTY, unitOperation.getStatusMessage());
        assertEquals(2, unitOperations.size());
        assertEquals(returnedId, unitOperations.get(0).getOperationId());
    }

    @DataProvider(name = "getOperationStatusTestData")
    public Object[][] getOperationStatusTestData() throws InterruptedException {

        OperationInitDTO operation1 = new OperationInitDTO(CORR_ID_1, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);

        BasicOrganization basicOrganization1 = new BasicOrganization();
        basicOrganization1.setId(RESIDENT_ORG_ID_2);
        basicOrganization1.setName(RESIDENT_ORG_NAME_2);
        basicOrganization1.setStatus(StringUtils.EMPTY);
        basicOrganization1.setCreated(StringUtils.EMPTY);
        basicOrganization1.setOrganizationHandle(StringUtils.EMPTY);

        return new Object[][]{
                { operation1, Collections.singletonList(RESIDENT_ORG_ID_2),
                        Collections.singletonList(basicOrganization1)},
        };
    }

    @Test(dataProvider = "getOperationStatusTestData", priority = 7)
    public void testGetOperationStatusRecords(OperationInitDTO initDTO, List<String> residentOrgIds,
                                                  List<BasicOrganization> basicOrgs)
            throws AsyncOperationStatusMgtException, InterruptedException, OrganizationManagementException {

        String returnedId = service.registerOperationStatus(initDTO, false);
        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(RESIDENT_ORG_ID_1);
        String fetchedOperationId = service.getOperations(TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY,
                5, StringUtils.EMPTY).get(0).getOperationId();

        UnitOperationInitDTO unit1 = new UnitOperationInitDTO(returnedId, RESIDENT_ORG_ID_1,
                RESIDENT_ORG_ID_2, SUCCESS, StringUtils.EMPTY);
        service.registerUnitOperationStatus(unit1);
        mockedOrgMap.put(residentOrgIds.get(0), basicOrgs.get(0));
        Thread.sleep(4000);

        when(organizationManager.getBasicOrganizationDetailsByOrgIDs(anyList())).thenReturn(mockedOrgMap);

        OperationResponseDTO fetchedOperation = service.getOperation(fetchedOperationId, TENANT_DOMAIN_1);
        List<UnitOperationResponseDTO> unitOperations = service.getUnitOperationStatusRecords(fetchedOperationId,
                TENANT_DOMAIN_1, StringUtils.EMPTY, StringUtils.EMPTY, 10, StringUtils.EMPTY);

        assertEquals(initDTO.getOperationType(), fetchedOperation.getOperationType());
        assertEquals(initDTO.getOperationSubjectType(), fetchedOperation.getOperationSubjectType());
        assertEquals(initDTO.getOperationSubjectId(), fetchedOperation.getOperationSubjectId());
        assertEquals(initDTO.getInitiatorId(), fetchedOperation.getInitiatorId());
        assertEquals(initDTO.getResidentOrgId(), fetchedOperation.getResidentOrgId());
        assertEquals(1, unitOperations.size());
        assertEquals(returnedId, unitOperations.get(0).getOperationId());
    }

    @DataProvider(name = "registerOperationStatusInvalidInputTestData")
    public Object[][] registerOperationStatusInvalidInputTestData() throws InterruptedException {

        OperationInitDTO operation1 = new OperationInitDTO(null, TYPE_USER_SHARE, SUBJECT_TYPE_USER, SUBJECT_ID_1,
                RESIDENT_ORG_ID_1, INITIATOR_ID_1, POLICY_SELECTIVE_SHARE);
        ErrorMessage e = ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS;

        return new Object[][]{
                { operation1, e.getCode(), e.getMessage(), e.getDescription() },
        };
    }

    @Test(dataProvider = "registerOperationStatusInvalidInputTestData", priority = 8)
    public void testRegisterOperationStatusInvalidInput(OperationInitDTO dto, String errorCode, String message,
                                                        String description) {

        try {
            service.registerOperationStatus(dto, false);
            Assert.fail("Expected AsyncOperationStatusMgtException was not thrown.");
        } catch (AsyncOperationStatusMgtException e) {
            Assert.assertEquals(e.getErrorCode(), errorCode);
            Assert.assertEquals(e.getMessage(), message);
            Assert.assertEquals(e.getDescription(), description);
        }
    }

    private String getStartOfCurrentYearBase64() {
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

    private String getStartOfNextYearBase64() {
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
