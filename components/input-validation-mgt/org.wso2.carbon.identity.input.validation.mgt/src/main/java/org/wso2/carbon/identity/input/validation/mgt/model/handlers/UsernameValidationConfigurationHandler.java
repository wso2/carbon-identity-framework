/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.model.handlers;

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AlphanumericValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.EmailFormatValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LengthValidator;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.DEFAULT_EMAIL_REGEX_PATTERN;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_INVALID_VALIDATORS_COMBINATION;

/**
 * Password Validation Configuration Handler.
 */
public class UsernameValidationConfigurationHandler extends AbstractFieldValidationConfigurationHandler {

    @Override
    public boolean canHandle(String field) {

        return USERNAME.equalsIgnoreCase(field);
    }

    @Override
    public ValidationConfiguration getDefaultValidationConfiguration(String tenantDomain) throws
            InputValidationMgtException {

        ValidationConfiguration configuration = new ValidationConfiguration();
        configuration.setField(USERNAME);
        List<RulesConfiguration> rules = new ArrayList<>();

        try {
            RealmConfiguration realmConfiguration = getRealmConfiguration(tenantDomain);
            String javaRegex = realmConfiguration.getUserStoreProperty(UserCoreConstants
                    .RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
            String jsRegex = realmConfiguration.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JS_REG_EX);

            // Return the JsRegex if the default regex has been updated by the user.
            if (!javaRegex.isEmpty() && !jsRegex.isEmpty() && !DEFAULT_EMAIL_REGEX_PATTERN.equals(javaRegex)) {
                rules.add(getRuleConfig("JsRegExValidator", JS_REGEX, jsRegex));
                configuration.setRegEx(rules);
            } else {
                rules.add(getRuleConfig(EmailFormatValidator.class.getSimpleName(),
                        ENABLE_VALIDATOR, Boolean.TRUE.toString()));
                configuration.setRules(rules);
            }
            return configuration;
        } catch (InputValidationMgtException e) {
            throw new InputValidationMgtException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(), e.getMessage());
        }
    }

    @Override
    public boolean validateValidationConfiguration(List<RulesConfiguration> configurationList)
            throws InputValidationMgtClientException {

        List<String> validatorNames = new ArrayList<>();
        configurationList.forEach(config -> validatorNames.add(config.getValidatorName()));
        int validConfigurations = 0;
        for (RulesConfiguration configuration: configurationList) {
            if (EmailFormatValidator.class.getSimpleName().equals(configuration.getValidatorName())) {
                if (Boolean.parseBoolean(configuration.getProperties().get(ENABLE_VALIDATOR))) {
                    validConfigurations += 1;
                }
                validatorNames.remove(EmailFormatValidator.class.getSimpleName());
            } else if (AlphanumericValidator.class.getSimpleName().equals(configuration.getValidatorName())) {
                if (Boolean.parseBoolean(configuration.getProperties().get(ENABLE_VALIDATOR))) {
                    if (validatorNames.contains(LengthValidator.class.getSimpleName())) {
                        validConfigurations += 1;
                        validatorNames.remove(LengthValidator.class.getSimpleName());
                    }
                }
                validatorNames.remove(AlphanumericValidator.class.getSimpleName());
            }
        }
        if (validConfigurations != 1 || !validatorNames.isEmpty()) {
            throw new InputValidationMgtClientException(ERROR_INVALID_VALIDATORS_COMBINATION.getCode(),
                    String.format(ERROR_INVALID_VALIDATORS_COMBINATION.getDescription(), USERNAME));
        }
        return true;
    }
}
