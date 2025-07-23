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

package org.wso2.carbon.identity.workflow.mgt;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestDAO;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link WorkflowManagementServiceImpl}.
 */
public class WorkflowManagementServiceImplTest {

    private static final String TEST_REQUEST_ID_1 = "test_request_id_1";
    private static final String TEST_REQUEST_ID_2 = "test_request_id_2";
    private static final String TEST_REQUEST_ID_3 = "test_request_id_3";
    private static final String INVALID_REQUEST_ID = "non_existent_id";

    private static final String OPERATION_UPDATE_USER = "UPDATE_USER";
    private static final String OPERATION_ADD_USER = "ADD_USER";
    private static final String OPERATION_DELETE_USER = "DELETE_USER";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final String CREATED_AT_1 = "2023-10-01 10:00:00";
    private static final String UPDATED_AT_1 = "2023-10-01 10:05:00";
    private static final String CREATED_AT_2 = "2023-10-01 11:00:00";
    private static final String UPDATED_AT_2 = "2023-10-01 11:05:00";
    private static final String CREATED_AT_3 = "2023-10-01 12:00:00";
    private static final String UPDATED_AT_3 = "2023-10-01 12:05:00";

    private static final String CREATED_BY = "admin";

    @Mock
    private WorkflowRequestDAO mockDAO;

    @Mock
    private WorkflowListener mockListener;

    private MockedStatic<WorkflowServiceDataHolder> mockedDataHolder;
    private WorkflowManagementServiceImpl service;

    private AutoCloseable mocks;

    @BeforeMethod
    public void setUp() throws Exception {

        mocks = MockitoAnnotations.openMocks(this);

        when(mockListener.isEnable()).thenReturn(true);

        List<WorkflowListener> listeners = new ArrayList<>();
        listeners.add(mockListener);

        WorkflowServiceDataHolder mockHolder = mock(WorkflowServiceDataHolder.class);
        when(mockHolder.getWorkflowListenerList()).thenReturn(listeners);

        mockedDataHolder = mockStatic(WorkflowServiceDataHolder.class);
        mockedDataHolder.when(WorkflowServiceDataHolder::getInstance).thenReturn(mockHolder);

        service = new WorkflowManagementServiceImpl();

        Field daoField = WorkflowManagementServiceImpl.class.getDeclaredField("workflowRequestDAO");
        daoField.setAccessible(true);
        daoField.set(service, mockDAO);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        mockedDataHolder.close();
        mocks.close();
    }

    @DataProvider(name = "validRequestData")
    public Object[][] provideValidRequestData() {

        return new Object[][]{
                {TEST_REQUEST_ID_1, CREATED_BY, OPERATION_UPDATE_USER, CREATED_AT_1, UPDATED_AT_1, STATUS_PENDING},
                {TEST_REQUEST_ID_2, CREATED_BY, OPERATION_ADD_USER, CREATED_AT_2, UPDATED_AT_2, STATUS_APPROVED},
                {TEST_REQUEST_ID_3, CREATED_BY, OPERATION_DELETE_USER, CREATED_AT_3, UPDATED_AT_3, STATUS_REJECTED}
        };
    }

    @Test(dataProvider = "validRequestData")
    public void testGetWorkflowRequestByValidRequestId(String requestId, String createdBy, String operation,
                                                       String createdAt, String updatedAt, String status)
            throws WorkflowException {

        WorkflowRequest expectedRequest = new WorkflowRequest();
        expectedRequest.setRequestId(requestId);
        expectedRequest.setCreatedBy(createdBy);
        expectedRequest.setOperationType(operation);
        expectedRequest.setCreatedAt(createdAt);
        expectedRequest.setUpdatedAt(updatedAt);
        expectedRequest.setStatus(status);

        when(mockDAO.getWorkflowRequest(requestId)).thenReturn(expectedRequest);

        WorkflowRequest result = service.getWorkflowRequestBean(requestId);

        assertNotNull(result, "Returned workflow request should not be null");
        assertEquals(result.getRequestId(), requestId, "Request ID should match");
        assertEquals(result.getOperationType(), operation, "Operation type should match");
        assertEquals(result.getStatus(), status, "Status should match");
        assertEquals(result.getCreatedBy(), createdBy, "Created by should match");

        verify(mockDAO).getWorkflowRequest(requestId);
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithInvalidId() throws Exception {

        when(mockDAO.getWorkflowRequest(INVALID_REQUEST_ID))
                .thenThrow(new WorkflowClientException("Invalid request ID"));
        service.getWorkflowRequestBean(INVALID_REQUEST_ID);
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithNullId() throws Exception {

        service.getWorkflowRequestBean(null);
    }
}
