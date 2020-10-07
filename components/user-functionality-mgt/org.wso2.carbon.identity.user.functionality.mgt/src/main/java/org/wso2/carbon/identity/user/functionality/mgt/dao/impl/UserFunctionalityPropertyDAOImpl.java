/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.functionality.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.configuration.mgt.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityMgtConstants;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityPropertyDAO;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * User functionality property DAO implementation.
 */
public class UserFunctionalityPropertyDAOImpl implements UserFunctionalityPropertyDAO {

    private static final Log log = LogFactory.getLog(UserFunctionalityPropertyDAOImpl.class.getName());
    private static String TABLE_NAME;

    private static String getOracleTableName() throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord(UserFunctionalityMgtConstants.SqlQueries.GET_ORACLE_TABLE_NAME,
                    ((resultSet, i) -> resultSet.getString("TABLE_NAME")), preparedStatement -> {

                    });
        } catch (DataAccessException e) {
            String message = "Error occurred while retrieving db table name from user_tables.";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
    }

    /**
     * Check whether the string, "oracle", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws DataAccessException If error occurred while checking the DB metadata.
     */
    private static boolean isOracleDB() throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        return jdbcTemplate.getDriverName().toLowerCase().contains(UserFunctionalityMgtConstants.ORACLE) ||
                jdbcTemplate.getDatabaseProductName().toLowerCase().contains(UserFunctionalityMgtConstants.ORACLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperties(String userId, int tenantId, String functionalityIdentifier,
                              Map<String, String> propertiesToAdd)
            throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        for (Map.Entry<String, String> entry : propertiesToAdd.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            try {
                String query;
                if (isOracleDB()) {
                    if (StringUtils.isEmpty(TABLE_NAME)) {
                        TABLE_NAME = getOracleTableName();
                    }
                    query = String.format(UserFunctionalityMgtConstants.SqlQueries.INSERT_PROPERTY_ORACLE, TABLE_NAME);
                } else {
                    query = UserFunctionalityMgtConstants.SqlQueries.INSERT_PROPERTY;
                }
                jdbcTemplate
                        .executeUpdate(query, preparedStatement -> {
                            preparedStatement.setString(1, UUID.randomUUID().toString());
                            preparedStatement.setString(2, userId);
                            preparedStatement.setInt(3, tenantId);
                            preparedStatement.setString(4, functionalityIdentifier);
                            preparedStatement.setString(5, propertyName);
                            preparedStatement.setString(6, propertyValue);
                        });
            } catch (DataAccessException e) {
                String message =
                        String.format(
                                "Error occurred while adding the property: %s for functionality: %s in user: %s," +
                                        " tenant id: %d", propertyName, functionalityIdentifier, userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFunctionalityManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllProperties(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        Map<String, String> properties = new HashMap<String, String>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            String query;
            if (isOracleDB()) {
                if (StringUtils.isEmpty(TABLE_NAME)) {
                    TABLE_NAME = getOracleTableName();
                }
                query = String.format(UserFunctionalityMgtConstants.SqlQueries.GET_ALL_PROPERTIES_ORACLE, TABLE_NAME);
            } else {
                query = UserFunctionalityMgtConstants.SqlQueries.GET_ALL_PROPERTIES;
            }
            jdbcTemplate
                    .executeQuery(query, (resultSet, rowNumber) ->
                                    properties.put(resultSet.getString(1), resultSet.getString(2)),
                            preparedStatement -> {
                                preparedStatement.setString(1, userId);
                                preparedStatement.setInt(2, tenantId);
                                preparedStatement.setString(3, functionalityIdentifier);
                            });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while retrieving functionality lock properties from DB " +
                            "for user Id: %s, tenant Id: %d and functionality id: %s.", userId, tenantId,
                    functionalityIdentifier);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(String userId, int tenantId, String functionalityIdentifier,
                                 Map<String, String> propertiesToUpdate)
            throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        for (Map.Entry<String, String> entry : propertiesToUpdate.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            try {
                String query;
                if (isOracleDB()) {
                    if (StringUtils.isEmpty(TABLE_NAME)) {
                        TABLE_NAME = getOracleTableName();
                    }
                    query = String.format(UserFunctionalityMgtConstants.SqlQueries.UPDATE_PROPERTY_VALUE_ORACLE,
                            TABLE_NAME);
                } else {
                    query = UserFunctionalityMgtConstants.SqlQueries.UPDATE_PROPERTY_VALUE;
                }
                jdbcTemplate
                        .executeUpdate(query, (preparedStatement -> {
                                    preparedStatement.setString(1, propertyValue);
                                    preparedStatement.setString(2, userId);
                                    preparedStatement.setInt(3, tenantId);
                                    preparedStatement.setString(4, functionalityIdentifier);
                                    preparedStatement.setString(5, propertyName);
                                }));
            } catch (DataAccessException e) {
                String message =
                        String.format(
                                "Error occurred while updating the functionality lock property: %s for functionality " +
                                        "Id: %s, user Id: %s and tenantId: %d.", propertyName, functionalityIdentifier,
                                userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFunctionalityManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePropertiesForUser(String userId, int tenantId, String functionalityIdentifier,
                                        Set<String> propertiesToDelete)
            throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        for (String propertyName : propertiesToDelete) {
            try {
                String query;
                if (isOracleDB()) {
                    if (StringUtils.isEmpty(TABLE_NAME)) {
                        TABLE_NAME = getOracleTableName();
                    }
                    query = String.format(UserFunctionalityMgtConstants.SqlQueries.DELETE_PROPERTY_ORACLE, TABLE_NAME);
                } else {
                    query = UserFunctionalityMgtConstants.SqlQueries.DELETE_PROPERTY;
                }
                jdbcTemplate
                        .executeUpdate(query, preparedStatement -> {
                            preparedStatement.setString(1, userId);
                            preparedStatement.setInt(2, tenantId);
                            preparedStatement.setString(3, functionalityIdentifier);
                            preparedStatement.setString(4, propertyName);
                        });
            } catch (DataAccessException e) {
                String message =
                        String.format(
                                "Error occurred while deleting functionality lock property from DB for functionality " +
                                        "Id: %s, property: %s, user Id: %s and tenant Id: %d.", functionalityIdentifier,
                                propertyName,
                                userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFunctionalityManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllPropertiesForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String query;
            if (isOracleDB()) {
                if (StringUtils.isEmpty(TABLE_NAME)) {
                    TABLE_NAME = getOracleTableName();
                }
                query = String.format(UserFunctionalityMgtConstants.SqlQueries.DELETE_ALL_PROPERTIES_FOR_MAPPING_ORACLE,
                        TABLE_NAME);
            } else {
                query = UserFunctionalityMgtConstants.SqlQueries.DELETE_ALL_PROPERTIES_FOR_MAPPING;
            }
            jdbcTemplate.executeUpdate(query, preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, functionalityIdentifier);

            });
        } catch (DataAccessException e) {
            String message = String.format(
                    "Error occurred while deleting functionality lock properties from DB for functionality" +
                            " Id: %s, user Id: %s and tenant Id: %d.", functionalityIdentifier, userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllPropertiesForTenant(int tenantId) throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String query;
            if (isOracleDB()) {
                if (StringUtils.isEmpty(TABLE_NAME)) {
                    TABLE_NAME = getOracleTableName();
                }
                query = String.format(UserFunctionalityMgtConstants.SqlQueries.DELETE_ALL_PROPERTIES_FOR_TENANT_ORACLE,
                        TABLE_NAME);
            } else {
                query = UserFunctionalityMgtConstants.SqlQueries.DELETE_ALL_PROPERTIES_FOR_TENANT;
            }
            jdbcTemplate.executeUpdate(query, preparedStatement -> {
                preparedStatement.setInt(1, tenantId);
            });
        } catch (DataAccessException e) {
            String message = String.format(
                    "Error occurred while deleting functionality lock properties from DB for tenant" +
                            " Id: %d.", tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
    }
}
