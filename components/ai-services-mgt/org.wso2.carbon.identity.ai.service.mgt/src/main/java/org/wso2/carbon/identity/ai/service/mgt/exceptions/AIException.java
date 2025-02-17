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

package org.wso2.carbon.identity.ai.service.mgt.exceptions;

/**
 * Generic Exception class for AI service.
 */
public class AIException extends Exception {

    private String errorCode;
    // This is the error message that comes from the server.
    private String serverMessage;
    // This is the status code that comes from the server.
    private int serverStatusCode;

    public AIException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

    public AIException(String message, Throwable cause) {

        super(message, cause);
    }

    public AIException(String message, String errorCode, int serverStatusCode, String serverMessage) {

        super(message);
        this.errorCode = errorCode;
        this.serverStatusCode = serverStatusCode;
        this.serverMessage = serverMessage;
    }

    public AIException(String message, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public String getServerMessage() {

        return serverMessage;
    }

    public int getServerStatusCode() {

        return serverStatusCode;
    }
}
