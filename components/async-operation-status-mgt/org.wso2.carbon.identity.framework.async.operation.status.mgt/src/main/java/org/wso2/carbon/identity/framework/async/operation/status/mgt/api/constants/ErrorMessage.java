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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants;

/**
 * Error messages.
 */
public enum ErrorMessage {

    // Client errors.
    ERROR_CODE_INVALID_OPERATION_ID("60001", "Invalid Operation ID.",
            "Operation with ID: %s doesn't exist."),
    ERROR_CODE_INVALID_UNIT_OPERATION_ID("60002", "Invalid Unit Operation ID.",
            "Unit Operation with ID: %s doesn't exist."),
    ERROR_CODE_USER_NOT_AUTHORIZED_TO_GET_STATUS("60003", "Unable to get async operation status.",
            "Unauthorized request to get asynchronous operation status."),
    ERROR_CODE_INVALID_LIMIT("60004", "Invalid Limit.", "Invalid limit requested."),
    ERROR_CODE_INVALID_REQUEST_BODY("60005", "Invalid request.",
            "Provided request body content is not in the expected format."),

    // Server errors.
    ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS("65001", "Error while persisting async operation status.",
            "Error while persisting Async Operation Status in the system."),
    ERROR_WHILE_UPDATING_ASYNC_OPERATION_STATUS("65002", "Error while updating async operation status.",
            "Error while updating Async Operation Status in the system."),
    ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS_UNIT("65003", "Error while persisting async operation status unit.",
            "Error while persisting Async Operation Status Unit in the system."),
    ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS("65004", "Error while retrieving async operation status.",
            "Error while retrieving Async Operation Status from the system."),
    ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT("65005", "Error while retrieving async operation status unit.",
            "Error while retrieving Async Operation Status Unit from the system."),
    ERROR_WHILE_RETRIEVING_BASIC_ORG_DETAILS("65006", "Error while retrieving org details.",
            "Error while retrieving org details from org ids."),
    ERROR_WHILE_RETRIEVING_ORG_NAME_FROM_ORG_ID("65007", "Error while retrieving org name.",
            "Error while retrieving org name from org id."),
    ERROR_WHILE_RESOLVING_ORG_ID_FROM_TENANT_DOMAIN("65008", "Error while resolving organization id.",
            "Error while resolving organization id from tenant domain."),
    ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT_COUNT("65009", "Error while retrieving async operation " +
            "status unit count.", "Error while retrieving Async Operation Status unit count from the system.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

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
