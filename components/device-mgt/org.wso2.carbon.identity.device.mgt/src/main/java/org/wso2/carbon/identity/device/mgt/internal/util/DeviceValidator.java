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

package org.wso2.carbon.identity.device.mgt.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;

/**
 * Device validator class.
 */
public class DeviceValidator {

    private static final Log LOG = LogFactory.getLog(DeviceValidator.class);

    /**
     * Validates that a required field is not blank.
     *
     * @param value     Value of the field.
     * @param fieldName Name of the field.
     * @throws DeviceMgtClientException If the field is not set.
     */
    public void validateRequiredField(String value, String fieldName)
            throws DeviceMgtClientException {

        if (StringUtils.isBlank(value)) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, fieldName);
        }
    }

    /**
     * Validates that the device carries every field the registration layer requires.
     * The device is constructed internally during the registration flow, so a missing field indicates
     * that the device was not fully built before registration rather than invalid user input. Validating
     * here keeps such a failure a clear, coded error instead of a constraint violation or a
     * NullPointerException raised inside the data layer.
     * The device model and the metadata are optional and are therefore not validated.
     *
     * @param device Device to be registered.
     * @throws DeviceMgtException If the device or any of its required fields is not set.
     */
    public void validateDeviceForRegistration(Device device) throws DeviceMgtException {

        if (device == null) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED, "device");
        }
        if (StringUtils.isBlank(device.getUserId())) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_USER_ID_REQUIRED);
        }

        validateRequiredDeviceField(device.getId(), "id");
        validateRequiredDeviceField(device.getDeviceName(), "deviceName");
        validateRequiredDeviceField(device.getPublicKey(), "publicKey");

        if (device.getStatus() == null) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED, "status");
        }
        if (device.getRegisteredAt() == null) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED, "registeredAt");
        }
    }

    /**
     * Validates a required device field that is expected to be set before registration.
     *
     * @param value     Value of the field.
     * @param fieldName Name of the field.
     * @throws DeviceMgtException If the field is not set.
     */
    public void validateRequiredDeviceField(String value, String fieldName) throws DeviceMgtException {

        if (StringUtils.isBlank(value)) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED, fieldName);
        }
    }

    /**
     * Validates the requested page size against the maximum items per page configured for the server.
     * A page size larger than the configured maximum is capped, so that a caller cannot load an
     * unbounded number of devices into memory.
     *
     * @param limit Requested page size.
     * @return The page size to use, capped at the configured maximum.
     * @throws DeviceMgtClientException If the requested page size is negative.
     */
    public int validateLimit(int limit) throws DeviceMgtClientException {

        if (limit < 0) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, "limit");
        }

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit > maximumItemsPerPage) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Given limit: " + limit + " exceeds the maximum items per page. Using the maximum: "
                        + maximumItemsPerPage);
            }
            return maximumItemsPerPage;
        }
        return limit;
    }

    /**
     * Validates that a device looked up by its identifier exists.
     *
     * @param device   Device previously looked up by {@code deviceId}, or {@code null} if not found.
     * @param deviceId UUID of the device, used only for the error message.
     * @throws DeviceMgtException If the device does not exist.
     */
    public void validateDeviceExists(Device device, String deviceId) throws DeviceMgtException {

        if (device == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }
    }
}
