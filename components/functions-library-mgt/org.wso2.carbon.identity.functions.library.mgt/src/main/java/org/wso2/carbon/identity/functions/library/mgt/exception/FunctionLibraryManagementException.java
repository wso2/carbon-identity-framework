/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Function library manager Exception.
 */
public class FunctionLibraryManagementException extends Exception {

    private String message;

    /**
     * FunctionLibraryManagementException.
     *
     * @param message Error message
     */
    public FunctionLibraryManagementException(String message) {

        super(message);
        this.message = message;
    }

    /**
     * FunctionLibraryManagementException.
     *
     * @param message Error message
     * @param e       Error
     */
    public FunctionLibraryManagementException(String message, Throwable e) {

        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }
}
