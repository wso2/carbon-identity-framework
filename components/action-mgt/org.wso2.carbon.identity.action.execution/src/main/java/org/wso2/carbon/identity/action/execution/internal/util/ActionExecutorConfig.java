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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
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
    private static final String HTTP_READ_TIMEOUT_PROPERTY = "Actions.HTTPClient.HTTPReadTimeout";
    private static final String HTTP_CONNECTION_REQUEST_TIMEOUT_PROPERTY =
            "Actions.HTTPClient.HTTPConnectionRequestTimeout";
    private static final String HTTP_CONNECTION_TIMEOUT_PROPERTY = "Actions.HTTPClient.HTTPConnectionTimeout";
    private static final String HTTP_CONNECTION_POOL_SIZE_PROPERTY = "Actions.HTTPClient.HTTPConnectionPoolSize";
    private static final String HTTP_REQUEST_RETRY_COUNT_PROPERTY = "Actions.HTTPClient.HTTPRequestRetryCount";
    private static final int DEFAULT_HTTP_REQUEST_RETRY_COUNT = 2;
    private static final int DEFAULT_HTTP_CONNECTION_POOL_SIZE = 20;
    private static final int DEFAULT_HTTP_READ_TIMEOUT_IN_MILLIS = 5000;
    private static final int DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS = 2000;
    private static final int DEFAULT_HTTP_CONNECTION_TIMEOUT_IN_MILLIS = 2000;

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
            case PRE_UPDATE_PASSWORD:
                return isActionTypeEnabled(ActionTypeConfig.PRE_UPDATE_PASSWORD.getActionTypeEnableProperty());
            case PRE_UPDATE_PROFILE:
                return isActionTypeEnabled(ActionTypeConfig.PRE_UPDATE_PROFILE.getActionTypeEnableProperty());
            case PRE_ISSUE_ID_TOKEN:
                return isActionTypeEnabled(ActionTypeConfig.PRE_ISSUE_ID_TOKEN.getActionTypeEnableProperty());
            default:
                return false;
        }
    }

    /**
     * Returns the HTTP request retry count based on the system configuration.
     *
     * @return The HTTP request retry count, or the default if the property is missing or invalid.
     */
    public int getHttpRequestRetryCount() {

        int retryCountPropertyValue = DEFAULT_HTTP_REQUEST_RETRY_COUNT;
        String retryCountValue = (String) IdentityConfigParser.getInstance().getConfiguration().
                get(HTTP_REQUEST_RETRY_COUNT_PROPERTY);
        if (StringUtils.isNotBlank(retryCountValue)) {
            try {
                retryCountPropertyValue = Integer.parseInt(retryCountValue);
            } catch (NumberFormatException e) {
                LOG.debug("Failed to read Http request retry count property in identity.xml." +
                        " Expects a number. Using the default value: " +
                        DEFAULT_HTTP_REQUEST_RETRY_COUNT, e);
            }
        }
        return retryCountPropertyValue;
    }

    /**
     * Returns the HTTP connection pool size based on the system configuration.
     *
     * @return The HTTP connection pool size, or the default if the property is missing or invalid.
     */
    public int getHttpConnectionPoolSize() {

        int poolSizePropertyValue = DEFAULT_HTTP_CONNECTION_POOL_SIZE;
        String poolSizeValue = (String) IdentityConfigParser.getInstance().getConfiguration().
                get(HTTP_CONNECTION_POOL_SIZE_PROPERTY);
        if (StringUtils.isNotBlank(poolSizeValue)) {
            try {
                poolSizePropertyValue = Integer.parseInt(poolSizeValue);
            } catch (NumberFormatException e) {
                LOG.debug("Failed to read Http client connection pool size property in identity.xml." +
                        " Expects a number. Using the default value: " +
                        DEFAULT_HTTP_CONNECTION_POOL_SIZE, e);
            }
        }
        return poolSizePropertyValue;
    }

    /**
     * Retrieves the HTTP read timeout configuration.
     * If the configuration value is invalid or missing, the default timeout value is parsed.
     *
     * @return The HTTP read timeout int value in milliseconds.
     */
    public int getHttpReadTimeoutInMillis() {

        return parseTimeoutConfig(HTTP_READ_TIMEOUT_PROPERTY, DEFAULT_HTTP_READ_TIMEOUT_IN_MILLIS);
    }

    /**
     * Retrieves the HTTP connection request timeout configuration.
     * If the configuration value is invalid or missing, the default timeout value is parsed.
     *
     * @return The HTTP connection request timeout int value in milliseconds.
     */
    public int getHttpConnectionRequestTimeoutInMillis() {

        return parseTimeoutConfig(HTTP_CONNECTION_REQUEST_TIMEOUT_PROPERTY,
                DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS);
    }

    /**
     * Retrieves the HTTP connection timeout configuration.
     * If the configuration value is invalid or missing, the default timeout value is parsed.
     *
     * @return The HTTP connection timeout int value in milliseconds.
     */
    public int getHttpConnectionTimeoutInMillis() {

        return parseTimeoutConfig(HTTP_CONNECTION_TIMEOUT_PROPERTY, DEFAULT_HTTP_CONNECTION_TIMEOUT_IN_MILLIS);
    }

    private int parseTimeoutConfig(String timeoutTypeName, int defaultTimeout) {

        int timeoutPropertyValue = defaultTimeout;
        String timeoutValue = (String) IdentityConfigParser.getInstance().getConfiguration().get(timeoutTypeName);
        if (StringUtils.isNotBlank(timeoutValue)) {
            try {
                timeoutPropertyValue = Integer.parseInt(timeoutValue);
            } catch (NumberFormatException e) {
                LOG.debug("Failed to read " + timeoutTypeName + " property in identity.xml." +
                        " Expects a number. Using the default value: " + defaultTimeout, e);
            }
        }
        return timeoutPropertyValue;
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

    private String getVersion(String actionTypePropertyName) {

        String versionPropertyValue =
                (String) IdentityConfigParser.getInstance().getConfiguration().get(actionTypePropertyName);
        if (StringUtils.isNotBlank(versionPropertyValue)) {
            return versionPropertyValue;
        }
        return null;
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
            default:
                break;
        }
        excludedParams.addAll(excludedParamsPropertyValue);
        return Collections.unmodifiableSet(excludedParams);
    }

    /**
     * Returns the allowed headers configured in the system configuration
     * 'actions.types.{actionType}.action_request.allowed_headers' for the given action type.
     *
     * @param actionType ActionType
     * @return a set of headers to allow in the action request.
     */
    public Set<String> getAllowedHeadersForActionType(ActionType actionType) {

        Set<String> allowedHeaders = new HashSet<>();
        String allowedPropertyKey = null;
        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                allowedPropertyKey = ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getAllowedHeaderProperty();
                break;
            default:
                break;
        }

        if (allowedPropertyKey != null) {
            allowedHeaders.addAll(getPropertyValues(allowedPropertyKey));
        }

        return Collections.unmodifiableSet(allowedHeaders);
    }

    /**
     * Returns the allowed parameters configured in the system configuration
     * 'actions.types.{actionType}.action_request.allowed_parameters' for the given action type.
     *
     * @param actionType ActionType
     * @return a set of parameters to allow in the action request.
     */
    public Set<String> getAllowedParamsForActionType(ActionType actionType) {

        Set<String> allowedParameters = new HashSet<>();
        String allowedPropertyKey = null;
        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                allowedPropertyKey = ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getAllowedParamsProperty();
                break;
            default:
                break;
        }

        if (allowedPropertyKey != null) {
            allowedParameters.addAll(getPropertyValues(allowedPropertyKey));
        }

        return Collections.unmodifiableSet(allowedParameters);
    }

    /**
     * Returns the up to which version is retired for the given action type. If not configured, returns null.
     *
     * @param actionType ActionType
     * @return Retired up to version as a String.
     */
    public String getRetiredUpToVersion(ActionType actionType) {

        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                return getVersion(ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getRetiredUpToVersionProperty());
            case AUTHENTICATION:
                return getVersion(ActionTypeConfig.AUTHENTICATION.getRetiredUpToVersionProperty());
            case PRE_UPDATE_PASSWORD:
                return getVersion(ActionTypeConfig.PRE_UPDATE_PASSWORD.getRetiredUpToVersionProperty());
            case PRE_UPDATE_PROFILE:
                return getVersion(ActionTypeConfig.PRE_UPDATE_PROFILE.getRetiredUpToVersionProperty());
            case PRE_ISSUE_ID_TOKEN:
                return getVersion(ActionTypeConfig.PRE_ISSUE_ID_TOKEN.getRetiredUpToVersionProperty());
            default:
                return null;
        }
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
    private enum ActionTypeConfig {
        PRE_ISSUE_ACCESS_TOKEN("Actions.Types.PreIssueAccessToken.Enable",
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreIssueAccessToken.ActionRequest.AllowedHeaders.Header",
                "Actions.Types.PreIssueAccessToken.ActionRequest.AllowedParameters.Parameter",
                "Actions.Types.PreIssueAccessToken.Version.RetiredUpTo"),
        AUTHENTICATION("Actions.Types.Authentication.Enable",
                "Actions.Types.Authentication.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.Authentication.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.Authentication.ActionRequest.AllowedHeaders.Header",
                "Actions.Types.Authentication.ActionRequest.AllowedParameters.Parameter",
                "Actions.Types.Authentication.Version.RetiredUpTo"),
        PRE_UPDATE_PASSWORD("Actions.Types.PreUpdatePassword.Enable",
                "Actions.Types.PreUpdatePassword.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreUpdatePassword.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreUpdatePassword.ActionRequest.AllowedHeaders.Header",
                "Actions.Types.PreUpdatePassword.ActionRequest.AllowedParameters.Parameter",
                "Actions.Types.PreUpdatePassword.Version.RetiredUpTo"),
        PRE_UPDATE_PROFILE("Actions.Types.PreUpdateProfile.Enable",
                "Actions.Types.PreUpdateProfile.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreUpdateProfile.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreUpdateProfile.ActionRequest.AllowedHeaders.Header",
                "Actions.Types.PreUpdateProfile.ActionRequest.AllowedParameters.Parameter",
                "Actions.Types.PreUpdateProfile.Version.RetiredUpTo"),
        PRE_ISSUE_ID_TOKEN("Actions.Types.PreIssueIdToken.Enable",
                "Actions.Types.PreIssueIdToken.ActionRequest.ExcludedHeaders.Header",
                "Actions.Types.PreIssueIdToken.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreIssueIdToken.ActionRequest.AllowedHeaders.Header",
                "Actions.Types.PreIssueIdToken.ActionRequest.AllowedParameters.Parameter",
                "Actions.Types.PreIssueIdToken.Version.RetiredUpTo");

        private final String actionTypeEnableProperty;
        private final String excludedHeadersProperty;
        private final String excludedParamsProperty;
        private final String allowedHeaderProperty;
        private final String allowedParamsProperty;
        private final String retiredUpToVersionProperty;

        ActionTypeConfig(String actionTypeEnableProperty, String excludedHeadersProperty,
                         String excludedParamsProperty, String allowedHeaderProperty, String allowedParamsProperty,
                         String retiredUpToVersionProperty) {

            this.actionTypeEnableProperty = actionTypeEnableProperty;
            this.excludedHeadersProperty = excludedHeadersProperty;
            this.excludedParamsProperty = excludedParamsProperty;
            this.allowedHeaderProperty = allowedHeaderProperty;
            this.allowedParamsProperty = allowedParamsProperty;
            this.retiredUpToVersionProperty = retiredUpToVersionProperty;
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

        public String getAllowedHeaderProperty() {

            return allowedHeaderProperty;
        }

        public String getAllowedParamsProperty() {

            return allowedParamsProperty;
        }

        public String getRetiredUpToVersionProperty() {

            return retiredUpToVersionProperty;
        }
    }
}
