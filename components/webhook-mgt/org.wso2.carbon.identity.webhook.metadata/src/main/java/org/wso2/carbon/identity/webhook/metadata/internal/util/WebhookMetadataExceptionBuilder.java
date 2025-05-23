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

import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;

/**
 * Utility class for building exceptions for the webhook metadata service.
 */
public class WebhookMetadataExceptionBuilder {

    /**
     * Build a server exception with a message.
     *
     * @param message Error message
     * @return WebhookMetadataServerException
     */
    public static WebhookMetadataServerException buildServerException(String message) {
        return new WebhookMetadataServerException(message);
    }

    /**
     * Build a server exception with a message and cause.
     *
     * @param message Error message
     * @param cause   Exception cause
     * @return WebhookMetadataServerException
     */
    public static WebhookMetadataServerException buildServerException(String message, Throwable cause) {
        return new WebhookMetadataServerException(message, cause);
    }

    /**
     * Build a client exception with a message.
     *
     * @param message Error message
     * @return WebhookMetadataClientException
     */
    public static WebhookMetadataClientException buildClientException(String message) {
        return new WebhookMetadataClientException(message);
    }

    /**
     * Build a client exception with a message and cause.
     *
     * @param message Error message
     * @param cause   Exception cause
     * @return WebhookMetadataClientException
     */
    public static WebhookMetadataClientException buildClientException(String message, Throwable cause) {
        return new WebhookMetadataClientException(message, cause);
    }
}
