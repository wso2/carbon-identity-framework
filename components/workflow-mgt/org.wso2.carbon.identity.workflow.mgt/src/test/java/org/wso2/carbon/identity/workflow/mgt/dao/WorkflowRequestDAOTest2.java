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

package org.wso2.carbon.identity.workflow.mgt.dao;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;

public class WorkflowRequestDAOTest2 {

    @Test
    public void testGetWorkflowRequestByValidRequestId() throws Exception {
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPrepStmt = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("UUID")).thenReturn("valid_id");
            when(mockResultSet.getString("OPERATION_TYPE")).thenReturn("ADD_USER");
            when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(mockResultSet.getString("STATUS")).thenReturn("PENDING");
            when(mockResultSet.getString("CREATED_BY")).thenReturn("admin");

            WorkflowRequestDAO dao = new WorkflowRequestDAO();
            String validRequestId = "valid_id";

            WorkflowRequest result = dao.getWorkflowRequest(validRequestId);

            assertNotNull(result);
            assertEquals(result.getRequestId(), "valid_id");
            assertEquals(result.getEventType(), "ADD_USER");
            assertEquals(result.getStatus(), "PENDING");
            assertEquals(result.getCreatedBy(), "admin");
            assertNotNull(result.getCreatedAt());
            assertNotNull(result.getUpdatedAt());

            verify(mockPrepStmt).setString(1, validRequestId);
            verify(mockPrepStmt).executeQuery();
        }
    }

    @Test
    public void testGetWorkflowRequestWithNullId() {
        WorkflowRequestDAO dao = new WorkflowRequestDAO();
        assertThrows(WorkflowClientException.class, () -> {
            dao.getWorkflowRequest(null);
        });
    }

    @Test
    public void testGetWorkflowRequestByInvalidRequestId() throws Exception {
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPrepStmt = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(false);

            WorkflowRequestDAO dao = new WorkflowRequestDAO();
            String invalidRequestId = "invalid_id";

            assertThrows(WorkflowClientException.class, () -> {
                dao.getWorkflowRequest(invalidRequestId);
            });

            verify(mockPrepStmt).setString(1, invalidRequestId);
            verify(mockPrepStmt).executeQuery();
        }
    }

    @Test
    public void testGetWorkflowRequestWithSQLException() throws Exception {
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPrepStmt = mock(PreparedStatement.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenThrow(new SQLException("Database error"));

            WorkflowRequestDAO dao = new WorkflowRequestDAO();
            String requestId = "test_id";

            assertThrows(InternalWorkflowException.class, () -> dao.getWorkflowRequest(requestId));

            verify(mockPrepStmt).setString(1, requestId);
            verify(mockPrepStmt).executeQuery();
        }
    }

    @Test
    public void testGetWorkflowRequestWithDeserializationError() throws Exception {
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPrepStmt = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("UUID")).thenReturn("test_id");
            when(mockResultSet.getString("OPERATION_TYPE")).thenReturn("UPDATE_USER");
            when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(1000000L));
            when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(2000000L));
            when(mockResultSet.getString("STATUS")).thenReturn("APPROVED");
            when(mockResultSet.getString("CREATED_BY")).thenReturn("testuser");
            when(mockResultSet.getBytes("REQUEST")).thenReturn("invalid_serialized_data".getBytes());

            WorkflowRequestDAO dao = new WorkflowRequestDAO();
            String requestId = "test_id";

            assertThrows(InternalWorkflowException.class, () -> dao.getWorkflowRequest(requestId));
        }
    }
}