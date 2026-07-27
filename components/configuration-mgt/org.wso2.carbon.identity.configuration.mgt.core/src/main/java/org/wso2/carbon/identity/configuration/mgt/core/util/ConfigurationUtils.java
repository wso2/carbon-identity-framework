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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementServerException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.PATH_SEPARATOR;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_PATH;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.SERVER_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.TENANT_CONTEXT_PATH_COMPONENT;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.TENANT_NAME_FROM_CONTEXT;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_CREATED_TIME_COLUMN_MSSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_CREATED_TIME_COLUMN_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_CREATED_TIME_COLUMN_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.MAX_QUERY_LENGTH_IN_BYTES_SQL;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isMSSqlDB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isOracleDB;

/**
 * Utility methods for configuration management.
 */
public class ConfigurationUtils {

    private static final Log log = LogFactory.getLog(ConfigurationUtils.class);

    /**
     * This method can be used to generate a ConfigurationManagementClientException from
     * ConfigurationConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @return ConfigurationManagementClientException.
     */
    public static ConfigurationManagementClientException handleClientException(
            ConfigurationConstants.ErrorMessages error) {

        return new ConfigurationManagementClientException(error.getMessage(), error.getCode());
    }

    /**
     * This method can be used to generate a ConfigurationManagementClientException from
     * ConfigurationConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConfigurationManagementClientException.
     */
    public static ConfigurationManagementClientException handleClientException(ConfigurationConstants.ErrorMessages error,
                                                                               String ...data) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementClientException(message, error.getCode());
    }

    public static ConfigurationManagementClientException handleClientException(ConfigurationConstants.ErrorMessages error,
                                                                               String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementServerException from
     * ConfigurationConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConfigurationManagementServerException.
     */
    public static ConfigurationManagementServerException handleServerException(ConfigurationConstants.ErrorMessages error,
                                                                               String data) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementServerException(message, error.getCode());
    }

    public static ConfigurationManagementServerException handleServerException(ConfigurationConstants.ErrorMessages error,
                                                                               String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementServerException(message, error.getCode(), e);
    }

    public static ConfigurationManagementServerException handleServerException(
            ConfigurationConstants.ErrorMessages error, Throwable e) {

        String message = populateMessageWithData(error);
        return new ConfigurationManagementServerException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementRuntimeException from
     * ConfigurationConstants.ErrorMessages object when an exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return ConsentManagementRuntimeException
     */
    public static ConfigurationManagementRuntimeException handleRuntimeException(ConfigurationConstants.ErrorMessages error,
                                                                                 String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementRuntimeException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementRuntimeException from ConfigurationConstants
     * .ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConsentManagementRuntimeException
     */
    public static ConfigurationManagementRuntimeException handleRuntimeException(ConfigurationConstants.ErrorMessages error,
                                                                                 String data) {

        String message = populateMessageWithData(error, data);
        return new ConfigurationManagementRuntimeException(message, error.getCode());
    }

    public static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    /**
     * Get upper limit of the length of the query when converted in to a byte array.
     *
     * @return Maximum length of the query when converted in to a byte array.
     */
    public static int getMaximumQueryLengthInBytes() {

        return StringUtils.isEmpty(MAX_QUERY_LENGTH_IN_BYTES_SQL) ? 4194304 : Integer.parseInt(MAX_QUERY_LENGTH_IN_BYTES_SQL);
    }

    /**
     * Checks whether the configuration management is enabled by checking the existence of the required tables.
     *
     * @return true if configuration management is enabled, false otherwise.
     */
    public static boolean isConfigurationManagementEnabled() {

        log.debug("Checking if configuration management is enabled.");
        return IdentityDatabaseUtil.isTableExists("IDN_CONFIG_TYPE")
                && IdentityDatabaseUtil.isTableExists("IDN_CONFIG_RESOURCE")
                && IdentityDatabaseUtil.isTableExists("IDN_CONFIG_ATTRIBUTE")
                && IdentityDatabaseUtil.isTableExists("IDN_CONFIG_FILE");
    }

    /**
     * Checks whether the CREATED_TIME field exists in the IDN_CONFIG_RESOURCE table.
     *
     * @return true if the CREATED_TIME field exists, false otherwise.
     */
    public static boolean isCreatedTimeFieldExists() {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            /*
            DB scripts without CREATED_TIME field can exists for H2 and MYSQL 5.7.
             */
            String sql = GET_CREATED_TIME_COLUMN_MYSQL;

            if (isMSSqlDB()) {
                sql = GET_CREATED_TIME_COLUMN_MSSQL;
            } else if (isOracleDB()) {
                sql = GET_CREATED_TIME_COLUMN_ORACLE;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                // Following statement will throw SQLException if the column is not found
                resultSet.findColumn(DB_SCHEMA_COLUMN_NAME_CREATED_TIME);
                // If we are here then the column exists.
                log.debug("CREATED_TIME field exists in IDN_CONFIG_RESOURCE table.");
                return true;
            } catch (SQLException e) {
                if (log.isDebugEnabled()) {
                    log.debug("CREATED_TIME field does not exist in IDN_CONFIG_RESOURCE table.", e);
                }
                return false;
            }
        } catch (IdentityRuntimeException | SQLException | DataAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while checking if CREATED_TIME field exists in IDN_CONFIG_RESOURCE table.",
                        e);
            }
            return false;
        }
    }

    /**
     * Sets the useCreatedTime flag in the ConfigurationManagerComponentDataHolder.
     * This flag indicates whether the created time field exists in the database.
     */
    public static void setUseCreatedTime() {

        log.debug("Setting useCreatedTime flag in ConfigurationManagerComponentDataHolder.");
        ConfigurationManagerComponentDataHolder.setUseCreatedTime(
                isConfigurationManagementEnabled() && isCreatedTimeFieldExists());
    }

    public static boolean useCreatedTimeField() {

        return ConfigurationManagerComponentDataHolder.getUseCreatedTime();
    }

    private static String populateMessageWithData(ConfigurationConstants.ErrorMessages error, String... data) {

        String message;
        if (data != null && data.length != 0) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }

    private static String populateMessageWithData(ConfigurationConstants.ErrorMessages error) {

        return error.getMessage();
    }

    /**
     * Creates the file endpoint using the parameters.
     *
     * @param fileId file id.
     * @param resourceName resource name.
     * @param resourceType resource type name.
     * @return tenant domain of the request is being served.
     */
    public static String getFilePath(String fileId, String resourceType, String resourceName) {

        return RESOURCE_PATH + PATH_SEPARATOR + resourceType + PATH_SEPARATOR + resourceName + PATH_SEPARATOR + FILE
                + PATH_SEPARATOR + fileId;
    }

    /**
     * Build URI prepending the user API context with to the endpoint.
     * /t/<tenant-domain>/api/identity/config-mgt/v1.0/<endpoint>
     *
     * @param endpoint relative endpoint path.
     * @return Fully qualified URI.
     */
    public static String buildURIForBody(String endpoint) {

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            try {
                String url = ServiceURLBuilder.create().addPath(SERVER_API_PATH_COMPONENT + endpoint).build()
                        .getRelativePublicURL();
                return URI.create(url).toString();
            } catch (URLBuilderException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while building URI.", e);
                }
            }
        }

        String tenantQualifiedRelativePath =
                String.format(TENANT_CONTEXT_PATH_COMPONENT, getTenantDomainFromContext()) + SERVER_API_PATH_COMPONENT;
        String url = IdentityUtil.getEndpointURIPath(tenantQualifiedRelativePath + endpoint, true, true);
        return URI.create(url).toString();
    }

    /**
     * Retrieves loaded tenant domain from carbon context.
     *
     * @return tenant domain of the request is being served.
     */
    public static String getTenantDomainFromContext() {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT) != null) {
            tenantDomain = (String) IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT);
        }
        return tenantDomain;
    }

}
