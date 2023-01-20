/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.exceptions;

/**
 * Input validation management related exceptions.
 */
public class InputValidationMgtException extends Exception {

    private String errorCode;
    private String description;

    /**
     * Constructor with error code and message.
     *
     * @param errorCode Error Code.
     * @param message   Error message.
     */
    public InputValidationMgtException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with errorCode, message and description.
     *
     * @param errorCode     Error Code.
     * @param message       Error Message.
     * @param description   Error Description.
     */
    public InputValidationMgtException(String errorCode, String message, String description) {
        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Constructor with error code, message and cause.
     *
     * @param errorCode Error Code.
     * @param message   Error message.
     * @param cause     If any error occurred when accessing the tenant.
     */
    public InputValidationMgtException(String errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with cause.
     *
     * @param cause If any error occurred when accessing the tenant.
     */
    public InputValidationMgtException(Throwable cause) {

        super(cause);
    }

    /**
     * Method to get error code.
     *
     * @return errorCode.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Method to get description.
     *
     * @return description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Method to set error code.
     *
     * @param errorCode Error Code.
     */
    protected void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }
}
