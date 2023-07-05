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

package org.wso2.carbon.identity.input.validation.mgt.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.input.validation.mgt.model.FieldValidationConfigurationHandler;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds data for Input Validation Mgt component.
 */
public class InputValidationDataHolder {

    private static InputValidationDataHolder instance = new InputValidationDataHolder();
    private static ConfigurationManager configurationManager = null;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private static Map<String, Validator> validators = new HashMap<>();
    private static Map<String, FieldValidationConfigurationHandler> validationConfigurationHandlers = new HashMap<>();

    /**
     * Get InputValidationDataHolder instance.
     *
     * @return InputValidationDataHolder.
     */
    public static InputValidationDataHolder getInstance() {

        return instance;
    }

    /**
     * Get Configuration Manager.
     *
     * @return ConfigurationManager.
     */
    public static ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    /**
     * Set Configuration manager.
     *
     * @param configurationManager Configuration Manager.
     */
    public static void setConfigurationManager(ConfigurationManager configurationManager) {

        InputValidationDataHolder.configurationManager = configurationManager;
    }

    public static Map<String, Validator> getValidators() {

        return validators;
    }

    public static Map<String, FieldValidationConfigurationHandler> getFieldValidationConfigurationHandlers() {

        return validationConfigurationHandlers;
    }

    /**
     * Get claim metadata management service.
     *
     * @return claim metadata management service.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    /**
     * Set claim metadata management service.
     *
     * @param claimMetadataManagementService    Claim metadata management service.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }
}
