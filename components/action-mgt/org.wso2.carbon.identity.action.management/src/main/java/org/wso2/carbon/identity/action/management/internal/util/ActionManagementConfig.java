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

package org.wso2.carbon.identity.action.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action.ActionTypes;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.Collections;
import java.util.List;

/**
 * Action Management Config util class.
 */
public class ActionManagementConfig {

    private static final ActionManagementConfig INSTANCE = new ActionManagementConfig();

    private static final Log LOG = LogFactory.getLog(ActionManagementConfig.class);

    private ActionManagementConfig() {

    }

    public static ActionManagementConfig getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves the property values for a given property key from the identity configuration of the server.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return A list of property values associated with the given key.
     */
    public List<String> getPropertyValues(String propertyKey) {

        Object propertyValue = IdentityConfigParser.getInstance().getConfiguration().get(propertyKey);

        if (propertyValue == null) {
            return Collections.emptyList();
        }

        if (propertyValue instanceof String) {
            return Collections.singletonList(propertyValue.toString());
        }

        if (propertyValue instanceof List) {
            return (List<String>) propertyValue;
        } else {
            LOG.warn("Invalid system configuration for: " + propertyKey +
                    " at /repository/conf/identity.xml. Expected a string list of values.");
            return Collections.emptyList();
        }
    }

    /**
     * Get the latest version of the action type.
     *
     * @param actionType Action type.
     * @return Latest version of the action type.
     */
    public String getLatestVersion(ActionTypes actionType) throws ActionMgtServerException {

        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                return getVersion(
                        ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getLatestVersionProperty(), actionType);
            case AUTHENTICATION:
                return getVersion(ActionTypeConfig.AUTHENTICATION.getLatestVersionProperty(), actionType);
            case PRE_UPDATE_PASSWORD:
                return getVersion(
                        ActionTypeConfig.PRE_UPDATE_PASSWORD.getLatestVersionProperty(), actionType);
            case PRE_UPDATE_PROFILE:
                return getVersion(
                        ActionTypeConfig.PRE_UPDATE_PROFILE.getLatestVersionProperty(), actionType);
            case PRE_ISSUE_ID_TOKEN:
                return getVersion(ActionTypeConfig.PRE_ISSUE_ID_TOKEN.getLatestVersionProperty(), actionType);
            default:
                throw new ActionMgtServerException("Unsupported action type: " + actionType);
        }
    }

    private String getVersion(String actionTypePropertyName, ActionTypes actionType) throws ActionMgtServerException {

        String versionPropertyValue =
                (String) IdentityConfigParser.getInstance().getConfiguration().get(actionTypePropertyName);
        if (StringUtils.isBlank(versionPropertyValue)) {
            throw new ActionMgtServerException(String.format("Unable to resolve the latest action version for " +
                    "action type: %s", actionType.getActionType()));
        }
        return versionPropertyValue;
    }

    /**
     * Enum to hold the configuration properties for each action type.
     */
    public enum ActionTypeConfig {

        PRE_ISSUE_ACCESS_TOKEN(
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreIssueAccessToken.Version.Latest"
        ),
        AUTHENTICATION(
                "Actions.Types.Authentication.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.Authentication.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.Authentication.Version.Latest"
        ),
        PRE_UPDATE_PASSWORD(
                "Actions.Types.PreUpdatePassword.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreUpdatePassword.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreUpdatePassword.Version.Latest"
        ),
        PRE_UPDATE_PROFILE(
                "Actions.Types.PreUpdateProfile.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreUpdateProfile.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreUpdateProfile.Version.Latest"
        ),
        PRE_ISSUE_ID_TOKEN(
                "Actions.Types.PreIssueIdToken.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreIssueIdToken.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreIssueIdToken.Version.Latest"
        );

        private final String excludedHeadersProperty;
        private final String excludedParamsProperty;
        private final String latestVersionProperty;

        ActionTypeConfig(String excludedHeadersProperty, String excludedParamsProperty, String latestVersionProperty) {

            this.excludedHeadersProperty = excludedHeadersProperty;
            this.excludedParamsProperty = excludedParamsProperty;
            this.latestVersionProperty = latestVersionProperty;
        }

        public String getExcludedHeadersProperty() {

            return excludedHeadersProperty;
        }

        public String getExcludedParamsProperty() {

            return excludedParamsProperty;
        }

        public String getLatestVersionProperty() {

            return latestVersionProperty;
        }
    }
}
