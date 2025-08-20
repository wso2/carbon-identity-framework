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

package org.wso2.carbon.identity.webhook.metadata.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage;

/**
 * Utility class for handling exceptions in the webhook management module.
 * This class provides methods to create client and server exceptions with
 * appropriate error codes, messages, and descriptions.
 */
public class WebhookMetadataExceptionHandler {

    private WebhookMetadataExceptionHandler() {

    }

    /**
     * Handle Webhook Metadata client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return WebhookMetadataClientException.
     */
    public static WebhookMetadataClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new WebhookMetadataClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle Webhook Metadata server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be formatted into the error description.
     * @return WebhookMetadataServerException.
     */
    public static WebhookMetadataServerException handleServerException(ErrorMessage error, Throwable e,
                                                                       String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new WebhookMetadataServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Handle Webhook Metadata server exceptions without a throwable.
     *
     * @param error Error message.
     * @param data  Data to be formatted into the error description.
     * @return WebhookMetadataServerException.
     */
    public static WebhookMetadataServerException handleServerException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }

        return new WebhookMetadataServerException(error.getMessage(), description, error.getCode());
    }
}
