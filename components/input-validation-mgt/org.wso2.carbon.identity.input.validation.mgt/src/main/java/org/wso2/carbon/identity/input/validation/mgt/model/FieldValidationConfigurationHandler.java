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

package org.wso2.carbon.identity.input.validation.mgt.model;

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtServerException;

import java.util.List;

/**
 * Interface of Field Validation Configuration Handler.
 */
public interface FieldValidationConfigurationHandler {

    /**
     * Check whether the field can be handled by the handler.
     *
     * @param field     Name of the field.
     * @return boolean
     */
    boolean canHandle(String field);

    /**
     * Retrieve default validator configuration for the field.
     *
     * @return  ValidationConfiguration
     */
    ValidationConfiguration getDefaultValidationConfiguration(String tenantDomain) throws InputValidationMgtException;

    /**
     * Validate whether given validator configuration are allowed.
     *
     * @return  boolean
     */
    boolean validateValidationConfiguration(List<RulesConfiguration> configurationList)
            throws InputValidationMgtClientException;

    /**
     * Perform post actions after validation configuration are updated.
     *
     * @param tenantDomain      Tenant Domain.
     * @param configuration     Updated validation configuration.
     */
    default void handlePostValidationConfigurationUpdate(String tenantDomain, ValidationConfiguration configuration)
            throws InputValidationMgtServerException {
    }
}
