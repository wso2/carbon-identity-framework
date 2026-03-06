/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
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
    private String description;

    /**
     * Constructs a DebugFrameworkException with message.
     *
     * @param message Error message.
     */
    public DebugFrameworkException(String message) {

        super(message);
    }

    /**
     * Constructs a DebugFrameworkException with message and cause.
     *
     * @param message Error message.
     * @param cause   Root cause exception.
     */
    public DebugFrameworkException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructs a DebugFrameworkException with error code, message, and description.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     */
    public DebugFrameworkException(String errorCode, String message, String description) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Constructs a DebugFrameworkException with error code, message, description, and cause.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     * @param cause       Root cause exception.
     */
    public DebugFrameworkException(String errorCode, String message, String description, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
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

    /**
     * Gets the error description.
     *
     * @return Error description string.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Sets the error description.
     *
     * @param description Error description string.
     */
    public void setDescription(String description) {

        this.description = description;
    }
}
