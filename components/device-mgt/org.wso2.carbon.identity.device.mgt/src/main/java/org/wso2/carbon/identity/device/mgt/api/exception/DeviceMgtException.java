/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.device.mgt.api.exception;

/**
 * Base checked exception for device management.
 */
public class DeviceMgtException extends Exception {

    private final String errorCode;
    private final String description;

    /**
     * Creates a new device management exception.
     *
     * @param message Error message.
     * @param description Error description.
     * @param errorCode Error code.
     */
    public DeviceMgtException(String message, String description, String errorCode) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Creates a new device management exception with the cause.
     *
     * @param message Error message.
     * @param description Error description.
     * @param errorCode Error code.
     * @param cause Root cause.
     */
    public DeviceMgtException(String message, String description, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    /**
     * Returns the error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Returns the error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return description;
    }
}

