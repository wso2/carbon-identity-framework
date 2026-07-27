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

package org.wso2.carbon.identity.event.publisher.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.event.publisher.api.constant.ErrorMessage;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherClientException;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherServerException;

/**
 * Utility class for handling exceptions in the event publisher module.
 * This class provides methods to create client and server exceptions with
 * appropriate error codes, messages, and descriptions.
 */
public class EventPublisherExceptionHandler {

    private EventPublisherExceptionHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Handle Event Publisher client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return EventPublisherClientException.
     */
    public static EventPublisherClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new EventPublisherClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle Event Publisher server exceptions with a throwable.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be formatted into the error description.
     * @return EventPublisherServerException.
     */
    public static EventPublisherServerException handleServerException(ErrorMessage error, Throwable e,
                                                                      String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new EventPublisherServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle Event Publisher server exceptions without a Throwable.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return EventPublisherServerException.
     */
    public static EventPublisherServerException handleServerException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new EventPublisherServerException(error.getCode(), error.getMessage(), description);
    }
}
