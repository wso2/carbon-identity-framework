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
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;

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
     *
     * @param device Device to be registered.
     * @throws DeviceMgtException If the device is not set.
     */
    public void validateDeviceForRegistration(Device device) throws DeviceMgtException {

        if (device == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, "device");
        }
    }

    /**
     * Validates the device association for registration.
     *
     * @param association Device association to be validated.
     * @throws DeviceMgtException If the association is invalid or missing required fields.
     */
    public void validateAssociation(DeviceAssociation association) throws DeviceMgtException {

        if (association == null || !(association instanceof UserDeviceAssociation)) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_ASSOCIATION);
        }
        UserDeviceAssociation userDeviceAssociation = (UserDeviceAssociation) association;
        if (StringUtils.isBlank(userDeviceAssociation.getUserId())) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_ASSOCIATION_FIELD_REQUIRED, "userId");
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
