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

package org.wso2.carbon.identity.policy.management.api.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

/**
 * Utility class for constructing policy management exceptions with the standard
 * (message, description, code[, cause]) shape backed by ErrorMessage entries.
 */
public class PolicyManagementExceptionHandler {

    private PolicyManagementExceptionHandler() {

    }

    public static PolicyManagementClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new PolicyManagementClientException(error.getMessage(), description, error.getCode());
    }

    public static PolicyManagementClientException handleClientException(ErrorMessage error, Throwable e,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new PolicyManagementClientException(error.getMessage(), description, error.getCode(), e);
    }

    public static PolicyManagementServerException handleServerException(ErrorMessage error, Throwable e,
                                                                        String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new PolicyManagementServerException(error.getMessage(), description, error.getCode(), e);
    }

    public static PolicyManagementServerException handleServerException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new PolicyManagementServerException(error.getMessage(), description, error.getCode());
    }
}
