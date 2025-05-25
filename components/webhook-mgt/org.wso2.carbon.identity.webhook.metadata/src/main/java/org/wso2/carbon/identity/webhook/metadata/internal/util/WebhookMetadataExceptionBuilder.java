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
 * Builder class for creating webhook metadata exceptions.
 */
public class WebhookMetadataExceptionBuilder {

    /**
     * Error codes for webhook metadata exceptions.
     */
    public static class ErrorCodes {

        // Client error codes start with CLIENT
        public static final String CLIENT_ERROR_CODE_PREFIX = "CLIENT-";
        public static final String INVALID_REQUEST = CLIENT_ERROR_CODE_PREFIX + "00001";
        public static final String PROFILE_NOT_FOUND = CLIENT_ERROR_CODE_PREFIX + "00002";
        public static final String SCHEMA_NOT_FOUND = CLIENT_ERROR_CODE_PREFIX + "00003";

        // Server error codes start with SERVER
        public static final String SERVER_ERROR_CODE_PREFIX = "SERVER-";
        public static final String ERROR_RETRIEVING_PROFILES = SERVER_ERROR_CODE_PREFIX + "00001";
        public static final String ERROR_RETRIEVING_PROFILE = SERVER_ERROR_CODE_PREFIX + "00002";
        public static final String ERROR_RETRIEVING_EVENTS = SERVER_ERROR_CODE_PREFIX + "00003";
        public static final String ERROR_LOADING_PROFILE_FILES = SERVER_ERROR_CODE_PREFIX + "00004";
    }

    /**
     * Build a webhook metadata client exception.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @return WebhookMetadataClientException
     */
    public static WebhookMetadataClientException buildClientException(String errorCode, String errorMessage) {

        return new WebhookMetadataClientException(errorCode, errorMessage);
    }

    /**
     * Build a webhook metadata client exception with a cause.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @param cause        Cause of the exception
     * @return WebhookMetadataClientException
     */
    public static WebhookMetadataClientException buildClientException(String errorCode, String errorMessage,
                                                                      Throwable cause) {

        return new WebhookMetadataClientException(errorCode, errorMessage, cause);
    }

    /**
     * Build a webhook metadata server exception.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @return WebhookMetadataServerException
     */
    public static WebhookMetadataServerException buildServerException(String errorCode, String errorMessage) {

        return new WebhookMetadataServerException(errorCode, errorMessage);
    }

    /**
     * Build a webhook metadata server exception with a cause.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @param cause        Cause of the exception
     * @return WebhookMetadataServerException
     */
    public static WebhookMetadataServerException buildServerException(String errorCode, String errorMessage,
                                                                      Throwable cause) {

        return new WebhookMetadataServerException(errorCode, errorMessage, cause);
    }
}
