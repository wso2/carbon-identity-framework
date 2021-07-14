/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import javax.sql.DataSource;

/**
 * A util class to support the Jdbc executions.
 */
public class JdbcUtils {

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static JdbcTemplate getNewTemplate() {

        DataSource dataSource = IdentityDatabaseUtil.getSessionDataSource();
        return new JdbcTemplate(dataSource);
    }


    /**
     * Check whether the DB type string contains in the driver name or db product name.
     *
     * @param dbType database type string.
     * @return true if the database type matches the driver type, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    private static boolean isDBTypeOf(String dbType) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        return jdbcTemplate.getDriverName().contains(dbType) || jdbcTemplate.getDatabaseProductName().contains(dbType);
    }

    /**
     * Check if the DB is H2.
     *
     * @return true if DB is H2.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.H2);
    }

    /**
     * Check if the DB is MySQL.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMySQLDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MY_SQL);
    }

    /**
     * Check if the DB is Maria DB.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMariaDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MARIA_DB);
    }

    /**
     * Check if the DB is DB2.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isDB2DB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.DB2);
    }

    /**
     * Check if the DB is PostgreSQL.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSQLDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.POSTGRE_SQL);
    }

    /**
     * Check if the DB is MSSql.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MICROSOFT) || isDBTypeOf(FrameworkConstants.S_MICROSOFT);
    }

    /**
     * Check if the DB is Oracle.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.ORACLE);
    }
}
