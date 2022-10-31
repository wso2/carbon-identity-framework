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
import org.wso2.carbon.identity.input.validation.mgt.model.DefaultValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.InputValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.PasswordValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RegExValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RepeatedCharacterValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.UniqueCharacterValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationParam;
import org.wso2.carbon.identity.input.validation.mgt.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JAVA_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LOWER_CASE_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_NUMERALS_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_REPEATED_CHR_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_SPECIAL_CHR_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_UPPER_CASE_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LOWER_CASE_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_NUMERALS_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_SPECIAL_CHR_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_UNIQUE_CHR_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_UPPER_CASE_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.UNIQUE_CHR_CASE_SENSITIVE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.REPEATED_CHR_CASE_SENSITIVE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.REPEATED_CHR_ENABLE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.UNIQUE_CHR_ENABLE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.VALIDATION_TYPE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.RULES;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_NO_CONFIGURATIONS_FOUND;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_PARAM_EMPTY;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_PARAM_NOT_SUPPORTED;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_WHILE_ADDING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_WHILE_UPDATING_CONFIGURATIONS;
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

        if (StringUtils.equalsIgnoreCase(param.getName(), PASSWORD)) {
            Utils.validatePassword(configuration, param.getValue());
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

        // Generate resource for password configurations.
        if (config.getPasswordValidator().getRulesValidator() != null) {
            RulesValidator pswRulesValidator = config.getPasswordValidator().getRulesValidator();
            // Add the params
            configAttributes.put(getAttributeName(PASSWORD, VALIDATION_TYPE), RULES);

            // Add attributes for length validation.
            if (pswRulesValidator.getLengthValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, MAX_LENGTH),
                        String.valueOf(pswRulesValidator.getLengthValidator().getMax()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_LENGTH),
                        String.valueOf(pswRulesValidator.getLengthValidator().getMin()));
            }
            // Add attributes for numerals length validation.
            if (pswRulesValidator.getNumeralsValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, MAX_NUMERALS_LENGTH),
                        String.valueOf(pswRulesValidator.getNumeralsValidator().getMax()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_NUMERALS_LENGTH),
                        String.valueOf(pswRulesValidator.getNumeralsValidator().getMin()));
            }
            // Add attributes for lower case letters validation.
            if (pswRulesValidator.getLowerCaseValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, MAX_LOWER_CASE_LENGTH),
                        String.valueOf(pswRulesValidator.getLowerCaseValidator().getMax()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_LOWER_CASE_LENGTH),
                        String.valueOf(pswRulesValidator.getLowerCaseValidator().getMin()));
            }
            // Add attributes for upper case letters validation.
            if (pswRulesValidator.getUpperCaseValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, MAX_UPPER_CASE_LENGTH),
                        String.valueOf(pswRulesValidator.getUpperCaseValidator().getMax()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_UPPER_CASE_LENGTH),
                        String.valueOf(pswRulesValidator.getUpperCaseValidator().getMin()));
            }
            // Add attributes for special characters length validation.
            if (pswRulesValidator.getSpecialCharacterValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, MAX_SPECIAL_CHR_LENGTH),
                        String.valueOf(pswRulesValidator.getSpecialCharacterValidator().getMax()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_SPECIAL_CHR_LENGTH),
                        String.valueOf(pswRulesValidator.getSpecialCharacterValidator().getMin()));
            }
            // Add attributes for Unique character validation.
            if (pswRulesValidator.getUniqueCharacterValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, UNIQUE_CHR_ENABLE),
                        String.valueOf(pswRulesValidator.getUniqueCharacterValidator().isEnable()));
                configAttributes.put(getAttributeName(PASSWORD, UNIQUE_CHR_CASE_SENSITIVE),
                        String.valueOf(pswRulesValidator.getUniqueCharacterValidator().isCaseSensitive()));
                configAttributes.put(getAttributeName(PASSWORD, MIN_UNIQUE_CHR_LENGTH),
                        String.valueOf(((UniqueCharacterValidator) pswRulesValidator.getUniqueCharacterValidator())
                                .getMinUniqueCharacter()));
            }
            // Add attributes for Repeated character validation.
            if (pswRulesValidator.getRepeatedCharacterValidator() != null) {
                configAttributes.put(getAttributeName(PASSWORD, REPEATED_CHR_ENABLE),
                        String.valueOf(pswRulesValidator.getRepeatedCharacterValidator().isEnable()));
                configAttributes.put(getAttributeName(PASSWORD, REPEATED_CHR_CASE_SENSITIVE),
                        String.valueOf(pswRulesValidator.getRepeatedCharacterValidator().isCaseSensitive()));
                configAttributes.put(getAttributeName(PASSWORD, MAX_REPEATED_CHR_LENGTH),
                        String.valueOf(((RepeatedCharacterValidator) pswRulesValidator.getRepeatedCharacterValidator())
                                .getMaxConsecutiveLength()));
            }
        } else if (config.getPasswordValidator().getRegExValidator() != null) {
            RegExValidator regExValidator = config.getPasswordValidator().getRegExValidator();

            // Add the params
            configAttributes.put(getAttributeName(PASSWORD, VALIDATION_TYPE), REGEX);

            configAttributes.put(getAttributeName(PASSWORD, JAVA_REGEX),
                    String.valueOf(regExValidator.getJavaRegExPattern()));
            configAttributes.put(getAttributeName(PASSWORD, JS_REGEX),
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
        if (attributesMap.containsKey(getAttributeName(PASSWORD, VALIDATION_TYPE))) {
            if (StringUtils.equalsIgnoreCase(attributesMap.get(getAttributeName(PASSWORD, VALIDATION_TYPE)),
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
                    if (getAttributeName(PASSWORD, MAX_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        lengthValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLengthValidator(lengthValidator);
                        continue;
                    }
                    if (getAttributeName(PASSWORD, MIN_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        lengthValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLengthValidator(lengthValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, MAX_NUMERALS_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        numeralsValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setNumeralsValidator(numeralsValidator);
                        continue;
                    }
                    if (getAttributeName(PASSWORD, MIN_NUMERALS_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        numeralsValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setNumeralsValidator(numeralsValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, MAX_UPPER_CASE_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        upperCaseValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setUpperCaseValidator(upperCaseValidator);
                        continue;
                    }
                    if (getAttributeName(PASSWORD, MIN_UPPER_CASE_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        upperCaseValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setUpperCaseValidator(upperCaseValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, MAX_LOWER_CASE_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        lowerCaseValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLowerCaseValidator(lowerCaseValidator);
                        continue;
                    }
                    if (getAttributeName(PASSWORD, MIN_LOWER_CASE_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        lowerCaseValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setLowerCaseValidator(lowerCaseValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, MAX_SPECIAL_CHR_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        specialChrValidator.setMax(Integer.parseInt(entry.getValue()));
                        rulesValidator.setSpecialCharacterValidator(specialChrValidator);
                        continue;
                    }
                    if (getAttributeName(PASSWORD, MIN_SPECIAL_CHR_LENGTH).equalsIgnoreCase(entry.getKey())) {
                        specialChrValidator.setMin(Integer.parseInt(entry.getValue()));
                        rulesValidator.setSpecialCharacterValidator(specialChrValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, UNIQUE_CHR_ENABLE).equalsIgnoreCase(entry.getKey()) &&
                            Boolean.parseBoolean(entry.getValue())) {
                        uniqueChrValidator.setEnable(true);
                        if (attributesMap.containsKey(getAttributeName(PASSWORD, UNIQUE_CHR_CASE_SENSITIVE))) {
                            uniqueChrValidator.setCaseSensitive(Boolean.parseBoolean(attributesMap
                                    .get(getAttributeName(PASSWORD, UNIQUE_CHR_CASE_SENSITIVE))));
                        }
                        if (attributesMap.containsKey(getAttributeName(PASSWORD, MIN_UNIQUE_CHR_LENGTH))) {
                            uniqueChrValidator.setMinUniqueCharacter(Integer.parseInt(attributesMap
                                    .get(getAttributeName(PASSWORD, MIN_UNIQUE_CHR_LENGTH))));
                        }
                        rulesValidator.setUniqueCharacterValidator(uniqueChrValidator);
                        continue;
                    }

                    if (getAttributeName(PASSWORD, REPEATED_CHR_ENABLE).equalsIgnoreCase(entry.getKey()) &&
                            Boolean.parseBoolean(entry.getValue())) {
                        repeatedChrValidator.setEnable(true);
                        if (attributesMap.containsKey(getAttributeName(PASSWORD, REPEATED_CHR_CASE_SENSITIVE))) {
                            repeatedChrValidator.setCaseSensitive(Boolean.parseBoolean(attributesMap
                                    .get(getAttributeName(PASSWORD, REPEATED_CHR_CASE_SENSITIVE))));
                        }
                        if (attributesMap.containsKey(getAttributeName(PASSWORD, MAX_REPEATED_CHR_LENGTH))) {
                            repeatedChrValidator.setMaxConsecutiveLength(Integer.parseInt(attributesMap
                                    .get(getAttributeName(PASSWORD, MAX_REPEATED_CHR_LENGTH))));
                        }
                        rulesValidator.setRepeatedCharacterValidator(repeatedChrValidator);
                    }
                }
                passwordValidator.setRulesValidator(rulesValidator);
            } else if (StringUtils.equalsIgnoreCase(attributesMap.get(getAttributeName(PASSWORD, VALIDATION_TYPE)),
                    REGEX)) {
                RegExValidator regExValidator = new RegExValidator();

                if (attributesMap.containsKey(getAttributeName(PASSWORD, JAVA_REGEX))) {
                    regExValidator.setJavaRegExPattern(attributesMap.get(getAttributeName(PASSWORD, JAVA_REGEX)));
                }
                if (attributesMap.containsKey(getAttributeName(PASSWORD, JS_REGEX))) {
                    regExValidator.setJsRegExPattern(attributesMap.get(getAttributeName(PASSWORD, JS_REGEX)));
                }
                passwordValidator.setRegExValidator(regExValidator);
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

    private String getAttributeName(String inputName, String attribute) {

        return inputName + "." + attribute;
    }
}
