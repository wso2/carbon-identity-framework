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

package org.wso2.carbon.identity.input.validation.mgt.test;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidatorConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LengthValidator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME;

/**
 * Testing the InputValidationManagementService class
 */
public class InputValidationManagementServiceTest {

    private InputValidationManagementService service;
    private String tenantName = "testTenant";
    private String fieldPassword = "password";
    private String fieldUsername = "username";
    private MockedStatic<InputValidationDataHolder> inputValidationDataHolder;

    @BeforeMethod
    public void setup() {

        service = new InputValidationManagementServiceImpl();
        inputValidationDataHolder = mockStatic(InputValidationDataHolder.class);
    }

    @AfterMethod
    public void tearDown() {

        inputValidationDataHolder.close();
    }

    @Test
    public void getInputValidatorsConfigurationTest() {

        when(InputValidationDataHolder.getValidators()).thenReturn(getValidators());
        List<ValidatorConfiguration> config = null;
        try {
            config = service.getValidatorConfigurations(tenantName);
            Assert.assertFalse(config.isEmpty());
        } catch (InputValidationMgtException e) {
            Assert.fail();
        }
    }

    @Test
    public void getInputValidationConfigurationTest() {

        Resources resources = getResources();
        ConfigurationManager configurationManager = mock(ConfigurationManager.class);
        when(InputValidationDataHolder.getConfigurationManager()).thenReturn(configurationManager);
        try {
            when(configurationManager.getResourcesByType(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME)).thenReturn(resources);
            List<ValidationConfiguration> updated = service.getInputValidationConfiguration(tenantName);
            Assert.assertFalse(updated.isEmpty());
        } catch (ConfigurationManagementException | InputValidationMgtException e) {
            Assert.fail();
        }
    }

    @Test
    public void updateInputValidationConfigurationTest() {

        Resources resources = getResources();
        ConfigurationManager configurationManager = mock(ConfigurationManager.class);
        when(InputValidationDataHolder.getConfigurationManager()).thenReturn(configurationManager);
        try {
            when(configurationManager.getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME,
                    INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + fieldPassword))
                    .thenReturn(resources.getResources().get(0));
            when(configurationManager.replaceResource(anyString(), (Resource) any()))
                    .thenReturn(resources.getResources().get(0));
            List<ValidationConfiguration> updated = service.updateInputValidationConfiguration(getValidationConfig(),
                    tenantName);
            Assert.assertEquals(updated.get(0).getField(), fieldPassword);
            Assert.assertEquals(updated.get(0).getRules().size(), 1);
        } catch (ConfigurationManagementException | InputValidationMgtException e) {
            Assert.fail();
        }
    }

    @Test
    public void testRevertInputValidationConfiguration() throws Exception {

        ConfigurationManager configurationManager = mock(ConfigurationManager.class);
        when(InputValidationDataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Case 1: Resource exists and should be deleted.
        String existingField = "existingField";
        List<String> fieldsToRevert = new ArrayList<>();
        fieldsToRevert.add(existingField);
        String resourceName = INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + existingField;
        Resource mockResource = new Resource();
        mockResource.setResourceName(resourceName);

        when(configurationManager.getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, resourceName))
                .thenReturn(mockResource);
        service.revertInputValidationConfiguration(fieldsToRevert, tenantName);
        verify(configurationManager, times(1)).deleteResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, resourceName);

        // Case 2: Resource does not exist, delete should not be called.
        String nonExistingField = "nonExistingField";
        fieldsToRevert.clear();
        fieldsToRevert.add(nonExistingField);
        String nonExistingResourceName = INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + nonExistingField;
        when(configurationManager.getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, nonExistingResourceName))
                .thenReturn(null);
        service.revertInputValidationConfiguration(fieldsToRevert, tenantName);
        // Verify deleteResource was not called again for this case (still 1 from previous case).
        verify(configurationManager, times(1)).deleteResource(anyString(), anyString());

        // Case 3: Multiple fields, one exists, one doesn't.
        fieldsToRevert.clear();
        fieldsToRevert.add(existingField);
        fieldsToRevert.add(nonExistingField);

        when(configurationManager.getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, resourceName))
                .thenReturn(mockResource);
        when(configurationManager.getResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, nonExistingResourceName))
                .thenReturn(null);

        service.revertInputValidationConfiguration(fieldsToRevert, tenantName);
        // ExistingField's resource should be deleted (called once more, total 2 times).
        verify(configurationManager, times(2)).deleteResource(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, resourceName);
         // NonExistingField's resource should not be deleted (still 0 times for this specific resource).
        verify(configurationManager, never()).deleteResource(
                INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME, nonExistingResourceName);
    }

    private Resources getResources() {

        Resources resources = new Resources();
        List<Resource> resourceList = new ArrayList<>();
        List<Attribute> attributesForPassword = new ArrayList<>();

        Attribute attribute1 = new Attribute();
        attribute1.setKey("LengthValidator.min.length");
        attribute1.setValue("5");
        attributesForPassword.add(attribute1);

        Attribute type = new Attribute();
        type.setKey("validation.type");
        type.setValue("RULE");
        attributesForPassword.add(type);

        List<Attribute> attributesForUsername = new ArrayList<>();

        Attribute attribute2 = new Attribute();
        attribute2.setKey("isEmail");
        attribute2.setValue("true");
        attributesForUsername.add(attribute1);

        Resource resourceForPassword = new Resource();
        resourceForPassword.setAttributes(attributesForPassword);
        resourceForPassword.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + fieldPassword);
        resourceList.add(resourceForPassword);

        Resource resourceForUsername = new Resource();
        resourceForUsername.setAttributes(attributesForPassword);
        resourceForUsername.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + fieldUsername);
        resourceList.add(resourceForUsername);

        resources.setResources(resourceList);
        return resources;
    }

    public List<ValidationConfiguration> getValidationConfig() {

        HashMap<String, String> properties = new HashMap<>();
        properties.put("min.length", "5");

        List<RulesConfiguration> rules = new ArrayList<>();
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(properties);
        rules.add(rule);

        List<ValidationConfiguration> configurations = new ArrayList<>();
        ValidationConfiguration configuration = new ValidationConfiguration();
        configuration.setField(fieldPassword);
        configuration.setRules(rules);
        configurations.add(configuration);
        return configurations;
    }

    private Map<String, Validator> getValidators() {

        Map<String, Validator> validators = new HashMap<>();
        validators.put(LengthValidator.class.getSimpleName(), new LengthValidator());
        return validators;
    }
}
