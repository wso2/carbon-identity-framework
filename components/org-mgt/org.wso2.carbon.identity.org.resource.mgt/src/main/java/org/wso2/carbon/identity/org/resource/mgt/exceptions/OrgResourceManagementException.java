/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.org.resource.mgt.exceptions;

/**
 * Exception class that represents exceptions thrown upon organization resource management.
 */
public class OrgResourceManagementException extends Exception {

    private String errorCode;
    private String description;

    private static final long serialVersionUID = -1982152066401023165L;

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message Detailed message
     */
    public OrgResourceManagementException(String message) {

        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message Detailed message
     * @param e       Cause as {@link Throwable}
     */
    public OrgResourceManagementException(String message, Throwable e) {

        super(message, e);
    }

    /**
     * Constructs a new exception with the specified error code and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     */
    public OrgResourceManagementException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new exception with the specified error code, message and description.
     *
     * @param errorCode   Error code.
     * @param message     Error message.
     * @param description Error description.
     */
    public OrgResourceManagementException(String errorCode, String message, String description) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Constructs a new exception with the specified error code, message and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     * @param cause     Cause as {@link Throwable}
     */
    public OrgResourceManagementException(String errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new exception with the specified error code, message, description and cause.
     *
     * @param errorCode Error code.
     * @param message   Detailed message.
     * @param cause     Cause as {@link Throwable}.
     * @param description Error description.
     */
    public OrgResourceManagementException(String errorCode, String message, String description,
                                          Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Returns the error code.
     *
     * @return Error code
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * This public method is required by the stub.
     *
     * @return Error message.
     */
    @Override
    public String getMessage() {

        return super.getMessage();
    }

    public String getDescription() {

        return description;
    }
}
