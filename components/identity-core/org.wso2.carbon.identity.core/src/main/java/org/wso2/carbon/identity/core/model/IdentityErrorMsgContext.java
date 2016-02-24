/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.core.model;

public class IdentityErrorMsgContext {
    private String errorCode = null;
    private int failedLoginAttempts = 0;
    private int maximumLoginAttempts = 0;

    public IdentityErrorMsgContext(String errorCode) {
        this.errorCode = errorCode;
    }

    public IdentityErrorMsgContext(String errorCode, int failedLoginAttempts, int maximumLoginAttempts) {
        this.errorCode = errorCode;
        this.failedLoginAttempts = failedLoginAttempts;
        this.maximumLoginAttempts = maximumLoginAttempts;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public int getMaximumLoginAttempts() {
        return maximumLoginAttempts;
    }

    public void setMaximumLoginAttempts(int maximumLoginAttempts) {
        this.maximumLoginAttempts = maximumLoginAttempts;
    }
}
