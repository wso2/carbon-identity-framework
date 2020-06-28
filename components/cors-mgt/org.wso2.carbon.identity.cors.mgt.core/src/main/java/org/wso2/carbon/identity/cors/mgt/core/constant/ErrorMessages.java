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

    /**
     * Unable to retrieve CORS origins.
     */
    ERROR_CODE_CORS_RETRIEVE("CMS-65001",
            "Unable to retrieve CORS Origins.",
            "Server encountered an error while retrieving the CORS Origins of %s."),

    /**
     * Unable to set CORS origins.
     */
    ERROR_CODE_CORS_SET("CMS-65002",
            "Unable to set CORS Origins.",
            "Server encountered an error while setting the CORS Origins of %s."),

    /**
     * Unable to add CORS origins.
     */
    ERROR_CODE_CORS_ADD("CMS-65003",
            "Unable to add CORS Origins.",
            "Server encountered an error while adding the CORS Origins to %s."),

    /**
     * Unable to delete CORS origins.
     */
    ERROR_CODE_CORS_DELETE("CMS-65004",
            "Unable to delete CORS Origins.",
            "Server encountered an error while deleting the CORS Origins of %s."),

    /**
     * Unable to retrieve CORS configuration.
     */
    ERROR_CODE_CORS_CONFIG_RETRIEVE("CMS-65005",
            "Unable to retrieve CORS configuration.",
            "Server encountered an error while retrieving the CORS configuration of %s."),

    /**
     * Unable to set CORS configuration.
     */
    ERROR_CODE_CORS_CONFIG_SET("CMS-65006",
            "Unable to set CORS configuration.",
            "Server encountered an error while setting the CORS configuration of %s."),

    /**
     * Stored origin is invalid.
     */
    ERROR_CODE_INVALID_STORED_ORIGIN("CMS-65007",
            "The stored origin is invalid.",
            "Server encountered an error validating the stored origin %s."),

    /**
     * Unable to validate the application ID
     */
    ERROR_CODE_VALIDATE_APP_ID("CMS-65008",
            "Unable to validate application ID.",
            "Server encountered an error while trying to validate the application ID %s."),

    /**
     * Invalid URI.
     */
    ERROR_CODE_INVALID_URI("CMS-60001",
            "Invalid URI",
            "%s is not a valid URI."),

    /**
     * Missing scheme.
     */
    ERROR_CODE_MISSING_SCHEME("CMS-60002",
            "Bad URI: Missing scheme, such as http or https",
            "%s is missing scheme."),

    /**
     * Missing host.
     */
    ERROR_CODE_MISSING_HOST("CMS-60003",
            "Bad origin URI: Missing authority (host)",
            "%s is missing host."),

    /**
     * Invalid tenant domain.
     */
    ERROR_CODE_INVALID_TENANT_DOMAIN("CMS-60004",
            "Invalid input.",
            "%s is not a valid tenant domain."),

    /**
     * Invalid application ID.
     */
    ERROR_CODE_INVALID_APP_ID("CMS-60005",
            "Invalid input.",
            "%s is not a valid application."),

    /**
     * Duplicate addition.
     */
    ERROR_CODE_ORIGIN_PRESENT("CMS-60006",
            "Duplicate addition of existing CORS Origin.",
            "Tenant %s already have %s as a CORS Origin."),

    /**
     * Non existing deletion.
     */
    ERROR_CODE_ORIGIN_NOT_PRESENT("CMS-60007",
            "Non existing CORS Origin deletion.",
            "Tenant %s doesn't have a CORS Origin with the ID of %s."),

    /**
     * Null origin.
     */
    ERROR_CODE_NULL_ORIGIN("CMS-60008",
            "The origin value must not be null.",
            "%s is not an acceptable origin value."),

    /**
     * Bad header.
     */
    ERROR_CODE_BAD_HEADER("CMS-60009",
            "Bad header name.",
            "%s is an invalid header name.");

    /**
     * The error code.
     */
    private final String code;

    /**
     * The error message.
     */
    private final String message;

    /**
     * The error description.
     */
    private final String description;

    /**
     * ErrorMessages constructor which takes the {@code code}, {@code message} and {@code description} as parameters.
     *
     * @param code        The error code.
     * @param message     The error message.
     * @param description The error description. Could be null where unnecessary.
     */
    ErrorMessages(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Get the {@code code}.
     *
     * @return Returns the {@code code} to be set.
     */
    public String getCode() {

        return code;
    }

    /**
     * Get the {@code message}.
     *
     * @return Returns the {@code message} to be set.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Get the {@code description}.
     *
     * @return Returns the {@code description} to be set.
     */
    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return code + ":" + message;
    }
}
