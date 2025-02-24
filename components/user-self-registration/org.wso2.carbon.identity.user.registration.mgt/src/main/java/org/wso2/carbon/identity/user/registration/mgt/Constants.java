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

package org.wso2.carbon.identity.user.registration.mgt;

/**
 * Constants for the self registration flow.
 */
public class Constants {

    public static final String EXECUTOR_FOR_USER_ONBOARDING = "UserOnboardingExecutor";
    public static final String EXECUTOR_FOR_PROMPT = "ViewPromptExecutor";


    // Constants for the registration flow json definition.
    public static final String COMPLETE = "COMPLETE";
    public static final String NEXT = "NEXT";
    public static final String EXECUTOR = "EXECUTOR";
    public static final String EXECUTOR_NAME = "EXECUTOR_NAME";
    public static final String AUTHENTICATOR_ID = "AUTHENTICATOR_ID";

    /**
     * Constants for the fields.
     */
    public static class Fields {

        public static final String COMPONENTS = "components";
        public static final String ACTION = "action";
    }

    /**
     * Constants for the node types.
     */
    public static class NodeTypes {

        public static final String PROMPT = "PROMPT";
        public static final String DECISION = "DECISION";
        public static final String TASK_EXECUTION = "TASK_EXECUTION";
    }

    /**
     * Constants for the step types.
     */
    public static class StepTypes {

        public static final String VIEW = "VIEW";
        public static final String REDIRECTION = "REDIRECTION";
    }

    public static class ComponentTypes {

        public static final String FORM = "FORM";
        public static final String BUTTON = "BUTTON";

    }

    /**
     * Constants for the action types.
     */
    public static class ActionTypes {

        public static final String EXECUTOR = "EXECUTOR";
        public static final String NEXT = "NEXT";
    }
}
