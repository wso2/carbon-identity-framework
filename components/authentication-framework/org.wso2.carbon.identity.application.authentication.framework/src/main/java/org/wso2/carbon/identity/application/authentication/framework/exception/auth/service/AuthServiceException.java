/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.exception.auth.service;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Exception class for authentication service.
 */
public class AuthServiceException extends IdentityException {

    private static final long serialVersionUID = 90220434357504216L;

    public AuthServiceException(String message) {

        super(message);
    }

    public AuthServiceException(String errorCode, String message) {

        super(errorCode, message);
    }

    public AuthServiceException(String message, Throwable cause) {

        super(message, cause);
    }

    public AuthServiceException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
