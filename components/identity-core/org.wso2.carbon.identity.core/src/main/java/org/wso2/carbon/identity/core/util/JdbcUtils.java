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
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.DB2;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.H2;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.INFORMIX;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MARIADB;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MICROSOFT;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MY_SQL;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORACLE;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.S_MICROSOFT;

public class JdbcUtils {

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static JdbcTemplate getNewTemplate() {

        return new JdbcTemplate(JDBCPersistenceManager.getInstance().getDataSource());
    }

    /**
     * Check if the DB is H2.
     *
     * @return true if H2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2DB() throws DataAccessException {

        return isDBTypeOf(H2);
    }

    /**
     * Check if the DB is DB2.
     *
     * @return true if DB2, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isDB2DB() throws DataAccessException {

        return isDBTypeOf(DB2);
    }

    /**
     * Check if the DB is MySQL.
     *
     * @return true if DB is MySQL.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMySQLDB() throws DataAccessException {

        return isDBTypeOf(MY_SQL);
    }

    /**
     * Check if the DB is MSSql.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws DataAccessException {

        return isDBTypeOf(MICROSOFT) || isDBTypeOf(S_MICROSOFT);
    }

    /**
     * Check if the DB is Maria DB.
     *
     * @return true if DB is Maria DB.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMariaDB() throws DataAccessException {

        return isDBTypeOf(MARIADB);
    }

    /**
     * Check if the DB is PostgreSQL.
     *
     * @return true if DB is PostgreSQL, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSQLDB() throws DataAccessException {

        return isDBTypeOf(POSTGRE_SQL);
    }

    /**
     * Check if the DB is Informix.
     *
     * @return true if DB is Informix, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isInformixDB() throws DataAccessException {

        return isDBTypeOf(INFORMIX);
    }

    /**
     * Check if the DB is Oracle.
     *
     * @return true if DB is Oracle, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws DataAccessException {

        return isDBTypeOf(ORACLE);
    }

    /**
     * Check if the DB is H2, MySQL or Postgres.
     *
     * @return true if DB is H2, MySQL or Postgres, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2MySqlOrPostgresDB() throws DataAccessException {

        return isDBTypeOf(MY_SQL) || isDBTypeOf(H2) || isDBTypeOf(POSTGRE_SQL) || isDBTypeOf(MARIADB);
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
