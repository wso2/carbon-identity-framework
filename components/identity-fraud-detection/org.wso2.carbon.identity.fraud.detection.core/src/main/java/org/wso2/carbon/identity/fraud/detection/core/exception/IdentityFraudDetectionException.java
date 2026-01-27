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
package org.wso2.carbon.identity.fraud.detection.core.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Exception class for Identity Fraud Detection component.
 */
public class IdentityFraudDetectionException extends IdentityException {

    private static final long serialVersionUID = 5777925141760413537L;

    /**
     * Constructor with error message.
     *
     * @param message Error message
     */
    public IdentityFraudDetectionException(String message) {

        super(message);
    }

    /**
     * Constructor with error code and message.
     *
     * @param errorCode Error code
     * @param message   Error message
     */
    public IdentityFraudDetectionException(String errorCode, String message) {

        super(errorCode, message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Error message
     * @param cause   Root cause
     */
    public IdentityFraudDetectionException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructor with error code, message and cause.
     *
     * @param errorCode Error code
     * @param message   Error message
     * @param cause     Root cause
     */
    public IdentityFraudDetectionException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
