/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core;

/**
 * IdentityRoleManagementException.
 */
public class IdentityRoleManagementException extends Exception {

    private static final long serialVersionUID = -2797937964788719880L;
    private String message;
    private String errorCode;

    /**
     * Constructs a new exception with the specified error message.
     *
     * @param message Detailed message.
     */
    public IdentityRoleManagementException(String message) {

        super(message);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified error message and cause.
     *
     * @param message Detailed message.
     * @param cause   The cause.
     */
    public IdentityRoleManagementException(String message, Throwable cause) {

        super(message, cause);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified error code and message.
     *
     * @param errorCode Error code.
     * @param message   Detailed message.
     */
    public IdentityRoleManagementException(String errorCode, String message) {

        this(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new exception with the specified error code, message and cause.
     *
     * @param errorCode Error code.
     * @param message   Detailed message.
     * @param cause     The cause.
     */
    public IdentityRoleManagementException(String errorCode, String message, Throwable cause) {

        this(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {

        return message;
    }

    /**
     * Returns the error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return errorCode;
    }
}
