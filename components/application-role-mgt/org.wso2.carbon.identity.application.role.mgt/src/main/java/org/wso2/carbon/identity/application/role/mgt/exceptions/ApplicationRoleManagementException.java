/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.role.mgt.exceptions;

/**
 * Application role management exception.
 */
public class ApplicationRoleManagementException extends Exception {

    private String errorCode;
    private String description;

    public ApplicationRoleManagementException(String message) {

        super(message);
    }

    public ApplicationRoleManagementException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationRoleManagementException(String message, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public ApplicationRoleManagementException(String message, String description, String errorCode) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public ApplicationRoleManagementException(String message, String description, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public String getDescription() {

        return description;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public void setDescription(String description) {

        this.description = description;
    }
}
