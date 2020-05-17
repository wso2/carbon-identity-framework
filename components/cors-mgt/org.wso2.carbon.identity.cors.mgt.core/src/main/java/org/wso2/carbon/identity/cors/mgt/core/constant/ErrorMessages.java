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

package org.wso2.carbon.identity.cors.mgt.core.constant;

/**
 * ErrorMessages enum holds the error codes and messages.
 * CMS stands for Cors Management Service.
 */
public enum ErrorMessages {

    ERROR_CODE_CORS_RETRIEVE("CMS-65001",
            "Unable to retrieve CORS Origins.",
            "Server encountered an error while retrieving the CORS Origins of %s."),
    ERROR_CODE_CORS_SET("CMS-65002",
            "Unable to set CORS Origins.",
            "Server encountered an error while setting the CORS Origins of %s."),
    ERROR_CODE_CORS_ADD("CMS-65003",
            "Unable to add CORS Origins.",
            "Server encountered an error while adding the CORS Origins to %s."),
    ERROR_CODE_CORS_DELETE("CMS-65004",
            "Unable to delete CORS Origins.",
            "Server encountered an error while deleting the CORS Origins of %s."),
    ERROR_CODE_INVALID_TENANT_DOMAIN("CMS-60001",
            "Invalid input.",
            "%s is not a valid tenant domain."),
    ERROR_CODE_EMPTY_LIST("CMS-60002",
            "Invalid input.",
            "Input CORS origin list cannot be empty."),
    ERROR_CODE_INVALID_ORIGIN("CMS-60003",
            "Invalid input.",
            "%s is an invalid origin."),
    ERROR_CODE_ORIGIN_PRESENT("CMS-60004",
            "Redundant addition of existing CORS Origin.",
            "Tenant %s already have %s as a CORS Origin."),
    ERROR_CODE_ORIGIN_NOT_PRESENT("CMS-60005",
            "Non existing CORS Origin deletion.",
            "Tenant %s doesn't have %s as a CORS Origin.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessages(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    public String getCode() {

        return code;
    }

    public String getMessage() {

        return message;
    }

    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return code + ":" + message;
    }
}
