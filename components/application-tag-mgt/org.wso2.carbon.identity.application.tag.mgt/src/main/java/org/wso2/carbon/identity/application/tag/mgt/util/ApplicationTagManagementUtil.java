/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.tag.mgt.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtClientException;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtServerException;
import org.wso2.carbon.identity.application.tag.mgt.constant.ApplicationTagManagementConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.DB2;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.H2;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.MARIADB;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.MICROSOFT;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.MYSQL;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.ORACLE;
import static org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants.POSTGRESQL;

/**
 * Utility class for Application Tag Management.
 */
public class ApplicationTagManagementUtil {

    /**
     * Handle Application Tag Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return APIResourceMgtClientException.
     */
    public static ApplicationTagMgtClientException handleClientException(
            ApplicationTagManagementConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ApplicationTagMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle Application Tag Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return APIResourceMgtServerException.
     */
    public static ApplicationTagMgtServerException handleServerException(
            ApplicationTagManagementConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ApplicationTagMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Check whether the string, "oracle", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws ApplicationTagMgtServerException {

        return isDBTypeOf(ORACLE);
    }

    /**
     * Check whether the string, "microsoft", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws ApplicationTagMgtServerException {

        return isDBTypeOf(MICROSOFT);
    }

    /**
     * Check whether the string, "postgresql", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSQL() throws ApplicationTagMgtServerException {

        return isDBTypeOf(POSTGRESQL);
    }

    /**
     * Check whether the string, "h2", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isH2() throws ApplicationTagMgtServerException {

        return isDBTypeOf(H2);
    }

    /**
     * Check whether the string, "mysql", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isMySQL() throws ApplicationTagMgtServerException {

        return isDBTypeOf(MYSQL);
    }

    /**
     * Check whether the string, "mariadb", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isMariaDB() throws ApplicationTagMgtServerException {

        return isDBTypeOf(MARIADB);
    }

    /**
     * Check whether the string, "db2", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    public static boolean isDB2() throws ApplicationTagMgtServerException {

        return isDBTypeOf(DB2);
    }


    /**
     * Check whether the DB type string contains in the driver name or db product name.
     *
     * @param dbType database type string.
     * @return true if the database type matches the driver type, false otherwise.
     * @throws ApplicationTagMgtServerException If error occurred while checking the DB metadata.
     */
    private static boolean isDBTypeOf(String dbType) throws ApplicationTagMgtServerException {

        try {
            NamedJdbcTemplate jdbcTemplate = getNewTemplate();
            return jdbcTemplate.getDriverName().toLowerCase().contains(dbType) ||
                    jdbcTemplate.getDatabaseProductName().toLowerCase().contains(dbType);
        } catch (DataAccessException e) {
            throw handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_DB_METADATA, e);
        }
    }

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }
}
