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

package org.wso2.carbon.identity.application.common.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtClientException;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;

import java.util.regex.Pattern;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildClientException;

/**
 * User Defined Local Authenticator Validator class.
 */
public class UserDefinedLocalAuthenticatorValidator {

    private static final String AUTHENTICATOR_NAME_REGEX = "^[custom_][a-zA-Z0-9-_]{3,}$";
    private final Pattern authenticatorNameRegexPattern = Pattern.compile(AUTHENTICATOR_NAME_REGEX);

    private static final String DISPLAY_NAME_REGEX = "^.{3,}$";
    private final Pattern disaplayNameRegexPattern = Pattern.compile(DISPLAY_NAME_REGEX);

    /**
     * Validate whether required fields exist.
     *
     * @param fieldName     Field name.
     * @param fieldValue    Field value.
     * @throws AuthenticatorMgtClientException if the provided field is empty.
     */
    public void validateForBlank(String fieldName, String fieldValue) throws AuthenticatorMgtClientException {

        if (StringUtils.isBlank(fieldValue)) {
            throw buildClientException(AuthenticatorMgtError.ERROR_BLANK_FIELD_VALUE, fieldName);
        }
    }

    /**
     * Validate the user defined local authenticator display name.
     *
     * @param displayName   The display name.
     * @throws AuthenticatorMgtClientException   if the display name is not valid.
     */
    public void validateDisplayName(String displayName) throws AuthenticatorMgtClientException {

        boolean isValidDisplayName = disaplayNameRegexPattern.matcher(displayName).matches();
        if (!isValidDisplayName) {
            throw buildClientException(AuthenticatorMgtError.ERROR_INVALID_DISPLAY_NAME,
                    displayName, DISPLAY_NAME_REGEX);
        }
    }

    /**
     * Validate the user defined local authenticator name.
     *
     * @param name  The authenticator name.
     * @throws AuthenticatorMgtClientException   if the authenticator name is not valid.
     */
    public void validateAuthenticatorName(String name) throws AuthenticatorMgtClientException {

        boolean isValidName = authenticatorNameRegexPattern.matcher(name).matches();
        if (!isValidName) {
            throw buildClientException(AuthenticatorMgtError.ERROR_INVALID_AUTHENTICATOR_NAME,
                    name, AUTHENTICATOR_NAME_REGEX);
        }
    }
}
