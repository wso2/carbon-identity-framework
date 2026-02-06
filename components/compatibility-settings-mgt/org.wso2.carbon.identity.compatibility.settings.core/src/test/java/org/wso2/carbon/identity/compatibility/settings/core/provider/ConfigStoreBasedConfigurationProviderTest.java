/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.provider;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_RESOURCE_PREFIX;
import static org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_RESOURCE_TYPE;

/**
 * Unit tests for {@link ConfigStoreBasedConfigurationProvider}.
 */
@Listeners(MockitoTestNGListener.class)
public class ConfigStoreBasedConfigurationProviderTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String SETTING_GROUP_SCIM2 = "scim2";
    private static final String SETTING_GROUP_OAUTH = "oauth";
    private static final String SETTING_KEY_1 = "conflictOnClaimUniquenessViolation";
    private static final String SETTING_KEY_2 = "enableLegacyFlow";
    private static final String SETTING_VALUE_TRUE = "true";
    private static final String SETTING_VALUE_FALSE = "false";

    @Mock
    private ConfigurationManager configurationManager;

    private ConfigStoreBasedConfigurationProvider provider;
    private MockedStatic<IdentityCompatibilitySettingsDataHolder> dataHolderMockedStatic;
    private IdentityCompatibilitySettingsDataHolder dataHolder;

    @BeforeMethod
    public void setUp() {

        provider = new ConfigStoreBasedConfigurationProvider();
        dataHolder = mock(IdentityCompatibilitySettingsDataHolder.class);

        dataHolderMockedStatic = mockStatic(IdentityCompatibilitySettingsDataHolder.class);
        dataHolderMockedStatic.when(IdentityCompatibilitySettingsDataHolder::getInstance).thenReturn(dataHolder);
    }

    @AfterMethod
    public void tearDown() {

        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
    }

    /**
     * Test getPriority returns correct value.
     */
    @Test
    public void testGetPriority() {

        assertEquals(provider.getPriority(), 100);
    }

    /**
     * Test getConfigurations returns empty settings when ConfigurationManager is null.
     */
    @Test
    public void testGetConfigurationsWithNullConfigurationManager() throws CompatibilitySettingException {

        when(dataHolder.getConfigurationManager()).thenReturn(null);

        CompatibilitySetting result = provider.getConfigurations(TENANT_DOMAIN);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test getConfigurations returns empty settings when no resources exist.
     */
    @Test
    public void testGetConfigurationsWithNoResources() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);
        Resources emptyResources = new Resources();
        emptyResources.setResources(new ArrayList<>());
        when(configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE))
                .thenReturn(emptyResources);

        CompatibilitySetting result = provider.getConfigurations(TENANT_DOMAIN);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test getConfigurations returns settings when resources exist.
     */
    @Test
    public void testGetConfigurationsWithResources() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        Resource resource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_TRUE);
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(resource);

        Resources resources = new Resources();
        resources.setResources(resourceList);

        when(configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE))
                .thenReturn(resources);

        CompatibilitySetting result = provider.getConfigurations(TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        assertNotNull(result.getCompatibilitySetting(SETTING_GROUP_SCIM2));
        assertEquals(result.getCompatibilitySetting(SETTING_GROUP_SCIM2)
                .getSettingValue(SETTING_KEY_1), SETTING_VALUE_TRUE);
    }

    /**
     * Test getConfigurations returns null when resource does not exist error.
     */
    @Test
    public void testGetConfigurationsResourceDoesNotExist() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        ConfigurationManagementException exception = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE))
                .thenThrow(exception);

        CompatibilitySetting result = provider.getConfigurations(TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test getConfigurations returns null when resource type does not exist error.
     */
    @Test
    public void testGetConfigurationsResourceTypeDoesNotExist() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        ConfigurationManagementException exception = new ConfigurationManagementException(
                "Resource type not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE))
                .thenThrow(exception);

        CompatibilitySetting result = provider.getConfigurations(TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test getConfigurations throws exception on other configuration errors.
     */
    @Test
    public void testGetConfigurationsThrowsExceptionOnError() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        ConfigurationManagementException exception = new ConfigurationManagementException(
                "Unknown error", "UNKNOWN_ERROR");
        when(configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE))
                .thenThrow(exception);

        try {
            provider.getConfigurations(TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_READING_COMPATIBILITY_SETTINGS_CONFIG.getCode());
        }
    }

    /**
     * Test getConfigurations with settingGroup returns null when setting group is blank.
     */
    @Test
    public void testGetConfigurationsWithBlankSettingGroup() throws CompatibilitySettingException {

        CompatibilitySettingGroup result = provider.getConfigurationsByGroup("", TENANT_DOMAIN);
        assertNull(result);

        result = provider.getConfigurationsByGroup(null, TENANT_DOMAIN);
        assertNull(result);
    }

    /**
     * Test getConfigurations with settingGroup returns null when ConfigurationManager is null.
     */
    @Test
    public void testGetConfigurationsWithSettingGroupNullConfigManager() throws CompatibilitySettingException {

        when(dataHolder.getConfigurationManager()).thenReturn(null);

        CompatibilitySettingGroup result = provider.getConfigurationsByGroup(SETTING_GROUP_SCIM2, TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test getConfigurations with settingGroup returns setting group.
     */
    @Test
    public void testGetConfigurationsWithSettingGroup() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        Resource resource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_TRUE);
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;

        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(true))).thenReturn(resource);

        CompatibilitySettingGroup result = provider.getConfigurationsByGroup(SETTING_GROUP_SCIM2, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getSettingGroup(), SETTING_GROUP_SCIM2);
        assertEquals(result.getSettingValue(SETTING_KEY_1), SETTING_VALUE_TRUE);
    }

    /**
     * Test getConfigurations with settingGroup and setting returns specific value.
     */
    @Test
    public void testGetConfigurationsWithSettingGroupAndSetting() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        Resource resource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_TRUE);
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;

        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(true))).thenReturn(resource);

        String result = provider.getConfigurationsByGroupAndSetting(SETTING_GROUP_SCIM2, SETTING_KEY_1, TENANT_DOMAIN);

        assertEquals(result, SETTING_VALUE_TRUE);
    }

    /**
     * Test getConfigurations with blank setting group or setting returns null.
     */
    @Test
    public void testGetConfigurationsWithBlankSettingOrGroup() throws CompatibilitySettingException {

        String result = provider.getConfigurationsByGroupAndSetting("", SETTING_KEY_1, TENANT_DOMAIN);
        assertNull(result);

        result = provider.getConfigurationsByGroupAndSetting(SETTING_GROUP_SCIM2, "", TENANT_DOMAIN);
        assertNull(result);

        result = provider.getConfigurationsByGroupAndSetting(null, SETTING_KEY_1, TENANT_DOMAIN);
        assertNull(result);
    }

    /**
     * Test getConfigurations with non-existent setting returns null.
     */
    @Test
    public void testGetConfigurationsWithNonExistentSetting() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        Resource resource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_TRUE);
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;

        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(true)))
                .thenReturn(resource);

        String result = provider.getConfigurationsByGroupAndSetting(SETTING_GROUP_SCIM2, "nonExistentSetting",
                TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test updateConfiguration with blank setting group returns null.
     */
    @Test
    public void testUpdateConfigurationWithBlankSettingGroup() throws CompatibilitySettingException {

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);

        CompatibilitySettingGroup result = provider.updateConfigurationGroup("", settingGroup, TENANT_DOMAIN);
        assertNull(result);

        result = provider.updateConfigurationGroup(null, settingGroup, TENANT_DOMAIN);
        assertNull(result);
    }

    /**
     * Test updateConfiguration with null compatibility setting returns null.
     */
    @Test
    public void testUpdateConfigurationWithNullSetting() throws CompatibilitySettingException {

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, null, TENANT_DOMAIN);
        assertNull(result);
    }

    /**
     * Test updateConfiguration with null ConfigurationManager returns null.
     */
    @Test
    public void testUpdateConfigurationWithNullConfigManager() throws CompatibilitySettingException {

        when(dataHolder.getConfigurationManager()).thenReturn(null);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test updateConfiguration adds new resource when not exists.
     */
    @Test
    public void testUpdateConfigurationAddsNewResource() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenReturn(new Resource());

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);

        assertNotNull(result);
        verify(configurationManager, times(1)).addResource(anyString(), any(Resource.class));
    }

    /**
     * Test updateConfiguration updates existing resource.
     */
    @Test
    public void testUpdateConfigurationUpdatesExistingResource() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource exists.
        Resource existingResource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_FALSE);
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenReturn(existingResource);

        when(configurationManager.replaceAttribute(anyString(), anyString(), any(Attribute.class)))
                .thenReturn(new Attribute());

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);

        assertNotNull(result);
        verify(configurationManager, times(1))
                .replaceAttribute(anyString(), anyString(), any(Attribute.class));
    }

    /**
     * Test updateConfiguration creates resource type when not exists.
     */
    @Test
    public void testUpdateConfigurationCreatesResourceType() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        // Resource type does not exist on first add.
        ConfigurationManagementException resourceTypeNotFound = new ConfigurationManagementException(
                "Resource type not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenThrow(resourceTypeNotFound)
                .thenReturn(new Resource());

        when(configurationManager.addResourceType(any())).thenReturn(null);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);

        assertNotNull(result);
        verify(configurationManager, times(1)).addResourceType(any());
        verify(configurationManager, times(2)).addResource(anyString(), any(Resource.class));
    }

    /**
     * Test updateConfiguration with CompatibilitySetting updates all groups.
     */
    @Test
    public void testUpdateConfigurationWithCompatibilitySetting() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Both resources do not exist.
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(anyString(), anyString(), eq(false)))
                .thenThrow(notFoundException);

        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenReturn(new Resource());

        CompatibilitySetting compatibilitySetting = new CompatibilitySetting();

        CompatibilitySettingGroup group1 = new CompatibilitySettingGroup();
        group1.setSettingGroup(SETTING_GROUP_SCIM2);
        group1.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);
        compatibilitySetting.addCompatibilitySetting(group1);

        CompatibilitySettingGroup group2 = new CompatibilitySettingGroup();
        group2.setSettingGroup(SETTING_GROUP_OAUTH);
        group2.addSetting(SETTING_KEY_2, SETTING_VALUE_FALSE);
        compatibilitySetting.addCompatibilitySetting(group2);

        CompatibilitySetting result = provider.updateConfiguration(compatibilitySetting, TENANT_DOMAIN);

        assertNotNull(result);
        verify(configurationManager, times(2)).addResource(anyString(), any(Resource.class));
    }

    /**
     * Test updateConfiguration with empty CompatibilitySetting returns null.
     */
    @Test
    public void testUpdateConfigurationWithEmptyCompatibilitySetting() throws CompatibilitySettingException {

        CompatibilitySetting emptySettings = new CompatibilitySetting();

        CompatibilitySetting result = provider.updateConfiguration(emptySettings, TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test updateConfiguration with null CompatibilitySetting returns null.
     */
    @Test
    public void testUpdateConfigurationWithNullCompatibilitySetting() throws CompatibilitySettingException {

        CompatibilitySetting result = provider.updateConfiguration(null, TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test updateConfiguration throws exception on update error.
     */
    @Test
    public void testUpdateConfigurationThrowsExceptionOnUpdateError() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource exists.
        Resource existingResource = createResource(SETTING_GROUP_SCIM2, SETTING_KEY_1, SETTING_VALUE_FALSE);
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenReturn(existingResource);

        ConfigurationManagementException updateException = new ConfigurationManagementException(
                "Update error", "UPDATE_ERROR");
        when(configurationManager.replaceAttribute(anyString(), anyString(), any(Attribute.class)))
                .thenThrow(updateException);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        try {
            provider.updateConfigurationGroup(SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS.getCode());
        }
    }

    /**
     * Test updateConfiguration skips adding resource with no attributes.
     */
    @Test
    public void testUpdateConfigurationSkipsEmptyAttributes() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        // No settings added - empty attributes.

        CompatibilitySettingGroup result = provider.updateConfigurationGroup(
                SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);

        assertNotNull(result);
        // Should not call addResource since attributes are empty.
        verify(configurationManager, never()).addResource(anyString(), any(Resource.class));
    }

    /**
     * Test getConfigurations(settingGroup, tenantDomain) throws server exception on unknown error.
     */
    @Test
    public void testGetConfigurationsWithSettingGroupThrowsServerException() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException unknownException = new ConfigurationManagementException(
                "Unknown error", "UNKNOWN_ERROR");
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(true))).thenThrow(unknownException);

        try {
            provider.getConfigurationsByGroup(SETTING_GROUP_SCIM2, TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_READING_COMPATIBILITY_SETTINGS_CONFIG.getCode());
            assertTrue(e.getDescription().contains(TENANT_DOMAIN));
        }
    }

    /**
     * Test getConfigurations(settingGroup, tenantDomain) returns null for resource not found.
     */
    @Test
    public void testGetConfigurationsWithSettingGroupResourceNotFound() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(true))).thenThrow(notFoundException);

        CompatibilitySettingGroup result = provider.getConfigurationsByGroup(SETTING_GROUP_SCIM2, TENANT_DOMAIN);

        assertNull(result);
    }

    /**
     * Test addResource throws server exception on error.
     */
    @Test
    public void testAddResourceThrowsServerException() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        // addResource throws unknown error.
        ConfigurationManagementException addResourceException = new ConfigurationManagementException(
                "Add resource error", "ADD_RESOURCE_ERROR");
        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenThrow(addResourceException);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        try {
            provider.updateConfigurationGroup(SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS.getCode());
            assertTrue(e.getDescription().contains(TENANT_DOMAIN));
        }
    }

    /**
     * Test createCompatibilityResourceType throws server exception on error.
     */
    @Test
    public void testCreateCompatibilityResourceTypeThrowsServerException() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        // Resource type does not exist on first add.
        ConfigurationManagementException resourceTypeNotFound = new ConfigurationManagementException(
                "Resource type not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenThrow(resourceTypeNotFound);

        // addResourceType throws error.
        ConfigurationManagementException addResourceTypeException = new ConfigurationManagementException(
                "Add resource type error", "ADD_RESOURCE_TYPE_ERROR");
        when(configurationManager.addResourceType(any())).thenThrow(addResourceTypeException);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        try {
            provider.updateConfigurationGroup(SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_CREATING_RESOURCE_TYPE.getCode());
            assertTrue(e.getDescription().contains(COMPATIBILITY_SETTINGS_RESOURCE_TYPE));
            assertTrue(e.getDescription().contains(TENANT_DOMAIN));
        }
    }

    /**
     * Test addResource after resource type creation throws server exception.
     */
    @Test
    public void testAddResourceAfterResourceTypeCreationThrowsServerException() throws Exception {

        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);

        // Resource does not exist.
        String formattedResourceName = COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + SETTING_GROUP_SCIM2;
        ConfigurationManagementException notFoundException = new ConfigurationManagementException(
                "Resource not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(eq(COMPATIBILITY_SETTINGS_RESOURCE_TYPE),
                eq(formattedResourceName), eq(false))).thenThrow(notFoundException);

        // Resource type does not exist on first add, then throws error on second add.
        ConfigurationManagementException resourceTypeNotFound = new ConfigurationManagementException(
                "Resource type not found",
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        ConfigurationManagementException secondAddException = new ConfigurationManagementException(
                "Second add error", "SECOND_ADD_ERROR");
        when(configurationManager.addResource(anyString(), any(Resource.class)))
                .thenThrow(resourceTypeNotFound)
                .thenThrow(secondAddException);

        when(configurationManager.addResourceType(any())).thenReturn(null);

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP_SCIM2);
        settingGroup.addSetting(SETTING_KEY_1, SETTING_VALUE_TRUE);

        try {
            provider.updateConfigurationGroup(SETTING_GROUP_SCIM2, settingGroup, TENANT_DOMAIN);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS.getCode());
            assertTrue(e.getDescription().contains(TENANT_DOMAIN));
        }
    }

    /**
     * Helper method to create a Resource with attributes.
     *
     * @param settingGroup Setting group name.
     * @param settingKey   Setting key.
     * @param settingValue Setting value.
     * @return Resource object.
     */
    private Resource createResource(String settingGroup, String settingKey, String settingValue) {

        Resource resource = new Resource();
        resource.setResourceName(COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + settingGroup);
        resource.setResourceType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(settingKey, settingValue));
        resource.setAttributes(attributes);

        return resource;
    }
}
