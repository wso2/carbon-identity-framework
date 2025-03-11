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

package org.wso2.carbon.identity.authorization.framework.model;

import java.util.List;

/**
 * The {@code ErrorResponse} class is a generic model class for an error response from an authorization engine.
 */
public class ErrorResponse {

    private String errorCode;
    private String errorMessage;
    private List<String> errorDetails;

    /**
     * Constructs an {@code ErrorResponse} object with the error message.
     *
     * @param errorMessage The error message.
     */
    public ErrorResponse(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    /**
     * Sets the error code of the error response.
     * @param errorCode The error code.
     */
    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    /**
     * Sets the error details of the error response.
     * @param errorDetails The error details.
     */
    public void setErrorDetails(List<String> errorDetails) {

        this.errorDetails = errorDetails;
    }

    /**
     * Returns the error code of the error response.
     *
     * @return The error code of the error response.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Returns the error message of the error response.
     *
     * @return The error message of the error response.
     */
    public String getErrorMessage() {

        return errorMessage;
    }

    /**
     * Returns the error details of the error response.
     *
     * @return The error details of the error response.
     */
    public List<String> getErrorDetails() {

        return errorDetails;
    }
}
