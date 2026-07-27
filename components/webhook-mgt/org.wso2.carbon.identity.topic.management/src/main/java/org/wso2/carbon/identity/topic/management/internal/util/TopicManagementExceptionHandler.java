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

package org.wso2.carbon.identity.topic.management.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementClientException;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementServerException;
import org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage;

/**
 * Utility class for handling exceptions in the topic management module.
 * This class provides methods to create client and server exceptions with
 * appropriate error codes, messages, and descriptions.
 */
public class TopicManagementExceptionHandler {

    private TopicManagementExceptionHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Handle Topic Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return TopicManagementClientException.
     */
    public static TopicManagementClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new TopicManagementClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle Topic Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be formatted into the error description.
     * @return TopicManagementServerException.
     */
    public static TopicManagementServerException handleServerException(ErrorMessage error, Throwable e,
                                                                       String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new TopicManagementServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle Topic Management server exceptions without a throwable.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return TopicManagementServerException.
     */
    public static TopicManagementServerException handleServerException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new TopicManagementServerException(error.getCode(), error.getMessage(), description);
    }
}
