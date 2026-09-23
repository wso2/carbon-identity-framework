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

package org.wso2.carbon.identity.device.registration.internal.util;

import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;

/**
 * Utility class for constructing device registration exceptions with the standard
 * (message, description, code[, cause]) shape backed by ErrorMessage entries.
 */
public class DeviceRegistrationExceptionHandler {

    private DeviceRegistrationExceptionHandler() {

    }

    /**
     * Builds a client exception from the given error.
     *
     * @param error Error message entry.
     * @param data  Optional values to format into the description.
     * @return Device registration client exception.
     */
    public static DeviceRegistrationException handleClientException(ErrorMessage error, String... data) {

        return new DeviceRegistrationException(DeviceRegistrationException.ErrorType.CLIENT, error.getMessage(),
                formatDescription(error, data), error.getCode());
    }

    /**
     * Builds a client exception from the given error and root cause.
     *
     * @param error Error message entry.
     * @param e     Root cause.
     * @param data  Optional values to format into the description.
     * @return Device registration client exception.
     */
    public static DeviceRegistrationException handleClientException(ErrorMessage error, Throwable e,
                                                                      String... data) {

        return new DeviceRegistrationException(DeviceRegistrationException.ErrorType.CLIENT, error.getMessage(),
                formatDescription(error, data), error.getCode(), e);
    }

    /**
     * Builds a server exception from the given error and root cause.
     *
     * @param error Error message entry.
     * @param e     Root cause.
     * @param data  Optional values to format into the description.
     * @return Device registration server exception.
     */
    public static DeviceRegistrationException handleServerException(ErrorMessage error, Throwable e,
                                                                      String... data) {

        return new DeviceRegistrationException(DeviceRegistrationException.ErrorType.SERVER, error.getMessage(),
                formatDescription(error, data), error.getCode(), e);
    }

    /**
     * Builds a server exception from the given error.
     *
     * @param error Error message entry.
     * @param data  Optional values to format into the description.
     * @return Device registration server exception.
     */
    public static DeviceRegistrationException handleServerException(ErrorMessage error, String... data) {

        return new DeviceRegistrationException(DeviceRegistrationException.ErrorType.SERVER, error.getMessage(),
                formatDescription(error, data), error.getCode());
    }

    private static String formatDescription(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (data != null && data.length > 0) {
            description = String.format(description, (Object[]) data);
        }
        return description;
    }
}
