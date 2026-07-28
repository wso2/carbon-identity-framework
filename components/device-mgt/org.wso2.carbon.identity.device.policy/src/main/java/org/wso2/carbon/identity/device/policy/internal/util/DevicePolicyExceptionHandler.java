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

package org.wso2.carbon.identity.device.policy.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

/**
 * Utility class for constructing policy management exceptions from device-policy error messages.
 */
public class DevicePolicyExceptionHandler {

    private DevicePolicyExceptionHandler() {

    }

    /**
     * Builds a client exception from a device policy error message.
     */
    public static PolicyManagementClientException handleClientException(DevicePolicyErrorMessage error,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new PolicyManagementClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Builds a client exception with a cause from a device policy error message.
     */
    public static PolicyManagementClientException handleClientException(DevicePolicyErrorMessage error, Throwable e,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new PolicyManagementClientException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Builds a server exception from a device policy error message.
     */
    public static PolicyManagementServerException handleServerException(DevicePolicyErrorMessage error,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new PolicyManagementServerException(error.getMessage(), description, error.getCode());
    }

    /**
     * Builds a server exception with a cause from a device policy error message.
     */
    public static PolicyManagementServerException handleServerException(DevicePolicyErrorMessage error, Throwable e,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new PolicyManagementServerException(error.getMessage(), description, error.getCode(), e);
    }
}
