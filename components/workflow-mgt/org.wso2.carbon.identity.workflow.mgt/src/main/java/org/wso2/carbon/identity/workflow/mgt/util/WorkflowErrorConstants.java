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

package org.wso2.carbon.identity.workflow.mgt.util;

/**
 * This class holds the constants related to the workflow management.
 */
public class WorkflowErrorConstants {

    /**
     * Relevant error messages and error codes.
     */
    public enum ErrorMessages {

        // Generic error messages.
        ERROR_CODE_USER_WF_ALREADY_EXISTS("WFM-10001",
                "There is a pending workflow already defined for the user."),
        ERROR_CODE_USER_ACCOUNT_PENDING_APPROVAL("WFM-10002",
                "The user authentication failed due to pending approval of the user: %s"),
        ERROR_CODE_USER_WF_USER_ACCOUNT_PENDING_DELETION("WFM-10003",
                "The user account is pending in the deletion workflow."),
        ERROR_CODE_USER_WF_USER_NOT_FOUND("WFM-10004",
                "The user is not found in the system."),
        ERROR_CODE_USER_WF_USER_ALREADY_EXISTS("WFM-10005",
                "The user already exists in the system."),
        ERROR_CODE_USER_WF_ROLE_NOT_FOUND("WFM-10006",
                "The role %s is not found in the system for assign the user."),
        ERROR_CODE_USER_WF_ROLE_PENDING_DELETION("WFM-10007",
                "There is the pending deletion workflow for the role: %s"),

        ERROR_CODE_ROLE_WF_PENDING_ALREADY_EXISTS("WFM-10008",
                "There is a pending workflow already defined for the role."),
        ERROR_CODE_ROLE_WF_ROLE_ALREADY_EXISTS("WFM-10009",
                "The role already exist in the system."),
        ERROR_CODE_ROLE_WF_USER_NOT_FOUND("WFM-10010",
                "The user %s is not found in the system for assign the role."),
        ERROR_CODE_ROLE_WF_USER_PENDING_DELETION("WFM-10011",
                "There is the pending deletion workflow for the user: %s"),
        ERROR_CODE_ROLE_WF_ROLE_NOT_FOUND("WFM-10012",
                "The role is not found in the system."),
        ERROR_CODE_ROLE_WF_USER_PENDING_APPROVAL_FOR_ROLE("WFM-10013",
                "The user %s is already pending approval for the role.");
        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + " - " + message;
        }
    }
}
