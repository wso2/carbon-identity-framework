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
                "The user authentication failed due to pending approval of the user: %s");
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
