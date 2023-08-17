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

package org.wso2.carbon.identity.input.validation.mgt.utils;

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.EmailFormatValidator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;

/**
 * Util class for input validation management.
 */
public class Util {

    private static final InputValidationManagementService inputValidationMgtService =
            new InputValidationManagementServiceImpl();
    private Util() {

    }
    public static boolean isEmailAsUserName(String tenantDomain) throws InputValidationMgtException {

        List<ValidationConfiguration> configurations;
        boolean isEmailAsUsername = false;
        try {
            configurations = inputValidationMgtService.getInputValidationConfiguration(tenantDomain);
            String field = USERNAME;
            ValidationConfiguration configurationList = configurations.stream().filter(config ->
                    field.equalsIgnoreCase(config.getField())).collect(Collectors.toList()).get(0);

            /* If configuration for username field is found in Input Validation Mgt service, validate against them,
             if not validate against the regex from the userStore. */
            if (configurationList != null && !configurationList.getRules().isEmpty()) {
                for (RulesConfiguration configuration: configurationList.getRules()) {
                    if (EmailFormatValidator.class.getSimpleName().equals(configuration.getValidatorName())) {
                        isEmailAsUsername = true;
                    }
                }
            }
            return isEmailAsUsername;

        } catch (InputValidationMgtException e) {

            throw new InputValidationMgtException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(), e.getMessage(),
                    e.getDescription());
        }
    }
}
