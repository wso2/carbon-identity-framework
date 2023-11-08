/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.exceptions;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Exception class for handling client attestation metadata management errors.
 */
public class ClientAttestationMgtException extends IdentityException {

    /**
     * Constructs a new `ClientAttestationMetaDataMgtException` with the specified error message.
     *
     * @param message The error message.
     */
    public ClientAttestationMgtException(String message) {

        super(message);
    }

    /**
     * Constructs a new `ClientAttestationMetaDataMgtException` with the specified error code and error message.
     *
     * @param errorCode The error code.
     * @param message   The error message.
     */
    public ClientAttestationMgtException(String errorCode, String message) {

        super(errorCode, message);
    }

    /**
     * Constructs a new `ClientAttestationMetaDataMgtException` with the specified error message and a throwable cause.
     *
     * @param message The error message.
     * @param cause   The throwable cause of the exception.
     */
    public ClientAttestationMgtException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructs a new `ClientAttestationMetaDataMgtException` with the specified error code, error message,
     * and a throwable cause.
     *
     * @param errorCode The error code.
     * @param message   The error message.
     * @param cause     The throwable cause of the exception.
     */
    public ClientAttestationMgtException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}

