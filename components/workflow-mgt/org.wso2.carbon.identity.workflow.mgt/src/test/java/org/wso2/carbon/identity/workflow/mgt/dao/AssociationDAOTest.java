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

import org.mockito.MockedStatic;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Test class for AssociationDAO.
 */
public class AssociationDAOTest {

    @Test
    public void testGetAssociationByInvalidAssociationId() {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            AssociationDAO dao = new AssociationDAO();
            String invalidAssociationId = "invalid_id";
            assertThrows(WorkflowClientException.class, () -> dao.getAssociation(invalidAssociationId));
        }
    }

    @Test
    public void testUpdateAssociationByInvalidAssociationId() throws SQLException {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(true))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            AssociationDAO dao = new AssociationDAO();
            Association associationDTO = new Association();
            associationDTO.setEventId("ADD_USER");
            associationDTO.setAssociationName("User Addition Association");
            associationDTO.setCondition("true");
            associationDTO.setWorkflowId("12345");
            associationDTO.setAssociationId("invalid_id");
            assertThrows(WorkflowClientException.class, () -> dao.updateAssociation(associationDTO));
        }
    }

    /**
     * Test data provider for filter parsing scenarios that should succeed.
     * 
     * @return Object array containing test cases with valid filter inputs
     */
    @DataProvider(name = "validFilterParsingTestData")
    public Object[][] getValidFilterParsingTestData() {
        return new Object[][]{
                // filter, description
                {null, "null filter"},
                {"*", "wildcard filter"},
                {"operation", "simple operation filter"},
                {"workflowId", "simple workflowId filter"},
                {"operation eq ADD_USER", "operation equals filter"},
                {"workflowId eq 12345", "workflowId equals filter"},
                {"  operation  eq  ADD_USER  ", "equals filter with whitespace"},
                {"operation eq ", "equals filter with empty value"},
                {"operation eq value with spaces", "equals filter with spaces in value"}
        };
    }

    /**
     * Test data provider for filter parsing scenarios that should fail.
     * 
     * @return Object array containing test cases with invalid filter inputs
     */
    @DataProvider(name = "invalidFilterParsingTestData")
    public Object[][] getInvalidFilterParsingTestData() {
        return new Object[][]{
                // filter, description, expectedErrorMessage
                {"", "empty filter", "Unsupported filter field: "},
                {"   ", "whitespace filter", "Unsupported filter field: "},
                {" eq ADD_USER", "equals filter with empty field", "Unsupported filter field: "},
                {"field eq value eq another", "equals filter with multiple eq", "Unsupported filter field: field"},
                {"randomField", "random field filter", "Unsupported filter field: randomField"},
                {"randomField eq randomValue", "random field equals filter", "Unsupported filter field: randomField"}
        };
    }

    @Test(dataProvider = "validFilterParsingTestData")
    public void testListPaginatedAssociationsFilterParsing(String filter, String description) 
            throws InternalWorkflowException, WorkflowClientException, SQLException {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            
            // Mock database connection and related objects.
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            
            // Mock JdbcUtils to return MySQL database type (simplest case).
            jdbcUtils.when(JdbcUtils::isMySQLDB).thenReturn(true);
            jdbcUtils.when(JdbcUtils::isH2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMariaDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isOracleDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isPostgreSQLDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isDB2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isInformixDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false); // No results

            AssociationDAO dao = new AssociationDAO();
            int tenantId = 1;
            int offset = 0;
            int limit = 10;

            // This should not throw an exception and should handle the filter parsing correctly.
            List<Association> result = dao.listPaginatedAssociations(tenantId, filter, offset, limit);

            assertNotNull(result, "Result should not be null for filter: " + description);
            assertTrue(result.isEmpty(), "Result should be empty for mock scenario: " + description);
        }
    }

    @Test(dataProvider = "invalidFilterParsingTestData")
    public void testListPaginatedAssociationsFilterParsingWithInvalidFilters(String filter, String description,
                                                                             String expectedErrorMessage) {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            
            // Mock database connection and related objects.
            Connection mockConnection = mock(Connection.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            
            // Mock JdbcUtils to return MySQL database type.
            jdbcUtils.when(JdbcUtils::isMySQLDB).thenReturn(true);
            jdbcUtils.when(JdbcUtils::isH2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMariaDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isOracleDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isPostgreSQLDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isDB2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isInformixDB).thenReturn(false);

            AssociationDAO dao = new AssociationDAO();
            int tenantId = 1;
            int offset = 0;
            int limit = 10;

            // This should throw WorkflowClientException for invalid filter field.
            try {
                dao.listPaginatedAssociations(tenantId, filter, offset, limit);
                fail("Expected WorkflowClientException for filter: " + description);
            } catch (WorkflowClientException exception) {
                assertEquals(exception.getMessage(), expectedErrorMessage, 
                        "Exception message should match expected for filter: " + description);
            } catch (InternalWorkflowException e) {
                fail("Unexpected InternalWorkflowException for filter: " + description);
            }
        }
    }

    @Test(dataProvider = "validFilterParsingTestData") 
    public void testGetAssociationsCountFilterParsing(String filter, String description) 
            throws InternalWorkflowException, SQLException {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            
            // Mock database connection and related objects.
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(0); // Return count of 0

            AssociationDAO dao = new AssociationDAO();
            int tenantId = 1;

            // This should not throw an exception and should handle the filter parsing correctly.
            int result = dao.getAssociationsCount(tenantId, filter);

            assertEquals(result, 0, "Count should be 0 for mock scenario: " + description);
        }
    }

    @Test
    public void testFilterParsingWithWorkflowIdFilterType() 
            throws InternalWorkflowException, WorkflowClientException, SQLException {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            
            // Mock database connection and related objects.
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            
            // Mock JdbcUtils to return MySQL database type.
            jdbcUtils.when(JdbcUtils::isMySQLDB).thenReturn(true);
            jdbcUtils.when(JdbcUtils::isH2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMariaDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isOracleDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isPostgreSQLDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isDB2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isInformixDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            AssociationDAO dao = new AssociationDAO();
            
            // Test workflowId filter type specifically.
            String workflowIdFilter = "workflowId eq 12345";
            List<Association> result = dao.listPaginatedAssociations(1, workflowIdFilter, 0, 10);

            assertNotNull(result, "Result should not be null for workflowId filter");
            assertTrue(result.isEmpty(), "Result should be empty for mock scenario");
        }
    }

    @Test
    public void testNullFilterHandling() throws InternalWorkflowException, WorkflowClientException, SQLException {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            
            // Mock database setup.
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            jdbcUtils.when(JdbcUtils::isMySQLDB).thenReturn(true);
            jdbcUtils.when(JdbcUtils::isH2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMariaDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isOracleDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isPostgreSQLDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isDB2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isInformixDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            AssociationDAO dao = new AssociationDAO();
            
            // Test specifically with null filter to ensure it's handled properly.
            List<Association> result = dao.listPaginatedAssociations(1, null, 0, 10);

            assertNotNull(result, "Result should not be null for null filter");
            assertTrue(result.isEmpty(), "Result should be empty for mock scenario");
        }
    }

    @Test
    public void testWildcardFilterHandling() throws InternalWorkflowException, WorkflowClientException, SQLException {
        
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            
            // Mock database setup.
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            jdbcUtils.when(JdbcUtils::isMySQLDB).thenReturn(true);
            jdbcUtils.when(JdbcUtils::isH2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMariaDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isOracleDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isPostgreSQLDB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isDB2DB).thenReturn(false);
            jdbcUtils.when(JdbcUtils::isInformixDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            AssociationDAO dao = new AssociationDAO();
            
            // Test specifically with wildcard filter to ensure it's handled like null.
            List<Association> result = dao.listPaginatedAssociations(1, "*", 0, 10);

            assertNotNull(result, "Result should not be null for wildcard filter");
            assertTrue(result.isEmpty(), "Result should be empty for mock scenario");
        }
    }
}
