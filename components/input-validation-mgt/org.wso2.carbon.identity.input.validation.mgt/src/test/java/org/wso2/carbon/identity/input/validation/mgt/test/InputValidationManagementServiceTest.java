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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
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
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME;

/**
 * Testing the InputValidationManagementService class
 */
@PrepareForTest({ InputValidationDataHolder.class })
public class InputValidationManagementServiceTest extends PowerMockTestCase {

    private InputValidationManagementService service;
    private String tenantName = "testTenant";
    private String field = "password";

    @BeforeMethod
    public void setup() {

        service = new InputValidationManagementServiceImpl();
        mockStatic(InputValidationDataHolder.class);
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
                        INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + field))
                        .thenReturn(resources.getResources().get(0));
            when(configurationManager.replaceResource(anyString(), (Resource) any()))
                    .thenReturn(resources.getResources().get(0));
            List<ValidationConfiguration> updated = service.updateInputValidationConfiguration(getValidationConfig(),
                    tenantName);
            Assert.assertEquals(updated.get(0).getField(), field);
            Assert.assertEquals(updated.get(0).getRules().size(), 1);
        } catch (ConfigurationManagementException | InputValidationMgtException e) {
            Assert.fail();
        }
    }

    private Resources getResources() {

        Resources resources = new Resources();
        List<Resource> resourceList = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();

        Attribute attribute = new Attribute();
        attribute.setKey("LengthValidator.min.length");
        attribute.setValue("5");
        attributes.add(attribute);

        Attribute type = new Attribute();
        type.setKey("validation.type");
        type.setValue("RULE");
        attributes.add(type);

        Resource resource = new Resource();
        resource.setAttributes(attributes);
        resource.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + field);
        resourceList.add(resource);
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
        configuration.setField(field);
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
