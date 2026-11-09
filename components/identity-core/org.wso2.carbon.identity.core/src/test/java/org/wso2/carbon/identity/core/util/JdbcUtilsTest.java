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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for the database product name based DB type checks in {@link JdbcUtils}.
 */
public class JdbcUtilsTest {

    @DataProvider(name = "databaseProductNames")
    public Object[][] databaseProductNames() {

        return new Object[][]{
                {"H2", "H2"},
                {"MySQL", "MySQL"},
                {"MariaDB", "MariaDB"},
                {"PostgreSQL", "PostgreSQL"},
                {"Oracle", "Oracle"},
                {"Microsoft SQL Server", "MSSQL"},
                {"DB2/LINUXX8664", "DB2"},
                {"DB2/NT64", "DB2"},
                {"Informix Dynamic Server", "Informix"},
                {null, "None"},
                {"", "None"}
        };
    }

    @Test(dataProvider = "databaseProductNames")
    public void testDBTypeByProductName(String productName, String expectedType) {

        assertEquals(JdbcUtils.isH2DB(productName), "H2".equals(expectedType), "isH2DB");
        assertEquals(JdbcUtils.isMySQLDB(productName), "MySQL".equals(expectedType), "isMySQLDB");
        assertEquals(JdbcUtils.isMariaDB(productName), "MariaDB".equals(expectedType), "isMariaDB");
        assertEquals(JdbcUtils.isPostgreSQLDB(productName), "PostgreSQL".equals(expectedType), "isPostgreSQLDB");
        assertEquals(JdbcUtils.isOracleDB(productName), "Oracle".equals(expectedType), "isOracleDB");
        assertEquals(JdbcUtils.isMSSqlDB(productName), "MSSQL".equals(expectedType), "isMSSqlDB");
        assertEquals(JdbcUtils.isDB2DB(productName), "DB2".equals(expectedType), "isDB2DB");
        assertEquals(JdbcUtils.isInformixDB(productName), "Informix".equals(expectedType), "isInformixDB");
    }
}
