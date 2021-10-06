/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.secret.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;

import java.util.UUID;
import java.util.regex.Pattern;

public class SecretUtils {

    private static final Log log = LogFactory.getLog(SecretUtils.class);
    private static final String SECRET_NAME_REGEX = "^[a-zA-Z][^\\s]{0,63}$";
    private static final String SECRET_VALUE_REGEX = "^.{1,2048}$";
    private static final String SECRET_DESCRIPTION_REGEX = "^.{0,1023}$";

    /**
     * This method can be used to generate a SecretManagementClientException from
     * SecretConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error SecretConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return SecretManagementClientException.
     */
    public static SecretManagementClientException handleClientException(SecretConstants.ErrorMessages error,
                                                                        String... data) {

        String message = populateMessageWithData(error, data);
        return new SecretManagementClientException(message, error.getCode());
    }

    public static SecretManagementClientException handleClientException(SecretConstants.ErrorMessages error,
                                                                        String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new SecretManagementClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a SecretManagementServerException from
     * SecretConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error SecretConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return SecretManagementServerException.
     */
    public static SecretManagementServerException handleServerException(SecretConstants.ErrorMessages error,
                                                                        String... data) {

        String message = populateMessageWithData(error, data);
        return new SecretManagementServerException(message, error.getCode());
    }

    public static SecretManagementServerException handleServerException(SecretConstants.ErrorMessages error,
                                                                        String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new SecretManagementServerException(message, error.getCode(), e);
    }

    public static SecretManagementServerException handleServerException(
            SecretConstants.ErrorMessages error, Throwable e) {

        String message = populateMessageWithData(error);
        return new SecretManagementServerException(message, error.getCode(), e);
    }

    public static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    private static String populateMessageWithData(SecretConstants.ErrorMessages error, String... data) {

        String message;
        if (data != null && data.length != 0) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }

    private static String populateMessageWithData(SecretConstants.ErrorMessages error) {

        return error.getMessage();
    }

    /**
     * Validate secret name according to the regex.
     *
     * @return valid or not
     */
    public static boolean isSecretNameRegexValid(String secretName) {
        Pattern regexPattern = Pattern.compile(SECRET_NAME_REGEX);
        return regexPattern.matcher(secretName).matches();
    }

    /**
     * Validate secret value according to the regex.
     *
     * @return valid or not
     */
    public static boolean isSecretValueRegexValid(String secretValue) {
        Pattern regexPattern = Pattern.compile(SECRET_VALUE_REGEX);
        return regexPattern.matcher(secretValue).matches();
    }

    /**
     * Validate secret description according to the regex.
     *
     * @return valid or not
     */
    public static boolean isSecretDescriptionRegexValid(String description) {
        Pattern regexPattern = Pattern.compile(SECRET_DESCRIPTION_REGEX);
        return regexPattern.matcher(description).matches();
    }

    /**
     * Get the secret name regex pattern.
     */
    public static String getSecretNameRegex() {
        return SECRET_NAME_REGEX;
    }

    /**
     * Get the secret value regex pattern.
     */
    public static String getSecretValueRegex() {
        return SECRET_VALUE_REGEX;
    }

    /**
     * Get the secret description regex pattern.
     */
    public static String getSecretDescriptionRegex() {
        return SECRET_DESCRIPTION_REGEX;
    }
}
