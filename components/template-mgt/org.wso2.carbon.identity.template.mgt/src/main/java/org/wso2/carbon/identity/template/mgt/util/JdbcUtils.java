/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.template.mgt.util;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.DB2;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.H2;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.MICROSOFT;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.MY_SQL;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.S_MICROSOFT;

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

        return new JdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }

    /**
     * Check if the DB is H2, MySQL or Postgres.
     *
     * @return true if DB is H2, MySQL or Postgres, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isH2MySqlOrPostgresDB() throws DataAccessException {

        return isDBTypeOf(MY_SQL) || isDBTypeOf(H2) || isDBTypeOf(POSTGRE_SQL);
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
     * Check if the DB is MSSql.
     *
     * @return true if DB is MSSql, false otherwise.
     * @throws DataAccessException if error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws DataAccessException {

        return isDBTypeOf(MICROSOFT) || isDBTypeOf(S_MICROSOFT);
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
