/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.utils;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;

public class RegistrationMgtUtils {

    private RegistrationMgtUtils() {

    }

    /**
     * Handle the registration flow management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow management server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the registration flow management client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow management client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description);
    }
}
