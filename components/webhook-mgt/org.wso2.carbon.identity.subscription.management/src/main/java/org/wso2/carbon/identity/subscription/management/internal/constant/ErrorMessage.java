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

package org.wso2.carbon.identity.subscription.management.internal.constant;

/**
 * Error messages for subscription management.
 */
public enum ErrorMessage {

    // Client errors (6xxxx range)

    // Server errors (65xxx range)
    ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND("SUBSCRIPTIONMGT-65016", "Webhook subscribers not found",
            "No webhook subscribers found in the system"),
    ERROR_CODE_WEBHOOK_SUBSCRIBER_NOT_FOUND("SUBSCRIPTIONMGT-65018", "Webhook subscriber not found",
            "The specified webhook subscriber: %s could not be found in the system.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    public String getCode() {

        return code;
    }

    public String getMessage() {

        return message;
    }

    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return code + " : " + message;
    }
}
