/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.utils;

import org.wso2.carbon.identity.input.validation.mgt.model.handlers.PasswordValidationConfigurationHandler;
import org.wso2.carbon.identity.input.validation.mgt.model.handlers.UsernameValidationConfigurationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;

/**
 * Class with the constants.
 */
public class Constants {

    public static final String INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME = "input-validation-configurations";
    public static final String INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX = "input-validation-configs-";
    public static final String SECONDARY_USER_STORE_DOMAIN_NAME = "DEFAULT";
    public static final String INPUT_VALIDATION_USERNAME_ENABLED_CONFIG = "InputValidation.Username.Enabled";
    public static final List<String> SUPPORTED_PARAMS = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add(PASSWORD);
                add(USERNAME);
            }});
    public static final Map<String, String> FIELD_VALIDATION_CONFIG_HANDLER_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {{
                put(USERNAME, UsernameValidationConfigurationHandler.class.getSimpleName());
                put(PASSWORD, PasswordValidationConfigurationHandler.class.getSimpleName());
            }});
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";

    /**
     * Class contains the configuration related constants.
     */
    public static class Configs {

        // Validator names.
        public static final String EMAIL_FORMAT_VALIDATOR = "EmailFormatValidator";

        // Keys for password rules validation.
        public static final String ERROR_CODE_PREFIX = "INM-";
        public static final String VALIDATION_TYPE = "validation.type";
        public static final String MIN_LENGTH = "min.length";
        public static final String MAX_LENGTH = "max.length";
        public static final String MIN_UNIQUE_CHR = "min.unique.character";
        public static final String MAX_CONSECUTIVE_CHR = "max.consecutive.character";
        public static final String ENABLE_VALIDATOR = "enable.validator";
        public static final String ENABLE_SPECIAL_CHARACTERS = "enable.special.characters";

        // Keys for password regEx validation.
        public static final String JS_REGEX = "regex";
        public static final String RULES = "RULE";
        public static final String REGEX = "REGEX";
        public static final String PASSWORD = "password";
        public static final String USERNAME = "username";
        public static final String PERIOD = ".";
        public static final String JAVA_REGEX_PATTERN = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])).{8,100}$";
        public static final String DEFAULT_ALPHANUMERIC_REGEX_PATTERN = "^(?=.*[a-zA-Z])[a-zA-Z0-9]+$";
        public static final String ALPHANUMERIC_REGEX_PATTERN_WITH_SPECIAL_CHARACTERS =
                "^(?=.*[a-zA-Z])[a-zA-Z0-9!@#$&'+\\\\=^_.{|}~-]+$";
        public static final String DEFAULT_EMAIL_JAVA_REGEX_PATTERN =
            "(^[\\u00C0-\\u00FFa-zA-Z0-9](?:(?![.+\\-_]{2})[\\u00C0-\\u00FF\\w.+\\-]){0,63}(?=[\\u00C0-\\u00FFa-zA-Z0" +
            "-9]).\\@(?![+.\\-_])(?:(?![.+\\-_]{2})[\\w.+\\-]){0,245}(?=[\\u00C0-\\u00FFa-zA-Z0-9]).\\.[a-zA-Z]{2,10})";
        public static final String DEFAULT_EMAIL_JS_REGEX_PATTERN =
            "(^[\\u00C0-\\u00FFa-zA-Z0-9](?:(?![.+\\-_]{2})[\\u00C0-\\u00FF\\w.+\\-]){0,63}(?=[\\u00C0-\\u00FFa-zA-Z0" +
            "-9]).\\@(?![+.\\-_])(?:(?![.+\\-_]{2})[\\w.+\\-]){0,245}(?=[\\u00C0-\\u00FFa-zA-Z0-9]).\\.[a-zA-Z]{2,10})";

        public static final String INPUT_VALIDATION_DEFAULT_VALIDATOR = "InputValidation.DefaultUserNameValidator";

        public static final String ALPHA_NUMERIC = "alphaNumeric";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client Errors.
        ERROR_NO_CONFIGURATIONS_FOUND("60001",
                "Input validation configurations are not found",
                "Input validation configurations are not configured for the tenant: %s"),
        ERROR_JAVA_REGEX_INVALID("60002",
                "Invalid regex pattern for Java.",
                "Invalid regex pattern provided for Java: %s"),

        ERROR_DEFAULT_MIN_MAX_MISMATCH("60003",
                "Invalid configurations.",
                "Invalid configuration values for %s as min: %s and max: %s"),
        ERROR_VALIDATION_PARAM_NOT_SUPPORTED("60004",
                "Unsupported parameter.",
                "The field: %s is not supported for validation configuration in the tenant %s."),
        ERROR_VALIDATION_REGEX_MISMATCH("60005",
                "REGEX_MISMATCH",
                "The %s does not match the regex: %s"),
        ERROR_VALIDATION_MIN_LENGTH_MISMATCH("60006",
                "MIN_LENGTH_NOT_SATISFIED",
                "The minimum length of %s should be %d."),
        ERROR_VALIDATION_MAX_LENGTH_MISMATCH("60007",
                "MAX_LENGTH_NOT_SATISFIED",
                "The maximum length of %s can be %d"),
        ERROR_VALIDATION_MIN_NUMERALS_LENGTH_MISMATCH("60008",
                "MIN_NUMERALS_LENGTH_NOT_SATISFIED",
                "The %s should contain at least %d digits."),
        ERROR_VALIDATION_MAX_NUMERALS_LENGTH_MISMATCH("60009",
                "MAX_NUMERALS_LENGTH_NOT_SATISFIED",
                "The %s can contain only %d digits."),
        ERROR_VALIDATION_MIN_UPPER_CASE_LENGTH_MISMATCH("60012",
                "MIN_UPPER_CASE_LENGTH_NOT_SATISFIED",
                "The %s should contain at least %d upper case characters."),
        ERROR_VALIDATION_MAX_UPPER_CASE_LENGTH_MISMATCH("60010",
                "MAX_UPPER_CASE_LENGTH_NOT_SATISFIED",
                "The %s can contain only %d upper case characters."),
        ERROR_VALIDATION_MIN_LOWER_CASE_LENGTH_MISMATCH("60011",
                "MIN_LOWER_CASE_LENGTH_NOT_SATISFIED",
                "The %s should contain at least %d lower case characters."),
        ERROR_VALIDATION_MAX_LOWER_CASE_LENGTH_MISMATCH("60012",
                "MAX_LOWER_CASE_LENGTH_NOT_SATISFIED",
                "The %s can contain only %d lower case characters."),
        ERROR_VALIDATION_MIN_SPECIAL_CHR_LENGTH_MISMATCH("60013",
                "MIN_SPECIAL_CHARACTER_LENGTH_NOT_SATISFIED",
                "The %s should contain at least %d special characters."),
        ERROR_VALIDATION_MAX_SPECIAL_CHR_LENGTH_MISMATCH("60014",
                "MAX_SPECIAL_CHARACTER_LENGTH_NOT_SATISFIED",
                "The %s can contain only %d special characters."),
        ERROR_VALIDATION_UNIQUE_CHR_MISMATCH("60015",
                "MIN_UNIQUE_CHARACTER_LENGTH_NOT_SATISFIED",
                "The %s should contain at least %d unique characters."),
        ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH("60016",
                "MAX_CONSECUTIVE_CHARACTER_LENGTH_NOT_SATISFIED",
                "The %s can contain only %d consecutive characters."),
        ERROR_PROPERTY_TYPE_MISMATCH("60017",
                "PROPERTY_TYPE_MISMATCH",
                "The property %s should be a %s value for the tenant %s."),
        ERROR_PROPERTY_NOT_SUPPORTED("60018",
                "PROPERTY_NOT_SUPPORTED",
                "The property %s not supported for %s in tenant: %s."),
        ERROR_VALIDATOR_NOT_SUPPORTED("60019",
                "VALIDATOR NOT SUPPORTED",
                "The validator %s is not a type of %s validator."),
        ERROR_CODE_CONFIGURE_EITHER_RULES_OR_REGEX("60020",
                "Invalid configuration format",
                "Validation configurations can be configured with one of them: rules or regex."),
        ERROR_CODE_REGEX_MISMATCH("60021",
                "Regex mismatch",
                "The %s should satisfy the %s pattern."),
        ERROR_VALIDATOR_NOT_SUPPORTED_FOR_FIELD("60022",
                "VALIDATOR NOT SUPPORTED FOR FIELD",
                "The %s validator does not support for %s field."),     
        ERROR_INVALID_VALIDATOR_PROPERTY_VALUE("60023",
                "INVALID_VALIDATOR_PROPERTY_VALUE",
                "The %s is an invalid value for property %s."),
        ERROR_VALIDATION_ALPHANUMERIC_FORMAT_MISMATCH("60024",
                "ALPHANUMERIC_VALIDATION_NOT_SATISFIED",
                "The %s can contain only %s pattern."),
        ERROR_VALIDATION_EMAIL_FORMAT_MISMATCH("60025",
                "EMAIL_VALIDATION_NOT_SATISFIED",
                "The %s can contain only %s pattern."),
        ERROR_INPUT_VALUE_NULL("60026",
                "INPUT_VALUE_NULL",
                "The value cannot be null"),
        ERROR_INVALID_VALIDATORS_COMBINATION("60027",
                "INVALID VALIDATOR COMBINATION",
                "Invalid validators combination is provided for field %s."),

        // Server Errors.
        ERROR_GETTING_EXISTING_CONFIGURATIONS("65001",
                "Unable to get input validation configurations.",
                "Error occurred while getting the input validation configurations for the tenant: %s"),
        ERROR_WHILE_ADDING_CONFIGURATIONS("65002",
                "Unable to add the configurations.",
                "Error occurred while adding the input validation configurations for the tenant: %s"),
        ERROR_WHILE_UPDATING_CONFIGURATIONS("65003",
                "Unable to update configurations.",
                "Error occurred while updating the input validation configurations for the tenant: %s");


        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        /**
         * Return the error code.
         *
         * @return Code.
         */
        public String getCode() {

            return code;
        }

        /**
         * Return the error message.
         *
         * @return Message.
         */
        public String getMessage() {

            return message;
        }

        /**
         * Return the error description.
         *
         * @return Description.
         */
        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
