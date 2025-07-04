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
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;

import java.util.List;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBED_CHANNEL_VALIDATION_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_EMPTY_WEBHOOK_REQUEST_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_INVALID_WEBHOOK_REQUEST_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.CHANNELS_SUBSCRIBED_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.ENDPOINT_URI_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.SECRET_FIELD;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.WEBHOOK_NAME_FIELD;

/**
 * Webhook validator class.
 */
public class WebhookValidator {

    private static final String WEBHOOK_NAME_REGEX = "^[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*[a-zA-Z0-9-_]$";
    private static final String ENDPOINT_URI_REGEX = "^https?://[^\\s/$.?#]\\S*";
    private static final String SECRET_REGEX = "^[ -~]{1,100}$";

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
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, SECRET_FIELD);
        }
    }

    /**
     * Validate Channels Subscribed.
     *
     * @param eventProfile       Event profile.
     * @param channelsSubscribed Channels subscribed.
     * @throws WebhookMgtException if the channels subscribed is not valid.
     */
    public void validateChannelsSubscribed(String eventProfile, List<Subscription> channelsSubscribed)
            throws WebhookMgtException {

        if (channelsSubscribed == null || channelsSubscribed.isEmpty()) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_EMPTY_WEBHOOK_REQUEST_FIELD, CHANNELS_SUBSCRIBED_FIELD);
        }
        List<EventProfile> eventProfileList = null;
        try {
            eventProfileList = WebhookManagementComponentServiceHolder.getInstance()
                    .getWebhookMetadataService().getSupportedEventProfiles();
        } catch (WebhookMetadataException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_SUBSCRIBED_CHANNEL_VALIDATION_ERROR, e);
        }
        EventProfile retrievedEventProfile = eventProfileList.stream()
                .filter(profile -> eventProfile.equalsIgnoreCase(profile.getProfile()))
                .findFirst()
                .orElse(null);
        if (retrievedEventProfile == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, CHANNELS_SUBSCRIBED_FIELD);
        }
        for (Subscription channel : channelsSubscribed) {
            boolean isValidChannel = retrievedEventProfile.getChannels().stream()
                    .anyMatch(c -> c.getUri().equalsIgnoreCase(channel.getChannelUri()));
            if (!isValidChannel) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ERROR_INVALID_WEBHOOK_REQUEST_FIELD, CHANNELS_SUBSCRIBED_FIELD);
            }
        }
    }
}
