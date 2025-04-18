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

package org.wso2.carbon.identity.framework.async.status.mgt.api.constants;

/**
 * Error messages.
 */
public enum ErrorMessage {

    // Server errors.
    ERROR_WHILE_STORING_ASYNC_OPERATION_STATUS("65101", "Error while storing async status.",
            "Error while persisting Async Status in the system."),
    ERROR_WHILE_UPDATING_ASYNC_OPERATION_STATUS("65102", "Error while updating async status.",
            "Error while updating Async Status in the system."),
    ERROR_WHILE_STORING_ASYNC_OPERATION_STATUS_UNIT("65103", "Error while storing async status unit.",
            "Error while persisting Async Status Unit in the system."),
    ERROR_WHILE_RETRIEVING_ASYNC_STATUS("65104",
            "Error while retrieving Async Status",
            "Error while retrieving Async Status from the system."),

    // Client errors.
    ERROR_CODE_INVALID_OPERATION_ID("65105", "Invalid Operation ID",
            "Operation with ID: %s doesn't exist."),
    ERROR_CODE_INVALID_UNIT_OPERATION_ID("65106", "Invalid Unit Operation ID",
            "Unit Operation with ID: %s doesn't exist."),
    ERROR_CODE_USER_NOT_AUTHORIZED_TO_GET_STATUS("65107", "Unable to get async status.",
            "Unauthorized request to get asynchronous operation status"),
    ERROR_CODE_INVALID_LIMIT("65108", "Invalid Limit.", "Invalid limit requested."),
    ERROR_CODE_INVALID_REQUEST_BODY("xx001", "Invalid request.",
            "Provided request body content is not in the expected format.");

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
