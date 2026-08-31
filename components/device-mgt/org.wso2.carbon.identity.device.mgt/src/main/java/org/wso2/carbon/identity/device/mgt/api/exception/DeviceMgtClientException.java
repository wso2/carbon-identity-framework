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
 * Client-side validation exception for device management.
 */
public class DeviceMgtClientException extends DeviceMgtException {

    /**
     * Creates a new client exception.
     *
     * @param message Error message.
     * @param description Error description.
     * @param errorCode Error code.
     */
    public DeviceMgtClientException(String message, String description, String errorCode) {

        super(message, description, errorCode);
    }

    /**
     * Creates a new client exception with the cause.
     *
     * @param message Error message.
     * @param description Error description.
     * @param errorCode Error code.
     * @param cause Root cause.
     */
    public DeviceMgtClientException(String message, String description, String errorCode, Throwable cause) {

        super(message, description, errorCode, cause);
    }
}

