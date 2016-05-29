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

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

public class InvalidCredentialsException extends AuthenticationFailedException {

    private static final long serialVersionUID = 6368867651869262347L;

    public InvalidCredentialsException(String message) {
        super(message);
    }
    public InvalidCredentialsException(String message, AuthenticatedUser user) {
        super(message,user);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCredentialsException(String message, AuthenticatedUser user, Throwable cause) {
        super(message, user, cause);
    }
}
