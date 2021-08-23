/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.exception;

import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityException;

/**
 * Authentication failed exception.
 */
public class AuthenticationFailedException extends IdentityException {

    private static final long serialVersionUID = -8680141408172156343L;

    private User user;

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, User user) {

        super(message);
        this.user = user;
    }

    public AuthenticationFailedException(String message, Throwable cause) {

        super(message, cause);
    }

    public AuthenticationFailedException(String message, User user, Throwable cause) {

        super(message, cause);
        this.user = user;
    }

    public AuthenticationFailedException(String errorCode, String message, User user, Throwable cause) {

        super(errorCode, message, cause);
        this.user = user;
    }

    public AuthenticationFailedException(String errorCode, String message, User user) {

        super(errorCode, message);
        this.user = user;
    }

    public AuthenticationFailedException(String errorCode, String message) {

        super(errorCode, message);
    }

    public AuthenticationFailedException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }

    public User getUser() {

        return user;
    }
}
