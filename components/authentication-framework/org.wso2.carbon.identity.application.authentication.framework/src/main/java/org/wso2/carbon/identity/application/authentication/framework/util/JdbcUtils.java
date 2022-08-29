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
 * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
 */
@Deprecated
public class JdbcUtils {

    /**
     * Enum to select the database type.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     */
    @Deprecated
    public enum Database {
        IDENTITY, SESSION
    }

    /**
     * Get a new Jdbc Template.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return a new Jdbc Template.
     */
    @Deprecated
    public static JdbcTemplate getNewTemplate() {

        DataSource dataSource = IdentityDatabaseUtil.getDataSource();
        return new JdbcTemplate(dataSource);
    }

    /**
     * Get a new Jdbc Template.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return a new Jdbc Template.
     */
    @Deprecated
    public static JdbcTemplate getNewTemplate(Database database) {

        DataSource dataSource;
        if (Database.SESSION.equals(database)) {
            dataSource = IdentityDatabaseUtil.getSessionDataSource();
        } else {
            dataSource = IdentityDatabaseUtil.getDataSource();
        }
        return new JdbcTemplate(dataSource);
    }

    /**
     * Check whether the DB type string contains in the driver name or db product name.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @param dbType database type string.
     * @return true if the database type matches the driver type, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    private static boolean isDBTypeOf(String dbType, Database database) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(database);
        return jdbcTemplate.getDriverName().contains(dbType) || jdbcTemplate.getDatabaseProductName().contains(dbType);
    }

    /**
     * Check if the DB is H2.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is H2.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isH2() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.H2, Database.IDENTITY);
    }

    /**
     * Check if the DB is H2.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is H2.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isH2(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.H2, database);
    }

    /**
     * Check if the DB is MySQL.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMySQLDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MY_SQL, Database.IDENTITY);
    }

    /**
     * Check if the DB is MySQL.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMySQLDB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MY_SQL, database);
    }

    /**
     * Check if the DB is Maria DB.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMariaDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MARIA_DB, Database.IDENTITY);
    }

    /**
     * Check if the DB is Maria DB.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMariaDB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MARIA_DB, database);
    }

    /**
     * Check if the DB is DB2.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isDB2DB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.DB2, Database.IDENTITY);
    }

    /**
     * Check if the DB is DB2.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isDB2DB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.DB2, database);
    }

    /**
     * Check if the DB is PostgreSQL.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isPostgreSQLDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.POSTGRE_SQL, Database.IDENTITY);
    }

    /**
     * Check if the DB is PostgreSQL.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isPostgreSQLDB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.POSTGRE_SQL, database);
    }

    /**
     * Check if the DB is MSSql.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMSSqlDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MICROSOFT, Database.IDENTITY) || isDBTypeOf(FrameworkConstants.S_MICROSOFT
                , Database.IDENTITY);

    }

    /**
     * Check if the DB is MSSql.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isMSSqlDB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.MICROSOFT, database) || isDBTypeOf(FrameworkConstants.S_MICROSOFT,
                database);
    }

    /**
     * Check if the DB is Oracle.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isOracleDB() throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.ORACLE, Database.IDENTITY);
    }

    /**
     * Check if the DB is Oracle.
     * @deprecated Please use {@link org.wso2.carbon.identity.core.util.JdbcUtils} instead.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    @Deprecated
    public static boolean isOracleDB(Database database) throws DataAccessException {

        return isDBTypeOf(FrameworkConstants.ORACLE, database);
    }
}
