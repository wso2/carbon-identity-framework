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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.configuration.mgt.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityMgtConstants;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityManagerDAO;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * User functionality manager DAO implementation.
 */
public class UserFunctionalityManagerDAOImpl implements UserFunctionalityManagerDAO {

    private static final Log log = LogFactory.getLog(UserFunctionalityManagerDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFunctionalityLock(String userId, int tenantId, String functionalityIdentifier,
                                     FunctionalityLockStatus functionalityLockStatus) throws
            UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        final String uuid = UUID.randomUUID().toString();
        try {
            jdbcTemplate.executeUpdate(UserFunctionalityMgtConstants.SqlQueries.INSERT_FUNCTIONALITY_MAPPING,
                    preparedStatement -> {
                        preparedStatement.setString(1, uuid);
                        preparedStatement.setString(2, userId);
                        preparedStatement.setInt(3, tenantId);
                        preparedStatement.setString(4, functionalityIdentifier);
                        preparedStatement.setBoolean(5, functionalityLockStatus.getLockStatus());
                        preparedStatement.setLong(6, functionalityLockStatus.getUnlockTime());
                        preparedStatement.setString(7, functionalityLockStatus.getLockReason());
                        preparedStatement.setString(8, functionalityLockStatus.getLockReasonCode());
                    });
        } catch (DataAccessException e) {
            String message =
                    String.format("Error occurred while adding the functionality: %s, for user: %s, for tenant id: " +
                                    "%d, having the parameters, functionality lock status: %b, functionality unlock " +
                                    "time: %d, functionality lock reason code: %s, functionality lock reason: %s.",
                            functionalityIdentifier, userId, tenantId, functionalityLockStatus.getLockStatus(),
                            functionalityLockStatus.getUnlockTime(), functionalityLockStatus.getLockReasonCode(),
                            functionalityLockStatus.getLockReason());
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
    public FunctionalityLockStatus getFunctionalityLockStatus(String userId, int tenantId,
                                                              String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        FunctionalityLockStatus functionalityLockStatus;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            functionalityLockStatus =
                    jdbcTemplate
                            .fetchSingleRecord(UserFunctionalityMgtConstants.SqlQueries.GET_FUNCTIONALITY_LOCK_STATUS,
                                    ((resultSet, i) -> new FunctionalityLockStatus(
                                            resultSet.getBoolean("IS_FUNCTIONALITY_LOCKED"),
                                            resultSet.getLong("FUNCTIONALITY_UNLOCK_TIME"),
                                            resultSet.getString("FUNCTIONALITY_LOCK_REASON_CODE"),
                                            resultSet.getString("FUNCTIONALITY_LOCK_REASON"))),
                                    preparedStatement -> {
                                        preparedStatement.setString(1, userId);
                                        preparedStatement.setInt(2, tenantId);
                                        preparedStatement.setString(3, functionalityIdentifier);
                                    });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while retrieving functionality lock status from DB for " +
                            "functionality identifier: %s, user Id: %s and tenant Id: %d.", functionalityIdentifier,
                    userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
        return functionalityLockStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLockStatusForUser(String userId, int tenantId, String functionalityIdentifier,
                                        FunctionalityLockStatus functionalityLockStatus) throws
            UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UserFunctionalityMgtConstants.SqlQueries.UPDATE_FUNCTIONALITY_MAPPING,
                    (preparedStatement -> {
                        setPreparedStatementForFunctionality(userId, tenantId, functionalityIdentifier,
                                functionalityLockStatus, preparedStatement);
                        preparedStatement.setString(8, userId);
                        preparedStatement.setInt(9, tenantId);
                        preparedStatement.setString(10, functionalityIdentifier);
                    }));
        } catch (DataAccessException e) {
            String message =
                    String.format("Error occurred while updating the functionality: %s for user Id: %s and tenant " +
                            "Id: %d.", functionalityIdentifier, userId, tenantId);
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
    public void deleteMappingForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UserFunctionalityMgtConstants.SqlQueries.DELETE_FUNCTIONALITY_MAPPING,
                    preparedStatement -> {
                        preparedStatement.setString(1, userId);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, functionalityIdentifier);
                    });
        } catch (DataAccessException e) {
            String message = String.format(
                    "Error occurred while deleting functionality from DB for functionality Id: %s, user " +
                            "Id: %s and tenant Id: %d.", functionalityIdentifier, userId, tenantId);
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
    public void deleteAllMappingsForTenant(int tenantId) throws UserFunctionalityManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(
                    UserFunctionalityMgtConstants.SqlQueries.DELETE_ALL_FUNCTIONALITY_MAPPINGS_FOR_TENANT,
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                    });
        } catch (DataAccessException e) {
            String message = String.format(
                    "Error occurred while deleting mappings from DB for tenant Id: %d.", tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFunctionalityManagementServerException(message, e);
        }
    }

    private void setPreparedStatementForFunctionality(String userId, int tenantId, String functionalityIdentifier,
                                                      FunctionalityLockStatus functionalityLockStatus,
                                                      PreparedStatement preparedStatement) throws SQLException {

        preparedStatement.setString(1, userId);
        preparedStatement.setInt(2, tenantId);
        preparedStatement.setString(3, functionalityIdentifier);
        preparedStatement.setBoolean(4, functionalityLockStatus.getLockStatus());
        preparedStatement.setLong(5, functionalityLockStatus.getUnlockTime());
        preparedStatement.setString(6, functionalityLockStatus.getLockReason());
        preparedStatement.setString(7, functionalityLockStatus.getLockReasonCode());
    }
}
