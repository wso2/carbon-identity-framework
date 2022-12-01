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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtServerException;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.*;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AbstractRegExValidator;
import org.wso2.carbon.identity.mgt.policy.PolicyViolationException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.*;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_NO_CONFIGURATIONS_FOUND;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_WHILE_ADDING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_WHILE_UPDATING_CONFIGURATIONS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME;

/**
 * Class for Input Validation Manager Implementation.
 */
public class InputValidationManagementServiceImpl implements InputValidationManagementService {

    private static final Log LOG = LogFactory.getLog(InputValidationManagementServiceImpl.class);

    @Override
    public List<ValidationConfiguration> updateInputValidationConfiguration(
            List<ValidationConfiguration> configurations, String tenantDomain) throws InputValidationMgtException {

        List<ValidationConfiguration> updatedResources = new ArrayList<>();

        for (ValidationConfiguration configuration: configurations) {
            ValidationConfiguration updatedResource = updateValidationConfiguration(configuration, tenantDomain);
            updatedResources.add(updatedResource);
        }
        return updatedResources;
    }

    @Override
    public List<ValidationConfiguration> getInputValidationConfiguration(String tenantDomain)
            throws InputValidationMgtException {

        List<Resource> resources = getResourcesByType(tenantDomain);

        // Convert resources to Validation Configurations.
        List<ValidationConfiguration> configurations = new ArrayList<>();
        for (Resource resource: resources) {
            configurations.add(buildValidationConfigFromResource(resource));
        }
        // Handle if there are no resources found for the tenant.
        if (configurations.isEmpty()) {
            throw new InputValidationMgtClientException(ERROR_NO_CONFIGURATIONS_FOUND.getCode(),
                    String.format(ERROR_NO_CONFIGURATIONS_FOUND.getDescription(), tenantDomain));
        }
        return configurations;
    }

    @Override
    public List<ValidatorConfiguration> getValidatorConfigurations (String tenantDomain)
            throws InputValidationMgtException {

        Map<String, Validator> validators = InputValidationDataHolder.getValidators();
        // Handle if no validators available.
        if (validators.isEmpty()) {
            throw new InputValidationMgtClientException(ERROR_NO_CONFIGURATIONS_FOUND.getCode(),
                    String.format(ERROR_NO_CONFIGURATIONS_FOUND.getDescription(), tenantDomain));
        }

        List<ValidatorConfiguration> configurations = new ArrayList<>();
        for (Map.Entry<String, Validator> entry : validators.entrySet()) {
            ValidatorConfiguration validatorConfig = buildValidator(entry);
            configurations.add(validatorConfig);
        }
        return configurations;
    }

    @Override
    public Map<String, Validator> getValidators(String tenantDomain) {

        return InputValidationDataHolder.getValidators();
    }

    @Override
    public boolean validatePassword(Object credential, UserStoreManager userStoreManager) throws UserStoreException {

        int tenantId = userStoreManager.getTenantId();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        List<ValidationConfiguration> configurations;
        try {
            configurations = getInputValidationConfiguration(tenantDomain);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to validate password for user. " + e.getDescription());
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

    /**
     * Method to build validator configuration.
     *
     * @param   entry Validator entry.
     * @return  ValidatorConfiguration.
     */
    private ValidatorConfiguration buildValidator(Map.Entry<String, Validator> entry) {

        ValidatorConfiguration validatorConfig = new ValidatorConfiguration();
        validatorConfig.setName(entry.getKey());
        Validator validator = entry.getValue();
        validatorConfig.setProperties(validator.getConfigurationProperties());
        if (validator instanceof AbstractRegExValidator) {
            validatorConfig.setType(REGEX);
        } else {
            validatorConfig.setType(RULES);
        }
        return validatorConfig;
    }

    /**
     * Method to update validation configuration.
     *
     * @param configuration Validation configuration.
     * @param tenantDomain  Tenant domain name.
     * @return  Updated validation configuration.
     * @throws InputValidationMgtServerException If an error occurred when updating resource.
     */
    private ValidationConfiguration updateValidationConfiguration(
            ValidationConfiguration configuration, String tenantDomain) throws InputValidationMgtServerException {

        String resourceName = INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + configuration.getField();
        Resource oldResource = getResource(resourceName, tenantDomain);
        Resource newResource = buildResourceFromValidationConfig(configuration);

        Resource updatedResource;
        if (oldResource == null) {
            // Create resource for the first time.
            updatedResource = addResource(newResource, tenantDomain);
        } else {
            // Update the existing resource.
            updatedResource = updateResource(newResource, tenantDomain);
        }
        return buildValidationConfigFromResource(updatedResource);
    }

    /**
     * Method to get resource.
     *
     * @param resourceName  Name of the resource.
     * @param tenantDomain  Tenant domain name.
     * @return  resource.
     * @throws InputValidationMgtServerException If an error occurred when getting resource.
     */
    private Resource getResource(String resourceName, String tenantDomain) throws InputValidationMgtServerException {

        Resource oldResource = null;
        try {
            oldResource = getConfigurationManager().getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME,
                    resourceName);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw new InputValidationMgtServerException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(),
                        String.format(ERROR_GETTING_EXISTING_CONFIGURATIONS.getMessage(), tenantDomain));
            }
        }
        return oldResource;
    }

    /**
     * Method to add a resource.
     *
     * @param newResource   New resource to be added.
     * @param tenantDomain  Tenant domain name.
     * @return  resource.
     * @throws InputValidationMgtServerException If an error occurred when adding a new resource.
     */
    private Resource addResource(Resource newResource, String tenantDomain) throws InputValidationMgtServerException {

        Resource updatedResource;
        try {
            updatedResource =
                    getConfigurationManager().addResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, newResource);
        } catch (ConfigurationManagementException e) {
            throw new InputValidationMgtServerException(ERROR_WHILE_ADDING_CONFIGURATIONS.getCode(),
                    String.format(ERROR_WHILE_ADDING_CONFIGURATIONS.getMessage(), tenantDomain));
        }
        return updatedResource;
    }

    /**
     * Method to update the resource.
     *
     * @param newResource   Resource to be updated.
     * @param tenantDomain  Tenant domain name.
     * @return  updated resource.
     * @throws InputValidationMgtServerException If an error occurred when updating a new resource.
     */
    private Resource updateResource(Resource newResource, String tenantDomain)
            throws InputValidationMgtServerException {

        Resource updatedResource;
        try {
            updatedResource =
                    getConfigurationManager().replaceResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, newResource);
        } catch (ConfigurationManagementException e) {
            throw new InputValidationMgtServerException(ERROR_WHILE_UPDATING_CONFIGURATIONS.getCode(),
                    String.format(ERROR_WHILE_UPDATING_CONFIGURATIONS.getMessage(), tenantDomain));
        }
        return updatedResource;
    }

    /**
     * Method to get resources by resource type.
     *
     * @param tenantDomain  Tenant domain name.
     * @return  resources.
     * @throws InputValidationMgtServerException If an error occurred when getting the resources.
     */
    private List<Resource> getResourcesByType(String tenantDomain) throws InputValidationMgtServerException {

        Resources resources;
        try {
            resources =
                    getConfigurationManager().getResourcesByType(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME);
        } catch (ConfigurationManagementException e) {
            throw new InputValidationMgtServerException(ERROR_WHILE_UPDATING_CONFIGURATIONS.getCode(),
                    String.format(ERROR_WHILE_UPDATING_CONFIGURATIONS.getMessage(), tenantDomain));
        }
        return resources.getResources();
    }

    /**
     * Method to build resource from validation configuration.
     *
     * @param config    Validation configuration.
     * @return resource.
     */
    private Resource buildResourceFromValidationConfig(ValidationConfiguration config) {

        String field = config.getField();

        Resource resource = new Resource();
        resource.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + field);
        Map<String, String> configAttributes = new HashMap<>();

        if (config.getRules() != null) {
            configAttributes.put(VALIDATION_TYPE, RULES);
            addRulesConfigToResource(configAttributes, config.getRules());
        } else if (config.getRegEx() != null) {
            configAttributes.put(VALIDATION_TYPE, REGEX);
            addRulesConfigToResource(configAttributes, config.getRegEx());
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
     * Method to add rules to resource.
     *
     * @param configAttributes  Configuration attributes.
     * @param rules             List of rules configurations.
     */
    private void addRulesConfigToResource(Map<String, String> configAttributes, List<RulesConfiguration> rules) {

        for (RulesConfiguration rule : rules) {
            String validatorName = rule.getValidatorName();
            for (Map.Entry<String, String> entry : rule.getProperties().entrySet()) {
                String key = validatorName + "." + entry.getKey();
                configAttributes.put(key, entry.getValue());
            }
        }
    }

    /**
     * Method to build validation configuration from resource.
     *
     * @param resource  Resource.
     * @return Validation Configuration.
     */
    private ValidationConfiguration buildValidationConfigFromResource(Resource resource) {

        ValidationConfiguration configuration = new ValidationConfiguration();
        configuration.setField(resource.getResourceName().substring(
                resource.getResourceName().lastIndexOf("-") + 1));
        Map<String, String> attributesMap = resource.getAttributes().stream()
                        .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));

        // Build rules configurations from mapping.
        Map<String, Map<String, String>> validatorConfig = buildValidatorConfigGroup(attributesMap);
        List<RulesConfiguration> rules =
                validatorConfig.entrySet().stream()
                        .map(rule -> new RulesConfiguration(rule.getKey(), rule.getValue()))
                        .collect(Collectors.toList());

        // Check the input validation criteria.
        if (attributesMap.containsKey(VALIDATION_TYPE) &&
                StringUtils.equalsIgnoreCase(RULES, attributesMap.get(VALIDATION_TYPE))) {
            configuration.setRules(rules);
        } else {
            configuration.setRegEx(rules);
        }
        return configuration;
    }

    /**
     * Method to group the validator configurations.
     *
     * @param attributesMap Properties.
     * @return  validator properties.
     */
    private Map<String, Map<String, String>> buildValidatorConfigGroup(Map<String, String> attributesMap) {

        Map<String, Map<String, String>> validatorConfig = new HashMap<>();

        for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
            String key = entry.getKey();
            // Skip validation type attribute
            if (StringUtils.equalsIgnoreCase(VALIDATION_TYPE, key)) {
                continue;
            }
            String validatorName = key.substring(0, key.indexOf("."));
            String propertyName = key.substring(key.indexOf(".") + 1);
            Map<String, String> properties = new HashMap<>();
            if (validatorConfig.containsKey(validatorName)) {
                properties = validatorConfig.get(validatorName);
            }
            properties.put(propertyName, entry.getValue());
            validatorConfig.put(validatorName, properties);
        }
        return validatorConfig;
    }

    /**
     * Get configuration manger.
     *
     * @return configuration manger.
     */
    private ConfigurationManager getConfigurationManager() {

        return InputValidationDataHolder.getConfigurationManager();
    }
}
