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

package org.wso2.carbon.identity.configuration.mgt.core.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.Template;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementServerException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceSearchBean;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.configuration.mgt.core.search.PlaceholderSQL;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveConditionValidator;
import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_DESCRIPTTION;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_FILE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_FILE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_HAS_FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.DB_SCHEMA_COLUMN_NAME_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_RESOURCE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_RESOURCE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants
        .DB_SCHEMA_COLUMN_NAME_VALUE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ADD_RESOURCE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ADD_RESOURCE_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_DELETE_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_DELETE_FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_DELETE_FILES;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_DELETE_RESOURCE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_DELETE_RESOURCE_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_FILES;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_FILES_BY_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_RESOURCE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_INSERT_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_INSERT_FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_QUERY_LENGTH_EXCEEDED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_REPLACE_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_REPLACE_RESOURCE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCES_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RETRIEVE_RESOURCE_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_SEARCH_TENANT_RESOURCES;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_UPDATE_ATTRIBUTE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_UPDATE_RESOURCE_TYPE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_CHECK_DB_METADATA;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.DELETE_ATTRIBUTE_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.DELETE_FILES_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.DELETE_FILE_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.DELETE_RESOURCE_ATTRIBUTES_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_ATTRIBUTES_BY_RESOURCE_ID_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_FILES_BY_RESOURCE_ID_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_FILES_BY_RESOURCE_TYPE_ID_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_RESOURCES_BY_RESOURCE_TYPE_ID_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_BY_ID_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_BY_ID_MSSQL_OR_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_RESOURCE_BY_ID_MYSQL_WITHOUT_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_BY_NAME_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_ID_TENANT_ID_BY_TYPE_ID_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_TYPE_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_RESOURCE_BY_NAME_MSSQL_OR_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_RESOURCE_BY_NAME_MYSQL_WITHOUT_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_RESOURCE_CREATED_TIME_BY_NAME_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_RESOURCE_NAME_BY_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_TENANT_RESOURCES_SELECT_COLUMNS_MSSQL_OR_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL_WITHOUT_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_ATTRIBUTES_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_ATTRIBUTES_POSTGRESQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_OR_UPDATE_ATTRIBUTES_MSSQL_OR_DB2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_OR_UPDATE_ATTRIBUTES_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_ATTRIBUTE_H2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_ATTRIBUTE_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_ATTRIBUTE_MSSQL_OR_DB2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_ATTRIBUTE_POSTGRESQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_ATTRIBUTE_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_RESOURCE_MYSQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_OR_UPDATE_RESOURCE_MSSQL_OR_DB2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_RESOURCE_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_RESOURCE_POSTGRESQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_OR_UPDATE_RESOURCE_MYSQL_WITHOUT_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_RESOURCE_TYPE_H2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_RESOURCE_TYPE_POSTGRESQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.
        INSERT_OR_UPDATE_RESOURCE_TYPE_MSSQL_OR_DB2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_OR_UPDATE_RESOURCE_TYPE_ORACLE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.INSERT_RESOURCE_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants
        .INSERT_RESOURCE_SQL_WITHOUT_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.MAX_QUERY_LENGTH_IN_BYTES_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.UPDATE_HAS_FILE_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.UPDATE_LAST_MODIFIED_SQL;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.UPDATE_RESOURCE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.UPDATE_RESOURCE_H2;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.generateUniqueID;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.getFilePath;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.getMaximumQueryLengthInBytes;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.handleClientException;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.handleServerException;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.useCreatedTimeField;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isMariaDB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isMySQLDB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isMSSqlDB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isPostgreSQLDB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isOracleDB;

/**
 * {@link ConfigurationDAO} implementation.
 */
public class ConfigurationDAOImpl implements ConfigurationDAO {

    private static final Log log = LogFactory.getLog(ConfigurationDAOImpl.class);
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resources getTenantResources(Condition condition) throws ConfigurationManagementException {

        PlaceholderSQL placeholderSQL = buildPlaceholderSQL(condition, useCreatedTimeField());
        if (placeholderSQL.getQuery().getBytes().length > getMaximumQueryLengthInBytes()) {
            if (log.isDebugEnabled()) {
                log.debug("Error building SQL query for the search. Search expression " +
                        "query length: " + placeholderSQL.getQuery().length() + " exceeds the maximum limit: " +
                        MAX_QUERY_LENGTH_IN_BYTES_SQL);
            }
            throw handleClientException(ERROR_CODE_QUERY_LENGTH_EXCEEDED, null);
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<ConfigurationRawDataCollector> configurationRawDataCollectors;
        try {
            configurationRawDataCollectors = jdbcTemplate.executeQuery(placeholderSQL.getQuery(),
                    (resultSet, rowNumber) -> {
                        ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder
                                configurationRawDataCollectorBuilder =
                                new ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder()
                                        .setResourceId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setResourceName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                                                calendar))
                                        .setResourceTypeName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE))
                                        .setResourceTypeDescription(resultSet.getString
                                                (DB_SCHEMA_COLUMN_NAME_DESCRIPTTION))
                                        .setAttributeKey(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY))
                                        .setAttributeValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE))
                                        .setAttributeId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID))
                                        .setFileId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_ID));
                        if (useCreatedTimeField()) {
                            configurationRawDataCollectorBuilder
                                    .setCreatedTime(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                            calendar));
                        }
                        return configurationRawDataCollectorBuilder.build();
                    }, preparedStatement -> {
                        for (int count = 0; count < placeholderSQL.getData().size(); count++) {
                            if (placeholderSQL.getData().get(count).getClass().equals(Integer.class)) {
                                preparedStatement.setInt(
                                        count + 1,
                                        (Integer) placeholderSQL.getData().get(count)
                                );
                            } else {
                                preparedStatement.setString(
                                        count + 1,
                                        (String) placeholderSQL.getData().get(count)
                                );
                            }
                        }
                    });
            /*
            Database call can contain duplicate data for some columns. Need to filter them in order to build the
            resource.
            */
            return configurationRawDataCollectors == null || configurationRawDataCollectors.size() == 0 ?
                    null : buildResourcesFromRawData(configurationRawDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_SEARCH_TENANT_RESOURCES, null, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResourceByName(int tenantId, String resourceTypeId, String resourceName)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<ConfigurationRawDataCollector> configurationRawDataCollectors;
        try {
            String queryWithCreatedTime = GET_RESOURCE_BY_NAME_MYSQL;
            String queryWithOutCreatedTime = GET_RESOURCE_BY_NAME_MYSQL_WITHOUT_CREATED_TIME;
            if (isOracleDB() || isMSSqlDB()) {
                queryWithCreatedTime = GET_RESOURCE_BY_NAME_MSSQL_OR_ORACLE;
            }
            configurationRawDataCollectors = jdbcTemplate.executeQuery(
                    useCreatedTimeField() ? queryWithCreatedTime : queryWithOutCreatedTime,
                    (resultSet, rowNumber) -> {
                        ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder
                                configurationRawDataCollectorBuilder =
                                new ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder()
                                        .setResourceId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setResourceName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                                                calendar))
                                        .setResourceTypeName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE))
                                        .setResourceTypeDescription(resultSet.getString
                                                (DB_SCHEMA_COLUMN_NAME_DESCRIPTTION))
                                        .setAttributeKey(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY))
                                        .setAttributeValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE))
                                        .setAttributeId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID))
                                        .setFileId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_ID))
                                        .setFileName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_NAME))
                                        .setHasFile(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_FILE))
                                        .setHasAttribute(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE));
                        if (useCreatedTimeField()) {
                            configurationRawDataCollectorBuilder.setCreatedTime(
                                    resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                            calendar));
                        }
                        return configurationRawDataCollectorBuilder.build();
                    }, preparedStatement -> {
                        int initialParameterIndex = 1;
                        preparedStatement.setString(initialParameterIndex, resourceName);
                        preparedStatement.setInt(++initialParameterIndex, tenantId);
                        preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                    });
            /*
            Database call can contain duplicate data for some columns. Need to filter them in order to build the
            resource.
            */
            return configurationRawDataCollectors == null || configurationRawDataCollectors.size() == 0 ?
                    null : buildResourceFromRawData(configurationRawDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_RESOURCE, resourceName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResourceById(String resourceId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<ConfigurationRawDataCollector> configurationRawDataCollectors;
        try {
            configurationRawDataCollectors = jdbcTemplate.executeQuery(
                    useCreatedTimeField() ? GET_RESOURCE_BY_ID_MYSQL : GET_RESOURCE_BY_ID_MYSQL_WITHOUT_CREATED_TIME,
                    (resultSet, rowNumber) -> {
                        ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder
                                configurationRawDataCollectorBuilder =
                                new ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder()
                                        .setResourceId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setResourceName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                                                calendar))
                                        .setResourceTypeName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE))
                                        .setResourceTypeDescription(resultSet.getString
                                                (DB_SCHEMA_COLUMN_NAME_DESCRIPTTION))
                                        .setAttributeKey(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY))
                                        .setAttributeValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE))
                                        .setAttributeId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID))
                                        .setFileId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_ID))
                                        .setHasFile(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_FILE))
                                        .setHasAttribute(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE));
                        if (useCreatedTimeField()) {
                            configurationRawDataCollectorBuilder.setCreatedTime(
                                    resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                            calendar)
                            );
                        }
                        return configurationRawDataCollectorBuilder.build();
                    },
                    preparedStatement -> preparedStatement.setString(1, resourceId));
            /*
            Database call can contain duplicate data for some columns. Need to filter them in order to build the
            resource.
            */
            return configurationRawDataCollectors == null || configurationRawDataCollectors.size() == 0 ?
                    null : buildResourceFromRawData(configurationRawDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_RESOURCE, "id = " + resourceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getTenantResourceById(int tenantId, String resourceId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<ConfigurationRawDataCollector> configurationRawDataCollectors;
        try {
            String queryWithCreatedTime = GET_RESOURCE_BY_ID_MYSQL;
            String queryWithOutCreatedTime = GET_RESOURCE_BY_ID_MYSQL_WITHOUT_CREATED_TIME;

            if (isOracleDB() || isMSSqlDB()) {
                queryWithCreatedTime = GET_RESOURCE_BY_ID_MSSQL_OR_ORACLE;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(useCreatedTimeField() ? queryWithCreatedTime : queryWithOutCreatedTime);
            sb.append(" AND R.TENANT_ID = ?");
            configurationRawDataCollectors = jdbcTemplate.executeQuery(sb.toString(), (resultSet, rowNumber) -> {
                        ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder
                                configurationRawDataCollectorBuilder =
                                new ConfigurationRawDataCollector.ConfigurationRawDataCollectorBuilder()
                                        .setResourceId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setResourceName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                                                calendar))
                                        .setResourceTypeName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE))
                                        .setResourceTypeDescription(resultSet.getString
                                                (DB_SCHEMA_COLUMN_NAME_DESCRIPTTION))
                                        .setAttributeKey(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY))
                                        .setAttributeValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE))
                                        .setAttributeId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID))
                                        .setFileId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_ID))
                                        .setHasFile(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_FILE))
                                        .setHasAttribute(resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE));
                        if (useCreatedTimeField()) {
                            configurationRawDataCollectorBuilder.setCreatedTime(
                                    resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                            calendar)
                            );
                        }
                        return configurationRawDataCollectorBuilder.build();
                    },
                    preparedStatement -> {
                        preparedStatement.setString(1, resourceId);
                        preparedStatement.setInt(2, tenantId);
                    });
            /*
            Database call can contain duplicate data for some columns. Need to filter them in order to build the
            resource.
            */
            return configurationRawDataCollectors == null || configurationRawDataCollectors.size() == 0 ?
                    null : buildResourceFromRawData(configurationRawDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_RESOURCE, "id = " + resourceId, e);
        }
    }

    /**
     * Get resourceId for the {@link Resource}.
     *
     * @param tenantId              Tenant Id of the {@link Resource}.
     * @param resourceTypeId        Type Id of the {@link Resource}.
     * @param resourceName          Name of the {@link Resource}.
     * @return resourceId for the given resource.
     * @throws TransactionException Transaction Exception.
     */
    private String getResourceId(int tenantId, String resourceTypeId, String resourceName) throws TransactionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        return jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(
                        GET_RESOURCE_ID_BY_NAME_SQL,
                        (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID),
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(initialParameterIndex, resourceName);
                            preparedStatement.setInt(++initialParameterIndex,tenantId);
                            preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                        }
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteResourceByName(int tenantId, String resourceTypeId, String resourceName)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            if (isMySQLDB()) {
                String resourceId = getResourceId(tenantId, resourceTypeId, resourceName);
                deleteFiles(resourceId);
            }
            jdbcTemplate.executeUpdate(SQLConstants.DELETE_RESOURCE_SQL, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resourceName);
                preparedStatement.setInt(++initialParameterIndex, tenantId);
                preparedStatement.setString(++initialParameterIndex, resourceTypeId);
            });
        } catch (DataAccessException | TransactionException e) {
            throw handleServerException(ERROR_CODE_DELETE_RESOURCE_TYPE, resourceName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteResourceById(int tenantId, String resourceId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            if (isMySQLDB()) {
                deleteFiles(resourceId);
            }
            jdbcTemplate.executeUpdate(SQLConstants.DELETE_RESOURCE_BY_ID_SQL, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resourceId);
                preparedStatement.setInt(++initialParameterIndex, tenantId);
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_RESOURCE, resourceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceResource(Resource resource) throws ConfigurationManagementException {

        String resourceTypeId = getResourceTypeByName(resource.getResourceType()).getId();
        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            Timestamp createdTime = jdbcTemplate.withTransaction(template -> {
                boolean isAttributeExists = resource.getAttributes() != null;
                if (isH2DB()) {
                    updateMetadataForH2(
                            resource, resourceTypeId, isAttributeExists, currentTime, useCreatedTimeField()
                    );
                } else {
                    updateMetadataForMYSQL(
                            resource, resourceTypeId, isAttributeExists, currentTime, useCreatedTimeField()
                    );
                }

                // Insert attributes.
                if (isAttributeExists) {
                    // Delete existing attributes.
                    template.executeUpdate(DELETE_RESOURCE_ATTRIBUTES_SQL, preparedStatement ->
                            preparedStatement.setString(1, resource.getResourceId()));
                    insertResourceAttributes(template, resource);

                }
                if (useCreatedTimeField()) {
                    return getCreatedTimeInResponse(resource, resourceTypeId);
                } else {
                    return null;
                }
            });
            resource.setLastModified(currentTime.toInstant().toString());
            if (createdTime != null) {
                resource.setCreatedTime(createdTime.toInstant().toString());
            }
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_REPLACE_RESOURCE, resource.getResourceName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceResourceWithFiles(Resource resource) throws ConfigurationManagementException {

        String resourceTypeId = getResourceTypeByName(resource.getResourceType()).getId();
        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            Timestamp createdTime = jdbcTemplate.withTransaction(template -> {
                boolean isAttributeExists = resource.getAttributes() != null && !resource.getAttributes().isEmpty();
                boolean isFileExists = resource.getFiles() != null && !resource.getFiles().isEmpty();

                // Update attributes.
                if (isAttributeExists) {
                    // Delete existing attributes.
                    template.executeUpdate(DELETE_RESOURCE_ATTRIBUTES_SQL, preparedStatement ->
                            preparedStatement.setString(1, resource.getResourceId()));
                    insertResourceAttributes(template, resource);
                }

                // Update Files.
                if (isFileExists) {
                    template.executeUpdate(DELETE_FILES_SQL, (
                            preparedStatement -> preparedStatement.setString(1, resource.getResourceId())
                    ));
                    for (ResourceFile file : resource.getFiles()) {
                        insertResourceFile(template, resource, file.getId(), file.getName(), file.getInputStream());
                    }
                }
                updateResourceMetadata(template, resource, isAttributeExists, isFileExists, currentTime);
                if (useCreatedTimeField()) {
                    return getCreatedTimeInResponse(resource, resourceTypeId);
                } else {
                    return null;
                }
            });
            resource.setLastModified(currentTime.toInstant().toString());
            if (createdTime != null) {
                resource.setCreatedTime(createdTime.toInstant().toString());
            }
        } catch (TransactionException e) {
            if (e.getCause() instanceof ConfigurationManagementException) {
                throw (ConfigurationManagementException) e.getCause();
            }
            throw handleServerException(ERROR_CODE_REPLACE_RESOURCE, resource.getResourceName(), e);
        }
    }

    @Override
    public boolean isExistingResource(int tenantId, String resourceId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        String resourceName;
        try {
            resourceName = jdbcTemplate.fetchSingleRecord(GET_RESOURCE_NAME_BY_ID, (resultSet, rowNumber) ->
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME),
                    preparedStatement -> {
                        preparedStatement.setString(1, resourceId);
                        preparedStatement.setInt(2, tenantId);
                    });
            return StringUtils.isNotEmpty(resourceName);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_RESOURCE, "id = " + resourceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResource(Resource resource) throws ConfigurationManagementException {

        String resourceTypeId = getResourceTypeByName(resource.getResourceType()).getId();
        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            jdbcTemplate.withTransaction(template -> {
                boolean isAttributeExists = resource.getAttributes() != null;
                boolean isFileExists = resource.getFiles() != null && !resource.getFiles().isEmpty();

                // Insert resource metadata.
                template.executeInsert(
                        useCreatedTimeField() ? INSERT_RESOURCE_SQL : INSERT_RESOURCE_SQL_WITHOUT_CREATED_TIME,
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(initialParameterIndex, resource.getResourceId());
                            preparedStatement.setInt(++initialParameterIndex,
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                            .getTenantId());
                            preparedStatement.setString(++initialParameterIndex, resource.getResourceName());
                            if (useCreatedTimeField()) {
                                preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                            }
                            preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                            if (isOracleOrMssql) {
                                preparedStatement.setInt(++initialParameterIndex, isFileExists ? 1 : 0);
                                preparedStatement.setInt(++initialParameterIndex, isAttributeExists ? 1 : 0);
                            } else {
                                preparedStatement.setBoolean(++initialParameterIndex, isFileExists);
                                preparedStatement.setBoolean(++initialParameterIndex, isAttributeExists);
                            }
                            preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                        }, resource, false);

                // Insert attributes.
                if (isAttributeExists) {
                    insertResourceAttributes(template, resource);
                }
                // Insert files.
                if (isFileExists) {
                    for (ResourceFile file : resource.getFiles()) {
                        insertResourceFile(template, resource, file.getId(), file.getName(),
                                file.getInputStream());
                    }
                }
                return null;
            });
            resource.setLastModified(currentTime.toInstant().toString());
            if (useCreatedTimeField()) {
                resource.setCreatedTime(currentTime.toInstant().toString());
            }
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ADD_RESOURCE, resource.getResourceName(), e);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResourceType(ResourceType resourceType) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeInsert(SQLConstants.INSERT_RESOURCE_TYPE_SQL, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resourceType.getId());
                preparedStatement.setString(++initialParameterIndex, resourceType.getName());
                preparedStatement.setString(++initialParameterIndex, resourceType.getDescription());
            }, resourceType, false);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ADD_RESOURCE_TYPE, resourceType.getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceResourceType(ResourceType resourceType) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String query = SQLConstants.INSERT_OR_UPDATE_RESOURCE_TYPE_MYSQL;
            if (isH2DB()) {
                query = INSERT_OR_UPDATE_RESOURCE_TYPE_H2;
            } else if (isPostgreSQLDB()) {
                query = INSERT_OR_UPDATE_RESOURCE_TYPE_POSTGRESQL;
            } else if (isMSSqlDB() || isDB2DB()) {
                query = INSERT_OR_UPDATE_RESOURCE_TYPE_MSSQL_OR_DB2;
            } else if (isOracleDB()) {
                query = INSERT_OR_UPDATE_RESOURCE_TYPE_ORACLE;
            }

            jdbcTemplate.executeInsert(query, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resourceType.getId());
                preparedStatement.setString(++initialParameterIndex, resourceType.getName());
                preparedStatement.setString(++initialParameterIndex, resourceType.getDescription());
            }, resourceType, false);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_UPDATE_RESOURCE_TYPE, resourceType.getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceType getResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException {

        return getResourceTypeByIdentifier(resourceTypeName, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceType getResourceTypeById(String resourceTypeId) throws ConfigurationManagementException {

        return getResourceTypeByIdentifier(null, resourceTypeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException {

        try {
            if (isMySQLDB()) {
                JdbcTemplate jdbcTemplateGetResourceTypeId = JdbcUtils.getNewTemplate();
                String resourceTypeId = jdbcTemplateGetResourceTypeId.withTransaction(template ->
                        template.fetchSingleRecord(GET_RESOURCE_TYPE_ID_BY_NAME_SQL,
                                (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID),
                                preparedStatement -> {
                                    int initialParameterIndex = 1;
                                    preparedStatement.setString(initialParameterIndex, resourceTypeName);
                                }
                        )
                );
                JdbcTemplate jdbcTemplateGetIds = JdbcUtils.getNewTemplate();
                jdbcTemplateGetIds.executeQuery(GET_RESOURCE_ID_TENANT_ID_BY_TYPE_ID_SQL, ((resultSet, rowNumber) -> {
                    String ResourceId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                    int TenantId = resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID);
                    try {
                        CachedBackedConfigurationDAO cachedBackedConfigurationDAO = new CachedBackedConfigurationDAO(this);
                        cachedBackedConfigurationDAO.deleteResourceById(TenantId, ResourceId);
                    } catch (ConfigurationManagementException e) {
                        log.error(ERROR_CODE_DELETE_RESOURCE + ResourceId, e);
                    }
                    return null;
                }), preparedStatement -> preparedStatement.setString(1, resourceTypeId));
            }
        } catch (TransactionException | DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_RESOURCE_TYPE, resourceTypeName, e);
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(selectDeleteResourceTypeQuery(null), (
                    preparedStatement -> preparedStatement.setString(1, resourceTypeName)
            ));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_RESOURCE_TYPE, resourceTypeName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAttribute(String attributeId, String resourceId, String attributeKey)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(DELETE_ATTRIBUTE_SQL, (
                        preparedStatement -> preparedStatement.setString(1, attributeId)
                ));
                template.executeUpdate(SQLConstants.UPDATE_LAST_MODIFIED_SQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setTimestamp(initialParameterIndex, new java.sql.Timestamp(new Date().getTime()),
                            calendar);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_DELETE_ATTRIBUTE, attributeKey, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(SQLConstants.UPDATE_ATTRIBUTE_MYSQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setString(initialParameterIndex, attribute.getValue());
                    preparedStatement.setString(++initialParameterIndex, attributeId);
                });
                template.executeUpdate(SQLConstants.UPDATE_LAST_MODIFIED_SQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setTimestamp(initialParameterIndex, new java.sql.Timestamp(new Date().getTime()),
                            calendar);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_UPDATE_ATTRIBUTE, attribute.getKey(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(SQLConstants.INSERT_ATTRIBUTE_MYSQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setString(initialParameterIndex, attributeId);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                    preparedStatement.setString(++initialParameterIndex, attribute.getKey());
                    preparedStatement.setString(++initialParameterIndex, attribute.getValue());

                });
                template.executeUpdate(SQLConstants.UPDATE_LAST_MODIFIED_SQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setTimestamp(initialParameterIndex, new java.sql.Timestamp(new Date().getTime()),
                            calendar);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_INSERT_ATTRIBUTE, attribute.getKey(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                String query = INSERT_OR_UPDATE_ATTRIBUTE_MYSQL;
                if (isH2DB()) {
                    query = INSERT_OR_UPDATE_ATTRIBUTE_H2;
                } else if (isPostgreSQLDB()) {
                    query = INSERT_OR_UPDATE_ATTRIBUTE_POSTGRESQL;
                } else if (isMSSqlDB() || isDB2DB()) {
                    query = INSERT_OR_UPDATE_ATTRIBUTE_MSSQL_OR_DB2;
                } else if (isOracleDB()) {
                    query = INSERT_OR_UPDATE_ATTRIBUTE_ORACLE;
                }
                template.executeUpdate(query, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setString(initialParameterIndex, attributeId);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                    preparedStatement.setString(++initialParameterIndex, attribute.getKey());
                    preparedStatement.setString(++initialParameterIndex, attribute.getValue());
                });
                template.executeUpdate(SQLConstants.UPDATE_LAST_MODIFIED_SQL, preparedStatement -> {
                    int initialParameterIndex = 1;
                    preparedStatement.setTimestamp(initialParameterIndex, new java.sql.Timestamp(new Date().getTime()),
                            calendar);
                    preparedStatement.setString(++initialParameterIndex, resourceId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_REPLACE_ATTRIBUTE, attribute.getKey(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByKey(String resourceId, String attributeKey) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord(SQLConstants.GET_ATTRIBUTE_SQL,
                    (resultSet, rowNumber) -> new Attribute(
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY),
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE),
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID)
                    ),
                    preparedStatement -> {
                        int initialParameterIndex = 1;
                        preparedStatement.setString(initialParameterIndex, attributeKey);
                        preparedStatement.setString(++initialParameterIndex, resourceId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ATTRIBUTE, attributeKey, e);
        }
    }

    private void updateMetadataForMYSQL(Resource resource, String resourceTypeId, boolean isAttributeExists,
                                        Timestamp currentTime, boolean useCreatedTime)
            throws TransactionException, ConfigurationManagementServerException {

        String query = INSERT_OR_UPDATE_RESOURCE_MYSQL;
        try {
            if (isPostgreSQLDB()) {
                query = INSERT_OR_UPDATE_RESOURCE_POSTGRESQL;
            } else if (isMSSqlDB() || isDB2DB()) {
                query = INSERT_OR_UPDATE_RESOURCE_MSSQL_OR_DB2;
            } else if (isOracleDB()) {
                query = INSERT_OR_UPDATE_RESOURCE_ORACLE;
            }

            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            final String finalQuery = query;
            jdbcTemplate.withTransaction(template ->
                    template.executeInsert(
                            useCreatedTime ? finalQuery :
                                    INSERT_OR_UPDATE_RESOURCE_MYSQL_WITHOUT_CREATED_TIME,
                            preparedStatement -> {
                                int initialParameterIndex = 1;
                                preparedStatement.setString(initialParameterIndex, resource.getResourceId());
                                preparedStatement.setInt(++initialParameterIndex,
                                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                .getTenantId());
                                preparedStatement.setString(++initialParameterIndex, resource.getResourceName());
                                if (useCreatedTime) {
                                    preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                                }
                                preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                            /*
                            Resource files are uploaded using a separate endpoint. Therefore resource creation does
                            not create files. It is allowed to create a resource without files or attributes in order
                            to allow  file upload after resource creation.
                            */

                                if (isOracleOrMssql) {
                                    preparedStatement.setInt(++initialParameterIndex, 0);
                                    preparedStatement.setInt(++initialParameterIndex, isAttributeExists ? 1 : 0);
                                } else {
                                    preparedStatement.setBoolean(++initialParameterIndex, false);
                                    preparedStatement.setBoolean(++initialParameterIndex, isAttributeExists);
                                }
                                preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                            }, resource, false)
            );
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    private void updateMetadataForH2(Resource resource, String resourceTypeId, boolean isAttributeExists,
                                     Timestamp currentTime, boolean useCreatedTime)
            throws TransactionException, ConfigurationManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        if (isResourceExists(resource, resourceTypeId)) {
            jdbcTemplate.withTransaction(template ->
                    template.executeInsert(UPDATE_RESOURCE_H2,
                            preparedStatement -> {
                                int initialParameterIndex = 1;
                                preparedStatement.setInt(initialParameterIndex,
                                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                                preparedStatement.setString(++initialParameterIndex, resource.getResourceName());
                                preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                                /*
                                Resource files are uploaded using a separate endpoint. Therefore resource creation
                                does not create files. It is allowed to create a resource without files or attributes
                                in order to allow file uploadafter resource creation.
                                */
                                preparedStatement.setBoolean(++initialParameterIndex, false);
                                preparedStatement.setBoolean(++initialParameterIndex, isAttributeExists);
                                preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                                preparedStatement.setString(++initialParameterIndex, resource.getResourceId());
                            }, resource, false
                    )
            );
        } else {
            try {
                boolean isOracleOrMsssql = isOracleDB() || isMSSqlDB();
                jdbcTemplate.withTransaction(template ->
                        template.executeInsert(
                                useCreatedTime ? INSERT_RESOURCE_SQL : INSERT_RESOURCE_SQL_WITHOUT_CREATED_TIME,
                                preparedStatement -> {
                                    int initialParameterIndex = 1;
                                    preparedStatement.setString(initialParameterIndex, resource.getResourceId());
                                    preparedStatement.setInt(++initialParameterIndex,
                                            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                                    preparedStatement.setString(++initialParameterIndex, resource.getResourceName());
                                    if (useCreatedTime) {
                                        preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                                    }
                                    preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                                /*
                                Resource files are uploaded using a separate endpoint. Therefore resource creation
                                does not create files. It is allowed to create a resource without files or attributes
                                in order to allow file upload after resource creation.
                                */

                                    if (isOracleOrMsssql) {
                                        preparedStatement.setInt(++initialParameterIndex, 0);
                                        preparedStatement.setInt(++initialParameterIndex, isAttributeExists ? 1 : 0);
                                    } else {
                                        preparedStatement.setBoolean(++initialParameterIndex, false);
                                        preparedStatement.setBoolean(++initialParameterIndex, isAttributeExists);
                                    }
                                    preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                                }, resource, false)
                );
            } catch (DataAccessException e) {
                throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
            }
        }
    }

    private void updateResourceMetadata(Template<?> template, Resource resource, boolean isAttributeExists, boolean
            isFileExists, Timestamp currentTime) throws ConfigurationManagementClientException, DataAccessException {

        try {
            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            template.executeUpdate(UPDATE_RESOURCE, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resource.getResourceName());
                preparedStatement.setTimestamp(++initialParameterIndex, currentTime, calendar);
                if (isOracleOrMssql) {
                    preparedStatement.setInt(++initialParameterIndex, isFileExists ? 1 : 0);
                    preparedStatement.setInt(++initialParameterIndex, isAttributeExists ? 1 : 0);
                } else {
                    preparedStatement.setBoolean(++initialParameterIndex, isFileExists);
                    preparedStatement.setBoolean(++initialParameterIndex, isAttributeExists);
                }
                preparedStatement.setString(++initialParameterIndex, resource.getResourceId());
            });
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw handleClientException(ERROR_CODE_RESOURCE_ALREADY_EXISTS, resource.getResourceName(), e);
            } else {
                throw e;
            }
        }
    }

    private boolean isResourceExists(Resource resource, String resourceTypeId) throws TransactionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        String resourceId = jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(
                        GET_RESOURCE_ID_BY_NAME_SQL,
                        (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID),
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(initialParameterIndex, resource.getResourceName());
                            preparedStatement.setInt(++initialParameterIndex,
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                            preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                        }
                )
        );
        return resourceId != null;
    }

    private PlaceholderSQL buildPlaceholderSQL(Condition condition, boolean useCreatedTime)
            throws ConfigurationManagementException {

        String queryWithCreatedTime = GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL;
        String queryWithOutCreatedTime = GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL_WITHOUT_CREATED_TIME;

        try {
            if (isOracleDB() || isMSSqlDB()) {
                queryWithCreatedTime = GET_TENANT_RESOURCES_SELECT_COLUMNS_MSSQL_OR_ORACLE;
            }
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }

        StringBuilder sb = new StringBuilder();

        sb.append(
                useCreatedTime ? queryWithCreatedTime : queryWithOutCreatedTime);
        sb.append("WHERE\n");
        try {
            PlaceholderSQL placeholderSQL = condition.buildQuery(
                    new PrimitiveConditionValidator(new ResourceSearchBean())
            );
            placeholderSQL.setQuery(
                    sb.append(placeholderSQL.getQuery()).toString()
            );
            return placeholderSQL;
        } catch (PrimitiveConditionValidationException e) {
            throw handleClientException(
                    ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR, e.getMessage(), e);
        }
    }

    private Resources buildResourcesFromRawData(List<ConfigurationRawDataCollector> configurationRawDataCollectors) {

        Map<String, Resource> resourcesCollector = new HashMap<>();
        Map<String, List<Attribute>> attributes = new HashMap<>(0); // attribute list pet resource
        Map<String, List<ResourceFile>> resourceFiles = new HashMap<>(0); // file list pet resource
        Map<String, List<String>> attributeKeySet = new HashMap<>();
        Map<String, List<String>> resourceFileIdSet = new HashMap<>();

        configurationRawDataCollectors.forEach(configurationRawDataCollector -> {
            String eachResourceId = configurationRawDataCollector.getResourceId();
            if (resourcesCollector.get(eachResourceId) == null) {
                Resource resource = new Resource();
                resource.setResourceId(configurationRawDataCollector.getResourceId());
                resource.setResourceName(configurationRawDataCollector.getResourceName());
                resource.setResourceType(configurationRawDataCollector.getResourceTypeName());
                resource.setHasFile(configurationRawDataCollector.isHasFile());
                resource.setHasAttribute(configurationRawDataCollector.isHasAttribute());
                if (configurationRawDataCollector.getCreatedTime() != null) {
                    resource.setCreatedTime(configurationRawDataCollector.getCreatedTime().toInstant().toString());
                }
                resource.setLastModified(configurationRawDataCollector.getLastModified().toInstant().toString());
                resource.setTenantDomain(
                        IdentityTenantUtil.getTenantDomain(configurationRawDataCollector.getTenantId())
                );

                // Initialize collectors for the resource
                attributes.put(eachResourceId, new ArrayList<>());
                attributeKeySet.put(eachResourceId, new ArrayList<>());
                resourceFiles.put(eachResourceId, new ArrayList<>());
                resourceFileIdSet.put(eachResourceId, new ArrayList<>());

                resourcesCollector.put(eachResourceId, resource);
            }

            if (!attributeKeySet.get(eachResourceId).contains(configurationRawDataCollector.getAttributeKey())) {
                attributeKeySet.get(eachResourceId).add(configurationRawDataCollector.getAttributeKey());
                if (configurationRawDataCollector.getAttributeKey() != null) {
                    attributes.get(eachResourceId).add(new Attribute(
                            configurationRawDataCollector.getAttributeKey(),
                            configurationRawDataCollector.getAttributeValue(),
                            configurationRawDataCollector.getAttributeId()
                    ));
                }
            }
            if (!resourceFileIdSet.get(eachResourceId).contains(configurationRawDataCollector.getFileId())) {
                resourceFileIdSet.get(eachResourceId).add(configurationRawDataCollector.getFileId());
                if (configurationRawDataCollector.getFileId() != null) {
                    resourceFiles.get(eachResourceId).add(new ResourceFile(configurationRawDataCollector.getFileId(),
                            configurationRawDataCollector.getFileName()));
                }
            }
        });
        resourcesCollector.values().forEach(resource -> {
            resource.setAttributes(attributes.get(resource.getResourceId()));
            resource.setFiles(resourceFiles.get(resource.getResourceId()));
        });
        return new Resources(new ArrayList<>(resourcesCollector.values()));
    }

    private Resource buildResourceFromRawData(List<ConfigurationRawDataCollector> configurationRawDataCollectors) {

        Resource resource = new Resource();
        List<Attribute> attributes = new ArrayList<>(0);
        List<ResourceFile> resourceFiles = new ArrayList<>(0);
        Set<String> attributeKeySet = new HashSet<>();
        Set<String> fileIdSet = new HashSet<>();
        configurationRawDataCollectors.forEach(configurationRawDataCollector -> {
            if (resource.getResourceId() == null) {
                resource.setResourceId(configurationRawDataCollector.getResourceId());
                resource.setHasFile(configurationRawDataCollector.isHasFile());
                resource.setHasAttribute(configurationRawDataCollector.isHasAttribute());
                resource.setResourceName(configurationRawDataCollector.getResourceName());
                resource.setResourceType(configurationRawDataCollector.getResourceTypeName());
                if (configurationRawDataCollector.getCreatedTime() != null) {
                    resource.setCreatedTime(configurationRawDataCollector.getCreatedTime().toInstant().toString());
                }
                resource.setLastModified(configurationRawDataCollector.getLastModified().toInstant().toString());
                resource.setTenantDomain(
                        IdentityTenantUtil.getTenantDomain(configurationRawDataCollector.getTenantId()));
            }

            if (!attributeKeySet.contains(configurationRawDataCollector.getAttributeKey())) {
                attributeKeySet.add(configurationRawDataCollector.getAttributeKey());
                if (configurationRawDataCollector.getAttributeKey() != null) {
                    attributes.add(new Attribute(
                            configurationRawDataCollector.getAttributeKey(),
                            configurationRawDataCollector.getAttributeValue(),
                            configurationRawDataCollector.getAttributeId()
                    ));
                }
            }
            if (!fileIdSet.contains(configurationRawDataCollector.getFileId())) {
                fileIdSet.add(configurationRawDataCollector.getFileId());
                if (configurationRawDataCollector.getFileId() != null) {
                    resourceFiles.add(new ResourceFile(configurationRawDataCollector.getFileId(),
                            configurationRawDataCollector.getFileName()));
                }
            }
        });
        resource.setAttributes(attributes);
        resource.setFiles(resourceFiles);
        return resource;
    }

    private String buildQueryForAttributes(Resource resource) throws ConfigurationManagementClientException,
            DataAccessException {

        StringBuilder sb = new StringBuilder();
        if (isH2DB()) {
            sb.append(SQLConstants.UPDATE_ATTRIBUTES_H2);
        } else if (isMySQLDB() || isPostgreSQLDB() || isMariaDB()) {
            sb.append(SQLConstants.INSERT_ATTRIBUTES_SQL);
        } else if (isMSSqlDB() || isDB2DB()) {
            sb.append(SQLConstants.INSERT_ATTRIBUTES_MSSQL_OR_DB2);
        } else if (isOracleDB()) {
            sb.append(SQLConstants.INSERT_ATTRIBUTES_ORACLE);
        }

        // Since attributes exist, query is already built for the first attribute.
        for (int i = 1; i < resource.getAttributes().size(); i++) {
            if (isOracleDB()) {
                sb.append(SQLConstants.INSERT_ATTRIBUTE_KEY_VALUE_ORACLE);
            } else {
                sb.append(SQLConstants.INSERT_ATTRIBUTE_KEY_VALUE_SQL);
            }
            if (sb.toString().getBytes().length > getMaximumQueryLengthInBytes()) {
                if (log.isDebugEnabled()) {
                    log.debug("Error building SQL query for the attribute insert. Number of attributes: " +
                            resource.getAttributes().size() + " exceeds the maximum limit: " +
                            MAX_QUERY_LENGTH_IN_BYTES_SQL);
                }
                throw handleClientException(ERROR_CODE_QUERY_LENGTH_EXCEEDED, null);
            }
        }
        if (isMySQLDB() || isMariaDB()) {
            sb.append(INSERT_OR_UPDATE_ATTRIBUTES_MYSQL);
        } else if (isPostgreSQLDB()) {
            sb.append(INSERT_OR_UPDATE_ATTRIBUTES_POSTGRESQL);
        } else if (isMSSqlDB() || isDB2DB()) {
            sb.append(INSERT_OR_UPDATE_ATTRIBUTES_MSSQL_OR_DB2);
        } else if (isOracleDB()) {
            sb.append(INSERT_OR_UPDATE_ATTRIBUTES_ORACLE);
        }
        return sb.toString();
    }

    private ResourceType getResourceTypeByIdentifier(String name, String id) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        ResourceType resourceTypeResponse;
        try {
            resourceTypeResponse = jdbcTemplate.fetchSingleRecord(
                    selectGetResourceTypeQuery(id),
                    (resultSet, rowNumber) -> {
                        ResourceType resourceType = new ResourceType();
                        resourceType.setId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID));
                        resourceType.setName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME));
                        resourceType.setDescription(resultSet.getString(DB_SCHEMA_COLUMN_NAME_DESCRIPTTION));
                        return resourceType;
                    }, preparedStatement ->
                            preparedStatement.setString(1, StringUtils.isEmpty(name) ? id : name)
            );
            return resourceTypeResponse;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_RESOURCE_TYPE, name, e);
        }
    }

    private String selectGetResourceTypeQuery(String id) {

        return StringUtils.isEmpty(id) ? SQLConstants.GET_RESOURCE_TYPE_BY_NAME_SQL :
                SQLConstants.GET_RESOURCE_TYPE_BY_ID_SQL;
    }

    private String selectDeleteResourceTypeQuery(String id) {

        return StringUtils.isEmpty(id) ? SQLConstants.DELETE_RESOURCE_TYPE_BY_NAME_SQL :
                SQLConstants.DELETE_RESOURCE_TYPE_BY_ID_SQL;
    }

    private Timestamp getCreatedTimeInResponse(Resource resource, String resourceTypeId)
            throws TransactionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        return jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(GET_RESOURCE_CREATED_TIME_BY_NAME_SQL,
                        (resultSet, rowNumber) -> resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME, calendar),
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(initialParameterIndex, resource.getResourceName());
                            preparedStatement.setInt(++initialParameterIndex,
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                            preparedStatement.setString(++initialParameterIndex, resourceTypeId);
                        }
                )
        );
    }

    @Override
    public void addFile(String fileId, String resourceId, String fileName, InputStream fileStream)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            boolean isPostgreSQL = isPostgreSQLDB();
            String sqlStmt = isH2DB() ? SQLConstants.INSERT_FILE_SQL_H2 : SQLConstants.INSERT_FILE_SQL;

            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(sqlStmt, preparedStatement -> {
                    preparedStatement.setString(1, fileId);
                    if (isPostgreSQL) {
                        preparedStatement.setBinaryStream(2, fileStream);
                    } else {
                        preparedStatement.setBlob(2, fileStream);
                    }
                    preparedStatement.setString(3, resourceId);
                    preparedStatement.setString(4, fileName);
                });
                template.executeUpdate(SQLConstants.UPDATE_HAS_FILE_SQL, preparedStatement -> {
                    if (isOracleOrMssql) {
                        preparedStatement.setInt(1, 1);
                    } else {
                        preparedStatement.setBoolean(1, true);
                    }
                    preparedStatement.setString(2, resourceId);
                });
                updateResourceLastModified(template, resourceId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_INSERT_FILE, fileId, e);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    @Override
    public InputStream getFileById(String resourceType, String resourceName, String fileId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            if (isPostgreSQLDB()) {
                return jdbcTemplate.fetchSingleRecord(getFileGetByIdSQL(), (resultSet, rowNumber) ->
                                resultSet.getBinaryStream(DB_SCHEMA_COLUMN_NAME_VALUE), preparedStatement ->
                        setPreparedStatementForFileGetById(resourceType, resourceName, fileId, preparedStatement));
            }
            Blob fileBlob = jdbcTemplate.fetchSingleRecord(getFileGetByIdSQL(),
                    (resultSet, rowNumber) -> resultSet.getBlob(DB_SCHEMA_COLUMN_NAME_VALUE), preparedStatement ->
                            setPreparedStatementForFileGetById(resourceType, resourceName, fileId, preparedStatement));
            return fileBlob != null ? fileBlob.getBinaryStream() : null;
        } catch (DataAccessException | SQLException e) {
            throw handleServerException(ERROR_CODE_GET_FILE, fileId, e);
        }
    }

    @Override
    public void deleteFileById(String resourceType, String resourceName, String fileId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            jdbcTemplate.withTransaction(template -> {

                // Get resource id for the deleting file.
                String sqlStmt = isH2DB() ? SQLConstants.GET_FILE_BY_ID_SQL_H2 : SQLConstants.GET_FILE_BY_ID_SQL;

                String resourceId = template.fetchSingleRecord(sqlStmt,
                        (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_ID),
                        preparedStatement -> {
                            preparedStatement.setString(1, fileId);
                            preparedStatement.setString(2, resourceName);
                            preparedStatement.setString(3, resourceType);
                        });
                template.executeUpdate(DELETE_FILE_SQL, (
                        preparedStatement -> preparedStatement.setString(1, fileId)
                ));

                List<String> availableFilesForTheResource = template.executeQuery(GET_FILES_BY_RESOURCE_ID_SQL,
                        ((resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID)),
                        preparedStatement -> preparedStatement.setString(1, resourceId));
                if (availableFilesForTheResource.isEmpty()) {
                    template.executeUpdate(UPDATE_HAS_FILE_SQL, preparedStatement -> {
                        if (isOracleOrMssql) {
                            preparedStatement.setInt(1, 0);
                        } else {
                            preparedStatement.setBoolean(1, false);
                        }
                        preparedStatement.setString(2, resourceId);
                    });
                }
                updateResourceLastModified(template, resourceId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_DELETE_FILE, fileId, e);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    private void updateResourceLastModified(Template<Object> template, String resourceId)
            throws DataAccessException {

        template.executeUpdate(UPDATE_LAST_MODIFIED_SQL, preparedStatement -> {
            preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()),
                    Calendar.getInstance(TimeZone.getTimeZone(UTC)));
            preparedStatement.setString(2, resourceId);
        });
    }

    @Override
    public List<ResourceFile> getFiles(String resourceId, String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_FILES_BY_RESOURCE_ID_SQL, ((resultSet, rowNumber) -> {
                String resourceFileId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                String resourceFileName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME);
                return new ResourceFile(resourceFileId, getFilePath(resourceFileId, resourceTypeName, resourceName),
                        resourceFileName);
            }), preparedStatement -> preparedStatement.setString(1, resourceId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_FILES, resourceName, e);
        }
    }

    @Override
    public List<ResourceFile> getFilesByResourceType(String resourceTypeId, int tenantId) throws ConfigurationManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_FILES_BY_RESOURCE_TYPE_ID_SQL,
                    ((resultSet, rowNumber) -> {
                        String resourceFileId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                        String resourceFileName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_FILE_NAME);
                        String resourceName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_NAME);
                        String resourceTypeName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE_NAME);
                        return new ResourceFile(
                                resourceFileId,
                                getFilePath(resourceFileId, resourceTypeName, resourceName),
                                resourceFileName
                        );
                    }),
                    preparedStatement -> {
                        preparedStatement.setString(1, resourceTypeId);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_FILES_BY_TYPE, resourceTypeId, e);
        }
    }

    @Override
    public void deleteFiles(String resourceId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            boolean isOracleOrMssql = isOracleDB() || isMSSqlDB();
            jdbcTemplate.withTransaction(template -> {

                template.executeUpdate(DELETE_FILES_SQL, (
                        preparedStatement -> preparedStatement.setString(1, resourceId)
                ));

                template.executeUpdate(UPDATE_HAS_FILE_SQL, preparedStatement -> {
                    if (isOracleOrMssql) {
                        preparedStatement.setInt(1, 0);
                    } else {
                        preparedStatement.setBoolean(1, false);
                    }
                    preparedStatement.setString(2, resourceId);
                });
                updateResourceLastModified(template, resourceId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_DELETE_FILES, resourceId, e);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    @Override
    public List<Resource> getResourcesByType(int tenantId, String resourceTypeId)
            throws ConfigurationManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String resourceTypeName = jdbcTemplate.fetchSingleRecord(SQLConstants.GET_RESOURCE_TYPE_BY_ID_SQL,
                    (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME),
                    preparedStatement -> preparedStatement.setString(1, resourceTypeId));
            return jdbcTemplate.executeQuery(GET_RESOURCES_BY_RESOURCE_TYPE_ID_SQL,
                    (LambdaExceptionUtils.rethrowRowMapper((resultSet, rowNumber) -> {
                        String resourceId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                        String resourceName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME);
                        String resourceLastModified = resultSet.getString(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED);
                        String resourceCreatedTime = resultSet.getString(DB_SCHEMA_COLUMN_NAME_CREATED_TIME);
                        String resourceHasFile = resultSet.getString(DB_SCHEMA_COLUMN_NAME_HAS_FILE);
                        if (StringUtils.equals(resourceHasFile, "1") && (isOracleDB() || isMSSqlDB() || isDB2DB())) {
                            resourceHasFile = "true";
                        }
                        String resourceHasAttribute = resultSet.getString(DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE);
                        Resource resource = new Resource();
                        resource.setCreatedTime(resourceCreatedTime);
                        resource.setHasAttribute(Boolean.valueOf(resourceHasAttribute));
                        resource.setResourceId(resourceId);
                        resource.setResourceName(resourceName);
                        resource.setLastModified(resourceLastModified);
                        resource.setHasFile(Boolean.valueOf(resourceHasFile));
                        resource.setTenantDomain(IdentityTenantUtil.getTenantDomain(tenantId));
                        resource.setFiles(getFiles(resourceId, resourceTypeName, resourceName));
                        resource.setAttributes(getAttributesByResourceId(resourceId));
                        return resource;
                    })),
                    preparedStatement -> {
                        preparedStatement.setString(1, resourceTypeId);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_RESOURCES_DOES_NOT_EXISTS, e);
        }
    }

    @Override
    public void deleteResourcesByType(int tenantId, String resourceTypeId) throws ConfigurationManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            if (isMySQLDB()) {
                /**
                 * Need to first delete files since in MySql Clustering scripts for NDB engine `ON DELETE CASCADE`is no
                 * longer supported for foreign keys on NDB tables when the child table contains a column that uses any
                 * of the BLOB or TEXT types. Issue: (https://github.com/wso2/product-is/issues/12167).
                 */
                List<Resource> resourceList = getResourcesByType(tenantId, resourceTypeId);
                if (!resourceList.isEmpty()) {
                    for (Resource resource : resourceList) {
                        deleteFiles(resource.getResourceId());
                    }
                }
            }
            jdbcTemplate.executeUpdate(SQLConstants.DELETE_RESOURCES_BY_RESOURCE_TYPE_ID_SQL, preparedStatement -> {
                int initialParameterIndex = 1;
                preparedStatement.setString(initialParameterIndex, resourceTypeId);
                preparedStatement.setInt(++initialParameterIndex, tenantId);
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_RESOURCES_DOES_NOT_EXISTS, e);
        }
    }

    /**
     * Get Attributes for the {@link Resource}.
     *
     * @param resourceId Id of the {@link Resource}.
     * @return A list of {@link Attribute} for the given resource.
     */
    private List<Attribute> getAttributesByResourceId(String resourceId)
            throws ConfigurationManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_ATTRIBUTES_BY_RESOURCE_ID_SQL,
                    ((resultSet, rowNumber) -> {
                        String attributeId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                        String attributeKey = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY);
                        String attributeValue = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE);
                        return new Attribute(
                                attributeKey,
                                attributeValue,
                                attributeId
                        );
                    }),
                    preparedStatement -> preparedStatement.setString(1, resourceId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_FILES_BY_TYPE, resourceId, e);
        }
    }

    private void insertResourceAttributes(Template<?> template, Resource resource) throws DataAccessException,
            ConfigurationManagementClientException {

        // Create sql query for attribute parameters.
        String attributesQuery = buildQueryForAttributes(resource);
        template.executeInsert(attributesQuery, preparedStatement -> {
            int attributeUpdateParameterIndex = 0;
            for (Attribute attribute : resource.getAttributes()) {
                preparedStatement.setString(++attributeUpdateParameterIndex, generateUniqueID());
                preparedStatement.setString(++attributeUpdateParameterIndex, resource.getResourceId());
                preparedStatement.setString(++attributeUpdateParameterIndex, attribute.getKey());
                preparedStatement.setString(++attributeUpdateParameterIndex, attribute.getValue());
            }
        }, resource, false);
    }

    private void insertResourceFile(Template<?> template, Resource resource, String fileId, String fileName,
                                    InputStream fileStream)
            throws ConfigurationManagementServerException {

        try {
            boolean isPostgreSQL = isPostgreSQLDB();
            String sqlStmt = isH2DB() ? SQLConstants.INSERT_FILE_SQL_H2 : SQLConstants.INSERT_FILE_SQL;

            template.executeUpdate(sqlStmt, preparedStatement -> {
                preparedStatement.setString(1, fileId);
                if (isPostgreSQL) {
                    preparedStatement.setBinaryStream(2, fileStream);
                } else {
                    preparedStatement.setBlob(2, fileStream);
                }
                preparedStatement.setString(3, resource.getResourceId());
                preparedStatement.setString(4, fileName);
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_DB_METADATA, e.getMessage(), e);
        }
    }

    private void setPreparedStatementForFileGetById(String resourceType, String resourceName, String fileId,
                                                    PreparedStatement preparedStatement) throws SQLException {

        preparedStatement.setString(1, fileId);
        preparedStatement.setString(2, resourceName);
        preparedStatement.setString(3, resourceType);
    }

    private String getFileGetByIdSQL() throws DataAccessException{

        return isH2DB() ? SQLConstants.GET_FILE_BY_ID_SQL_H2 : SQLConstants.GET_FILE_BY_ID_SQL;
    }
}
