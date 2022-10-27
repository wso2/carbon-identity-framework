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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.CharacterCounter;
import org.wso2.carbon.identity.input.validation.mgt.model.DefaultValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.InputValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.PasswordValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RegExValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RepeatedCharacterValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.UniqueCharacterValidator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_DEFAULT_MIN_MAX_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_JAVA_REGEX_INVALID;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_JS_REGEX_INVALID;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MAX_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MAX_LOWER_CASE_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MAX_NUMERALS_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MAX_SPECIAL_CHR_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MAX_UPPER_CASE_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MIN_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MIN_LOWER_CASE_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MIN_NUMERALS_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MIN_SPECIAL_CHR_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_MIN_UPPER_CASE_LENGTH_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_UNIQUE_CHR_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_REGEX_MISMATCH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH;

/**
 * Class to manage util methods.
 */
public class Utils {

    /**
     * Method to validate configuration.
     *
     * @param config    InputValidationConfiguration.
     * @throws InputValidationMgtClientException If an error occurred in validating configuration.
     */
    public static void validateConfig(InputValidationConfiguration config) throws InputValidationMgtClientException {

        // Validate password Configuration.
        PasswordValidator passwordValidator = config.getPasswordValidator();
        if (passwordValidator.getRulesValidator() != null) {
            validateRules(passwordValidator.getRulesValidator());
        } else if (passwordValidator.getRegExValidator() != null) {
            // Validate regex patterns.
            RegExValidator regExValidator = passwordValidator.getRegExValidator();
            String javaRegEx = regExValidator.getJavaRegExPattern();
            String jsRegEx = regExValidator.getJsRegExPattern();
            validateRegex(javaRegEx, ERROR_JAVA_REGEX_INVALID);
            validateRegex(jsRegEx, ERROR_JS_REGEX_INVALID);
        }
    }

    /**
     * This method validates the password.
     *
     * @param configuration Password validation configuration.
     * @param value         Password.
     * @throws InputValidationMgtClientException If error occurred in validating the password.
     */
    public static void validatePassword(InputValidationConfiguration configuration, String value)
            throws InputValidationMgtClientException {

        if (configuration.getPasswordValidator() != null &&
                configuration.getPasswordValidator().getRulesValidator() != null) {

            RulesValidator rulesValidator = configuration.getPasswordValidator().getRulesValidator();
            CharacterCounter counts = Utils.countValues(value);

            if (rulesValidator.getLengthValidator() != null) {
                if (rulesValidator.getLengthValidator().getMin() > 0) {
                    int min = rulesValidator.getLengthValidator().getMin();
                    if (value.length() < min) {
                        handleException(ERROR_VALIDATION_MIN_LENGTH_MISMATCH, PASSWORD, min);
                    }
                }
                if (rulesValidator.getLengthValidator().getMax() > 0) {
                    int max = rulesValidator.getLengthValidator().getMax();
                    if (value.length() > max) {
                        handleException(ERROR_VALIDATION_MAX_LENGTH_MISMATCH, PASSWORD, max);
                    }
                }
            }
            if (rulesValidator.getNumeralsValidator() != null) {
                if (rulesValidator.getNumeralsValidator().getMin() > 0 &&
                        counts.getNumberOfDigits() < rulesValidator.getNumeralsValidator().getMin()) {
                    handleException(ERROR_VALIDATION_MIN_NUMERALS_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getNumeralsValidator().getMin());
                }
                if (rulesValidator.getNumeralsValidator().getMax() > 0 &&
                        counts.getNumberOfDigits() > rulesValidator.getNumeralsValidator().getMax()) {
                    handleException(ERROR_VALIDATION_MAX_NUMERALS_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getNumeralsValidator().getMax());
                }
            }
            if (rulesValidator.getUpperCaseValidator() != null) {
                if (rulesValidator.getUpperCaseValidator().getMin() > 0 &&
                        counts.getNumberOfUpperCase() < rulesValidator.getUpperCaseValidator().getMin()) {
                    handleException(ERROR_VALIDATION_MIN_UPPER_CASE_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getUpperCaseValidator().getMin());
                }
                if (rulesValidator.getUpperCaseValidator().getMax() > 0 &&
                        counts.getNumberOfUpperCase() > rulesValidator.getUpperCaseValidator().getMax()) {
                    handleException(ERROR_VALIDATION_MAX_UPPER_CASE_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getUpperCaseValidator().getMax());
                }
            }
            if (rulesValidator.getLowerCaseValidator() != null) {
                if (rulesValidator.getLowerCaseValidator().getMin() > 0 &&
                        counts.getNumberOfLowerCase() < rulesValidator.getLowerCaseValidator().getMin()) {
                    handleException(ERROR_VALIDATION_MIN_LOWER_CASE_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getLowerCaseValidator().getMin());
                }
                if (rulesValidator.getLowerCaseValidator().getMax() > 0 &&
                        counts.getNumberOfLowerCase() > rulesValidator.getLowerCaseValidator().getMax()) {
                    handleException(ERROR_VALIDATION_MAX_LOWER_CASE_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getLowerCaseValidator().getMax());
                }
            }
            if (rulesValidator.getSpecialCharacterValidator() != null) {
                if (rulesValidator.getSpecialCharacterValidator().getMin() > 0 &&
                        counts.getNumberOfSpecialChrs() < rulesValidator.getSpecialCharacterValidator().getMin()) {
                    handleException(ERROR_VALIDATION_MIN_SPECIAL_CHR_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getSpecialCharacterValidator().getMin());
                }
                if (rulesValidator.getSpecialCharacterValidator().getMax() > 0 &&
                        counts.getNumberOfSpecialChrs() > rulesValidator.getSpecialCharacterValidator().getMax()) {
                    handleException(ERROR_VALIDATION_MAX_SPECIAL_CHR_LENGTH_MISMATCH, PASSWORD,
                            rulesValidator.getSpecialCharacterValidator().getMax());
                }
            }
            if (rulesValidator.getUniqueCharacterValidator() != null &&
                    rulesValidator.getUniqueCharacterValidator().isEnable()
            ) {
                UniqueCharacterValidator uniqueValidator = (UniqueCharacterValidator) rulesValidator
                        .getUniqueCharacterValidator();
                if (Utils.findDistinctChars(value, uniqueValidator.isCaseSensitive()) < uniqueValidator.getMinUniqueCharacter()) {
                    handleException(ERROR_VALIDATION_UNIQUE_CHR_MISMATCH, PASSWORD,
                            uniqueValidator.getMinUniqueCharacter());
                }
            }
            if (rulesValidator.getRepeatedCharacterValidator() != null &&
                    rulesValidator.getRepeatedCharacterValidator().isEnable()
            ) {
                RepeatedCharacterValidator repeatedCharacterValidator = (RepeatedCharacterValidator) rulesValidator
                        .getRepeatedCharacterValidator();
                if (Utils.findMaxConsecutiveLength(value, repeatedCharacterValidator
                        .isCaseSensitive()) > repeatedCharacterValidator.getMaxConsecutiveLength()) {
                    handleException(ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH, PASSWORD,
                            repeatedCharacterValidator.getMaxConsecutiveLength());
                }
            }
        } else if (configuration.getPasswordValidator() != null &&
                configuration.getPasswordValidator().getRegExValidator() != null &&
                StringUtils.isNotEmpty(configuration.getPasswordValidator().getRegExValidator().getJsRegExPattern())
        ) {
            String regex = configuration.getPasswordValidator().getRegExValidator().getJsRegExPattern();
            // Compile the ReGex
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(value);
            if (!m.matches()) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_REGEX_MISMATCH.getCode(),
                        ERROR_VALIDATION_REGEX_MISMATCH.getMessage(),
                        String.format(ERROR_VALIDATION_REGEX_MISMATCH.getDescription(), PASSWORD,
                                value, regex));
            }
        }
    }
    /**
     * This method counts the different type of chars in a string.
     *
     * @param word  String.
     * @return  counts.
     */
    public static CharacterCounter countValues(String word) {

        int digits = 0;
        int upperCase = 0;
        int lowerCase = 0;
        int specialChr = 0;

        for (int i = 0; i < word.length(); i++)
        {
            char chr = word.charAt(i);
            if (chr >= 'A' && chr <= 'Z')
                upperCase++;
            else if (chr >= 'a' && chr <= 'z')
                lowerCase++;
            else if (chr >= '0' && chr <= '9')
                digits++;
            else
                specialChr++;
        }
        return new CharacterCounter(digits, upperCase, lowerCase, specialChr);
    }

    /**
     * This method finds the number of distinct characters in a string.
     *
     * @param word          String.
     * @param caseSensitive Shows whether it is case-sensitive.
     * @return Number of distinct characters.
     */
    public static int findDistinctChars(String word, boolean caseSensitive) {

        Set<Character> distinctChars = new LinkedHashSet<>();

        if (!caseSensitive) {
            word = word.toLowerCase();
        }

        for (int i = 0; i < word.length(); i++) {
            char chr = word.charAt(i);
            distinctChars.add(chr);
        }
        return distinctChars.size();
    }

    /**
     * This method finds the maximum number of consecutive characters.
     *
     * @param word          String.
     * @param caseSensitive Shows whether it is case-sensitive.
     * @return  maximum number of consecutive characters.
     */
    public static int findMaxConsecutiveLength(String word, boolean caseSensitive) {

        int count = 1;
        int consecutiveLen = 1;
        if (!caseSensitive) {
            word = word.toLowerCase();
        }
        for (int i = 1; i < word.length(); i++) {
            if (word.charAt(i) == word.charAt(i - 1)) {
                count++;
                if (count > consecutiveLen) {
                    consecutiveLen = count;
                }
            } else {
                count = 1;
            }
        }
        return consecutiveLen;
    }

    /**
     * Method to validate regex.
     *
     * @param regex Regex pattern.
     * @param error Error.
     * @throws InputValidationMgtClientException If error occurred in validating regex pattern.
     */
    private static void validateRegex(String regex, ErrorMessages error) throws InputValidationMgtClientException {

        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw new InputValidationMgtClientException(error.getCode(),
                    String.format(error.getDescription(), regex));
        }
    }

    /**
     * Method to validate configuration rules.
     *
     * @param rules Set of rules.
     * @throws InputValidationMgtClientException If an error occurred in validating rules.
     */
    private static void validateRules(RulesValidator rules) throws InputValidationMgtClientException {

        // Validate default validators.
        validateRule(rules.getLengthValidator());
        validateRule(rules.getNumeralsValidator());
        validateRule(rules.getLowerCaseValidator());
        validateRule(rules.getUpperCaseValidator());
        validateRule(rules.getSpecialCharacterValidator());

        // Validate unique character validator.
        if (rules.getUniqueCharacterValidator() != null &&
                ((UniqueCharacterValidator) rules.getUniqueCharacterValidator()).getMinUniqueCharacter() < 1) {
            rules.getUniqueCharacterValidator().setEnable(false);
        }
        // Validate unique character validator.
        if (rules.getRepeatedCharacterValidator() != null &&
                ((RepeatedCharacterValidator) rules.getRepeatedCharacterValidator()).getMaxConsecutiveLength() < 1) {
            rules.getRepeatedCharacterValidator().setEnable(false);
        }
    }

    /**
     * Method to validate a rule.
     *
     * @param validator Default validator.
     * @throws InputValidationMgtClientException If an error occurred in validating a rule.
     */
    private static void validateRule(DefaultValidator validator) throws InputValidationMgtClientException {

        if (validator != null) {
            int min = validator.getMin();
            int max = validator.getMax();

            if (min > max) {
                throw new InputValidationMgtClientException(ERROR_DEFAULT_MIN_MAX_MISMATCH.getCode(),
                        String.format(ERROR_DEFAULT_MIN_MAX_MISMATCH.getDescription(), validator.getClass(), min, max));
            }
        }
    }

    private static void handleException(Constants.ErrorMessages error, String data, int limit)
            throws InputValidationMgtClientException {

        throw new InputValidationMgtClientException(error.getCode(), error.getMessage(),
                String.format(error.getDescription(), data, limit));
    }
}
