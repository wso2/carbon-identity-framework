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

package org.wso2.carbon.identity.subscription.management.api.exception;

/**
 * Exception class for server-side errors in subscription management.
 */
public class SubscriptionManagementServerException extends SubscriptionManagementException {

    /**
     * Constructs a new exception with the specified detail message, description, and error code.
     *
     * @param message     The detail message
     * @param description The description
     * @param errorCode   The error code
     */
    public SubscriptionManagementServerException(String errorCode, String message, String description) {

        super(errorCode, message, description);
    }

    /**
     * Constructs a new exception with the specified detail message, description, error code, and cause.
     *
     * @param message     The detail message
     * @param description The description
     * @param errorCode   The error code
     * @param cause       The cause
     */
    public SubscriptionManagementServerException(String errorCode, String message, String description,
                                                 Throwable cause) {

        super(errorCode, message, description, cause);
    }
}
