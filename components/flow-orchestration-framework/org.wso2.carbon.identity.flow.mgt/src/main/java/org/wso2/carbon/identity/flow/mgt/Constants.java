/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.mgt;

/**
 * Constants for the orchestration flow.
 */
public class Constants {

    public static final String COMPLETE = "COMPLETE";
    public static final String DEFAULT_FLOW_NAME = "defaultFlow";
    public static final String END_NODE_ID = "END";

    private Constants() {

    }

    public enum ErrorMessages {

        // Server errors.
        ERROR_CODE_ADD_DEFAULT_FLOW("65001", "Error while updating the flow.",
                "Unexpected server error while updating the default flow in tenant, %s"),

        ERROR_CODE_GET_DEFAULT_FLOW("65002", "Error while retrieving the flow.",
                "Unexpected server error while retrieving the default flow from database in " +
                        "tenant, %s"),

        ERROR_CODE_SERIALIZE_PAGE_CONTENT("65003", "Error while serializing the page content.",
                "Unexpected server error while serializing the page content for step %s" +
                        " in tenant, %s"),
        ERROR_CODE_INVALID_NODE("65004", "Node id not found.", "Could not resolve a valid node with id, %s to create " +
                "a link."),
        ERROR_CODE_DESERIALIZE_PAGE_CONTENT("65005", "Error while deserializing the page content.",
                "Unexpected server error while deserializing the page content for step %s" +
                        " in tenant, %s"),
        ERROR_CODE_GET_GRAPH_FAILED("65006", "Error while retrieving the graph.",
                "Unexpected server error while retrieving the graph for tenant, " +
                        "%s"),
        ERROR_CODE_GET_FIRST_STEP_ID("65007", "Error while retrieving the first step id.",
                "Unexpected server error while retrieving the first step id for tenant, %s"),
        ERROR_CODE_CLEAR_CACHE_FAILED("65008", "Error while clearing the cache.",
                "Unexpected server error while clearing the cache for tenant, %s"),
        ERROR_CODE_ADDING_FLOW_CONFIG("65009", "Error while adding the flow config.",
                "Unexpected server error while adding the flow config for tenant, %s"),
        ERROR_CODE_GETTING_FLOW_CONFIG("65010", "Error while retrieving the flow config.",
                "Unexpected server error while retrieving the flow config for tenant, %s"),
        ERROR_CODE_UPDATING_FLOW_CONFIG("65011", "Error while updating the flow config.",
                "Unexpected server error while updating the flow config for tenant, %s"),

        // Client errors.
        ERROR_CODE_UNSUPPORTED_STEP_TYPE("60001", "Unsupported step type.",
                "The step type, %s is not supported."),
        ERROR_CODE_STEP_DATA_NOT_FOUND("60002", "Step data not found.",
                "The step data for step, %s is not found."),
        ERROR_CODE_COMPONENT_DATA_NOT_FOUND("60003", "Component data not found.",
                "The step, %s of type view must have components defined."),
        ERROR_CODE_INVALID_ACTION_FOR_BUTTON("60004", "Invalid action configuration.",
                "The component, %s of type button must have an action defined."),
        ERROR_CODE_NEXT_ACTION_NOT_FOUND("60005", "Invalid action configuration.",
                "Next step is not defined for the action in component, %s."),
        ERROR_CODE_EXECUTOR_INFO_NOT_FOUND("60006", "Invalid action configuration.",
                "Executor data is not defined for the action type EXECUTOR in component, " +
                        "%s."),
        ERROR_CODE_MULTIPLE_STEP_EXECUTORS("60007", "Multiple executors defined for the step.",
                "Multiple executors are defined for the step, %s."),
        ERROR_CODE_UNSUPPORTED_ACTION_TYPE("60008", "Unsupported action type.",
                "The action type, %s defined for component %s is not supported."),
        ERROR_CODE_INVALID_NEXT_STEP("600009", "Invalid next step configuration.", "Cannot resolve a step for the " +
                "next id, %s."),
        ERROR_CODE_ACTION_DATA_NOT_FOUND("60010", "Action data not found.",
                "The step, %s of type %s must have action defined"),
        ERROR_CODE_INVALID_ACTION_TYPE("60011", "Invalid action type.",
                "The action type, %s is not supported for step, %s of type %s."),
        ERROR_CODE_INVALID_FIRST_NODE("60012", "Invalid first node.",
                "Invalid first node configuration: either no first node is defined or multiple " +
                        "first nodes are present."),
        ERROR_CODE_UNSUPPORTED_NODE_ID("60013", "Node id is not supported.", "%s cannot be " +
                "used as a Node ID."),
        ;

        private static final String ERROR_PREFIX = "RFM";
        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = ERROR_PREFIX + "-" + code;
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

    public enum FlowTypes {

        REGISTRATION("REGISTRATION"),
        PASSWORD_RECOVERY("PASSWORD_RECOVERY"),
        INVITED_USER_REGISTRATION("INVITED_USER_REGISTRATION");

        private final String type;

        FlowTypes(String type) {

            this.type = type;
        }

        public String getType() {

            return type;
        }
    }

    /**
     * Constants for the node types.
     */
    public static class NodeTypes {

        public static final String DECISION = "DECISION";
        public static final String TASK_EXECUTION = "TASK_EXECUTION";
        public static final String PROMPT_ONLY = "PROMPT_ONLY";

        private NodeTypes() {

        }
    }

    /**
     * Constants for the step types.
     */
    public static class StepTypes {

        public static final String VIEW = "VIEW";
        public static final String REDIRECTION = "REDIRECTION";
        public static final String INTERNAL_PROMPT = "INTERNAL_PROMPT";
        public static final String EXECUTION = "EXECUTION";
        public static final String WEBAUTHN = "WEBAUTHN";
        public static final String USER_ONBOARD = "USER_ONBOARD";

        private StepTypes() {

        }
    }

    public static class ComponentTypes {

        public static final String FORM = "FORM";
        public static final String BUTTON = "BUTTON";
        public static final String INPUT = "INPUT";
        public static final String CAPTCHA = "CAPTCHA";

        private ComponentTypes() {

        }
    }

    /**
     * Constants for the action types.
     */
    public static class ActionTypes {

        public static final String EXECUTOR = "EXECUTOR";
        public static final String NEXT = "NEXT";

        private ActionTypes() {

        }
    }

    /**
     * Constants for defined executor types.
     */
    public static class ExecutorTypes {

        public static final String USER_ONBOARDING = "UserOnboardingExecutor";

        private ExecutorTypes() {

        }
    }

    /**
     * Constants for the flow configurations.
     */
    public static class FlowConfigConstants {

        public static final String RESOURCE_TYPE = "flow-mgt-config";
        public static final String RESOURCE_NAME_PREFIX = "flow-mgt-config-";
        public static final String FLOW_TYPE = "flowType";
        public static final String IS_ENABLED = "isEnabled";
        public static final String IS_AUTO_LOGIN_ENABLED = "isAutoLoginEnabled";

        private FlowConfigConstants() {

        }
    }
}
