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

import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtClientException;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;

import java.util.regex.Pattern;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildClientException;

/**
 * User Defined Local Authenticator Validator class.
 */
public class UserDefinedLocalAuthenticatorValidator {

    private static final String AUTHENTICATOR_NAME_REGEX = "^custom-[a-zA-Z0-9-_]{3,}$";
    private final Pattern authenticatorNameRegexPattern = Pattern.compile(AUTHENTICATOR_NAME_REGEX);

    private static final String DISPLAY_NAME_REGEX = "^.{3,}$";
    private final Pattern disaplayNameRegexPattern = Pattern.compile(DISPLAY_NAME_REGEX);

    private static final String URL_REGEX = "^https?://.+";
    private final Pattern urlRegexPattern = Pattern.compile(URL_REGEX);

    private static final String AMR_VALUE_REGEX = "^[a-zA-Z0-9_-]{2,}$";
    private final Pattern amrRegexPattern = Pattern.compile(AMR_VALUE_REGEX); //Configure AMR value regex pattern in XML

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

    /**
     * Validate the user defined local authenticator name.
     *
     * @param url  The url which need to be validated.
     * @throws AuthenticatorMgtClientException   if the authenticator name is not valid.
     */
    public void validateUrl(String url) throws AuthenticatorMgtClientException {

        boolean isValidName = urlRegexPattern.matcher(url).matches();
        if (!isValidName) {
            throw buildClientException(AuthenticatorMgtError.ERROR_INVALID_URL,
                    url, URL_REGEX);
        }
    }

    /**
     * Validate the user defined local authenticator AMR value.
     *
     * @param amrValue The AMR value.
     * @throws AuthenticatorMgtClientException if the AMR value is not valid.
     */
    public void validateAmrValue(String amrValue) throws AuthenticatorMgtClientException {

        boolean isValid = amrRegexPattern.matcher(amrValue).matches();
        if (!isValid) {
            throw buildClientException(AuthenticatorMgtError.ERROR_INVALID_AMR_VALUE,
                    amrValue, AMR_VALUE_REGEX);
        }
    }
}
