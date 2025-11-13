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

package org.wso2.carbon.identity.external.api.client.api.exception;

import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;

/**
 * Exception class for API Client related exceptions.
 */
public class APIClientException extends Exception {

    private final ErrorMessage errorMessage;
    private final String descriptionData;

    /**
     * Constructor with error message, description data and cause.
     *
     * @param errorMessage    Error message enum.
     * @param descriptionData Description data for the error.
     * @param e               Throwable cause.
     */
    public APIClientException(ErrorMessage errorMessage, String descriptionData,  Throwable e) {

        super(e);
        this.errorMessage = errorMessage;
        this.descriptionData = descriptionData;
    }

    /**
     * Constructor with error message and description data.
     *
     * @param errorMessage    Error message enum.
     * @param descriptionData Description data for the error.
     */
    public APIClientException(ErrorMessage errorMessage, String descriptionData) {

        this.errorMessage = errorMessage;
        this.descriptionData = descriptionData;
    }

    /**
     * Get the error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return errorMessage.getCode();
    }

    /**
     * Get the error message.
     *
     * @return Error message.
     */
    @Override
    public String getMessage() {

        return errorMessage.getMessage();
    }

    /**
     * Get the error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return getErrorDescriptionWithData(errorMessage, descriptionData);
    }

    /**
     * Include context data to error message.
     *
     * @param error Error message.
     * @param data  Context data.
     * @return Formatted error message.
     */
    private static String getErrorDescriptionWithData(ErrorMessage error, String data) {

        String message;
        if (data != null) {
            message = String.format(error.getDescription(), data);
        } else {
            message = error.getDescription();
        }
        return message;
    }
}
