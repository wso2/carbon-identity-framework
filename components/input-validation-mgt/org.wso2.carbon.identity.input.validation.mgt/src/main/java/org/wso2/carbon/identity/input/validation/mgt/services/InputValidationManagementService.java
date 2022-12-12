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

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidatorConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Interface for input validation manager.
 */
public interface InputValidationManagementService {
    /**
     * Method to update input validation configuration.
     *
     * @param inputValidationConfiguration Configuration for input validation.
     * @param tenantDomain                 Tenant domain.
     * @return Input Validation Configuration.
     * @throws InputValidationMgtException If an error occurred in updating configuration.
     */
    List<ValidationConfiguration> updateInputValidationConfiguration
            (List<ValidationConfiguration> inputValidationConfiguration, String tenantDomain)
            throws InputValidationMgtException;

    /**
     * Method to get input validation configuration.
     *
     * @param tenantDomain Tenant domain.
     * @return Input Validation Configuration.
     * @throws InputValidationMgtException If an error occurred in getting configuration.
     */
    List<ValidationConfiguration> getInputValidationConfiguration (String tenantDomain)
            throws InputValidationMgtException;

    /**
     * Method to get validator configurations.
     *
     * @param tenantDomain  tenant domain.
     * @throws InputValidationMgtException If an error occurred when getting validator configurations.
     */
    List<ValidatorConfiguration> getValidatorConfigurations(String tenantDomain) throws InputValidationMgtException;

    /**
     * Method to get validators.
     *
     * @param tenantDomain  Tenant domain name.
     * @return  Validators.
     */
    Map<String, Validator> getValidators(String tenantDomain);

    /**
     * Method to get configuration from user store.
     *
     * @param tenantDomain  Tenant domain.
     * @return configuration.
     */
    List<ValidationConfiguration> getConfigurationFromUserStore(String tenantDomain) throws InputValidationMgtException;
}
