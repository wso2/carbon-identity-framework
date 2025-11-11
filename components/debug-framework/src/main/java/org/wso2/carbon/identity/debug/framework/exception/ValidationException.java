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
 * Exception thrown when validation of debug requests or responses fails.
 * Used for validation errors in OAuth2 parameters, configurations, etc.
 */
public class ValidationException extends DebugFrameworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ValidationException with error code and message.
     *
     * @param errorCode Error code for categorization.
     * @param message   Error message.
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a ValidationException with error code, message, and cause.
     *
     * @param errorCode Error code for categorization.
     * @param message   Error message.
     * @param cause     Root cause exception.
     */
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Constructs a ValidationException with message.
     *
     * @param message Error message.
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    /**
     * Constructs a ValidationException with message and cause.
     *
     * @param message Error message.
     * @param cause   Root cause exception.
     */
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}
