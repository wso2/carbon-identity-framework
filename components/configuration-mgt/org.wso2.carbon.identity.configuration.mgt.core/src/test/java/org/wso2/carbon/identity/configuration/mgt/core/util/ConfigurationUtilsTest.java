/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.util;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ConfigurationUtils class.
 */
public class ConfigurationUtilsTest {

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private MockedStatic<JdbcUtils> jdbcUtilsMockedStatic;
    private MockedStatic<ConfigurationManagerComponentDataHolder> dataHolderMockedStatic;

    @BeforeMethod
    public void setUp() {

        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class);
        dataHolderMockedStatic = mockStatic(ConfigurationManagerComponentDataHolder.class);
    }

    @AfterMethod
    public void tearDown() {

        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
        if (jdbcUtilsMockedStatic != null) {
            jdbcUtilsMockedStatic.close();
        }
        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
    }

    @Test
    public void testIsConfigurationManagementEnabled_WhenAllTablesExist() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_TYPE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_RESOURCE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() ->
                        IdentityDatabaseUtil.isTableExists("IDN_CONFIG_ATTRIBUTE")).thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_FILE"))
                .thenReturn(true);

        boolean result = ConfigurationUtils.isConfigurationManagementEnabled();
        Assert.assertTrue(result, "Configuration management should be enabled when all tables exist");
    }

    @Test
    public void testIsConfigurationManagementEnabled_WhenSomeTablesAreMissing() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_TYPE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_RESOURCE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() ->
                        IdentityDatabaseUtil.isTableExists("IDN_CONFIG_ATTRIBUTE")).thenReturn(false);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_FILE"))
                .thenReturn(true);


        boolean result = ConfigurationUtils.isConfigurationManagementEnabled();
        Assert.assertFalse(result, "Configuration management should be disabled when some tables are missing");
    }

    @Test
    public void testIsConfigurationManagementEnabled_WhenAllTablesAreMissing() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists(anyString()))
                .thenReturn(false);


        boolean result = ConfigurationUtils.isConfigurationManagementEnabled();
        Assert.assertFalse(result, "Configuration management should be disabled when all tables are missing");
    }

    @Test
    public void testIsCreatedTimeFieldExists_MySQLWithCreatedTimeField() throws SQLException {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
        jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.findColumn("CREATED_TIME")).thenReturn(1);


        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertTrue(result, "CREATED_TIME field should exist for MySQL database");
    }

    @Test
    public void testIsCreatedTimeFieldExists_MySQLWithoutCreatedTimeField() throws SQLException {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
        jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.findColumn("CREATED_TIME")).thenThrow(new SQLException("Column not found"));


        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertFalse(result, "CREATED_TIME field should not exist when SQLException is thrown");
    }

    @Test
    public void testIsCreatedTimeFieldExists_MSSQLWithCreatedTimeField() throws SQLException {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(true);
        jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.findColumn("CREATED_TIME")).thenReturn(1); // Column exists

        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertTrue(result, "CREATED_TIME field should exist for MSSQL database");
    }

    @Test
    public void testIsCreatedTimeFieldExists_OracleWithCreatedTimeField() throws SQLException {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
        jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.findColumn("CREATED_TIME")).thenReturn(1); // Column exists

        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertTrue(result, "CREATED_TIME field should exist for Oracle database");
    }

    @Test
    public void testIsCreatedTimeFieldExists_DatabaseConnectionException() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenThrow(new IdentityRuntimeException("Database connection failed"));


        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertFalse(result, "Should return false when database connection fails");
    }

    @Test
    public void testIsCreatedTimeFieldExists_IdentityRuntimeException() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenThrow(new IdentityRuntimeException("Identity runtime exception"));

        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertFalse(result, "Should return false when IdentityRuntimeException is thrown");
    }

    @Test
    public void testIsCreatedTimeFieldExists_PrepareStatementException() throws SQLException {

        Connection mockConnection = mock(Connection.class);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
        jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare statement failed"));

        boolean result = ConfigurationUtils.isCreatedTimeFieldExists();
        Assert.assertFalse(result, "Should return false when prepare statement fails");
    }

    @Test
    public void testSetUseCreatedTime_WhenConfigManagementEnabledAndCreatedTimeFieldExists() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists(anyString()))
                .thenReturn(true);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.findColumn("CREATED_TIME")).thenReturn(1);

            dataHolderMockedStatic.when(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(true))
                    .thenAnswer(invocation -> null);

            ConfigurationUtils.setUseCreatedTime();
            dataHolderMockedStatic.verify(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(true));
        } catch (SQLException e) {
            Assert.fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    public void testSetUseCreatedTime_WhenConfigManagementDisabled() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_TYPE"))
                .thenReturn(false);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_RESOURCE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_ATTRIBUTE"))
                .thenReturn(true);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists("IDN_CONFIG_FILE"))
                .thenReturn(true);

        dataHolderMockedStatic.when(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false))
                .thenAnswer(invocation -> null);

        ConfigurationUtils.setUseCreatedTime();
        dataHolderMockedStatic.verify(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false));
    }

    @Test
    public void testSetUseCreatedTime_WhenCreatedTimeFieldDoesNotExist() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists(anyString()))
                .thenReturn(true);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            jdbcUtilsMockedStatic.when(JdbcUtils::isMSSqlDB).thenReturn(false);
            jdbcUtilsMockedStatic.when(JdbcUtils::isOracleDB).thenReturn(false);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.findColumn("CREATED_TIME")).thenThrow(new SQLException("Column not found"));

            dataHolderMockedStatic.when(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false))
                    .thenAnswer(invocation -> null);

            ConfigurationUtils.setUseCreatedTime();
            dataHolderMockedStatic.verify(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false));

        } catch (SQLException e) {
            Assert.fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    public void testSetUseCreatedTime_WhenBothConfigManagementDisabledAndCreatedTimeFieldMissing() {

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.isTableExists(anyString()))
                .thenReturn(false);

        dataHolderMockedStatic.when(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false))
                .thenAnswer(invocation -> null);

        ConfigurationUtils.setUseCreatedTime();
        dataHolderMockedStatic.verify(() -> ConfigurationManagerComponentDataHolder.setUseCreatedTime(false));
    }
}
