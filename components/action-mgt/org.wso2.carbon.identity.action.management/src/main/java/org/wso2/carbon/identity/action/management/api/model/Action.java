/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.api.model;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Action.
 */
public class Action {

    /**
     * Action Type.
     */
    public enum ActionTypes {

        PRE_ISSUE_ACCESS_TOKEN(
                "preIssueAccessToken",
                "PRE_ISSUE_ACCESS_TOKEN",
                "Pre Issue Access Token",
                "Configure an extension point for modifying access token via a custom service.",
                Category.PRE_POST),
        PRE_UPDATE_PASSWORD(
                "preUpdatePassword",
                "PRE_UPDATE_PASSWORD",
                "Pre Update Password",
                "Configure an extension point for modifying user password via a custom service.",
                Category.PRE_POST),
        PRE_UPDATE_PROFILE(
                "preUpdateProfile",
                "PRE_UPDATE_PROFILE",
                "Pre Update Profile",
                "Configure an extension point for modifying user profile via a custom service.",
                Category.PRE_POST),
        PRE_REGISTRATION(
                "preRegistration",
                "PRE_REGISTRATION",
                "Pre Registration",
                "Configure an extension point for modifying user registration via a custom service.",
                Category.PRE_POST),
        AUTHENTICATION(
                "authentication",
                "AUTHENTICATION",
                "Authentication",
                "Configure an extension point for user authentication via a custom service.",
                Category.IN_FLOW),
        PRE_ISSUE_ID_TOKEN(
                "preIssueIdToken",
                "PRE_ISSUE_ID_TOKEN",
                "Pre Issue ID Token",
                "Configure an extension point for modifying ID token via a custom service.",
                Category.PRE_POST);

        private final String pathParam;
        private final String actionType;
        private final String displayName;
        private final String description;

        private final Category category;

        ActionTypes(String pathParam, String actionType, String displayName, String description, Category category) {

            this.pathParam = pathParam;
            this.actionType = actionType;
            this.displayName = displayName;
            this.description = description;
            this.category = category;
        }

        public String getPathParam() {

            return pathParam;
        }

        public String getActionType() {

            return actionType;
        }

        public String getDisplayName() {

            return displayName;
        }

        public String getDescription() {

            return description;
        }

        public Category getCategory() {

            return category;
        }

        public static ActionTypes[] filterByCategory(Category category) {

            return Arrays.stream(ActionTypes.values())
                    .filter(actionType -> actionType.category.equals(category))
                    .toArray(ActionTypes[]::new);
        }

        /**
         * Category Enum.
         * Defines the category of the action types.
         */
        public enum Category {
            PRE_POST,
            IN_FLOW
        }
    }

    /**
     * Action Status Enum.
     */
    public enum Status {

        ACTIVE,
        INACTIVE
    }

    private String id;
    private ActionTypes type;
    private String name;
    private String description;
    private Status status;
    private String actionVersion;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private EndpointConfig endpointConfig;
    private ActionRule rule;

    public Action(ActionResponseBuilder actionResponseBuilder) {

        this.id = actionResponseBuilder.id;
        this.type = actionResponseBuilder.type;
        this.name = actionResponseBuilder.name;
        this.description = actionResponseBuilder.description;
        this.status = actionResponseBuilder.status;
        this.actionVersion = actionResponseBuilder.actionVersion;
        this.createdAt = actionResponseBuilder.createdAt;
        this.updatedAt = actionResponseBuilder.updatedAt;
        this.endpointConfig = actionResponseBuilder.endpointConfig;
        this.rule = actionResponseBuilder.rule;
    }

    public Action(ActionRequestBuilder actionRequestBuilder) {

        this.name = actionRequestBuilder.name;
        this.actionVersion = actionRequestBuilder.actionVersion;
        this.description = actionRequestBuilder.description;
        this.endpointConfig = actionRequestBuilder.endpointConfig;
        this.rule = actionRequestBuilder.rule;
    }

    public String getId() {

        return id;
    }

    public ActionTypes getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }

    public Status getStatus() {

        return status;
    }

    public String getActionVersion() {

        return actionVersion;
    }

    public Timestamp getCreatedAt() {

        return createdAt;
    }

    public Timestamp getUpdatedAt() {

        return updatedAt;
    }

    public EndpointConfig getEndpoint() {

        return endpointConfig;
    }

    public ActionRule getActionRule() {

        return rule;
    }

    /**
     * ActionResponseBuilder.
     */
    public static class ActionResponseBuilder {

        private String id;
        private ActionTypes type;
        private String name;
        private String description;
        private Status status;
        private String actionVersion;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private EndpointConfig endpointConfig;
        private ActionRule rule;

        public ActionResponseBuilder id(String id) {

            this.id = id;
            return this;
        }

        public ActionResponseBuilder type(ActionTypes type) {

            this.type = type;
            return this;
        }

        public ActionResponseBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ActionResponseBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ActionResponseBuilder status(Status status) {

            this.status = status;
            return this;
        }

        public ActionResponseBuilder actionVersion(String actionVersion) {

            this.actionVersion = actionVersion;
            return this;
        }

        public ActionResponseBuilder createdAt(Timestamp createdAt) {

            this.createdAt = createdAt;
            return this;
        }

        public ActionResponseBuilder updatedAt(Timestamp updatedAt) {

            this.updatedAt = updatedAt;
            return this;
        }

        public ActionResponseBuilder endpoint(EndpointConfig endpointConfig) {

            this.endpointConfig = endpointConfig;
            return this;
        }

        public ActionResponseBuilder rule(ActionRule rule) {

            this.rule = rule;
            return this;
        }

        public Action build() {

            return new Action(this);
        }
    }

    /**
     * ActionRequestBuilder.
     */
    public static class ActionRequestBuilder {

        private String name;
        private String description;
        private String actionVersion;
        private EndpointConfig endpointConfig;
        private ActionRule rule;

        public ActionRequestBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ActionRequestBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ActionRequestBuilder actionVersion(String actionVersion) {

            this.actionVersion = actionVersion;
            return this;
        }

        public ActionRequestBuilder endpoint(EndpointConfig endpointConfig) {

            this.endpointConfig = endpointConfig;
            return this;
        }

        public ActionRequestBuilder rule(ActionRule rule) {

            this.rule = rule;
            return this;
        }

        public Action build() {

            return new Action(this);
        }
    }
}
