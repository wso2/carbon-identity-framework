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

package org.wso2.carbon.identity.webhook.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;

import java.util.List;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_EMPTY_WEBHOOK_REQUEST_FIELD;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_INVALID_WEBHOOK_REQUEST_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.CHANNELS_SUBSCRIBED_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.ENDPOINT_URI_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.WEBHOOK_NAME_FIELD;

/**
 * Webhook validator class.
 */
public class WebhookValidator {

    private static final String WEBHOOK_NAME_REGEX = "^[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*[a-zA-Z0-9-_]$";
    private static final String ENDPOINT_URI_REGEX = "^https?://[^\\s/$.?#]\\S*";
    // According to RFC 9910 a header name must contain only alphanumeric characters, period (.) and hyphen (-),
    // and should start with an alphanumeric character.
    private static final String SECRET_REGEX = "^[A-Za-z0-9._~+-]{32,128}$";

    private final Pattern webhookNameRegexPattern = Pattern.compile(WEBHOOK_NAME_REGEX);
    private final Pattern endpointUriRegexPattern = Pattern.compile(ENDPOINT_URI_REGEX);
    private final Pattern secretRegexPattern = Pattern.compile(SECRET_REGEX);

    /**
     * Validate whether required fields exist.
     *
     * @param fieldValue Field value.
     * @throws WebhookMgtClientException if the provided field is empty.
     */
    public void validateForBlank(String fieldName, String fieldValue) throws WebhookMgtClientException {

        if (StringUtils.isBlank(fieldValue)) {
            throw WebhookManagementExceptionHandler.handleClientException(ERROR_EMPTY_WEBHOOK_REQUEST_FIELD,
                    fieldName);
        }
    }

    /**
     * Validate the webhook name.
     *
     * @param name Webhook name.
     * @throws WebhookMgtClientException if the name is not valid.
     */
    public void validateWebhookName(String name) throws WebhookMgtClientException {

        boolean isValidName = webhookNameRegexPattern.matcher(name).matches();
        if (!isValidName) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, WEBHOOK_NAME_FIELD);
        }
    }

    /**
     * Validate the endpoint URI.
     *
     * @param uri Endpoint uri.
     * @throws WebhookMgtClientException if the uri is not valid.
     */
    public void validateEndpointUri(String uri) throws WebhookMgtClientException {

        boolean isValidUri = endpointUriRegexPattern.matcher(uri).matches();
        if (!isValidUri) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, ENDPOINT_URI_FIELD);
        }
    }

    /**
     * Validate the webhook secret.
     *
     * @param secret Webhook secret.
     * @throws WebhookMgtClientException if the secret is not valid.
     */
    public void validateWebhookSecret(String secret) throws WebhookMgtClientException {

        boolean isValidSecret = secretRegexPattern.matcher(secret).matches();
        if (!isValidSecret) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, SECRET_REGEX);
        }
    }

    /**
     * Validate Channels Subscribed.
     *
     * @param channelsSubscribed Channels subscribed.
     * @throws WebhookMgtClientException if the channels subscribed is not valid.
     */
    public void validateChannelsSubscribed(List<String> channelsSubscribed)
            throws WebhookMgtClientException {

        if (channelsSubscribed == null || channelsSubscribed.isEmpty()) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_EMPTY_WEBHOOK_REQUEST_FIELD, CHANNELS_SUBSCRIBED_FIELD);
        }
        //TODO: Validate the channels against webhok metadata service
    }
}
