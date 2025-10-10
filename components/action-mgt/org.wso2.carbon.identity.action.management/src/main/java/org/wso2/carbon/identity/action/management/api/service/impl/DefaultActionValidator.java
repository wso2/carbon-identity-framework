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

package org.wso2.carbon.identity.action.management.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Action.ActionTypes;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.service.ActionValidator;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementConfig;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementExceptionHandler;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Action validator class.
 */
public class DefaultActionValidator implements ActionValidator {

    private static final String ACTION_NAME_REGEX = "^[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*[a-zA-Z0-9-_]$";
    private static final String ENDPOINT_URI_REGEX = "^https?://[^\\s/$.?#]\\S*";
    // According to RFC 9910 a header name must contain only alphanumeric characters, period (.) and hyphen (-),
    // and should start with an alphanumeric character.
    private static final String HEADER_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9-.]+$";
    private static final String GENERAL_DELIMITER_REGEX = "[:/?#\\[\\]@]";

    private final Pattern actionNameRegexPattern = Pattern.compile(ACTION_NAME_REGEX);
    private final Pattern endpointUriRegexPattern = Pattern.compile(ENDPOINT_URI_REGEX);
    private final Pattern headerRegexPattern = Pattern.compile(HEADER_REGEX);
    private final Pattern generalDelimiterRegex = Pattern.compile(GENERAL_DELIMITER_REGEX);

    static DefaultActionValidator instance = new DefaultActionValidator();
    private static final Log LOG = LogFactory.getLog(DefaultActionValidator.class);

    public static DefaultActionValidator getInstance() {
        return instance;
    }

    @Override
    public ActionTypes getSupportedActionType() {

         throw new UnsupportedOperationException("This method is not allowed for DefaultActionValidator.");
    }

    /**
     * Perform pre validations on action model when creating an action.
     *
     * @param action Action creation model.
     * @throws ActionMgtException if action model is invalid.
     */
    @Override
    public void doPreAddActionValidations(Action.ActionTypes actionType, String actionVersion, Action action)
            throws ActionMgtException {

        validateForBlank(ActionMgtConstants.ACTION_NAME_FIELD, action.getName());
        validateForBlank(ActionMgtConstants.ENDPOINT_URI_FIELD, action.getEndpoint().getUri());
        validateActionName(action.getName());
        validateEndpointUri(action.getEndpoint().getUri());
        doEndpointAuthenticationValidation(action.getEndpoint().getAuthentication());
        doValidateAllowedHeaders(action.getEndpoint().getAllowedHeaders());
        doValidateAllowedParams(action.getEndpoint().getAllowedParameters());
        isRulesApplicableForActionVersion(actionVersion, action);
    }

    /**
     * Perform pre validations on action model when updating an existing action.
     * This is specifically used during HTTP PATCH operation and only validate non-null and non-empty fields.
     *
     * @param action Action update model.
     * @throws ActionMgtException if action model is invalid.
     */
    @Override
    public void doPreUpdateActionValidations(Action.ActionTypes actionType, String actionVersion, Action action)
            throws ActionMgtException {

        validateActionVersion(actionType, action.getActionVersion());
        if (action.getName() != null) {
            validateActionName(action.getName());
        }
        if (action.getEndpoint() != null && action.getEndpoint().getUri() != null) {
            validateEndpointUri(action.getEndpoint().getUri());
        }
        if (action.getEndpoint() != null && action.getEndpoint().getAuthentication() != null) {
            doEndpointAuthenticationValidation(action.getEndpoint().getAuthentication());
        }
        if (action.getEndpoint() != null) {
            doValidateAllowedHeaders(action.getEndpoint().getAllowedHeaders());
            doValidateAllowedParams(action.getEndpoint().getAllowedParameters());
        }
        isRulesApplicableForActionVersion(actionVersion, action);
    }

    /**
     * Perform pre validations on endpoint authentication model.
     *
     * @param authentication Endpoint authentication model.
     * @throws ActionMgtClientException if endpoint authentication model is invalid.
     */
    public void doEndpointAuthenticationValidation(Authentication authentication) throws ActionMgtClientException {

        Authentication.Type authenticationType = authentication.getType();
        validateForBlank(ActionMgtConstants.ENDPOINT_AUTHENTICATION_TYPE_FIELD,
                authenticationType.getName());
        switch (authenticationType) {
            case BASIC:
                validateForBlank(ActionMgtConstants.USERNAME_FIELD,
                        authentication.getProperty(Authentication.Property.USERNAME).getValue());
                validateForBlank(ActionMgtConstants.PASSWORD_FIELD,
                        authentication.getProperty(Authentication.Property.PASSWORD).getValue());
                break;
            case BEARER:
                validateForBlank(ActionMgtConstants.ACCESS_TOKEN_FIELD,
                        authentication.getProperty(Authentication.Property.ACCESS_TOKEN).getValue());
                break;
            case API_KEY:
                String apiKeyHeader = authentication.getProperty(Authentication.Property.HEADER).getValue();
                validateForBlank(ActionMgtConstants.API_KEY_HEADER_FIELD, apiKeyHeader);
                validateHeader(apiKeyHeader);
                validateForBlank(ActionMgtConstants.API_KEY_VALUE_FIELD,
                        authentication.getProperty(Authentication.Property.VALUE).getValue());
                break;
            case NONE:
            default:
                break;
        }
    }

    /**
     * Validates the list of allowed headers for an action.
     *
     * @param allowedHeaders List of allowed headers to validate.
     * @throws ActionMgtClientException If any header is invalid or excluded.
     */
    public void doValidateAllowedHeaders(List<String> allowedHeaders)
            throws ActionMgtClientException {

        if (allowedHeaders == null) {
            return;
        }

        for (String header : allowedHeaders) {
            validateForBlank(ActionMgtConstants.ALLOWED_HEADERS_FIELD, header);
            validateHeader(header);
        }
        validateAllowedHeaders(allowedHeaders);
    }

    /**
     * Validates the list of allowed parameters for an action.
     *
     * @param allowedParameters List of allowed parameters to validate.
     * @throws ActionMgtClientException If any parameter is invalid or excluded.
     */
    public void doValidateAllowedParams(List<String> allowedParameters)
            throws ActionMgtClientException {

        if (allowedParameters == null) {
            return;
        }

        for (String param : allowedParameters) {
            validateForBlank(ActionMgtConstants.ALLOWED_PARAMETERS_FIELD, param);
            validateParameter(param);
        }
        validateAllowedParameters(allowedParameters);
    }

    /**
     * Validate configured headers by filtering out excluded headers that are configured at server level.
     *
     * @param allowedHeadersInAction List of allowed headers configured at action level.
     * @throws ActionMgtClientException If any header is excluded by the server configuration.
     */
    private void validateAllowedHeaders(List<String> allowedHeadersInAction) throws ActionMgtClientException {

        List<String> excludedHeadersServerConfig = ActionManagementConfig.getInstance().getPropertyValues(
                ActionManagementConfig.ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getExcludedHeadersProperty());
        boolean hasExcluded = allowedHeadersInAction.stream().anyMatch(excludedHeadersServerConfig::contains);
        if (hasExcluded) {
            throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_NOT_ALLOWED_HEADER);
        }
    }

    /**
     * Validate configured parameters by filtering out excluded parameters that are configured at server level.
     *
     * @param allowedParametersInAction List of allowed parameters configured at action level.
     * @throws ActionMgtClientException If any parameter is excluded by the server configuration.
     */
    private void validateAllowedParameters(List<String> allowedParametersInAction) throws ActionMgtClientException {

        List<String> excludedParamsServerConfig = ActionManagementConfig.getInstance().getPropertyValues(
                ActionManagementConfig.ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getExcludedParamsProperty());
        boolean hasExcluded = allowedParametersInAction.stream().anyMatch(excludedParamsServerConfig::contains);
        if (hasExcluded) {
            throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_NOT_ALLOWED_PARAMETER);
        }
    }

    /**
     * Validate whether required fields exist.
     *
     * @param fieldValue Field value.
     * @throws ActionMgtClientException if the provided field is empty.
     */
    public void validateForBlank(String fieldName, String fieldValue) throws ActionMgtClientException {

        if (StringUtils.isBlank(fieldValue)) {
            throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_EMPTY_ACTION_REQUEST_FIELD,
                    fieldName);
        }
    }

    /**
     * Validate the action name.
     *
     * @param name Action name.
     * @throws ActionMgtClientException if the name is not valid.
     */
    public void validateActionName(String name) throws ActionMgtClientException {

        boolean isValidName = actionNameRegexPattern.matcher(name).matches();
        if (!isValidName) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_ACTION_REQUEST_FIELD, ActionMgtConstants.ACTION_NAME_FIELD);
        }
    }

    /**
     * Validate the endpoint URI.
     *
     * @param uri Endpoint uri.
     * @throws ActionMgtClientException if the uri is not valid.
     */
    public void validateEndpointUri(String uri) throws ActionMgtClientException {

        boolean isValidUri = endpointUriRegexPattern.matcher(uri).matches();
        if (!isValidUri) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_ACTION_REQUEST_FIELD, ActionMgtConstants.ENDPOINT_URI_FIELD);
        }
    }

    /**
     * Validate the header.
     *
     * @param header Header name.
     * @throws ActionMgtClientException if the header is invalid.
     */
    public void validateHeader(String header) throws ActionMgtClientException {

        boolean isValidHeader = headerRegexPattern.matcher(header).matches();
        if (!isValidHeader) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_ACTION_REQUEST_FIELD, header);
        }
    }

    /**
     * Validate the provided parameter to ensure it does not contain general delimiters.
     *
     * @param param The parameter to validate.
     * @throws ActionMgtClientException If the parameter contains general delimiters.
     */
    public void validateParameter(String param) throws ActionMgtClientException {

        boolean hasGeneralDelimiters = generalDelimiterRegex.matcher(param).find();

        if (hasGeneralDelimiters) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_ACTION_REQUEST_FIELD, param);
        }
    }

    /**
     * Validate the action version update during an update operation to ensure only the latest version is used.
     *
     * @param actionType    Action type.
     * @param actionVersion Action version.
     * @throws ActionMgtException If any error occurred during resolving latest action version or provided version is
     * not action version is not the latest
     */
    public void validateActionVersion(ActionTypes actionType, String actionVersion) throws ActionMgtException {

        if (actionVersion == null) {
            return;
        }

        if (ActionManagementConfig.getInstance().getLatestVersion(actionType).equals(actionVersion)) {
            return;
        }
        throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_INVALID_ACTION_VERSION_UPDATE,
                ActionManagementConfig.getInstance().getLatestVersion(actionType));
    }

    /**
     * Validate the action version update during an update operation to ensure only the latest version is used.
     *
     * @param action    Action.
     * @throws ActionMgtException If any error occurred during resolving latest action version or provided version is
     * not action version is not the latest
     */
    public void isRulesApplicableForActionVersion(String actionVersion, Action action) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Skipping rule revalidation as rules are already validated for the action type. If action " +
                    "version–specific rule validation is required, it must be handled in the corresponding " +
                    "downstream action component.");
        }
    }
}
