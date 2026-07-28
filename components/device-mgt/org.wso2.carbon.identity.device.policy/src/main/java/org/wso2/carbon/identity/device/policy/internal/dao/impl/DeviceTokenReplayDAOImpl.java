/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.internal.constant.DevicePolicySQLConstants;
import org.wso2.carbon.identity.device.policy.internal.dao.DeviceTokenReplayDAO;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

import java.sql.Timestamp;

/**
 * JDBC implementation of the device token replay store.
 */
public class DeviceTokenReplayDAOImpl implements DeviceTokenReplayDAO {

    private static final Log LOG = LogFactory.getLog(DeviceTokenReplayDAOImpl.class);

    @Override
    public boolean isTokenReplayed(String jti, int tenantId) throws PolicyManagementServerException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            Integer result = jdbcTemplate.<Integer, RuntimeException>withTransaction(
                    template -> template.fetchSingleRecord(
                            DevicePolicySQLConstants.Query.IS_TOKEN_REPLAYED,
                            (resultSet, rowNumber) -> resultSet.getInt(1),
                            preparedStatement -> {
                                preparedStatement.setString(DevicePolicySQLConstants.Column.JTI, jti);
                                preparedStatement.setInt(DevicePolicySQLConstants.Column.TENANT_ID, tenantId);
                            }));
            return result != null;

        } catch (TransactionException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAY_CHECK_FAILED, e);
        }
    }

    @Override
    public void storeToken(String jti, int tenantId, Timestamp issuedAt, Timestamp expiryTime)
            throws PolicyManagementServerException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeInsert(
                        DevicePolicySQLConstants.Query.STORE_TOKEN_JTI,
                        preparedStatement -> {
                            preparedStatement.setString(DevicePolicySQLConstants.Column.JTI, jti);
                            preparedStatement.setInt(DevicePolicySQLConstants.Column.TENANT_ID, tenantId);
                            preparedStatement.setObject(DevicePolicySQLConstants.Column.IAT, issuedAt);
                            preparedStatement.setObject(DevicePolicySQLConstants.Column.EXPIRY_TIME, expiryTime);
                        },
                        null,
                        false);
                return null;
            });

        } catch (TransactionException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAY_STORE_FAILED, e);
        }
    }

    @Override
    public void removeExpiredTokens(Timestamp cutoff) throws PolicyManagementServerException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeUpdate(
                        DevicePolicySQLConstants.Query.DELETE_EXPIRED_TOKENS,
                        preparedStatement -> preparedStatement.setObject(
                                DevicePolicySQLConstants.Column.EXPIRY_TIME, cutoff));
                return null;
            });

        } catch (TransactionException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAY_CLEANUP_FAILED, e);
        }
    }
}
