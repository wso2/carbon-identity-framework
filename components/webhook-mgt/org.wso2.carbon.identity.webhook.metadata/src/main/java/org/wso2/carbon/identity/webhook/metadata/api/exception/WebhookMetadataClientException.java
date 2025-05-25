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

package org.wso2.carbon.identity.webhook.metadata.api.exception;

/**
 * Exception class for webhook metadata client errors.
 */
public class WebhookMetadataClientException extends WebhookMetadataException {

    private static final long serialVersionUID = -8743232645196393268L;

    /**
     * Constructor with error code and error message.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     */
    public WebhookMetadataClientException(String errorCode, String errorMessage) {

        super(errorCode, errorMessage);
    }

    /**
     * Constructor with error code, error message and cause.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @param cause        Cause of the exception
     */
    public WebhookMetadataClientException(String errorCode, String errorMessage, Throwable cause) {

        super(errorCode, errorMessage, cause);
    }
}
