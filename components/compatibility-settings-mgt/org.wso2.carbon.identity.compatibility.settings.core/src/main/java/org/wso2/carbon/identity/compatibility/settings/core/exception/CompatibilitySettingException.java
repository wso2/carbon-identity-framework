/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Base exception class for compatibility setting related errors.
 */
public class CompatibilitySettingException extends IdentityException {

    private static final long serialVersionUID = 7845916089562752179L;

    private String description;

    /**
     * Constructor with message.
     *
     * @param message Error message.
     */
    public CompatibilitySettingException(String message) {

        super(message);
    }

    /**
     * Constructor with error code and message.
     *
     * @param errorCode Error code.
     * @param message   Error message.
     */
    public CompatibilitySettingException(String errorCode, String message) {

        super(errorCode, message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Error message.
     * @param cause   Throwable cause.
     */
    public CompatibilitySettingException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructor with error code, message and cause.
     *
     * @param errorCode Error code.
     * @param message   Error message.
     * @param cause     Throwable cause.
     */
    public CompatibilitySettingException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }

    /**
     * Constructor with error code, message, description and cause.
     *
     * @param errorCode   Error code.
     * @param message     Error message.
     * @param description Error description.
     * @param cause       Throwable cause.
     */
    public CompatibilitySettingException(String errorCode, String message, String description, Throwable cause) {

        super(errorCode, message, cause);
        this.description = description;
    }

    /**
     * Constructor with error code, message and description.
     *
     * @param errorCode   Error code.
     * @param message     Error message.
     * @param description Error description.
     */
    public CompatibilitySettingException(String errorCode, String message, String description) {

        super(errorCode, message);
        this.description = description;
    }

    /**
     * Get the error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set the error description.
     *
     * @param description Error description.
     */
    public void setDescription(String description) {

        this.description = description;
    }
}
