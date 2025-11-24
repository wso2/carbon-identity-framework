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

package org.wso2.carbon.identity.debug.framework.exception;

/**
 * Base exception class for the Debug Framework.
 * All framework-specific exceptions should extend this class.
 */
public class DebugFrameworkException extends Exception {

    private static final long serialVersionUID = 1L;
    private String errorCode;

    /**
     * Constructs a DebugFrameworkException with error code and message.
     *
     * @param errorCode Error code for categorization.
     * @param message   Error message.
     */
    public DebugFrameworkException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a DebugFrameworkException with error code, message, and cause.
     *
     * @param errorCode Error code for categorization.
     * @param message   Error message.
     * @param cause     Root cause exception.
     */
    public DebugFrameworkException(String errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a DebugFrameworkException with message.
     *
     * @param message Error message.
     */
    public DebugFrameworkException(String message) {

        super(message);
        this.errorCode = "DEBUG_FRAMEWORK_ERROR";
    }

    /**
     * Constructs a DebugFrameworkException with message and cause.
     *
     * @param message Error message.
     * @param cause   Root cause exception.
     */
    public DebugFrameworkException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = "DEBUG_FRAMEWORK_ERROR";
    }

    /**
     * Gets the error code.
     *
     * @return Error code string.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Sets the error code.
     *
     * @param errorCode Error code string.
     */
    public void setErrorCode(String errorCode) {
        
        this.errorCode = errorCode;
    }
}
