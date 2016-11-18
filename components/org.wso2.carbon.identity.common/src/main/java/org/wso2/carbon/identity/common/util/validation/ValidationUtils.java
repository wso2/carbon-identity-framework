/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.common.util.validation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.Constants;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.internal.config.ConfigParser;
import org.wso2.carbon.identity.common.internal.validation.ValidationConfig;
import org.wso2.carbon.identity.common.util.IdentityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines utility methods to be used with input validation
 */
public class ValidationUtils {

    private static final ValidationConfig validatorConfig = new ValidationConfig();
    private static final String msgSection1 = "The provided input ";
    private static final String msgSection2 = "does not match any of the white list patterns [ %s ]";
    private static final String msgSection3 =
            "contains illegal characters matching one of the black list patterns [ %s ]";
    private static final String msgSection4 = " or ";
    private static final String PATTERN_NOT_REGISTERED = "No regex pattern registered for the provided key : %s";
    public static final String DEFAULT_FILE_NAME_REGEX = "^(?!(?:CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\.[^.]*)?$)" +
                                                         "[^<>:\"/\\\\|?*\\x00-\\x1F]*[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]$";

    private static Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

    private static volatile ValidationUtils instance = null;

    public static final ThreadLocal<Map<String, Object>> threadLocals = new ThreadLocal<Map<String, Object>> () {

        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap();
        }
    };

    private ValidationUtils() {
        ConfigParser.getInstance();
    }

    public static ValidationUtils getInstance() {
        if(instance == null) {
            synchronized (ValidationUtils.class) {
                if(instance == null) {
                    instance = new ValidationUtils();
                }
            }
        }
        return instance;
    }

    /**
     * Defines a predefined set of pattern list
     */
    public static enum ValidatorPattern {
        DIGITS_ONLY("^[0-9]+$"),
        ALPHABETIC_ONLY("^[a-zA-Z]+$"),
        ALPHANUMERICS_ONLY("^[a-zA-Z0-9]+$"),
        URL("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$"),
        EMAIL("^\\s*?(.+)@(.+?)\\s*$"),
        WHITESPACE_EXISTS(".*\\s+.*"),
        URI_RESERVED_EXISTS(".*[:/\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=]+.*"),
        URI_UNSAFE_EXISTS(".*[<>%\\{\\}\\|\\^~\\[\\]`]+.*"),
        HTML_META_EXISTS(".*[&<>\"'/]+.*"),
        XML_META_EXISTS(".*[&<>\"']+.*"),
        REGEX_META_EXISTS(".*[\\\\\\^\\$\\.\\|\\?\\*\\+\\(\\)\\[\\{]+.*"),
        HTTP_URL("^(http:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$"),
        HTTPS_URL("^(https:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$"),
        FTP_URL("^(ftp:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$"),
        REGISTRY_INVALID_CHARS_EXISTS("[~!@#;%^*()+={}|<>\\\\\"'/,]+");

        private String regex;

        ValidatorPattern(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }

    static {
        for (ValidatorPattern pattern : ValidatorPattern.values()) {
            validatorConfig.addPattern(pattern.name(), pattern.getRegex());
        }
    }

    /**
     * Validates the provided input against the given white list patterns
     *
     * @param input             input
     * @param whiteListPatterns a String array of white list pattern keys
     * @return true if matches with any of the white list patterns
     */
    public static boolean isValidOverWhiteListPatterns(String input, String... whiteListPatterns) {
        if (ArrayUtils.isEmpty(whiteListPatterns)) {
            throw new IllegalArgumentException("Should provide at least one white list pattern");
        }

        if (StringUtils.isEmpty(input)) {
            return true;
        }

        validatePatternKeys(whiteListPatterns);

        boolean isValid = false;
        for (String key : whiteListPatterns) {
            if (validatorConfig.getPattern(key) != null) {
                isValid = validatorConfig.getPattern(key).matcher(input).matches();
                if (isValid) {
                    break;
                }
            }
        }

        return isValid;
    }

    /**
     * Validates the provided input against the given black list patterns
     *
     * @param input             input
     * @param blackListPatterns a String array of black list pattern keys
     * @return true if does not match with any of the black list patterns
     */
    public static boolean isValidOverBlackListPatterns(String input, String... blackListPatterns) {
        if (ArrayUtils.isEmpty(blackListPatterns)) {
            throw new IllegalArgumentException("Should provide at least one black list pattern");
        }

        if (StringUtils.isEmpty(input)) {
            return true;
        }

        validatePatternKeys(blackListPatterns);

        boolean isValid = false;
        for (String key : blackListPatterns) {
            if (validatorConfig.getPattern(key) != null) {
                isValid = !validatorConfig.getPattern(key).matcher(input).matches();
                if (!isValid) {
                    break;
                }
            }
        }

        return isValid;
    }

    /**
     * Validates the provided input against the given white list and black list patterns.
     * Precedence was give to the white list patterns. Thus, if the input is both white listed and blacklisted it
     * will be considered as valid.
     *
     * @param input             input
     * @param whiteListPatterns a String array of white list pattern keys
     * @param blackListPatterns a String array of black list pattern keys
     * @return isWhiteListed || isNotBlackListed
     */
    public static boolean isValid(String input, String[] whiteListPatterns, String[] blackListPatterns) {
        if (ArrayUtils.isEmpty(whiteListPatterns) || ArrayUtils.isEmpty(blackListPatterns)) {
            throw new IllegalArgumentException("Should provide at least one white list pattern and black list pattern");
        }

        return isValidOverWhiteListPatterns(input, whiteListPatterns) ||
                isValidOverBlackListPatterns(input, blackListPatterns);

    }

    /**
     * Returns the input if valid over the given white list patterns else throws an InputValidationException
     *
     * @param input             input
     * @param whiteListPatterns a String array of white list pattern keys
     * @return input if valid over the given white list patterns else throws an InputValidationException
     */
    public static String getValidInputOverWhiteListPatterns(String input, String... whiteListPatterns)
            throws IdentityException {

        if (StringUtils.isEmpty(input) || isValidOverWhiteListPatterns(input, whiteListPatterns)) {
            return input;
        }

        throw IdentityException.error(msgSection1 + String.format(msgSection2, getPatternString(whiteListPatterns)));
    }

    /**
     * Returns the input if valid over the given black list patterns else throws an InputValidationException
     *
     * @param input             input
     * @param blackListPatterns a String array of black list pattern keys
     * @return input if valid over the given black list patterns else throws an InputValidationException
     */
    public static String getValidInputOverBlackListPatterns(String input, String... blackListPatterns)
            throws IdentityException {

        if (StringUtils.isEmpty(input) || isValidOverBlackListPatterns(input, blackListPatterns)) {
            return input;
        }

        throw IdentityException.error(msgSection1 + String.format(msgSection3, getPatternString(blackListPatterns)));
    }

    /**
     * Returns the input if valid over the given white list and black list patterns else throws an
     * InputValidationException
     *
     * @param input             input
     * @param whiteListPatterns a String array of white list pattern keys
     * @param blackListPatterns a String array of black list pattern keys
     * @return input if valid over the given white list and black list patterns else throws an InputValidationException
     */
    public static String getValidInput(String input, String[] whiteListPatterns, String[] blackListPatterns)
            throws IdentityException {

        if (StringUtils.isEmpty(input) || isValid(input, whiteListPatterns, blackListPatterns)) {
            return input;
        }

        StringBuilder message = new StringBuilder();
        message.append(msgSection1);
        message.append(String.format(msgSection2, getPatternString(whiteListPatterns)));
        message.append(msgSection4);
        message.append(String.format(msgSection3, getPatternString(blackListPatterns)));

        throw IdentityException.error(message.toString());
    }

    /**
     * Adds a validation pattern and stores it against the provided key.
     * Throws an IllegalArgumentException if pattern key or pattern is empty, or if a pattern exists for the given key
     *
     * @param key   pattern key
     * @param regex pattern regex
     */
    public static void addPattern(String key, String regex) {
        validatorConfig.addPattern(key, regex);
    }

    /**
     * Removes a validation pattern
     *
     * @param key pattern key
     */
    public static void removePattern(String key) {
        validatorConfig.removePattern(key);
    }

    /**
     * Checks if a pattern exists for the provided key
     *
     * @param key pattern key
     * @return true if pattern exists or false if pattern does not exist
     */
    public static boolean patternExists(String key) {
        return validatorConfig.patternExists(key);
    }

    private static String getPatternString(String[] patterns) {
        StringBuilder patternString = new StringBuilder();
        for (int i = 0; i < patterns.length; i++) {
            patternString.append(validatorConfig.getPattern(patterns[i]).pattern());
            if ((patterns.length - 1) != i) {
                patternString.append(", ");
            }
        }

        return patternString.toString();
    }

    /**
     * Check if all provided patterns keys have a corresponding regex registered.
     *
     * @param patterns array of pattern keys to be checked
     */
    private static void validatePatternKeys(String[] patterns) {
        for (String key : patterns) {
            if (!patternExists(key)) {
                throw new IllegalArgumentException(String.format(PATTERN_NOT_REGISTERED, key));
            }
        }
    }

    public static boolean isNotBlank(String input) {
        if (StringUtils.isNotBlank(input) && !Constants.NULL.equals(input.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBlank(String input) {
        if (StringUtils.isBlank(input) || Constants.NULL.equals(input.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidFileName(String fileName){
        String fileNameRegEx = null; // read filename regex from identity.yaml

        if(isBlank(fileNameRegEx)){
            fileNameRegEx = DEFAULT_FILE_NAME_REGEX;
        }

        Pattern pattern = Pattern.compile(fileNameRegEx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE |
                                                         Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }
}
