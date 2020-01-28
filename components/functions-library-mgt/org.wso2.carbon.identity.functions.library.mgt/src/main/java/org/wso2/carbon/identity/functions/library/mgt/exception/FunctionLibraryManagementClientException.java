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

package org.wso2.carbon.identity.functions.library.mgt.exception;

/**
 * Function library manager client Exception.
 */
public class FunctionLibraryManagementClientException extends FunctionLibraryManagementException {

    private String message;
    private String errorCode;

    /**
     * FunctionLibraryManagementClientException.
     *
     * @param message   Error message
     * @param errorCode Error code
     */
    public FunctionLibraryManagementClientException(String errorCode, String message) {

        super(errorCode, message);
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * FunctionLibraryManagementClientException.
     *
     * @param message   Error message
     * @param errorCode Error code
     * @param cause     Error
     */
    public FunctionLibraryManagementClientException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }

    @Override
    public String getErrorCode() {

        return errorCode;
    }
}
