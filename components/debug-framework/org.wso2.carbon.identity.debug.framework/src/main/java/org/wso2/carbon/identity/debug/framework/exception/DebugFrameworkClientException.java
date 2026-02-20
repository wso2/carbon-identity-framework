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
 * Exception thrown when client-side errors occur in the Debug Framework.
 * Represents validation errors, missing required parameters, or invalid input.
 */
public class DebugFrameworkClientException extends DebugFrameworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DebugFrameworkClientException with message.
     *
     * @param message Error message.
     */
    public DebugFrameworkClientException(String message) {

        super(message);
    }

    /**
     * Constructs a DebugFrameworkClientException with error code, message, and description.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     */
    public DebugFrameworkClientException(String errorCode, String message, String description) {

        super(errorCode, message, description);
    }

    /**
     * Constructs a DebugFrameworkClientException with error code, message, description, and cause.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     * @param cause       Root cause exception.
     */
    public DebugFrameworkClientException(String errorCode, String message, String description, Throwable cause) {

        super(errorCode, message, description, cause);
    }
}
