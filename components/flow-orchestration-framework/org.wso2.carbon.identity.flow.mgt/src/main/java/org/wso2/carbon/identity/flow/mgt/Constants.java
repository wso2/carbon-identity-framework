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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants for the orchestration flow.
 */
public class Constants {

    public static final String COMPLETE = "COMPLETE";
    public static final String DEFAULT_FLOW_NAME = "defaultFlow";
    public static final String END_NODE_ID = "END";

    public static final String IDP_NAME = "idpName";

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
        ERROR_CODE_INVOKING_AI_SERVICE("65012", "Error while invoking the AI service.",
                "Unexpected server error while invoking the AI service for tenant, %s"),
        ERROR_CODE_DELETE_FLOW("65013", "Error while deleting the flow.",
                "Unexpected server error while deleting the flow for tenant, %s"),

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

        REGISTRATION("REGISTRATION", FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED,
                FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED, FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED,
                FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED),
        PASSWORD_RECOVERY("PASSWORD_RECOVERY", FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED,
                FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED),
        INVITED_USER_REGISTRATION("INVITED_USER_REGISTRATION", FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED,
                FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED);

        private final String type;
        private final ArrayList<FlowCompletionConfig> supportedFlowCompletionConfigs = new ArrayList<>();

        FlowTypes(String type, FlowCompletionConfig... requiredFlowCompletionConfigs) {

            this.type = type;
            this.supportedFlowCompletionConfigs.addAll(Arrays.asList(requiredFlowCompletionConfigs));
        }

        public String getType() {

            return type;
        }

        public ArrayList<FlowCompletionConfig> getSupportedFlowCompletionConfigs() {

            return supportedFlowCompletionConfigs;
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
        public static final String END = "END";

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

    public static class FlowAIConstants {

        public static final String FLOW_AI_ENDPOINT = "AIServices.FlowAI.Endpoint";
        public static final String FLOW_AI_GENERATE_PATH = "AIServices.FlowAI.GenerateRequestPath";
        public static final String FLOW_AI_STATUS_PATH = "AIServices.FlowAI.StatusRequestPath";
        public static final String FLOW_AI_RESULT_PATH = "AIServices.FlowAI.ResultRequestPath";

        public static final String FLOW_TYPE = "flow_type";
        public static final String USER_QUERY = "user_query";
        public static final String OPERATION_ID = "operation_id";
        // Flow generation status constants.
        public static final String STATUS = "status";
        public static final String FAILED_STATUS = "FAILED";
        public static final String OPTIMIZING_QUERY = "optimizing_query";
        public static final String FETCHING_SAMPLES = "fetching_samples";
        public static final String GENERATING_FLOW = "generating_flow";
        public static final String COMPLETED = "completed";
        // Flow generation response constants.
        public static final String DATA = "data";

        private FlowAIConstants() {

        }
    }

    /**
     * Constants for the flow completion configs.
     */
    public enum FlowCompletionConfig {

        IS_AUTO_LOGIN_ENABLED("isAutoLoginEnabled"),
        IS_EMAIL_VERIFICATION_ENABLED("isEmailVerificationEnabled"),
        IS_ACCOUNT_LOCK_ON_CREATION_ENABLED("isAccountLockOnCreationEnabled"),
        IS_FLOW_COMPLETION_NOTIFICATION_ENABLED("isFlowCompletionNotificationEnabled");

        private final String config;
        private final String defaultValue;

        private static final Map<String, FlowCompletionConfig> LOOKUP = new HashMap<>();

        static {
            for (FlowCompletionConfig constant : values()) {
                LOOKUP.put(constant.config, constant);
            }
        }

        FlowCompletionConfig(String config) {

            this.config = config;
            this.defaultValue = "false";
        }

        FlowCompletionConfig(String config, String defaultValue) {

            this.config = config;
            this.defaultValue = defaultValue;
        }

        public String getConfig() {

            return config;
        }

        public String getDefaultValue() {

            return defaultValue;
        }

        public static FlowCompletionConfig fromConfig(String config) {

            if (LOOKUP.containsKey(config)) {
                return LOOKUP.get(config);
            }
            return null;
        }
    }
}
