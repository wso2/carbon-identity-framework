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

import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestFilterResponse;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for {@link WorkflowRequestDAO}.
 */
public class WorkflowRequestDAOTest {

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

    private static final Timestamp CREATED_AT_1 = Timestamp.valueOf("2023-10-01 10:00:00");
    private static final Timestamp UPDATED_AT_1 = Timestamp.valueOf("2023-10-01 10:05:00");
    private static final Timestamp CREATED_AT_2 = Timestamp.valueOf("2023-10-01 11:00:00");
    private static final Timestamp UPDATED_AT_2 = Timestamp.valueOf("2023-10-01 11:05:00");
    private static final Timestamp CREATED_AT_3 = Timestamp.valueOf("2023-10-01 12:00:00");
    private static final Timestamp UPDATED_AT_3 = Timestamp.valueOf("2023-10-01 12:05:00");

    private static final String REQUEST_PARAMS = null;
    private static final String CREATED_BY = "admin";

    private static final String DB_NAME = "workflow_request_dao_db";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private WorkflowRequestDAO workflowRequestDAO = new WorkflowRequestDAO();

    @BeforeClass
    public void setUp() throws Exception {

        initiateH2Database(getFilePath());
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider(name = "validRequestData")
    public Object[][] provideValidRequestData() {

        return new Object[][] {
                { TEST_REQUEST_ID_1, CREATED_BY, OPERATION_UPDATE_USER, CREATED_AT_1, UPDATED_AT_1,
                        STATUS_PENDING },
                { TEST_REQUEST_ID_2, CREATED_BY, OPERATION_ADD_USER, CREATED_AT_2, UPDATED_AT_2,
                        STATUS_APPROVED },
                { TEST_REQUEST_ID_3, CREATED_BY, OPERATION_DELETE_USER, CREATED_AT_3, UPDATED_AT_3,
                        STATUS_REJECTED }
        };
    }

    @Test
    public void testFilterWorkflowRequests() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            String startTime = "2023-10-01 09:00:00";
            String endTime = "2023-11-01 09:00:00";

            WorkflowRequestFilterResponse workflowRequestFilterResponse =
                    workflowRequestDAO.getFilteredRequests("admin", "ADD_USER", startTime, endTime,
                    "CREATED", -1234, "APPROVED", 10, 1);
            assertNotNull(workflowRequestFilterResponse, "WorkflowRequestFilterResponse should not be null");
            assertEquals(workflowRequestFilterResponse.getTotalCount(), 2,
                    "Total results should match the expected count");
            assertEquals(workflowRequestFilterResponse.getRequests()[0].getRequestId(), "test_request_id_2",
                    "Could not find the expected request ID in the results");
        }
    }

    @Test(dataProvider = "validRequestData")
    public void testGetWorkflowRequestWithValidId(
            String requestId,
            String expectedCreatedBy,
            String expectedOperation,
            Timestamp expectedCreatedAt,
            Timestamp expectedUpdatedAt,
            String expectedStatus) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            WorkflowRequest result = workflowRequestDAO.getWorkflowRequest(requestId);

            assertNotNull(result, "Workflow request should not be null for ID: " + requestId);
            assertEquals(result.getRequestId(), requestId, "Request ID mismatch");
            assertEquals(result.getOperationType(), expectedOperation, "Operation type mismatch");
            assertEquals(result.getCreatedAt(), expectedCreatedAt.toInstant().toString(),
                    "Created at timestamp mismatch");
            assertEquals(result.getUpdatedAt(), expectedUpdatedAt.toInstant().toString(),
                    "Updated at timestamp mismatch");
            assertEquals(result.getStatus(), expectedStatus, "Status mismatch");
            assertEquals(result.getCreatedBy(), CREATED_BY, "Created by mismatch");
            assertEquals(result.getRequestParams(), REQUEST_PARAMS, "Request params mismatch");
        }
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithInvalidId() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            workflowRequestDAO.getWorkflowRequest(INVALID_REQUEST_ID);
        }
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithNullId() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            workflowRequestDAO.getWorkflowRequest(null);
        }
    }

    @Test(dependsOnMethods = "testGetWorkflowRequestWithValidId")
    public void testDeleteRequestWithValidId() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            WorkflowRequest requestBeforeDelete = workflowRequestDAO.getWorkflowRequest(TEST_REQUEST_ID_1);
            if (requestBeforeDelete == null) {
                throw new Exception("Precondition failed: Request with ID " + TEST_REQUEST_ID_1 + " should exist.");
            }

            // Delete the request.
            workflowRequestDAO.deleteRequest(TEST_REQUEST_ID_1);

            // Verify the request no longer exists.
            try {
                workflowRequestDAO.getWorkflowRequest(TEST_REQUEST_ID_1);
            } catch (WorkflowClientException e) {
                // Expected exception since the request should be deleted.
                assertNotNull(e, "Exception should be thrown for deleted request");
            }
        }
    }

    /**
     * Gets a connection to the H2 database.
     *
     * @return A Connection object to the H2 database.
     * @throws SQLException If an error occurs while getting the connection.
     */
    private static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new SQLException("No datasource initiated for database: " + DB_NAME);
    }

    /**
     * Initiates the H2 database with the provided script.
     *
     * @param scriptPath Path to the H2 database script.
     * @throws Exception If an error occurs while initiating the database.
     */
    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME + ";DB_CLOSE_DELAY=-1");
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    /**
     * Closes the H2 database connection.
     *
     * @throws Exception If an error occurs while closing the database.
     */
    private static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Gets the file path for the H2 database script.
     *
     * @return The file path as a string.
     */
    private static String getFilePath() {

        return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
    }
}
