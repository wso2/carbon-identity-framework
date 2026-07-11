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

package org.wso2.carbon.identity.device.mgt.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.internal.constant.DeviceMgtSQLConstants;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * JDBC implementation for registered device persistence.
 */
public class DeviceManagementDAOImpl implements DeviceManagementDAO {

    private static final Log LOG = LogFactory.getLog(DeviceManagementDAOImpl.class);

    @Override
    public Device registerDevice(Device device, int tenantId)
            throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeInsert(
                        DeviceMgtSQLConstants.Query.REGISTER_DEVICE,
                        preparedStatement -> {
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.ID, device.getId());
                            preparedStatement.setString(
                                    DeviceMgtSQLConstants.Column.DEVICE_NAME, device.getDeviceName());
                            preparedStatement.setString(
                                    DeviceMgtSQLConstants.Column.DEVICE_MODEL, device.getDeviceModel());
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.PUBLIC_KEY, device.getPublicKey());
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.STATUS, device.getStatus());
                            preparedStatement.setObject(
                                    DeviceMgtSQLConstants.Column.REGISTERED_AT, device.getRegisteredAt());
                            preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.METADATA, device.getMetadata());
                        },
                        device,
                        false);
                template.executeInsert(
                        DeviceMgtSQLConstants.Query.ADD_USER_DEVICE,
                        preparedStatement -> {
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.DEVICE_ID, device.getId());
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.USER_ID, device.getUserId());
                            preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                        },
                        device,
                        false);
                return null;
            });

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_REGISTERING_DEVICE, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device registered successfully with ID: " + device.getId());
        }
        return device;
    }

    @Override
    public Device getDeviceById(String deviceId, int tenantId)
            throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            return jdbcTemplate.<Device, RuntimeException>withTransaction(
                    template -> template.fetchSingleRecord(
                            DeviceMgtSQLConstants.Query.GET_DEVICE_BY_ID,
                            (resultSet, rowNumber) -> new Device.Builder()
                                    .id(resultSet.getString(DeviceMgtSQLConstants.Column.ID))
                                    .userId(resultSet.getString(DeviceMgtSQLConstants.Column.USER_ID))
                                    .deviceName(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_NAME))
                                    .deviceModel(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_MODEL))
                                    .publicKey(resultSet.getString(DeviceMgtSQLConstants.Column.PUBLIC_KEY))
                                    .status(resultSet.getString(DeviceMgtSQLConstants.Column.STATUS))
                                    .registeredAt(resultSet.getTimestamp(DeviceMgtSQLConstants.Column.REGISTERED_AT))
                                    .metadata(resultSet.getString(DeviceMgtSQLConstants.Column.METADATA))
                                    .build(),
                            preparedStatement -> {
                                preparedStatement.setString(DeviceMgtSQLConstants.Column.ID, deviceId);
                                preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                            }));

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_DEVICE, e);
        }
    }

    @Override
    public List<Device> getDevicesByUserId(String userId, int tenantId)
            throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            return jdbcTemplate.<List<Device>, RuntimeException>withTransaction(
                    template -> template.executeQuery(
                            DeviceMgtSQLConstants.Query.GET_DEVICES_BY_USER_ID,
                            (resultSet, rowNumber) -> new Device.Builder()
                                    .id(resultSet.getString(DeviceMgtSQLConstants.Column.ID))
                                    .userId(resultSet.getString(DeviceMgtSQLConstants.Column.USER_ID))
                                    .deviceName(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_NAME))
                                    .deviceModel(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_MODEL))
                                    .publicKey(resultSet.getString(DeviceMgtSQLConstants.Column.PUBLIC_KEY))
                                    .status(resultSet.getString(DeviceMgtSQLConstants.Column.STATUS))
                                    .registeredAt(resultSet.getTimestamp(DeviceMgtSQLConstants.Column.REGISTERED_AT))
                                    .metadata(resultSet.getString(DeviceMgtSQLConstants.Column.METADATA))
                                    .build(),
                            preparedStatement -> {
                                preparedStatement.setString(DeviceMgtSQLConstants.Column.USER_ID, userId);
                                preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                            }));

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_DEVICE, e);
        }
    }

    @Override
    public List<Device> getAllDevices(int tenantId) throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            return jdbcTemplate.<List<Device>, RuntimeException>withTransaction(
                    template -> template.executeQuery(
                            DeviceMgtSQLConstants.Query.GET_ALL_DEVICES,
                            (resultSet, rowNumber) -> new Device.Builder()
                                    .id(resultSet.getString(DeviceMgtSQLConstants.Column.ID))
                                    .userId(resultSet.getString(DeviceMgtSQLConstants.Column.USER_ID))
                                    .deviceName(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_NAME))
                                    .deviceModel(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_MODEL))
                                    .publicKey(resultSet.getString(DeviceMgtSQLConstants.Column.PUBLIC_KEY))
                                    .status(resultSet.getString(DeviceMgtSQLConstants.Column.STATUS))
                                    .registeredAt(resultSet.getTimestamp(DeviceMgtSQLConstants.Column.REGISTERED_AT))
                                    .metadata(resultSet.getString(DeviceMgtSQLConstants.Column.METADATA))
                                    .build(),
                            preparedStatement -> preparedStatement.setInt(
                                    DeviceMgtSQLConstants.Column.TENANT_ID, tenantId)));

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_DEVICE, e);
        }
    }

    @Override
    public List<Device> getDevices(int tenantId, int offset, int limit) throws DeviceMgtException {

        // FETCH NEXT 0 ROWS (MS SQL) is invalid and an empty page is meaningless, so short-circuit.
        if (limit <= 0) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(offset, 0);

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            PaginationStyle style = resolvePaginationStyle();
            String query = resolvePaginatedQuery(style);
            List<Device> devices = jdbcTemplate.<List<Device>, RuntimeException>withTransaction(
                    template -> template.executeQuery(
                            query,
                            (resultSet, rowNumber) -> new Device.Builder()
                                    .id(resultSet.getString(DeviceMgtSQLConstants.Column.ID))
                                    .userId(resultSet.getString(DeviceMgtSQLConstants.Column.USER_ID))
                                    .deviceName(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_NAME))
                                    .deviceModel(resultSet.getString(DeviceMgtSQLConstants.Column.DEVICE_MODEL))
                                    .publicKey(resultSet.getString(DeviceMgtSQLConstants.Column.PUBLIC_KEY))
                                    .status(resultSet.getString(DeviceMgtSQLConstants.Column.STATUS))
                                    .registeredAt(resultSet.getTimestamp(DeviceMgtSQLConstants.Column.REGISTERED_AT))
                                    .metadata(resultSet.getString(DeviceMgtSQLConstants.Column.METADATA))
                                    .build(),
                            preparedStatement -> {
                                preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                                bindPaginationParams(preparedStatement, style, safeOffset, limit);
                            }));
            return devices != null ? devices : Collections.emptyList();
        } catch (TransactionException | DataAccessException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_DEVICE, e);
        }
    }

    @Override
    public int getDeviceCount(int tenantId) throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Integer count = jdbcTemplate.<Integer, RuntimeException>withTransaction(
                    template -> template.fetchSingleRecord(
                            DeviceMgtSQLConstants.Query.GET_DEVICES_COUNT,
                            (resultSet, rowNumber) -> resultSet.getInt(1),
                            preparedStatement -> preparedStatement.setInt(
                                    DeviceMgtSQLConstants.Column.TENANT_ID, tenantId)));
            return count != null ? count : 0;
        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_DEVICE, e);
        }
    }

    /**
     * Supported database-specific pagination dialects.
     */
    private enum PaginationStyle {
        DEFAULT, MSSQL, ORACLE, DB2
    }

    private PaginationStyle resolvePaginationStyle() throws DataAccessException {

        // H2, MySQL, MariaDB and PostgreSQL all accept the LIMIT ... OFFSET ... syntax (DEFAULT).
        if (JdbcUtils.isOracleDB()) {
            return PaginationStyle.ORACLE;
        }
        if (JdbcUtils.isDB2DB()) {
            return PaginationStyle.DB2;
        }
        if (JdbcUtils.isMSSqlDB()) {
            return PaginationStyle.MSSQL;
        }
        return PaginationStyle.DEFAULT;
    }

    private String resolvePaginatedQuery(PaginationStyle style) {

        switch (style) {
            case ORACLE:
                return DeviceMgtSQLConstants.Query.GET_ALL_DEVICES_PAGINATED_ORACLE;
            case DB2:
                return DeviceMgtSQLConstants.Query.GET_ALL_DEVICES_PAGINATED_DB2;
            case MSSQL:
                return DeviceMgtSQLConstants.Query.GET_ALL_DEVICES_PAGINATED_MSSQL;
            default:
                return DeviceMgtSQLConstants.Query.GET_ALL_DEVICES_PAGINATED;
        }
    }

    private void bindPaginationParams(NamedPreparedStatement preparedStatement, PaginationStyle style,
                                      int offset, int limit) throws SQLException {

        switch (style) {
            case ORACLE:
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.UPPER_BOUND, offset + limit);
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.OFFSET, offset);
                break;
            case DB2:
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.LOWER_BOUND, offset + 1);
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.UPPER_BOUND, offset + limit);
                break;
            case MSSQL:
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.OFFSET, offset);
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.LIMIT, limit);
                break;
            default:
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.LIMIT, limit);
                preparedStatement.setInt(DeviceMgtSQLConstants.Column.OFFSET, offset);
        }
    }

    @Override
    public Device updateDeviceName(String deviceId, String deviceName, int tenantId)
            throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeUpdate(
                        DeviceMgtSQLConstants.Query.UPDATE_DEVICE_NAME,
                        preparedStatement -> {
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.DEVICE_NAME, deviceName);
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.ID, deviceId);
                            preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_UPDATING_DEVICE, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device name updated for device ID: " + deviceId);
        }

        return getDeviceById(deviceId, tenantId);
    }

    @Override
    public void deleteDevice(String deviceId, int tenantId)
            throws DeviceMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeUpdate(
                        DeviceMgtSQLConstants.Query.DELETE_USER_DEVICE,
                        preparedStatement -> {
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.DEVICE_ID, deviceId);
                            preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                template.executeUpdate(
                        DeviceMgtSQLConstants.Query.DELETE_DEVICE,
                        preparedStatement -> {
                            preparedStatement.setString(DeviceMgtSQLConstants.Column.ID, deviceId);
                            preparedStatement.setInt(DeviceMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });

        } catch (TransactionException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_DELETING_DEVICE, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deleted with ID: " + deviceId);
        }
    }
}
