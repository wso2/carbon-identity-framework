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

package org.wso2.carbon.identity.action.management.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;

import java.util.regex.Pattern;

/**
 * Utility class for Action Management.
 */
public class ActionManagementUtil {

    public static final String ACTION_NAME_REGEX = "^[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*[a-zA-Z0-9-_]$";
    public static final String ENDPOINT_URI_REGEX = "^[a-zA-Z][a-zA-Z0-9+.-]*://[^\\s/$.?#].\\S*";
    public static final String HEADER_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9-.]+$";

    /**
     * Handle Action Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return ActionMgtClientException.
     */
    public static ActionMgtClientException handleClientException(
            ActionMgtConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ActionMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle Action Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return ActionMgtServerException.
     */
    public static ActionMgtServerException handleServerException(
            ActionMgtConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ActionMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Validate whether required fields exist.
     *
     * @param field Field value.
     * @throws ActionMgtClientException if the provided field is empty.
     */
    public static void isFieldEmpty(String field) throws ActionMgtClientException {

        if (StringUtils.isBlank(field)) {
            throw ActionManagementUtil.handleClientException(ActionMgtConstants.ErrorMessages.
                    ERROR_EMPTY_ACTION_REQUEST_FIELD);
        }
    }

    /**
     * Validate the action name.
     *
     * @param name Action name.
     * @throws ActionMgtClientException if the name is not valid.
     */
    public static void isValidActionName(String name) throws ActionMgtClientException {

        Pattern regexPattern = Pattern.compile(ACTION_NAME_REGEX);
        boolean isValidName = regexPattern.matcher(name).matches();
        if (!isValidName) {
            throw ActionManagementUtil.handleClientException(ActionMgtConstants.ErrorMessages.
                    ERROR_INVALID_ACTION_REQUEST_FIELD);
        }
    }

    /**
     * Validate the endpoint URI.
     *
     * @param uri Endpoint uri.
     * @throws ActionMgtClientException if the uri is not valid.
     */
    public static void isValidEndpointUri(String uri) throws ActionMgtClientException {

        Pattern regexPattern = Pattern.compile(ENDPOINT_URI_REGEX);
        boolean isValidUri = regexPattern.matcher(uri).matches();
        if (!isValidUri) {
            throw ActionManagementUtil.handleClientException(ActionMgtConstants.ErrorMessages.
                    ERROR_INVALID_ACTION_REQUEST_FIELD);
        }
    }

    /**
     * Validate the header.
     *
     * @param header Header name.
     * @throws ActionMgtClientException if the header is invalid.
     */
    public static void isValidHeader(String header) throws ActionMgtClientException {

        Pattern regexPattern = Pattern.compile(HEADER_REGEX);
        boolean isValidHeader = regexPattern.matcher(header).matches();
        if (!isValidHeader) {
            throw ActionManagementUtil.handleClientException(ActionMgtConstants.ErrorMessages.
                    ERROR_INVALID_ACTION_REQUEST_FIELD);
        }
    }
}
