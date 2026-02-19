/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.util;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import javax.sql.DataSource;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.DB2;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.H2;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.INFORMIX;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MARIADB;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MICROSOFT;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MY_SQL;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORACLE;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.S_MICROSOFT;

/**
 * An util class to support the Jdbc executions.
 */
public class JdbcUtils {

    /**
     * Enum to select the database type.
     */
    public enum Database {
        IDENTITY, SESSION
    }

    /**
     * Get a new Jdbc Template. Identity database is used.
     *
     * @return a new Jdbc Template.
     */
    public static JdbcTemplate getNewTemplate() {

        return new JdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }

    /**
     * Get a new Jdbc Template for the given database type.
     *
     * @param database Database type.
     * @return a new Jdbc Template.
     */
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
     * Get a new Named Jdbc Template. Identity database is used.
     *
     * @return a new Named Jdbc Template.
     */
    public static NamedJdbcTemplate getNewNamedJdbcTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }

    /**
     * Get a new Named Jdbc Template for the given database type.
     *
     * @param database Database type.
     * @return a new Named Jdbc Template.
     */
    public static NamedJdbcTemplate getNewNamedJdbcTemplate(Database database) {

        DataSource dataSource;
        if (Database.SESSION.equals(database)) {
            dataSource = IdentityDatabaseUtil.getSessionDataSource();
        } else {
            dataSource = IdentityDatabaseUtil.getDataSource();
        }
        return new NamedJdbcTemplate(dataSource);
    }

    /**
     * Check if the DB is H2. Identity database is used.
     *
     * @return true if H2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2DB() throws DataAccessException {

        return isDBTypeOf(H2, Database.IDENTITY);
    }

    /**
     * Check if the DB is H2 for the given database type.
     *
     * @param database database type.
     * @return true if H2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2DB(Database database) throws DataAccessException {

        return isDBTypeOf(H2, database);
    }

    /**
     * Check if the DB is DB2. Identity database is used.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isDB2DB() throws DataAccessException {

        return isDBTypeOf(DB2, Database.IDENTITY);
    }

    /**
     * Check if the DB is H2 for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if H2, false otherwise.
     */
    public static boolean isH2DB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(H2);
    }

    /**
     * Check if the DB is DB2 for the given database type.
     *
     * @param database database type.
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isDB2DB(Database database) throws DataAccessException {

        return isDBTypeOf(DB2, database);
    }

    /**
     * Check if the DB is DB2 for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if DB2, false otherwise.
     */
    public static boolean isDB2DB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(DB2);
    }

    /**
     * Check if the DB is MySQL. Identity database is used.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMySQLDB() throws DataAccessException {

        return isDBTypeOf(MY_SQL, Database.IDENTITY);
    }

    /**
     * Check if the DB is MySQL for the given database product name.
     *
     * @param databaseProductName Database product name.
     * @return true if DB is MySQL.
     */
    public static boolean isMySQLDB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(MY_SQL);
    }

    /**
     * Check if the DB is MySQL for the given database type.
     *
     * @param database database type.
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMySQLDB(Database database) throws DataAccessException {

        return isDBTypeOf(MY_SQL, database);
    }

    /**
     * Check if the DB is MSSql. Identity database is used.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws DataAccessException {

        return isDBTypeOf(MICROSOFT, Database.IDENTITY) || isDBTypeOf(S_MICROSOFT, Database.IDENTITY);
    }

    /**
     * Check if the DB is MSSql for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if MSSql, false otherwise.
     */
    public static boolean isMSSqlDB(String databaseProductName) {

        return databaseProductName != null && (databaseProductName.contains(MICROSOFT) || 
                databaseProductName.toLowerCase().contains(S_MICROSOFT));
    }

    /**
     * Check if the DB is MSSql for the given database type.
     *
     * @param database database type.
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB(Database database) throws DataAccessException {

        return isDBTypeOf(MICROSOFT, database) || isDBTypeOf(S_MICROSOFT, database);
    }

    /**
     * Check if the DB is Maria DB. Identity database is used.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMariaDB() throws DataAccessException {

        return isDBTypeOf(MARIADB, Database.IDENTITY);
    }

    /**
     * Check if the DB is MariaDB for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if MariaDB, false otherwise.
     */
    public static boolean isMariaDB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(MARIADB);
    }

    /**
     * Check if the DB is Maria DB for the given database type.
     *
     * @param database database type.
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMariaDB(Database database) throws DataAccessException {

        return isDBTypeOf(MARIADB, database);
    }

    /**
     * Check if the DB is PostgreSQL. Identity database is used.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSQLDB() throws DataAccessException {

        return isDBTypeOf(POSTGRE_SQL, Database.IDENTITY);
    }

    /**
     * Check if the DB is PostgreSQL for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if PostgreSQL, false otherwise.
     */
    public static boolean isPostgreSQLDB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(POSTGRE_SQL);
    }

    /**
     * Check if the DB is PostgreSQL for the given database type.
     *
     * @param database database type.
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSQLDB(Database database) throws DataAccessException {

        return isDBTypeOf(POSTGRE_SQL, database);
    }

    /**
     * Check if the DB is Informix. Identity database is used.
     *
     * @return true if DB is Informix, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isInformixDB() throws DataAccessException {

        return isDBTypeOf(INFORMIX, Database.IDENTITY);
    }

    /**
     * Check if the DB is Informix for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if Informix, false otherwise.
     */
    public static boolean isInformixDB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(INFORMIX);
    }

    /**
     * Check if the DB is Informix for the given database type.
     *
     * @param database database type.
     * @return true if DB is Informix, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isInformixDB(Database database) throws DataAccessException {

        return isDBTypeOf(INFORMIX, database);
    }

    /**
     * Check if the DB is Oracle. Identity database is used.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws DataAccessException {

        return isDBTypeOf(ORACLE, Database.IDENTITY);
    }

    /**
     * Check if the DB is Oracle for the given database product name.
     *
     * @param databaseProductName database product name.
     * @return true if Oracle, false otherwise.
     */
    public static boolean isOracleDB(String databaseProductName) {

        return databaseProductName != null && databaseProductName.contains(ORACLE);
    }

    /**
     * Check if the DB is Oracle for the given database type.
     *
     * @param database database type.
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB(Database database) throws DataAccessException {

        return isDBTypeOf(ORACLE, database);
    }

    /**
     * Check whether the DB type string contains in the driver name or db product name.
     *
     * @param dbType database type string.
     * @param database database type from enum.
     * @return true if the database type matches the driver type, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    private static boolean isDBTypeOf(String dbType, Database database) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(database);
        return jdbcTemplate.getDriverName().contains(dbType) || jdbcTemplate.getDatabaseProductName().contains(dbType);
    }
}
