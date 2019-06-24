/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt;

import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;

/**
 * Base exception for consent management feature.
 */
public class SessionManagementException extends Exception {

    private String errorCode;
    private int httpStatusCode;
    private String description;

    public SessionManagementException() {

        super();
    }

    public SessionManagementException(SessionMgtConstants.ErrorMessages error, int httpStatusCode, Throwable cause) {

        super(error.getMessage(), cause);
        this.errorCode = error.getCode();
        this.httpStatusCode = httpStatusCode;

    }

    public SessionManagementException(SessionMgtConstants.ErrorMessages error, int httpStatusCode, String
            description, Throwable cause) {

        super(error.getMessage(), cause);
        this.errorCode = error.getCode();
        this.httpStatusCode = httpStatusCode;
        this.description = description;

    }

    public SessionManagementException(String message, Throwable cause) {

        super(message, cause);
    }

    public SessionManagementException(String message, String errorCode, int httpStatusCode) {

        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public SessionManagementException(String message, String errorCode, int httpStatusCode, String description,
                                      Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
        this.description = description;
    }

    public SessionManagementException(Throwable cause) {

        super(cause);
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

}
