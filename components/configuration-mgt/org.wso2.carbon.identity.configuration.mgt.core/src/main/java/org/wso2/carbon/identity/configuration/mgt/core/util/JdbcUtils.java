/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

/**
 * A util class to support the Jdbc executions.
 * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
 */
@Deprecated
public class JdbcUtils {

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#getNewTemplate()} instead.
     */
    @Deprecated
    public static JdbcTemplate getNewTemplate() {

        return org.wso2.carbon.identity.core.util.JdbcUtils.getNewTemplate();
    }

    /**
     * Check if the DB is H2, MySQL or Postgres.
     *
     * @return true if DB is H2, MySQL or Postgres, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated
     */
    @Deprecated
    public static boolean isH2MySqlOrPostgresDB() throws DataAccessException {

        return isH2DB() || org.wso2.carbon.identity.core.util.JdbcUtils.isMySQLDB() ||
                org.wso2.carbon.identity.core.util.JdbcUtils.isPostgreSQLDB() ||
                org.wso2.carbon.identity.core.util.JdbcUtils.isMariaDB();
    }

    /**
     * Check if the DB is H2.
     *
     * @return true if DB is H2.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isH2DB()} instead.
     */
    @Deprecated
    public static boolean isH2() throws DataAccessException {

        return isH2DB();
    }

    /**
     * Check if the DB is MySQL.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isMySQLDB()} instead.
     */
    @Deprecated
    public static boolean isMySQLDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isMySQLDB();
    }

    /**
     * Check if the DB is Maria DB.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isMariaDB()} instead.
     */
    @Deprecated
    public static boolean isMariaDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isMariaDB();
    }

    /**
     * Check if the DB is DB2.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isDB2DB()} instead.
     */
    @Deprecated
    public static boolean isDB2DB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isDB2DB();
    }

    /**
     * Check if the DB is PostgreSQL.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isPostgreSQLDB()} instead.
     */
    @Deprecated
    public static boolean isPostgreSQLDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isPostgreSQLDB();
    }

    /**
     * Check if the DB is MSSql.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isMSSqlDB()} instead.
     */
    @Deprecated
    public static boolean isMSSqlDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isMSSqlDB();
    }

    /**
     * Check if the DB is Informix.
     *
     * @return true if DB is Informix, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isInformixDB()} instead.
     */
    @Deprecated
    public static boolean isInformixDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isInformixDB();
    }

    /**
     * Check if the DB is Oracle.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     * @deprecated use {@link org.wso2.carbon.identity.core.util.JdbcUtils#isOracleDB()} instead.
     */
    @Deprecated
    public static boolean isOracleDB() throws DataAccessException {

        return org.wso2.carbon.identity.core.util.JdbcUtils.isOracleDB();
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
}
