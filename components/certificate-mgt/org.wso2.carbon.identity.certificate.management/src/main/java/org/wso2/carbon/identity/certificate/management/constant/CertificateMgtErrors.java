/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.certificate.management.constant;

/**
 * This class contains error messages for Certificate management service.
 */
public enum CertificateMgtErrors {

    // Client errors.
    ERROR_CERTIFICATE_DOES_NOT_EXIST("60001", "Unable to perform the operation.",
            "Certificate with the id: %s does not exist."),
    ERROR_CERTIFICATE_DOES_NOT_EXIST_WITH_GIVEN_NAME("60001", "Unable to perform the operation.",
            "Certificate with the name: %s does not exist."),
    ERROR_EMPTY_FIELD("60002", "Invalid request.", "%s cannot be empty."),
    ERROR_INVALID_FIELD("60003", "Invalid request.", "%s is invalid."),
    ERROR_INVALID_CERTIFICATE_CONTENT("60004", "Invalid request.",
            "Certificate content is invalid."),

    // Server errors.
    ERROR_WHILE_ADDING_CERTIFICATE("65001", "Error while adding Certificate.",
            "Error while persisting Certificate '%s' in the system."),
    ERROR_WHILE_RETRIEVING_CERTIFICATE("65002", "Error while retrieving Certificate.",
            "Error while retrieving Certificate with id: %s from the system."),
    ERROR_WHILE_UPDATING_CERTIFICATE("65003", "Error while updating Certificate.",
            "Error while updating Certificate with id: %s in the system."),
    ERROR_WHILE_DELETING_CERTIFICATE("65004", "Error while deleting Certificate.",
            "Error while deleting Certificate with id: %s from the system."),
    ERROR_WHILE_RETRIEVING_CERTIFICATE_BY_NAME("65005", "Error while retrieving Certificate.",
            "Error while retrieving Certificate with name: %s from the system.");

    private final String code;
    private final String message;
    private final String description;

    CertificateMgtErrors(String code, String message, String description) {

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
}
