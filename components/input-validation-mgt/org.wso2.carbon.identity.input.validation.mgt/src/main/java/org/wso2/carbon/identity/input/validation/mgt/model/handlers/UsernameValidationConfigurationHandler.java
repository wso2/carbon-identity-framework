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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtServerException;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AbstractRegExValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AlphanumericValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.EmailFormatValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LengthValidator;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ALPHA_NUMERIC;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.DEFAULT_EMAIL_JS_REGEX_PATTERN;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.EMAIL_FORMAT_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.INPUT_VALIDATION_DEFAULT_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.EMAIL_CLAIM_URI;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_INVALID_VALIDATORS_COMBINATION;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.SUPER_TENANT_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JS_REG;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JS_REG_EX;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX;

/**
 * Username Validation Configuration Handler.
 */
public class UsernameValidationConfigurationHandler extends AbstractFieldValidationConfigurationHandler {

    private static final Log log = LogFactory.getLog(AbstractRegExValidator.class);

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
            String usernameRegEx = getUsernameRegEx(realmConfiguration);

            if (!usernameRegEx.isEmpty() && !DEFAULT_EMAIL_JS_REGEX_PATTERN.equals(usernameRegEx)
                    && isAlphaNumericValidationByDefault()) {
                rules.add(getDefaultLengthValidatorRuleConfig());
                rules.add(getRuleConfig(AlphanumericValidator.class.getSimpleName(),
                        ENABLE_VALIDATOR, Boolean.TRUE.toString()));
                configuration.setRules(rules);
            } else if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                    .equals(SUPER_TENANT_DOMAIN)) {
                // Return the JSRegex if the tenant domain is carbon.super.
                rules.add(getRuleConfig("JsRegExValidator", JS_REGEX, usernameRegEx));
                configuration.setRegEx(rules);
            } else {
                rules.add(getRuleConfig(EmailFormatValidator.class.getSimpleName(), ENABLE_VALIDATOR,
                        Boolean.TRUE.toString()));
                configuration.setRules(rules);
            }
            return configuration;
        } catch (InputValidationMgtException e) {
            throw new InputValidationMgtException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(), e.getMessage(),
                    e.getDescription());
        }
    }

    private boolean isAlphaNumericValidationByDefault() {

        String defaultValidator = IdentityUtil.getProperty(INPUT_VALIDATION_DEFAULT_VALIDATOR);
        return defaultValidator != null && StringUtils.equalsIgnoreCase(ALPHA_NUMERIC, defaultValidator);
    }

    @Override
    public boolean validateValidationConfiguration(List<RulesConfiguration> configurationList)
            throws InputValidationMgtClientException {

        List<String> validatorNames = new ArrayList<>();
        configurationList.forEach(config -> validatorNames.add(config.getValidatorName()));
        int validConfigurations = 0;
        /* Valid configurations for username must be either EmailFormatValidator is set to true or Alphanumeric
        validator is set to true along with Length Validator. */
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
                    } else {
                        throw new InputValidationMgtClientException(ERROR_INVALID_VALIDATORS_COMBINATION.getCode(),
                            "LengthValidator must be configured along with the AlphanumericValidator for username.");
                    }
                }
                validatorNames.remove(AlphanumericValidator.class.getSimpleName());
            }
        }

        // To be allowed, there must be exactly one valid combination and no other
        if (validConfigurations == 1 && validatorNames.isEmpty()) {
            return true;
        }
        throw new InputValidationMgtClientException(ERROR_INVALID_VALIDATORS_COMBINATION.getCode(),
                String.format(ERROR_INVALID_VALIDATORS_COMBINATION.getDescription(), USERNAME));
    }

    /**
     * Get default regex for the username.
     *
     * @param realmConfig   RealmConfiguration for the tenant.
     */
    private String getUsernameRegEx(RealmConfiguration realmConfig) {

        if (MultitenantUtils.isEmailUserName()) {
            if (StringUtils.isNotBlank(realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX))) {
                return PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX;
            }
            if (StringUtils.isBlank(realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG_EX))
                    && StringUtils.isBlank(realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG))) {
                return UserCoreConstants.RealmConfig.EMAIL_VALIDATION_REGEX;
            }
        }

        if (StringUtils.isNotBlank(realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG_EX))) {
            return realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG_EX);
        }

        if (StringUtils.isNotBlank(realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG))) {
            return realmConfig.getUserStoreProperty(PROPERTY_USER_NAME_JS_REG);
        }

        if (log.isDebugEnabled()) {
            log.debug("No default regex configurations are found for field username");
        }
        return new String();
    }

    /**
     * Perform post actions after validation configuration are updated.
     *
     * @param tenantDomain      Tenant Domain.
     * @param configuration     Updated validation configuration.
     */
    @Override
    public void handlePostValidationConfigurationUpdate(String tenantDomain, ValidationConfiguration configuration)
            throws InputValidationMgtServerException {

        for (RulesConfiguration rule: configuration.getRules()) {
            /*
             Set required and support by default properties of the email attribute to true, when username field
             validation configurations are set to emailFormatValidator.
             */
            if (rule != null && EMAIL_FORMAT_VALIDATOR.equals(rule.getValidatorName())) {
                if (Boolean.parseBoolean(rule.getProperties().get(ENABLE_VALIDATOR))) {
                    try {
                        updateEmailClaim(tenantDomain);
                    } catch (ClaimMetadataException e) {
                        throw new InputValidationMgtServerException(e);
                    }
                }
                break;
            }
        }
    }

    /**
     * Set required and support by default properties of the email attribute to true.
     *
     * @param tenantDomain   Tenant Domain.
     */
    private void updateEmailClaim(String tenantDomain) throws ClaimMetadataException {

        List<LocalClaim> localClaimList = InputValidationDataHolder.getInstance()
                .getClaimMetadataManagementService().getLocalClaims(tenantDomain);

        if (localClaimList.isEmpty()) {
            return;
        }

        for (LocalClaim claim : localClaimList) {
            if (StringUtils.equals(claim.getClaimURI(), EMAIL_CLAIM_URI)) {
                claim.setClaimProperty("Required", Boolean.TRUE.toString());
                claim.setClaimProperty("SupportedByDefault", Boolean.TRUE.toString());
                InputValidationDataHolder.getInstance().getClaimMetadataManagementService()
                        .updateLocalClaim(claim, tenantDomain);
                break;
            }
        }
    }

    /**
     * Method to retrieve to default length validation for alpha numeric rule configuration.
     *
     * @return RulesConfiguration.
     */
    private RulesConfiguration getDefaultLengthValidatorRuleConfig() {

        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName(LengthValidator.class.getSimpleName());
        Map<String, String> properties = new HashMap<>();
        properties.put(MIN_LENGTH, "5");
        properties.put(MAX_LENGTH, "30");
        rule.setProperties(properties);
        return rule;
    }
}
