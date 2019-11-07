/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Exception class that represents exceptions thrown upon application management.
 */
public class IdentityApplicationManagementException extends IdentityException {

    private String message;

    private static final long serialVersionUID = -1982152066401023165L;

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message Detailed message
     */
    public IdentityApplicationManagementException(String message) {

        super(message);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message Detailed message
     * @param e       Cause as {@link Throwable}
     */
    public IdentityApplicationManagementException(String message, Throwable e) {

        super(message, e);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified error code and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     */
    public IdentityApplicationManagementException(String errorCode, String message) {

        super(errorCode, message);
    }

    /**
     * Constructs a new exception with the specified error code, message and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     * @param cause     Cause as {@link Throwable}
     */
    public IdentityApplicationManagementException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
