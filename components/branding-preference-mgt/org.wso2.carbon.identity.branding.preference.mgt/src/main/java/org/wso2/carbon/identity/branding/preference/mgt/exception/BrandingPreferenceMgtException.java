/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.branding.preference.mgt.exception;

/**
 * Base exception for branding preference management feature.
 */
public class BrandingPreferenceMgtException extends Exception {

    private String errorCode;

    public BrandingPreferenceMgtException() {

        super();
    }

    public BrandingPreferenceMgtException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

    public BrandingPreferenceMgtException(String message, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public BrandingPreferenceMgtException(Throwable cause) {

        super(cause);
    }

    public String getErrorCode() {

        return errorCode;
    }

    protected void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }
}
