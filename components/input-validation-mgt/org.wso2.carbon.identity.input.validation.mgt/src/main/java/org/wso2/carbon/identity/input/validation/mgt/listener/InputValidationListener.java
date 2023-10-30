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

package org.wso2.carbon.identity.input.validation.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementServiceImpl;
import org.wso2.carbon.identity.mgt.policy.PolicyViolationException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ERROR_CODE_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_WHILE_UPDATING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VALIDATION_USERNAME_ENABLED_CONFIG;

/**
 * Lister class to validate the password.
 */
public class InputValidationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log LOG = LogFactory.getLog(InputValidationListener.class);
    private static final boolean IS_USERNAME_VALIDATION_ENABLED =
            Boolean.parseBoolean(IdentityUtil.getProperty(INPUT_VALIDATION_USERNAME_ENABLED_CONFIG));
    private final InputValidationManagementService inputValidationMgtService =
            new InputValidationManagementServiceImpl();

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 3;
    }

    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        Map<String, String> validationRequiredFieldWithValues = new HashMap<>();
        if (!UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()) {
            validationRequiredFieldWithValues.put(PASSWORD, credential.toString());
        }
        if (IS_USERNAME_VALIDATION_ENABLED && !UserCoreUtil.getSkipUsernamePatternValidationThreadLocal()) {
            validationRequiredFieldWithValues.put(USERNAME, userName);
        }
        return validate(validationRequiredFieldWithValues, userStoreManager);
    }

    public boolean doPreUpdateCredentialByAdminWithID(String userID, Object newCredential,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        return validate(Collections.singletonMap(PASSWORD, newCredential.toString()), userStoreManager);
    }

    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        return validate(Collections.singletonMap(PASSWORD, newCredential.toString()), userStoreManager);
    }

    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        return validate(Collections.singletonMap(PASSWORD, newCredential.toString()), userStoreManager);
    }

    public boolean doPreUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential,
                                               UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        return validate(Collections.singletonMap(PASSWORD, newCredential.toString()), userStoreManager);

    }

    /**
     * Method to validate the values.
     *
     * @param inputValuesForFieldsMap     Map of fields and values that need to be validated,
     *                                    eg: {key: username, value: abcuser}
     * @param userStoreManager            User store manager.
     * @return  Validity of the field.
     * @throws UserStoreException   If an error occurred while validating.
     */
    private boolean validate(Map<String, String> inputValuesForFieldsMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        int tenantId = userStoreManager.getTenantId();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        Map<String, Validator> validators = InputValidationDataHolder.getValidators();

        List<ValidationConfiguration> configurations;
        try {
            configurations = inputValidationMgtService.getInputValidationConfiguration(tenantDomain);

            /* Validate provide value for each field in the `inputValuesForFieldsMap` against the configurations of the
             corresponding field. */
            for (String field: inputValuesForFieldsMap.keySet()) {
                ValidationConfiguration configuration = configurations.stream().filter(config ->
                        field.equalsIgnoreCase(config.getField())).collect(Collectors.toList()).get(0);
                if (configuration != null) {
                    try {
                        if (PASSWORD.equals(field)) {
                            UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
                        } else if (USERNAME.equals(field)) {
                            UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
                        }
                        String valueProvidedForField = inputValuesForFieldsMap.get(field);
                        validateAgainstConfiguration(configuration, validators, field, valueProvidedForField,
                                tenantDomain);
                    } catch (InputValidationMgtClientException e) {
                        LOG.error(new StringFormattedMessage("Failed to validate %s for user. " +
                                e.getDescription(), field));
                        throw new UserStoreException(ERROR_CODE_PREFIX + e.getErrorCode() + ":" + e.getDescription(),
                                new PolicyViolationException(e.getDescription()));
                    }
                }
            }
        } catch (InputValidationMgtException e) {
            return ERROR_WHILE_UPDATING_CONFIGURATIONS.getCode().equals(e.getErrorCode());
        }
        return true;
    }

    private boolean validateAgainstConfiguration(ValidationConfiguration configuration, Map<String, Validator>
            validators, String field, String value, String tenantDomain) throws InputValidationMgtClientException {

        List<RulesConfiguration> rules = new ArrayList<>();
        if (configuration.getRegEx() != null) {
            rules = configuration.getRegEx();
        } else if (configuration.getRules() != null) {
            rules = configuration.getRules();
        }
        for (RulesConfiguration rule: rules) {
            Validator validator = validators.get(rule.getValidatorName());
            ValidationContext context = new ValidationContext();
            context.setField(field);
            context.setValue(value);
            context.setTenantDomain(tenantDomain);
            context.setProperties(rule.getProperties());
            validator.validate(context);
        }
        return true;
    }
}
