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

package org.wso2.carbon.identity.action.execution.util;

/**
 * Diagnostics Logs Constants for Action execution service.
 */
public class ActionExecutionLogConstants {

    private ActionExecutionLogConstants() {}

    public static final String ACTION_EXECUTION = "action-execution";

    /**
     * Action IDs.
     */
    public static class ActionIDs {

        public static final String EXECUTE_ACTION = "execute-action";
        public static final String PROCESS_ACTION_REQUEST = "process-action-request";
        public static final String SEND_ACTION_REQUEST = "send-action-request";
        public static final String RECEIVE_ACTION_RESPONSE = "receive-action-response";
        public static final String VALIDATE_ACTION_OPERATIONS = "validate-action-operations";
        public static final String EXECUTE_ACTION_OPERATIONS = "execute-action-operations";
    }
}
