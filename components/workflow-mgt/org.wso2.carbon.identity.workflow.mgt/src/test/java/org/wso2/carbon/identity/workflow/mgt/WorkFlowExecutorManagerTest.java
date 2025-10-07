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

import org.apache.commons.lang.SerializationUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Unit tests for {@link WorkFlowExecutorManager}.
 * This class specifically tests the handleCallback method with focus on DELETED and ABORTED status handling.
 */
public class WorkFlowExecutorManagerTest {

    private static final String TEST_UUID = "test-uuid-123";
    private static final String STATUS_APPROVED = "APPROVED";

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;

    @Mock
    private WorkflowServiceDataHolder mockWorkflowServiceDataHolder;

    @BeforeMethod
    public void setUp() {

        openMocks(this);

        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);

        // Set up workflow listener.
        List<WorkflowExecutorManagerListener> workflowListeners = new ArrayList<>();
        when(mockWorkflowServiceDataHolder.getExecutorListenerList()).thenReturn(workflowListeners);
    }

    @AfterMethod
    public void tearDown() {

        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
    }

    /**
     * Test public handleCallback method that delegates to private handleCallback.
     */
    @Test
    public void testHandleCallbackWhenWorkflowRequestAbortedOrDeleted() throws Exception {

        Map<String, Object> additionalParams = new HashMap<>();

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(SQLConstants.REQUEST_ID_COLUMN)).thenReturn("REQUEST_ID");

        WorkflowRequest workflowRequest = new WorkflowRequest();

        byte[] workflowRequestBytes = SerializationUtils.serialize(workflowRequest);
        when(mockResultSet.getBytes(SQLConstants.REQUEST_COLUMN)).thenReturn(workflowRequestBytes);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection())
                .thenReturn(mockConnection);

        when(mockResultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN))
                .thenReturn(WorkflowRequestStatus.ABORTED.toString());
        WorkFlowExecutorManager.getInstance().handleCallback(TEST_UUID, STATUS_APPROVED, additionalParams);

        when(mockResultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN))
                .thenReturn(WorkflowRequestStatus.DELETED.toString());
        WorkFlowExecutorManager.getInstance().handleCallback(TEST_UUID, STATUS_APPROVED, additionalParams);
    }
}
