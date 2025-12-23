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

package org.wso2.carbon.identity.webhook.management.internal.constant;

/**
 * Error messages for webhook management.
 */
public enum ErrorMessage {

    // Client errors (6xxxx range)
    // Continuation of client error codes can be found in the Webhook Management API layer as well.
    ERROR_CODE_WEBHOOK_NOT_FOUND("WEBHOOKMGT-60001", "Webhook not found",
            "The requested webhook %s could not be found in the system."),
    ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS("WEBHOOKMGT-60003", "Webhook endpoint already exists",
            "A webhook with the given endpoint: %s already exists in the system."),
    ERROR_CODE_INVALID_REQUEST("WEBHOOKMGT-60004", "Invalid request",
            "The request is invalid. %s"),
    ERROR_CODE_INVALID_WEBHOOK_ENDPOINT("WEBHOOKMGT-60005", "Invalid webhook endpoint",
            "The webhook endpoint URL is invalid or not accessible."),
    ERROR_CODE_INVALID_WEBHOOK_EVENT("WEBHOOKMGT-60006", "Invalid webhook event",
            "The provided webhook event is invalid or not supported."),
    ERROR_CODE_INVALID_WEBHOOK_SECRET("WEBHOOKMGT-60007", "Invalid webhook secret",
            "The provided webhook secret is invalid."),
    ERROR_CODE_WEBHOOK_ACTIVATION_ERROR("WEBHOOKMGT-60008", "Webhook activation error",
            "The webhook could not be activated for webhook id %s"),
    ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR("WEBHOOKMGT-60009", "Webhook deactivation error",
            "The webhook could not be deactivated for webhook id %s"),
    ERROR_EMPTY_WEBHOOK_REQUEST_FIELD("WEBHOOKMGT-60011", "Invalid request.", "%s is empty."),
    ERROR_INVALID_WEBHOOK_REQUEST_FIELD("WEBHOOKMGT-60012", "Invalid request.",
            "%s is invalid."),
    ERROR_CODE_WEBHOOK_ALREADY_ACTIVE("WEBHOOKMGT-60013", "Webhook already active",
            "The webhook: %s is already active and cannot be activated again."),
    ERROR_CODE_WEBHOOK_ALREADY_INACTIVE("WEBHOOKMGT-60014", "Webhook already inactive",
            "The webhook: %s is already inactive and cannot be deactivated again."),
    ERROR_CODE_WEBHOOK_RETRY_ERROR("WEBHOOKMGT-60015", "Webhook retry error",
            "An error occurred while retrying the webhook: %s."),
    ERROR_MAXIMUM_WEBHOOKS_PER_TENANT_REACHED("WEBHOOKMGT-60016", "Maximum number of webhooks reached.",
            "The maximum number of webhooks allowed has been reached. Max allowed: %s. Please delete an " +
                    "existing webhook before adding a new one."),

    // Server errors (65xxx range) | Remaining codes are continued in API layer.
    // Continuation of server error codes can be found in the Webhook Management API layer as well.
    ERROR_CODE_WEBHOOK_ADD_ERROR("WEBHOOKMGT-65001", "Error occurred while adding webhook",
            "An internal server error occurred while adding the webhook."),
    ERROR_CODE_WEBHOOK_UPDATE_ERROR("WEBHOOKMGT-65002", "Error occurred while updating webhook",
            "An internal server error occurred while updating the webhook: %s."),
    ERROR_CODE_WEBHOOK_DELETE_ERROR("WEBHOOKMGT-65003", "Error occurred while deleting webhook",
            "An internal server error occurred while deleting the webhook: %s."),
    ERROR_CODE_WEBHOOK_GET_ERROR("WEBHOOKMGT-65004", "Error occurred while retrieving webhook",
            "An internal server error occurred while retrieving the webhook: %s."),
    ERROR_CODE_WEBHOOK_LIST_ERROR("WEBHOOKMGT-65005", "Error occurred while listing webhooks",
            "An internal server error occurred while listing webhooks for tenant: %s."),
    ERROR_CODE_WEBHOOK_ENDPOINT_LIST_ERROR("WEBHOOKMGT-65006", "Error occurred while listing webhook endpoints",
            "An internal server error occurred while listing webhook endpoints for tenant: %s."),
    ERROR_CODE_WEBHOOK_ENDPOINT_GET_ERROR("WEBHOOKMGT-65007", "Error occurred while retrieving webhook endpoint",
            "An internal server error occurred while retrieving the webhook endpoint: %s."),
    ERROR_CODE_WEBHOOK_EVENT_ADD_ERROR("WEBHOOKMGT-65008", "Error occurred while adding webhook event",
            "An internal server error occurred while adding events to the webhook."),
    ERROR_CODE_WEBHOOK_EVENT_REMOVE_ERROR("WEBHOOKMGT-65009", "Error occurred while removing webhook event",
            "An internal server error occurred while removing events from the webhook."),
    ERROR_CODE_WEBHOOK_EVENT_LIST_ERROR("WEBHOOKMGT-65010", "Error occurred while listing webhook events",
            "An internal server error occurred while listing webhook events."),
    ERROR_CODE_WEBHOOK_TEST_ERROR("WEBHOOKMGT-65011", "Error occurred while testing webhook",
            "An internal server error occurred while testing the webhook."),
    ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR("WEBHOOKMGT-65012", "Error occurred while updating webhook status",
            "An internal server error occurred while updating the webhook: %s status."),
    ERROR_CODE_UNEXPECTED_ERROR("WEBHOOKMGT-65013", "Unexpected error occurred",
            "An unexpected error occurred while processing the request."),
    ERROR_CODE_WEBHOOK_SUBSCRIPTION_ERROR("WEBHOOKMGT-65014", "Webhook subscription error",
            "An error occurred while subscribing to the webhook: %s."),
    ERROR_CODE_WEBHOOK_UNSUBSCRIPTION_ERROR("WEBHOOKMGT-65015", "Webhook unsubscription error",
            "An error occurred while unsubscribing from the webhook: %s."),
    ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND("WEBHOOKMGT-65016", "Webhook subscribers not found",
            "No webhook subscribers found in the system"),
    ERROR_CODE_WEBHOOK_ENDPOINT_EXISTENCE_CHECK_ERROR("WEBHOOKMGT-65017",
            "Error occurred while checking webhook endpoint existence",
            "An internal server error occurred while checking the existence of the webhook endpoint: %s."),
    ERROR_CODE_WEBHOOK_SUBSCRIBER_NOT_FOUND("WEBHOOKMGT-65018", "Webhook subscriber not found",
            "The specified webhook subscriber: %s could not be found in the system."),
    ERROR_CODE_WEBHOOK_SUBSCRIBED_CHANNEL_VALIDATION_ERROR("WEBHOOKMGT-65019",
            "Webhook subscribed channel validation error",
            "The provided subscribed channels are invalid or not supported."),
    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR("WEBHOOKMGT-65020", "Webhook endpoint secret encryption error",
            "An error occurred while encrypting the webhook: %s endpoint secret."),
    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR("WEBHOOKMGT-65021", "Webhook endpoint secret delete error",
            "An error occurred while deleting the webhook: %s endpoint secret."),
    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR("WEBHOOKMGT-65022", "Webhook endpoint secret decryption error",
            "An error occurred while decrypting the webhook: %s endpoint secret."),
    ERROR_CODE_WEBHOOK_DELETE_NOT_ALLOWED_ERROR("WEBHOOKMGT-65023", "Webhook deletion not allowed",
            "The webhook: %s cannot be deleted in this state."),
    ERROR_CODE_WEBHOOK_ACTIVATION_NOT_ALLOWED_ERROR("WEBHOOKMGT-65024", "Webhook activation not allowed",
            "The webhook: %s cannot be activated in this state."),
    ERROR_CODE_WEBHOOK_DEACTIVATION_NOT_ALLOWED_ERROR("WEBHOOKMGT-65025", "Webhook deactivation not allowed",
            "The webhook: %s cannot be deactivated in this state."),
    ERROR_CODE_WEBHOOK_RETRY_STATUS_UPDATE_ERROR("WEBHOOKMGT-65026", "Webhook retry status update error",
            "An error occurred while updating the retry status of the webhook: %s."),
    ERROR_UPDATE_OPERATION_NOT_SUPPORTED("WEBHOOKMGT-65027", "Unable to perform the update operation.",
            "Update operation is not supported for %s"),
    ERROR_OPERATION_NOT_SUPPORTED("WEBHOOKMGT-65028", "Unable to perform the operation.",
            "Operation is not supported for %s adapter type."),
    ERROR_WHILE_RETRIEVING_WEBHOOKS_COUNT("WEBHOOKMGT-65029", "Error while retrieving webhook count.",
            "An error occurred while retrieving the webhook count for tenant: %s."),
    ERROR_CODE_WEBHOOK_ACTIVATION_ADAPTER_ERROR("WEBHOOKMGT-65030", "Webhook activation error",
            "An error occurred while activating the webhook: %s."),
    ERROR_CODE_WEBHOOK_DEACTIVATION_ADAPTER_ERROR("WEBHOOKMGT-65031", "Webhook deactivation error",
            "An error occurred while deactivating the webhook: %s."),
    ERROR_CODE_WEBHOOK_RETRY_ADAPTER_ERROR("WEBHOOKMGT-65032", "Webhook retry error",
            "An error occurred while retrying the webhook: %s."),
    ERROR_CODE_ACTIVE_WEBHOOKS_BY_PROFILE_CHANNEL_ERROR("WEBHOOKMGT-65033",
            "Error occurred while retrieving active webhooks by channel",
            "An error occurred while retrieving active webhooks for channel: %s and tenant: %s.");

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
