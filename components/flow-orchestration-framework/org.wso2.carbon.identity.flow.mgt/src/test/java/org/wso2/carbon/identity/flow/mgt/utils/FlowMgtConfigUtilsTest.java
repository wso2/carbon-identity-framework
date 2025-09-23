/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.utils;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.model.FlowConfigDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.FLOW_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.IS_AUTO_LOGIN_ENABLED;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.IS_ENABLED;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.RESOURCE_TYPE;

/**
 * Unit tests for {@link FlowMgtConfigUtils}.
 */
public class FlowMgtConfigUtilsTest {

    private ConfigurationManager configurationManager;
    private MockedStatic<FlowMgtServiceDataHolder> flowMgtServiceDataHolderMock;
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String FLOW_TYPE_REGISTRATION = "REGISTRATION";
    private static final String FLOW_TYPE_PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
    private static final String FLOW_TYPE_INVITED_USER_REGISTRATION = "INVITED_USER_REGISTRATION";

    @BeforeMethod
    public void setUp() {

        configurationManager = mock(ConfigurationManager.class);
        FlowMgtServiceDataHolder serviceDataHolder = mock(FlowMgtServiceDataHolder.class);

        flowMgtServiceDataHolderMock = Mockito.mockStatic(FlowMgtServiceDataHolder.class);

        flowMgtServiceDataHolderMock.when(FlowMgtServiceDataHolder::getInstance).thenReturn(serviceDataHolder);
        when(serviceDataHolder.getConfigurationManager()).thenReturn(configurationManager);
    }

    @AfterMethod
    public void tearDown() {

        if (flowMgtServiceDataHolderMock != null) {
            flowMgtServiceDataHolderMock.close();
        }
    }

    @Test
    public void testAddFlowConfigNewResource() throws Exception {

        FlowConfigDTO flowConfigDTO = createSampleFlowConfig();
        Resource newResource = createSampleResource();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);
        when(configurationManager.addResource(eq(RESOURCE_TYPE), any(Resource.class))).thenReturn(newResource);

        FlowConfigDTO result = FlowMgtConfigUtils.addFlowConfig(flowConfigDTO, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        verify(configurationManager).addResource(eq(RESOURCE_TYPE), any(Resource.class));
    }

    @Test
    public void testAddFlowConfigUpdateExistingResource() throws Exception {

        FlowConfigDTO flowConfigDTO = createSampleFlowConfig();
        Resource existingResource = createSampleResource();
        Resource updatedResource = createSampleResource();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(existingResource);
        when(configurationManager.replaceResource(eq(RESOURCE_TYPE), any(Resource.class))).thenReturn(updatedResource);

        FlowConfigDTO result = FlowMgtConfigUtils.addFlowConfig(flowConfigDTO, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        verify(configurationManager).replaceResource(eq(RESOURCE_TYPE), any(Resource.class));
    }

    @Test
    public void testAddFlowConfigWithResourceTypeCreation() throws Exception {

        FlowConfigDTO flowConfigDTO = createSampleFlowConfig();
        Resource newResource = createSampleResource();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);
        when(configurationManager.addResource(eq(RESOURCE_TYPE), any(Resource.class)))
                .thenThrow(new ConfigurationManagementException("Resource type does not exist",
                        ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()))
                .thenReturn(newResource);
        when(configurationManager.addResourceType(any(ResourceTypeAdd.class))).thenReturn(null);

        FlowConfigDTO result = FlowMgtConfigUtils.addFlowConfig(flowConfigDTO, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        verify(configurationManager).addResourceType(any(ResourceTypeAdd.class));
        verify(configurationManager, times(2)).addResource(eq(RESOURCE_TYPE), any(Resource.class));
    }

    @Test
    public void testGetFlowConfig() throws Exception {

        Resource resource = createSampleResource();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(resource);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigReturnsDefaultRegistrationFlow() throws Exception {

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                        Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigReturnsDefaultPasswordRecoveryFlow() throws Exception {

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_PASSWORD_RECOVERY, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_PASSWORD_RECOVERY);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigReturnsDefaultInvitedUserRegistrationFlow() throws Exception {

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_INVITED_USER_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_INVITED_USER_REGISTRATION);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigs() throws Exception {

        Resources resources = new Resources();
        List<Resource> resourceList = Collections.singletonList(createSampleResource());
        resources.setResources(resourceList);

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);

        List<String> flowTypes = Arrays.asList("REGISTRATION", "PASSWORD_RECOVERY", "INVITED_USER_REGISTRATION");
        for (String flowType : flowTypes) {
            Assert.assertTrue(result.stream().anyMatch(config -> config.getFlowType().equals(flowType)));
        }
    }

    @Test
    public void testGetFlowConfigsReturnsDefaultWhenNoResources() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(null);

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);
        for (FlowConfigDTO config : result) {

            Assert.assertFalse(config.getIsEnabled());
            Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
            switch (config.getFlowType()) {
                case FLOW_TYPE_REGISTRATION:
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
                    break;
                case FLOW_TYPE_PASSWORD_RECOVERY:
                case FLOW_TYPE_INVITED_USER_REGISTRATION:
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
                    break;
                default:
                    Assert.fail("Unexpected flow type: " + config.getFlowType());
            }
        }
    }

    @Test
    public void testGetFlowConfigsHandlesKnownExceptions() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE))
                .thenThrow(new ConfigurationManagementException("Resource type does not exist",
                        ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()));

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);
    }

    @Test
    public void testGetFlowConfigsHandlesResourceDoesNotExistException() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE))
                .thenThrow(new ConfigurationManagementException("Resource does not exist",
                        ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode()));

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);
        for (FlowConfigDTO config : result) {
            Assert.assertFalse(config.getIsEnabled());
            Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
            switch (config.getFlowType()) {
                case FLOW_TYPE_REGISTRATION:
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
                    break;
                case FLOW_TYPE_PASSWORD_RECOVERY:
                case FLOW_TYPE_INVITED_USER_REGISTRATION:
                    Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                            Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
                    break;
                default:
                    Assert.fail("Unexpected flow type: " + config.getFlowType());
            }
        }
    }

    @Test
    public void testGetFlowConfigHandlesException() throws Exception {

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean()))
                .thenThrow(new ConfigurationManagementException("Resource type does not exist",
                        ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()));

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testAddFlowConfigWithResourceCreationFailure() throws Exception {

        FlowConfigDTO flowConfigDTO = createSampleFlowConfig();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(null);
        when(configurationManager.addResource(eq(RESOURCE_TYPE), any(Resource.class)))
                .thenThrow(new ConfigurationManagementException("Resource type does not exist",
                        ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()));
        when(configurationManager.addResourceType(any(ResourceTypeAdd.class)))
                .thenThrow(new ConfigurationManagementException("Failed to create resource type", "Resource type " +
                        "creation error code"));

        try {
            FlowMgtConfigUtils.addFlowConfig(flowConfigDTO, TENANT_DOMAIN);
            Assert.fail("Expected FlowMgtServerException to be thrown");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FlowMgtServerException);
            Assert.assertTrue(e.getMessage().contains("Error while adding the flow config"));
        }
    }

    @Test
    public void testGetFlowConfigWithEmptyAttributes() throws Exception {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME_PREFIX + FLOW_TYPE_REGISTRATION);
        resource.setResourceType(RESOURCE_TYPE);
        resource.setAttributes(Collections.emptyList());

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(resource);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigWithNullAttributes() throws Exception {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME_PREFIX + FLOW_TYPE_REGISTRATION);
        resource.setResourceType(RESOURCE_TYPE);
        resource.setAttributes(null);

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(resource);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertFalse(result.getIsEnabled());
        Assert.assertFalse(Boolean.parseBoolean(
                result.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testBuildFlowConfigFromResourceWithAllAttributes() throws Exception {

        Resource resource = createCompleteResource();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(resource);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(result.getIsEnabled());
        Assert.assertTrue(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertTrue(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertTrue(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertTrue(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testBuildFlowConfigFromResourceWithMissingAttributes() throws Exception {

        Resource resource = createResourceWithMissingAttributes();

        when(configurationManager.getResource(eq(RESOURCE_TYPE), anyString(), anyBoolean())).thenReturn(resource);

        FlowConfigDTO result = FlowMgtConfigUtils.getFlowConfig(FLOW_TYPE_REGISTRATION, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getFlowType(), FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(result.getIsEnabled());

        // Missing attributes should take default values.
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED)));
        Assert.assertFalse(Boolean.parseBoolean(result.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED)));
    }

    @Test
    public void testGetFlowConfigsWithEmptyResourceList() throws Exception {

        Resources resources = new Resources();
        resources.setResources(Collections.emptyList());

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);
        for (FlowConfigDTO config : result) {
            Assert.assertFalse(config.getIsEnabled());
            Assert.assertFalse(Boolean.parseBoolean(config.getFlowCompletionConfig(
                Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED)));
        }
    }

    @Test
    public void testGetFlowConfigsWithMultipleResources() throws Exception {

        Resources resources = new Resources();
        List<Resource> resourceList = Arrays.asList(
                createResourceForFlowType("REGISTRATION"),
                createResourceForFlowType("PASSWORD_RECOVERY")
        );
        resources.setResources(resourceList);

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        List<FlowConfigDTO> result = FlowMgtConfigUtils.getFlowConfigs(TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 3);

        long registrationCount = result.stream()
                .filter(config -> "REGISTRATION".equals(config.getFlowType()))
                .count();
        long passwordRecoveryCount = result.stream()
                .filter(config -> "PASSWORD_RECOVERY".equals(config.getFlowType()))
                .count();
        long invitedUserCount = result.stream()
                .filter(config -> "INVITED_USER_REGISTRATION".equals(config.getFlowType()))
                .count();

        Assert.assertEquals(registrationCount, 1);
        Assert.assertEquals(passwordRecoveryCount, 1);
        Assert.assertEquals(invitedUserCount, 1);
    }

    private FlowConfigDTO createSampleFlowConfig() {

        FlowConfigDTO flowConfigDTO = new FlowConfigDTO();
        flowConfigDTO.setFlowType(FLOW_TYPE_REGISTRATION);
        flowConfigDTO.setIsEnabled(true);
        flowConfigDTO.addAllFlowCompletionConfigs(Constants.FlowTypes.REGISTRATION.getSupportedFlowCompletionConfigs());
        return flowConfigDTO;
    }

    private Resource createSampleResource() {

        return createResourceForFlowType(FLOW_TYPE_REGISTRATION);
    }

    private Resource createResourceForFlowType(String flowType) {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME_PREFIX + flowType);
        resource.setResourceType(RESOURCE_TYPE);
        resource.setTenantDomain(TENANT_DOMAIN);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(FLOW_TYPE, flowType));
        attributes.add(new Attribute(IS_ENABLED, "true"));
        for (Constants.FlowCompletionConfig property : Constants.FlowTypes
                .valueOf(flowType).getSupportedFlowCompletionConfigs()) {
            attributes.add(new Attribute(property.getConfig(), property.getDefaultValue()));
        }
        resource.setAttributes(attributes);
        return resource;
    }

    private Resource createCompleteResource() {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME_PREFIX + FLOW_TYPE_REGISTRATION);
        resource.setResourceType(RESOURCE_TYPE);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(FLOW_TYPE, FLOW_TYPE_REGISTRATION));
        attributes.add(new Attribute(IS_ENABLED, "true"));
        attributes.add(new Attribute(IS_AUTO_LOGIN_ENABLED, "true"));
        attributes.add(new Attribute(Constants.FlowCompletionConfig.IS_EMAIL_VERIFICATION_ENABLED.getConfig(), "true"));
        attributes.add(new Attribute(
                Constants.FlowCompletionConfig.IS_ACCOUNT_LOCK_ON_CREATION_ENABLED.getConfig(), "true"));
        attributes.add(new Attribute(
                Constants.FlowCompletionConfig.IS_FLOW_COMPLETION_NOTIFICATION_ENABLED.getConfig(), "true"));
        resource.setAttributes(attributes);
        return resource;
    }

    private Resource createResourceWithMissingAttributes() {

        return createResourceWithMissingAttributes(FLOW_TYPE_REGISTRATION);
    }

    private Resource createResourceWithMissingAttributes(String flowType) {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME_PREFIX + flowType);
        resource.setResourceType(RESOURCE_TYPE);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(FLOW_TYPE, flowType));
        attributes.add(new Attribute(IS_ENABLED, "true"));
        resource.setAttributes(attributes);
        return resource;
    }
}
