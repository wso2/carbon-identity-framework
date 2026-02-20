/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;

/**
 * Utility class for Debug Framework exception handling.
 * Provides methods to create exceptions from ErrorMessages enum.
 */
public class DebugFrameworkUtils {

    private DebugFrameworkUtils() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Handle the debug framework client exceptions.
     *
     * @param error Error message enum.
     * @param data  The error message data for formatting description.
     * @return DebugFrameworkClientException.
     */
    public static DebugFrameworkClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new DebugFrameworkClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the debug framework client exceptions with cause.
     *
     * @param error Error message enum.
     * @param e     Throwable cause.
     * @param data  The error message data for formatting description.
     * @return DebugFrameworkClientException.
     */
    public static DebugFrameworkClientException handleClientException(ErrorMessages error, Throwable e,
                                                                      Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new DebugFrameworkClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the debug framework server exceptions.
     *
     * @param error Error message enum.
     * @param data  The error message data for formatting description.
     * @return DebugFrameworkServerException.
     */
    public static DebugFrameworkServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new DebugFrameworkServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the debug framework server exceptions with cause.
     *
     * @param error Error message enum.
     * @param e     Throwable cause.
     * @param data  The error message data for formatting description.
     * @return DebugFrameworkServerException.
     */
    public static DebugFrameworkServerException handleServerException(ErrorMessages error, Throwable e,
                                                                      Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new DebugFrameworkServerException(error.getCode(), error.getMessage(), description, e);
    }
}

