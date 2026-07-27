/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.util;

import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link IdentityDatabaseUtil}.
 */
@Listeners(MockitoTestNGListener.class)
public class IdentityDatabaseUtilTest {

    private static final String CUSTOM_SCHEMA_VALUE = "custom_schema";
    private static final String PARENT_SCHEMA_VALUE = "oracle_parent_schema";
    private static final String TEST_TABLE = "IDN_TEST_TABLE";
    private static final String CONNECTION_SCHEMA = "public";
    private static final String CONNECTION_CATALOG = "testdb";

    @Test(description = "Test isTableExists() for PostgreSQL when a custom schema is configured, " +
            "verifying that the custom schema is used in the metadata query")
    public void testIsTableExists_postgresWithCustomSchema_usesCustomSchema() throws Exception {

        Connection mockConnection = buildMockConnection(IdentityCoreConstants.POSTGRE_SQL);
        DatabaseMetaData metaData = mockConnection.getMetaData();
        ResultSet mockResultSet = resultSetWithRow(true);
        when(metaData.getTables(eq(CONNECTION_CATALOG), eq(CUSTOM_SCHEMA_VALUE),
                eq(TEST_TABLE), any())).thenReturn(mockResultSet);

        JDBCPersistenceManager mockManager = mock(JDBCPersistenceManager.class);
        when(mockManager.getDBConnection(anyBoolean())).thenReturn(mockConnection);
        when(mockManager.getPostgresSchema()).thenReturn(CUSTOM_SCHEMA_VALUE);

        try (MockedStatic<JDBCPersistenceManager> staticMock = mockStatic(JDBCPersistenceManager.class)) {
            staticMock.when(JDBCPersistenceManager::getInstance).thenReturn(mockManager);
            assertTrue(IdentityDatabaseUtil.isTableExists(TEST_TABLE),
                    "isTableExists() should return true when the table is found under the custom PostgreSQL schema");
        }

        verify(metaData).getTables(CONNECTION_CATALOG, CUSTOM_SCHEMA_VALUE, TEST_TABLE, new String[]{"TABLE"});
    }

    @Test(description = "Test isTableExists() for PostgreSQL when a custom schema is not configured, " +
            "verifying that the connection schema is used in the metadata query")
    public void testIsTableExists_postgresWithoutCustomSchema_usesConnectionSchema() throws Exception {

        Connection mockConnection = buildMockConnection(IdentityCoreConstants.POSTGRE_SQL);
        DatabaseMetaData metaData = mockConnection.getMetaData();
        ResultSet mockResultSet = resultSetWithRow(true);
        when(metaData.getTables(eq(CONNECTION_CATALOG), eq(CONNECTION_SCHEMA),
                eq(TEST_TABLE), any())).thenReturn(mockResultSet);

        JDBCPersistenceManager mockManager = mock(JDBCPersistenceManager.class);
        when(mockManager.getDBConnection(anyBoolean())).thenReturn(mockConnection);
        when(mockManager.getPostgresSchema()).thenReturn(null);

        try (MockedStatic<JDBCPersistenceManager> staticMock = mockStatic(JDBCPersistenceManager.class)) {
            staticMock.when(JDBCPersistenceManager::getInstance).thenReturn(mockManager);
            assertTrue(IdentityDatabaseUtil.isTableExists(TEST_TABLE),
                    "isTableExists() should fall back to the connection schema when no custom schema is configured");
        }

        verify(metaData).getTables(CONNECTION_CATALOG, CONNECTION_SCHEMA, TEST_TABLE, new String[]{"TABLE"});
    }

    @Test(description = "Test isTableExists() for PostgreSQL when a blank custom schema is configured, " +
            "verifying that the blank custom schema is treated as absent and the connection schema is used")
    public void testIsTableExists_postgresWithBlankCustomSchema_usesConnectionSchema() throws Exception {

        Connection mockConnection = buildMockConnection(IdentityCoreConstants.POSTGRE_SQL);
        DatabaseMetaData metaData = mockConnection.getMetaData();
        ResultSet mockResultSet = resultSetWithRow(true);
        when(metaData.getTables(eq(CONNECTION_CATALOG), eq(CONNECTION_SCHEMA),
                eq(TEST_TABLE), any())).thenReturn(mockResultSet);

        JDBCPersistenceManager mockManager = mock(JDBCPersistenceManager.class);
        when(mockManager.getDBConnection(anyBoolean())).thenReturn(mockConnection);
        when(mockManager.getPostgresSchema()).thenReturn("   ");

        try (MockedStatic<JDBCPersistenceManager> staticMock = mockStatic(JDBCPersistenceManager.class)) {
            staticMock.when(JDBCPersistenceManager::getInstance).thenReturn(mockManager);
            assertTrue(IdentityDatabaseUtil.isTableExists(TEST_TABLE),
                    "isTableExists() should treat a blank custom schema as absent and use the connection schema");
        }

        verify(metaData).getTables(CONNECTION_CATALOG, CONNECTION_SCHEMA, TEST_TABLE, new String[]{"TABLE"});
    }

    @Test(description = "Test isTableExists() for PostgreSQL when a custom schema is configured but the table is " +
            "not found, verifying that the method returns false")
    public void testIsTableExists_postgresTableNotFound_returnsFalse() throws Exception {

        Connection mockConnection = buildMockConnection(IdentityCoreConstants.POSTGRE_SQL);
        DatabaseMetaData metaData = mockConnection.getMetaData();
        ResultSet mockResultSet = resultSetWithRow(false);
        when(metaData.getTables(any(), any(), eq(TEST_TABLE), any())).thenReturn(mockResultSet);

        JDBCPersistenceManager mockManager = mock(JDBCPersistenceManager.class);
        when(mockManager.getDBConnection(anyBoolean())).thenReturn(mockConnection);
        when(mockManager.getPostgresSchema()).thenReturn(CUSTOM_SCHEMA_VALUE);

        try (MockedStatic<JDBCPersistenceManager> staticMock = mockStatic(JDBCPersistenceManager.class)) {
            staticMock.when(JDBCPersistenceManager::getInstance).thenReturn(mockManager);
            assertFalse(IdentityDatabaseUtil.isTableExists(TEST_TABLE),
                    "isTableExists() should return false when the table is not found under the custom schema");
        }
    }

    @Test(description = "Test isTableExists() for Oracle when a parent schema is configured, " +
            "verifying that the parent schema is used in the metadata query")
    public void testIsTableExists_oracleWithParentSchema_usesParentSchema() throws Exception {

        Connection mockConnection = buildMockConnection(IdentityCoreConstants.ORACLE);
        DatabaseMetaData metaData = mockConnection.getMetaData();
        ResultSet mockResultSet = resultSetWithRow(true);
        when(metaData.getTables(eq(CONNECTION_CATALOG), eq(PARENT_SCHEMA_VALUE),
                eq(TEST_TABLE), any())).thenReturn(mockResultSet);

        JDBCPersistenceManager mockManager = mock(JDBCPersistenceManager.class);
        when(mockManager.getDBConnection(anyBoolean())).thenReturn(mockConnection);
        when(mockManager.getParentSchema()).thenReturn(PARENT_SCHEMA_VALUE);

        try (MockedStatic<JDBCPersistenceManager> staticMock = mockStatic(JDBCPersistenceManager.class)) {
            staticMock.when(JDBCPersistenceManager::getInstance).thenReturn(mockManager);
            assertTrue(IdentityDatabaseUtil.isTableExists(TEST_TABLE),
                    "isTableExists() should use the Oracle parentSchema override (regression)");
        }

        verify(metaData).getTables(CONNECTION_CATALOG, PARENT_SCHEMA_VALUE, TEST_TABLE, new String[]{"TABLE"});
    }

    /**
     * Builds a mock {@link Connection} whose metadata reports the given {@code dbProductName}.
     * Schema and catalog return the test constants; {@code storesLowerCaseIdentifiers()} is false.
     */
    private Connection buildMockConnection(String dbProductName) throws Exception {

        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn(dbProductName);
        when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
        when(connection.getSchema()).thenReturn(CONNECTION_SCHEMA);
        when(connection.getCatalog()).thenReturn(CONNECTION_CATALOG);
        return connection;
    }

    private ResultSet resultSetWithRow(boolean hasRow) throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(hasRow);
        return rs;
    }
}
