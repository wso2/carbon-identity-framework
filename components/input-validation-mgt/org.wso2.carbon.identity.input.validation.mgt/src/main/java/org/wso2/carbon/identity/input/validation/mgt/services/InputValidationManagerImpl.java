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

package org.wso2.carbon.identity.input.validation.mgt.services;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtServerException;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.*;
import org.wso2.carbon.identity.input.validation.mgt.utils.Constants;
import org.wso2.carbon.identity.input.validation.mgt.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.*;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.*;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_NAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME;

/**
 * Class for Input Validation Manager Implementation.
 */
public class InputValidationManagerImpl implements InputValidationManager {

    private static final List<String> SUPPORTED_PARAMS = Arrays.asList(
            PASSWORD
    );

    @Override
    public InputValidationConfiguration updateInputValidationConfiguration(InputValidationConfiguration config,
                                                                           String tenantDomain)
            throws InputValidationMgtException {

        Utils.validateConfig(config);
        Resource oldResource = null;
        try {
            oldResource = getConfigurationManager().getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME,
                    INPUT_VAL_CONFIG_RESOURCE_NAME);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw new InputValidationMgtServerException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(),
                        String.format(ERROR_GETTING_EXISTING_CONFIGURATIONS.getMessage(), tenantDomain));
            }
        }
        Resource newResource = buildResourceFromInputValidationConfig(config);
        Resource updatedResource;

        if (oldResource == null) {
            // Add the configurations for the first time.
            try {
                updatedResource =
                        getConfigurationManager().addResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, newResource);
            } catch (ConfigurationManagementException e) {
                throw new InputValidationMgtServerException(ERROR_WHILE_ADDING_CONFIGURATIONS.getCode(),
                        String.format(ERROR_WHILE_ADDING_CONFIGURATIONS.getMessage(), tenantDomain));
            }
        } else {
            // Update existing configurations.
            try {
                updatedResource =
                        getConfigurationManager().replaceResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, newResource);
            } catch (ConfigurationManagementException e) {
                throw new InputValidationMgtServerException(ERROR_WHILE_UPDATING_CONFIGURATIONS.getCode(),
                        String.format(ERROR_WHILE_UPDATING_CONFIGURATIONS.getMessage(), tenantDomain));
            }
        }
        return buildInputValidationConfigFromResource(updatedResource);
    }

    @Override
    public InputValidationConfiguration getInputValidationConfiguration(String tenantDomain)
            throws InputValidationMgtException {

        Resource resource;
        try {
            resource = getConfigurationManager().getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME,
                    INPUT_VAL_CONFIG_RESOURCE_NAME);
        } catch (ConfigurationManagementException e) {
            if (ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw new InputValidationMgtClientException(ERROR_NO_CONFIGURATIONS_FOUND.getCode(),
                        String.format(ERROR_NO_CONFIGURATIONS_FOUND.getDescription(), tenantDomain));
            }
            throw new InputValidationMgtServerException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(),
                    String.format(ERROR_GETTING_EXISTING_CONFIGURATIONS.getMessage(), tenantDomain));
        }
        return buildInputValidationConfigFromResource(resource);
    }

    @Override
    public void validateValues (String tenantDomain, ValidationParam param) throws InputValidationMgtException {

        if (param == null || StringUtils.isEmpty(param.getName()) || StringUtils.isEmpty(param.getValue())) {
            throw new InputValidationMgtClientException(ERROR_VALIDATION_PARAM_EMPTY.getCode(),
                    String.format(ERROR_VALIDATION_PARAM_EMPTY.getDescription(), tenantDomain));
        }
        if (!SUPPORTED_PARAMS.contains(param.getName())) {
            throw new InputValidationMgtClientException(ERROR_VALIDATION_PARAM_NOT_SUPPORTED.getCode(),
                    String.format(ERROR_VALIDATION_PARAM_NOT_SUPPORTED.getDescription(), param.getName()));
        }

        InputValidationConfiguration configuration = getInputValidationConfiguration(tenantDomain);
        String value = param.getValue();
        String name = param.getName();

        if (StringUtils.equalsIgnoreCase(name, PASSWORD)) {
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
                    if (Utils.findDistinctChrs(value, uniqueValidator.isCaseSensitive()) < uniqueValidator.getMinUniqueCharacter()) {
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
                            String.format(ERROR_VALIDATION_REGEX_MISMATCH.getDescription(), param.getName(),
                                    param.getValue(), regex));
                }
            }
        }
    }

    /**
     * Method to build resource from input validation configuration.
     *
     * @param config    Input validation configuration.
     * @return resource.
     */
    private Resource buildResourceFromInputValidationConfig(InputValidationConfiguration config) {

        Resource resource = new Resource();
        resource.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME);
        Map<String, String> configAttributes = new HashMap<>();

        if (config.getPasswordValidator().getRulesValidator() != null) {
            RulesValidator pswRulesValidator = config.getPasswordValidator().getRulesValidator();
            // Add the params
            configAttributes.put(INPUT_VAL_PASSWORD_VALIDATION_TYPE, RULES);

            // Add attributes for length validation.
            if (pswRulesValidator.getLengthValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getLengthValidator().getMax()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getLengthValidator().getMin()));
            }
            // Add attributes for numerals length validation.
            if (pswRulesValidator.getNumeralsValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_NUMERALS_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getNumeralsValidator().getMax()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_NUMERALS_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getNumeralsValidator().getMin()));
            }
            // Add attributes for lower case letters validation.
            if (pswRulesValidator.getLowerCaseValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_LOWER_CASE_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getLowerCaseValidator().getMax()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_LOWER_CASE_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getLowerCaseValidator().getMin()));
            }
            // Add attributes for upper case letters validation.
            if (pswRulesValidator.getUpperCaseValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_UPPER_CASE_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getUpperCaseValidator().getMax()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_UPPER_CASE_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getUpperCaseValidator().getMin()));
            }
            // Add attributes for special characters length validation.
            if (pswRulesValidator.getSpecialCharacterValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_SPECIAL_CHR_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getSpecialCharacterValidator().getMax()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_SPECIAL_CHR_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getSpecialCharacterValidator().getMin()));
            }
            // Add attributes for Unique character validation.
            if (pswRulesValidator.getUniqueCharacterValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_UNIQUE_CHR_ENABLE_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getUniqueCharacterValidator().isEnable()));
                configAttributes.put(INPUT_VAL_PASSWORD_UNIQUE_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getUniqueCharacterValidator().isCaseSensitive()));
                configAttributes.put(INPUT_VAL_PASSWORD_MIN_UNIQUE_CHR_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(((UniqueCharacterValidator) pswRulesValidator.getUniqueCharacterValidator())
                                .getMinUniqueCharacter()));
            }
            // Add attributes for Repeated character validation.
            if (pswRulesValidator.getRepeatedCharacterValidator() != null) {
                configAttributes.put(INPUT_VAL_PASSWORD_REPEATED_CHR_ENABLE_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getRepeatedCharacterValidator().isEnable()));
                configAttributes.put(INPUT_VAL_PASSWORD_REPEATED_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME,
                        String.valueOf(pswRulesValidator.getRepeatedCharacterValidator().isCaseSensitive()));
                configAttributes.put(INPUT_VAL_PASSWORD_MAX_REPEATED_CHR_LENGTH_ATTRIBUTE_NAME,
                        String.valueOf(((RepeatedCharacterValidator) pswRulesValidator.getRepeatedCharacterValidator())
                                .getMaxConsecutiveLength()));
            }
        } else if (config.getPasswordValidator().getRegExValidator() != null) {
            RegExValidator regExValidator = config.getPasswordValidator().getRegExValidator();

            // Add the params
            configAttributes.put(INPUT_VAL_PASSWORD_VALIDATION_TYPE, REGEX);

            configAttributes.put(INPUT_VAL_PASSWORD_JAVA_REGEX_ATTRIBUTE_NAME,
                    String.valueOf(regExValidator.getJavaRegExPattern()));
            configAttributes.put(INPUT_VAL_PASSWORD_JS_REGEX_ATTRIBUTE_NAME,
                    String.valueOf(regExValidator.getJsRegExPattern()));
        }

        List<Attribute> resourceAttributes =
                configAttributes.entrySet().stream()
                        .filter(attribute -> attribute.getValue() != null && !"null".equals(attribute.getValue()))
                        .map(attribute -> new Attribute(attribute.getKey(), attribute.getValue()))
                        .collect(Collectors.toList());
        resource.setAttributes(resourceAttributes);

        return resource;
    }

    /**
     * Method to build Input validation configuration from resource.
     *
     * @param resource  Resource.
     * @return Input Validation Configuration.
     */
    private InputValidationConfiguration buildInputValidationConfigFromResource(Resource resource) {

        InputValidationConfiguration configuration = new InputValidationConfiguration();
        PasswordValidator passwordValidator = new PasswordValidator();

        Map<String, String> attributesMap =
                resource.getAttributes().stream()
                        .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));

        // Check the input validation criteria.
        if (attributesMap.containsKey(INPUT_VAL_PASSWORD_VALIDATION_TYPE)) {
            if (StringUtils.equalsIgnoreCase(attributesMap.get(INPUT_VAL_PASSWORD_VALIDATION_TYPE),
                    RULES)){

                RulesValidator rulesValidator = new RulesValidator();
                DefaultValidator lengthValidator = new DefaultValidator();
                DefaultValidator numeralsValidator = new DefaultValidator();
                DefaultValidator upperCaseValidator = new DefaultValidator();
                DefaultValidator lowerCaseValidator = new DefaultValidator();
                DefaultValidator specialChrValidator = new DefaultValidator();
                UniqueCharacterValidator uniqueChrValidator = new UniqueCharacterValidator();
                RepeatedCharacterValidator repeatedChrValidator = new RepeatedCharacterValidator();

                for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
                    if (INPUT_VAL_PASSWORD_MAX_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        lengthValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLengthValidator(lengthValidator);
                        continue;
                    }
                    if (INPUT_VAL_PASSWORD_MIN_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        lengthValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLengthValidator(lengthValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_MAX_NUMERALS_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        numeralsValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setNumeralsValidator(numeralsValidator);
                        continue;
                    }
                    if (INPUT_VAL_PASSWORD_MIN_NUMERALS_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        numeralsValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setNumeralsValidator(numeralsValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_MAX_UPPER_CASE_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        upperCaseValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setUpperCaseValidator(upperCaseValidator);
                        continue;
                    }
                    if (INPUT_VAL_PASSWORD_MIN_UPPER_CASE_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        upperCaseValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setUpperCaseValidator(upperCaseValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_MAX_LOWER_CASE_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        lowerCaseValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLowerCaseValidator(lowerCaseValidator);
                        continue;
                    }
                    if (INPUT_VAL_PASSWORD_MIN_LOWER_CASE_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        lowerCaseValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLowerCaseValidator(lowerCaseValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_MAX_SPECIAL_CHR_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        specialChrValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setSpecialCharacterValidator(specialChrValidator);
                        continue;
                    }
                    if (INPUT_VAL_PASSWORD_MIN_SPECIAL_CHR_LENGTH_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey())) {
                        specialChrValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setSpecialCharacterValidator(specialChrValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_UNIQUE_CHR_ENABLE_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey()) &&
                            Boolean.parseBoolean(entry.getValue())) {
                        uniqueChrValidator.setEnable(true);
                        if (attributesMap.containsKey(INPUT_VAL_PASSWORD_UNIQUE_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME)) {
                            uniqueChrValidator.setCaseSensitive(Boolean.parseBoolean(attributesMap
                                    .get(INPUT_VAL_PASSWORD_UNIQUE_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME)));
                        }
                        if (attributesMap.containsKey(INPUT_VAL_PASSWORD_MIN_UNIQUE_CHR_LENGTH_ATTRIBUTE_NAME)) {
                            uniqueChrValidator.setMinUniqueCharacter(Integer.parseInt(attributesMap
                                    .get(INPUT_VAL_PASSWORD_MIN_UNIQUE_CHR_LENGTH_ATTRIBUTE_NAME)));
                        }
                        rulesValidator.setUniqueCharacterValidator(uniqueChrValidator);
                        continue;
                    }

                    if (INPUT_VAL_PASSWORD_REPEATED_CHR_ENABLE_ATTRIBUTE_NAME.equalsIgnoreCase(entry.getKey()) &&
                            Boolean.parseBoolean(entry.getValue())) {
                        repeatedChrValidator.setEnable(true);
                        if (attributesMap.containsKey(INPUT_VAL_PASSWORD_REPEATED_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME)) {
                            repeatedChrValidator.setCaseSensitive(Boolean.parseBoolean(attributesMap
                                    .get(INPUT_VAL_PASSWORD_REPEATED_CHR_CASE_SENSITIVE_ATTRIBUTE_NAME)));
                        }
                        if (attributesMap.containsKey(INPUT_VAL_PASSWORD_MAX_REPEATED_CHR_LENGTH_ATTRIBUTE_NAME)) {
                            repeatedChrValidator.setMaxConsecutiveLength(Integer.parseInt(attributesMap
                                    .get(INPUT_VAL_PASSWORD_MAX_REPEATED_CHR_LENGTH_ATTRIBUTE_NAME)));
                        }
                        rulesValidator.setRepeatedCharacterValidator(repeatedChrValidator);
                    }
                }
                passwordValidator.setRulesValidator(rulesValidator);
            } else if (StringUtils.equalsIgnoreCase(attributesMap.get(INPUT_VAL_PASSWORD_VALIDATION_TYPE),
                    REGEX)) {
                RegExValidator regExValidator = new RegExValidator();

                if (attributesMap.containsKey(INPUT_VAL_PASSWORD_JAVA_REGEX_ATTRIBUTE_NAME)) {
                    regExValidator.setJavaRegExPattern(attributesMap.get(INPUT_VAL_PASSWORD_JAVA_REGEX_ATTRIBUTE_NAME));
                }
                if (attributesMap.containsKey(INPUT_VAL_PASSWORD_JS_REGEX_ATTRIBUTE_NAME)) {
                    regExValidator.setJsRegExPattern(attributesMap.get(INPUT_VAL_PASSWORD_JS_REGEX_ATTRIBUTE_NAME));
                }
            }
        }
        configuration.setPasswordValidator(passwordValidator);
        return configuration;
    }

    /**
     * Get configuration manger.
     *
     * @return configuration manger.
     */
    private ConfigurationManager getConfigurationManager() {

        return InputValidationDataHolder.getConfigurationManager();
    }

    private void handleException(Constants.ErrorMessages error, String data, int limit)
            throws InputValidationMgtClientException {

        throw new InputValidationMgtClientException(error.getCode(), error.getMessage(),
                String.format(error.getDescription(), data, limit));
    }
}
