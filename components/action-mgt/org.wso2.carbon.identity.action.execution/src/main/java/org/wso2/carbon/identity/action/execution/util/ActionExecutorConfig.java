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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class holds the system configurations for the Action Executor Service.
 * This includes Action related configurations and Action Type related configurations.
 */
public class ActionExecutorConfig {

    private static final ActionExecutorConfig INSTANCE = new ActionExecutorConfig();

    private static final Log LOG = LogFactory.getLog(ActionExecutorConfig.class);

    private static final String EXCLUDED_HEADERS_IN_ACTION_REQUEST_PROPERTY =
            "Actions.ActionRequest.ExcludedHeaders.Header";
    private static final String EXCLUDED_PARAMS_IN_ACTION_REQUEST_PROPERTY =
            "Actions.ActionRequest.ExcludedParameters.Parameter";

    private ActionExecutorConfig() {

    }

    public static ActionExecutorConfig getInstance() {

        return INSTANCE;
    }

    /**
     * Returns a boolean value based on the system configuration: 'actions.types.{action_type}.enable' that
     * enables or disables action execution for the given action type.
     *
     * @param actionType Action Type
     * @return 'true' if configuration is 'true', return 'false' otherwise.
     */
    public boolean isExecutionForActionTypeEnabled(ActionType actionType) {

        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                return isActionTypeEnabled(ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getActionTypeEnableProperty());
            case AUTHENTICATION:
                return isActionTypeEnabled(ActionTypeConfig.AUTHENTICATION.getActionTypeEnableProperty());
            default:
                return false;
        }
    }

    private boolean isActionTypeEnabled(String actionTypePropertyName) {

        boolean isActionTypeEnabled = false;
        String actionTypeEnabledPropertyValue =
                (String) IdentityConfigParser.getInstance().getConfiguration().get(actionTypePropertyName);
        if (StringUtils.isNotBlank(actionTypeEnabledPropertyValue)) {
            return Boolean.parseBoolean(actionTypeEnabledPropertyValue);
        }
        return isActionTypeEnabled;
    }

    /**
     * Returns the union set of headers configured in the system configuration 'actions.action_request.excluded_headers'
     * and actions.types.{actionType}.action_request.excluded_headers to exclude in the action request.
     * These headers are expected to be configured in lower case as headers are case-insensitive.
     *
     * @param actionType ActionType
     * @return a set of headers to exclude in the action request.
     */
    public Set<String> getExcludedHeadersInActionRequestForActionType(ActionType actionType) {

        Set<String> excludedHeaders = getExcludedHeadersInActionRequestForAllTypes();
        List<String> excludedHeadersPropertyValue = new ArrayList<>();
        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                excludedHeadersPropertyValue = getPropertyValues(
                        ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getExcludedHeadersProperty());
                break;
            case AUTHENTICATION:
                excludedHeadersPropertyValue = getPropertyValues(
                        ActionTypeConfig.AUTHENTICATION.getExcludedHeadersProperty());
                break;
            default:
                break;
        }
        excludedHeaders.addAll(excludedHeadersPropertyValue);
        return Collections.unmodifiableSet(excludedHeaders);
    }

    private Set<String> getExcludedHeadersInActionRequestForAllTypes() {

        List<String> excludedHeadersPropertyValue =
                getPropertyValues(EXCLUDED_HEADERS_IN_ACTION_REQUEST_PROPERTY);

        return new HashSet<>(excludedHeadersPropertyValue);
    }

    /**
     * Returns the union set of params configured in the system configuration 'actions.action_request.excluded_params'
     * and actions.types.{actionType}.action_request.excluded_parameters to exclude in the action request.
     *
     * @param actionType Action Type
     * @return a set of params to exclude in the action request.
     */
    public Set<String> getExcludedParamsInActionRequestForActionType(ActionType actionType) {

        Set<String> excludedParams = getExcludedParamsInActionRequestForAllTypes();
        List<String> excludedParamsPropertyValue = new ArrayList<>();
        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                excludedParamsPropertyValue = getPropertyValues(
                        ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getExcludedParamsProperty());

                break;
            case AUTHENTICATION:
                excludedParamsPropertyValue = getPropertyValues(
                        ActionTypeConfig.AUTHENTICATION.getExcludedParamsProperty());
                break;
            default:
                break;
        }
        excludedParams.addAll(excludedParamsPropertyValue);
        return Collections.unmodifiableSet(excludedParams);
    }

    private Set<String> getExcludedParamsInActionRequestForAllTypes() {

        List<String> excludedParamsPropertyValue =
                getPropertyValues(EXCLUDED_PARAMS_IN_ACTION_REQUEST_PROPERTY);

        return new HashSet<>(excludedParamsPropertyValue);
    }

    private List<String> getPropertyValues(String propertyKey) {

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
     * Enum to hold the configuration properties for each action type.
     */
    private static enum ActionTypeConfig {
        PRE_ISSUE_ACCESS_TOKEN("Actions.Types.PreIssueAccessToken.Enable",
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter"),
        AUTHENTICATION("Actions.Types.Authentication.Enable",
                "Actions.Types.Authentication.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.Authentication.ActionRequest.ExcludedParameters.Parameter");

        private final String actionTypeEnableProperty;
        private final String excludedHeadersProperty;
        private final String excludedParamsProperty;

        ActionTypeConfig(String actionTypeEnableProperty, String excludedHeadersProperty,
                         String excludedParamsProperty) {

            this.actionTypeEnableProperty = actionTypeEnableProperty;
            this.excludedHeadersProperty = excludedHeadersProperty;
            this.excludedParamsProperty = excludedParamsProperty;
        }

        public String getActionTypeEnableProperty() {

            return actionTypeEnableProperty;
        }

        public String getExcludedHeadersProperty() {

            return excludedHeadersProperty;
        }

        public String getExcludedParamsProperty() {

            return excludedParamsProperty;
        }
    }
}
