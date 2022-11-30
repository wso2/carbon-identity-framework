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
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ERROR_CODE_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_NO_CONFIGURATIONS_FOUND;

/**
 * Lister class to validate the password.
 */
public class InputValidationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(InputValidationListener.class);
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

        if (UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()) {
            return true;
        }
        int tenantId = userStoreManager.getTenantId();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        List<ValidationConfiguration> configurations;
        try {
            configurations = inputValidationMgtService.getInputValidationConfiguration(tenantDomain);
            configurations = configurations.stream().filter(f -> PASSWORD.equalsIgnoreCase(f.getField()))
                    .collect(Collectors.toList());
            if (configurations.isEmpty()) {
                return true;
            }
            UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
            Map<String, Validator> validators = InputValidationDataHolder.getValidators();
            ValidationConfiguration configuration = configurations.get(0);
            List<RulesConfiguration> rules = new ArrayList<>();
            if (configuration.getRegEx() != null) {
                rules = configuration.getRegEx();
            } else if (configuration.getRules() != null) {
                rules = configuration.getRules();
            }
            for (RulesConfiguration rule: rules) {
                Validator validator = validators.get(rule.getValidatorName());
                ValidationContext context = new ValidationContext();
                context.setField(PASSWORD);
                context.setValue(credential.toString());
                context.setTenantDomain(tenantDomain);
                context.setProperties(rule.getProperties());
                validator.validate(context);
            }
        } catch (InputValidationMgtException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to validate password for user: " + userName);
            }
            if (e.getErrorCode().equals(ERROR_NO_CONFIGURATIONS_FOUND.getCode())) {
                return true;
            }
            if (e instanceof InputValidationMgtClientException) {
                throw new UserStoreException(ERROR_CODE_PREFIX + e.getErrorCode() + ":" + e.getDescription(),
                        new PolicyViolationException(e.getDescription()));
            }
            return false;
        }
        return true;
    }
}
