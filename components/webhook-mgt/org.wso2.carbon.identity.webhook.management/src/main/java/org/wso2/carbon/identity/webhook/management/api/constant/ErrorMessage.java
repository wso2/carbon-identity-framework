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

package org.wso2.carbon.identity.webhook.management.api.constant;

/**
 * Error messages for webhook management.
 */
public enum ErrorMessage {

    // Client errors (6xxxx range)
    ERROR_CODE_WEBHOOK_NOT_FOUND("60001", "Webhook not found",
            "The requested webhook could not be found in the system."),
    ERROR_CODE_WEBHOOK_ALREADY_EXISTS("60002", "Webhook endpoint already exists",
            "A webhook with the given ID already exists in the system."),
    ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS("60003", "Webhook already exists",
            "A webhook with the given endpoint: %s already exists in the system."),
    ERROR_CODE_INVALID_REQUEST("60004", "Invalid request",
            "The request is invalid. %s"),
    ERROR_CODE_INVALID_WEBHOOK_ENDPOINT("60005", "Invalid webhook endpoint",
            "The webhook endpoint URL is invalid or not accessible."),
    ERROR_CODE_INVALID_WEBHOOK_EVENT("60006", "Invalid webhook event",
            "The provided webhook event is invalid or not supported."),
    ERROR_CODE_INVALID_WEBHOOK_SECRET("60007", "Invalid webhook secret",
            "The provided webhook secret is invalid."),
    ERROR_CODE_WEBHOOK_ACTIVATION_ERROR("60008", "Webhook activation error",
            "The webhook could not be activated for webhook id %s"),
    ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR("60009", "Webhook deactivation error",
            "The webhook could not be deactivated for webhook id %s"),

    // Server errors (65xxx range)
    ERROR_CODE_WEBHOOK_ADD_ERROR("65001", "Error occurred while adding webhook",
            "An internal server error occurred while adding the webhook: %s."),
    ERROR_CODE_WEBHOOK_UPDATE_ERROR("65002", "Error occurred while updating webhook",
            "An internal server error occurred while updating the webhook: %s."),
    ERROR_CODE_WEBHOOK_DELETE_ERROR("65003", "Error occurred while deleting webhook",
            "An internal server error occurred while deleting the webhook: %s."),
    ERROR_CODE_WEBHOOK_GET_ERROR("65004", "Error occurred while retrieving webhook",
            "An internal server error occurred while retrieving the webhook: %s."),
    ERROR_CODE_WEBHOOK_LIST_ERROR("65005", "Error occurred while listing webhooks",
            "An internal server error occurred while listing webhooks for tenant: %s."),
    ERROR_CODE_WEBHOOK_ENDPOINT_LIST_ERROR("65006", "Error occurred while listing webhooks endpoints",
            "An internal server error occurred while listing webhook endpoints for tenant: %s."),
    ERROR_CODE_WEBHOOK_ENDPOINT_GET_ERROR("65007", "Error occurred while retrieving webhook endpoint",
            "An internal server error occurred while retrieving the webhook endpoint: %s."),
    ERROR_CODE_WEBHOOK_EVENT_ADD_ERROR("65008", "Error occurred while adding webhook event",
            "An internal server error occurred while adding events to the webhook."),
    ERROR_CODE_WEBHOOK_EVENT_REMOVE_ERROR("65009", "Error occurred while removing webhook event",
            "An internal server error occurred while removing events from the webhook."),
    ERROR_CODE_WEBHOOK_EVENT_LIST_ERROR("65010", "Error occurred while listing webhook events",
            "An internal server error occurred while listing webhook events."),
    ERROR_CODE_WEBHOOK_TEST_ERROR("65011", "Error occurred while testing webhook",
            "An internal server error occurred while testing the webhook."),
    ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR("65012", "Error occurred while updating webhook status",
            "An internal server error occurred while updating the webhook: %s status."),
    ERROR_CODE_UNEXPECTED_ERROR("65014", "Unexpected error occurred",
            "An unexpected error occurred while processing the request."),
    ERROR_CODE_WEBHOOK_SUBSCRIPTION_ERROR("65015", "Webhook subscription error",
            "An error occurred while subscribing to the webhook: %s."),
    ERROR_CODE_WEBHOOK_UNSUBSCRIPTION_ERROR("65016", "Webhook unsubscription error",
            "An error occurred while unsubscribing from the webhook: %s."),
    ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND("65017", "Webhook subscribers not found",
            "No webhook subscribers found in the system for tenant: %s.");

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
