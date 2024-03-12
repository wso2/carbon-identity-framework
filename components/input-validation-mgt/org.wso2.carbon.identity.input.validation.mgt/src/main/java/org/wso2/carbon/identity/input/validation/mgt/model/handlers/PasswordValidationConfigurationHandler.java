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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LengthValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LowerCaseValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.NumeralValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.SpecialCharacterValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.UpperCaseValidator;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ALPHA_NUMERIC;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.INPUT_VALIDATION_DEFAULT_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JAVA_REGEX_PATTERN;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;

/**
 * Password Validation Configuration Handler.
 */
public class PasswordValidationConfigurationHandler extends AbstractFieldValidationConfigurationHandler {

    @Override
    public boolean canHandle(String field) {

        return PASSWORD.equalsIgnoreCase(field);
    }

    @Override
    public ValidationConfiguration getDefaultValidationConfiguration(String tenantDomain) throws
            InputValidationMgtException {

        ValidationConfiguration configuration = new ValidationConfiguration();
        configuration.setField(PASSWORD);
        List<RulesConfiguration> rules = new ArrayList<>();
        try {
            RealmConfiguration realmConfiguration = getRealmConfiguration(tenantDomain);
            String javaRegex = realmConfiguration.getUserStoreProperty(UserCoreConstants
                    .RealmConfig.PROPERTY_JAVA_REG_EX);
            String jsRegex = realmConfiguration.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JS_REG_EX);

            // Return the JsRegex if the default regex has been updated by the user.
            if (!javaRegex.isEmpty() && !jsRegex.isEmpty() && !JAVA_REGEX_PATTERN.equals(javaRegex)) {

                if (isRuleBasedValidationByDefault()) {
                    rules.add(getRuleConfig(LengthValidator.class.getSimpleName(), MIN_LENGTH, "8"));
                    rules.add(getRuleConfig(LengthValidator.class.getSimpleName(), MAX_LENGTH, "30"));
                    rules.add(getRuleConfig(NumeralValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                    rules.add(getRuleConfig(UpperCaseValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                    rules.add(getRuleConfig(LowerCaseValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                    rules.add(getRuleConfig(SpecialCharacterValidator.class.getSimpleName(), MIN_LENGTH, "0"));
                    configuration.setRules(rules);
                } else {
                    rules.add(getRuleConfig("JsRegExValidator", JS_REGEX, jsRegex));
                    configuration.setRegEx(rules);
                }
            } else {
                rules.add(getRuleConfig(LengthValidator.class.getSimpleName(), MIN_LENGTH, "8"));
                rules.add(getRuleConfig(LengthValidator.class.getSimpleName(), MAX_LENGTH, "30"));
                rules.add(getRuleConfig(NumeralValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                rules.add(getRuleConfig(UpperCaseValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                rules.add(getRuleConfig(LowerCaseValidator.class.getSimpleName(), MIN_LENGTH, "1"));
                rules.add(getRuleConfig(SpecialCharacterValidator.class.getSimpleName(), MIN_LENGTH, "0"));
                configuration.setRules(rules);
            }
            return configuration;
        } catch (InputValidationMgtException e) {
            throw new InputValidationMgtException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(), e.getMessage());
        }
    }

    private boolean isRuleBasedValidationByDefault() {

        String defaultValidator = IdentityUtil.getProperty(INPUT_VALIDATION_DEFAULT_VALIDATOR);
        return defaultValidator != null && StringUtils.equalsIgnoreCase(ALPHA_NUMERIC, defaultValidator);
    }
}
