/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.exception;

public class RegistrationFrameworkException extends Exception {

    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String description;

    public RegistrationFrameworkException(String message) {

        super(message);
    }

    public RegistrationFrameworkException(String errorCode, String message, String description, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public RegistrationFrameworkException(String errorCode, String message, String description) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public RegistrationFrameworkException(String message, Throwable cause) {

        super(message, cause);
    }

    public RegistrationFrameworkException(Throwable cause) {

        super(cause);
    }
}
