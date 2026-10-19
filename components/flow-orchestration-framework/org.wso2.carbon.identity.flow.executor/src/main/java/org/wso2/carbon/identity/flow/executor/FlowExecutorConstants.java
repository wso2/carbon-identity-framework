/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.executor;

/**
 * Constants for the common flow executors.
 */
public class FlowExecutorConstants {

    public static final String FLOW_ID = "flowId";

    private FlowExecutorConstants() {

    }

    /**
     * Constants for the flow handoff executor.
     */
    public static class FlowHandoff {

        public static final String FLOW_HANDOFF_EXECUTOR = "FlowHandoffExecutor";
        public static final String FLOW_HANDOFF_CREATED_TIMESTAMP = "flowHandoffCreatedTimestamp";
        public static final String HANDOFF = "handoff";
        public static final String EXPIRY_TIME_METADATA_KEY = "expiryTime";
        public static final String TARGET_URL_METADATA_KEY = "targetUrl";
        public static final long DEFAULT_HANDOFF_EXPIRY_SECONDS = 60;

        private FlowHandoff() {

        }
    }

    /**
     * Error messages of the common flow executors.
     */
    public enum ErrorMessages {

        // Flow handoff errors.
        ERROR_CODE_INVALID_HANDOFF("60301",
                "Invalid flow handoff.",
                "The flow handoff is invalid."),
        ERROR_CODE_HANDOFF_EXPIRED("60302",
                "Flow handoff expired.",
                "The flow handoff link has expired.");

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
    }
}
