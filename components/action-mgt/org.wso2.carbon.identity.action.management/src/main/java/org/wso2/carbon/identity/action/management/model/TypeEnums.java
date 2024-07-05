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

package org.wso2.carbon.identity.action.management.model;

/**
 * TypeEnums.
 */
public class TypeEnums {

    /**
     * Action Type.
     */
    public enum ActionTypes {

        PRE_ISSUE_ACCESS_TOKEN(
                "preIssueAccessToken",
                "PRE_ISSUE_ACCESS_TOKEN",
                "Pre Issue Access Token.",
                "Configure an extension point for modifying access token via a custom service."),
        PRE_UPDATE_PASSWORD(
                "preUpdatePassword",
                "PRE_UPDATE_PASSWORD",
                "Pre Update Password.",
                "Configure an extension point for modifying user " +
                "password via a custom service."),
        PRE_UPDATE_PROFILE(
                "preUpdateProfile",
                "PRE_UPDATE_PROFILE",
                "Pre Update Profile.",
                "Configure an extension point for modifying user profile via a custom service."),
        PRE_REGISTRATION(
                "preRegistration",
                "PRE_REGISTRATION",
                "Pre Registration.",
                "Configure an extension point for modifying user registration via a custom service.");

        private final String pathParam;
        private final String actionType;
        private final String displayName;
        private final String description;

        ActionTypes(String pathParam, String actionType, String displayName, String description) {

            this.pathParam = pathParam;
            this.actionType = actionType;
            this.displayName = displayName;
            this.description = description;
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
    }

    /**
     * Authentication Type.
     */
    public enum AuthenticationType {

        NONE("NONE", new String[]{}),
        BEARER("BEARER", new String[]{"accessToken"}),
        BASIC("BASIC", new String[]{"username", "password"}),
        API_KEY("API_KEY", new String[]{"header", "value"});

        private final String type;
        private final String[] properties;

        AuthenticationType(String type, String[]  properties) {

            this.type = type;
            this.properties = properties;
        }

        public String getType() {

            return type;
        }

        public String[] getProperties() {

            return properties;
        }
    }
}
