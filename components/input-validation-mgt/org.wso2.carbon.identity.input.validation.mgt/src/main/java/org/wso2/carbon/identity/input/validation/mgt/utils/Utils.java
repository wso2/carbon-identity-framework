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

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.*;

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
    public static int findDistinctChrs(String word, boolean caseSensitive) {

        Set<Character> distinctChrs = new LinkedHashSet<Character>();

        if (!caseSensitive) {
            word = word.toLowerCase();
        }

        for (int i = 0; i < word.length(); i++) {
            char chr = word.charAt(i);
            distinctChrs.add(chr);
        }
        return distinctChrs.size();
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
}
