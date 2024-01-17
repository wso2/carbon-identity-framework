/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exception class for client side errors in application management.
 */
public class IdentityApplicationManagementClientException extends IdentityApplicationManagementException {

    private static final long serialVersionUID = 546145354402013968L;

    private String[] messages;

    public IdentityApplicationManagementClientException(String[] messages) {

        super(Arrays.toString(messages));
        if (messages == null) {
            return;
        }
        List<String> msgList = new ArrayList<>();
        for (String msg: messages) {
            if (!msg.trim().isEmpty()) {
                msgList.add(msg);
            }
        }
        this.messages = msgList.toArray(new String[0]);
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message Detailed message
     */
    public IdentityApplicationManagementClientException(String message) {

        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message Detailed message
     * @param e       Cause as {@link Throwable}
     */
    public IdentityApplicationManagementClientException(String message, Throwable e) {

        super(message, e);
    }

    /**
     * Constructs a new exception with the specified error code and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     */
    public IdentityApplicationManagementClientException(String errorCode, String message) {

        super(errorCode, message);
    }

    /**
     * Constructs a new exception with the specified error code, message and cause.
     *
     * @param errorCode Error code
     * @param message   Detailed message
     * @param cause     Cause as {@link Throwable}
     */
    public IdentityApplicationManagementClientException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
    
    /**
     * Constructs a new exception with the specified error code, message and description.
     *
     * @param errorCode   Error code.
     * @param message     Error message.
     * @param description Error description.
     */
    public IdentityApplicationManagementClientException(String errorCode, String message, String description) {
        
        super(errorCode, message, description);
    }
    
    /**
     * Constructs a new exception with the specified error code, message, description and cause.
     *
     * @param errorCode   Error code
     * @param message     Detailed message
     * @param cause       Cause as {@link Throwable}
     * @param description Error description.
     */
    public IdentityApplicationManagementClientException(String errorCode, String message, String description,
                                                        Throwable cause) {
        
        super(errorCode, message, description, cause);
    }

    public String[] getMessages() {

        return messages;
    }
}
