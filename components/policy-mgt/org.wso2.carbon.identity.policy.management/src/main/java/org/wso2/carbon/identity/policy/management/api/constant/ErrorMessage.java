/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.api.constant;

/**
 * Error messages for Policy Management.
 */
public enum ErrorMessage {

    // Client errors.
    ERROR_POLICY_NOT_FOUND("PM-60001", "Policy not found.",
            "No policy found for the given policy id: %s."),
    ERROR_INVALID_POLICY_REQUEST_FIELD("PM-60002", "Invalid request.",
            "%s is empty or invalid."),
    ERROR_INVALID_POLICY_RULE("PM-60003", "Invalid policy rule.",
            "Policy rule validation failed: %s"),
    ERROR_POLICY_ALREADY_EXISTS("PM-60004", "Policy already exists.",
            "A policy with name '%s' already exists for the tenant."),
    ERROR_DUPLICATE_PLATFORM_IN_POLICY("PM-60005", "Duplicate platform in policy.",
            "Policy '%s' has more than one rule for platform '%s'."),

    // Server errors.
    ERROR_WHILE_ADDING_POLICY("PM-65001", "Error while adding Policy.",
            "Error while persisting Policy in the system."),
    ERROR_WHILE_RETRIEVING_POLICY("PM-65002", "Error while retrieving Policy.",
            "Error while retrieving Policy from the system."),
    ERROR_WHILE_UPDATING_POLICY("PM-65003", "Error while updating Policy.",
            "Error while updating Policy in the system."),
    ERROR_WHILE_DELETING_POLICY("PM-65004", "Error while deleting Policy.",
            "Error while deleting Policy from the system."),
    ERROR_WHILE_ADDING_RULE_FOR_POLICY("PM-65005", "Error while adding Rule for Policy.",
            "Error while adding Rule for Policy: %s in the system."),
    ERROR_WHILE_UPDATING_RULE_FOR_POLICY("PM-65006", "Error while updating Rule for Policy.",
            "Error while updating Rule for Policy: %s in the system."),
    ERROR_WHILE_DELETING_RULE_FOR_POLICY("PM-65007", "Error while deleting Rule for Policy.",
            "Error while deleting Rule for Policy: %s from the system.");

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
