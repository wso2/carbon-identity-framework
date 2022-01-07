/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.session.extender.exception;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkClientException;

/**
 * Client exception for the Session Extender endpoint.
 */
public class SessionExtenderClientException extends FrameworkClientException {

    private String description;
    private String errorMessage;

    public SessionExtenderClientException(String message) {
        super(message);
    }

    public SessionExtenderClientException(String errorCode, String message) {
        super(errorCode, message);
    }

    public SessionExtenderClientException(String errorCode, String errorMessage, String description) {

        super(errorCode, description);
        this.description = description;
        this.errorMessage = errorMessage;
    }

    public SessionExtenderClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionExtenderClientException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public String getDescription() {
        return description;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
