/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.api.exception;

/**
 * Base exception class for device policy operations.
 */
public class DevicePolicyException extends Exception {

    private final String errorCode;
    private final String description;

    /**
     * Constructs a new DevicePolicyException with the specified detail message.
     *
     * @param message Detail message.
     */
    public DevicePolicyException(String message) {

        this(message, null, null);
    }

    /**
     * Constructs a new DevicePolicyException with the specified detail message and cause.
     *
     * @param message Detail message.
     * @param cause   Cause of the exception.
     */
    public DevicePolicyException(String message, Throwable cause) {

        this(message, null, null, cause);
    }

    /**
     * Constructs a new DevicePolicyException with detail message, description and error code.
     *
     * @param message     Detail message.
     * @param description Error description.
     * @param errorCode   Error code.
     */
    public DevicePolicyException(String message, String description, String errorCode) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Constructs a new DevicePolicyException with detail message, description, error code and cause.
     *
     * @param message     Detail message.
     * @param description Error description.
     * @param errorCode   Error code.
     * @param cause       Cause of the exception.
     */
    public DevicePolicyException(String message, String description, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Get the error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return this.errorCode;
    }

    /**
     * Get the error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return this.description;
    }
}
