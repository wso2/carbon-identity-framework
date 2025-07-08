/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.subscription.management.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementClientException;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementServerException;
import org.wso2.carbon.identity.subscription.management.internal.constant.ErrorMessage;

/**
 * Utility class for handling exceptions in the subscription management module.
 * This class provides methods to create client and server exceptions with
 * appropriate error codes, messages, and descriptions.
 */
public class SubscriptionManagementExceptionHandler {

    private SubscriptionManagementExceptionHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Handle Subscription Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return SubscriptionManagementClientException.
     */
    public static SubscriptionManagementClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new SubscriptionManagementClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle Subscription Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be formatted into the error description.
     * @return SubscriptionManagementServerException.
     */
    public static SubscriptionManagementServerException handleServerException(ErrorMessage error, Throwable e,
                                                                              String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new SubscriptionManagementServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle Subscription Management server exceptions without a throwable.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return SubscriptionManagementServerException.
     */
    public static SubscriptionManagementServerException handleServerException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new SubscriptionManagementServerException(error.getCode(), error.getMessage(), description);
    }
}
