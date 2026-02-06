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

package org.wso2.carbon.identity.compatibility.settings.core;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.CompatibilitySettingsEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingClientException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingConfigurationProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingMetaDataProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link CompatibilitySettingsManagerImpl}.
 */
@Listeners(MockitoTestNGListener.class)
public class CompatibilitySettingsManagerImplTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String SETTING_GROUP = "testSettingGroup";
    private static final String SETTING_GROUP_2 = "testSettingGroup2";
    private static final String SETTING_KEY = "testSetting";
    private static final String SETTING_KEY_2 = "testSetting2";
    private static final String SETTING_VALUE = "testValue";
    private static final String SETTING_VALUE_2 = "testValue2";

    @Mock
    private CompatibilitySettingsEvaluator evaluator1;

    @Mock
    private CompatibilitySettingsEvaluator evaluator2;

    @Mock
    private CompatibilitySettingMetaDataProvider metaDataProvider;

    @Mock
    private CompatibilitySettingConfigurationProvider configurationProvider;

    private MockedStatic<IdentityCompatibilitySettingsDataHolder> dataHolderMockedStatic;
    private IdentityCompatibilitySettingsDataHolder dataHolder;

    @BeforeMethod
    public void setUp() {

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
     * Test constructor initializes with empty metadata when no providers.
     */
    @Test
    public void testConstructorWithNoProviders() {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        assertNotNull(manager.getMetaData());
        assertTrue(manager.getSupportedSettings().isEmpty());
    }

    /**
     * Test constructor loads metadata from provider.
     */
    @Test
    public void testConstructorLoadsMetadataFromProvider() throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        assertNotNull(manager.getMetaData());
        Map<String, String[]> supportedSettings = manager.getSupportedSettings();
        assertNotNull(supportedSettings);
        assertTrue(supportedSettings.containsKey(SETTING_GROUP));
    }

    /**
     * Test constructor handles provider exception gracefully.
     */
    @Test
    public void testConstructorHandlesProviderException() throws CompatibilitySettingException {

        CompatibilitySettingMetaDataProvider exceptionProvider = mock(CompatibilitySettingMetaDataProvider.class);
        when(exceptionProvider.getMetaData()).thenThrow(new CompatibilitySettingException("Test error"));
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(exceptionProvider));

        // Should not throw exception.
        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        assertNotNull(manager.getMetaData());
    }

    /**
     * Test getSupportedSettings returns correct settings from metadata.
     */
    @Test
    public void testGetSupportedSettingsReturnsCorrectSettings() throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaDataWithMultipleGroups();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        Map<String, String[]> supportedSettings = manager.getSupportedSettings();

        assertNotNull(supportedSettings);
        assertEquals(supportedSettings.size(), 2);
        assertTrue(supportedSettings.containsKey(SETTING_GROUP));
        assertTrue(supportedSettings.containsKey(SETTING_GROUP_2));
    }

    /**
     * Test evaluate returns empty result when no evaluators.
     */
    @Test
    public void testEvaluateWithNoEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(new ArrayList<>());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        CompatibilitySetting result = manager.evaluate(context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate invokes all evaluators that can handle the context.
     */
    @Test
    public void testEvaluateInvokesAllHandlingEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1, evaluator2));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator2.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup1());
        when(evaluator2.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup2());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        CompatibilitySetting result = manager.evaluate(context);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluate(any(CompatibilitySettingContext.class));
        verify(evaluator2, times(1)).evaluate(any(CompatibilitySettingContext.class));
    }

    /**
     * Test evaluate skips evaluators that cannot handle the context.
     */
    @Test
    public void testEvaluateSkipsNonHandlingEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1, evaluator2));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator2.canHandle(any())).thenReturn(false);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        manager.evaluate(context);

        verify(evaluator1, times(1)).evaluate(any(CompatibilitySettingContext.class));
        verify(evaluator2, never()).evaluate(any(CompatibilitySettingContext.class));
    }

    /**
     * Test evaluate merges results from multiple evaluators.
     */
    @Test
    public void testEvaluateMergesResultsFromMultipleEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1, evaluator2));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator2.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup1());
        when(evaluator2.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup2());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        CompatibilitySetting result = manager.evaluate(context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 2);
        assertNotNull(result.getCompatibilitySetting(SETTING_GROUP));
        assertNotNull(result.getCompatibilitySetting(SETTING_GROUP_2));
    }

    /**
     * Test evaluate with settingGroup invokes all evaluators that can handle the context.
     */
    @Test
    public void testEvaluateWithSettingGroupInvokesEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));


        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluateByGroup(eq(SETTING_GROUP), any(CompatibilitySettingContext.class)))
                .thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        CompatibilitySetting result = manager.evaluateByGroup(SETTING_GROUP, context);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluateByGroup(eq(SETTING_GROUP), any(CompatibilitySettingContext.class));
    }

    /**
     * Test evaluate with settingGroup merges results from all evaluators.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingInvokesEvaluators() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluateByGroupAndSetting(eq(SETTING_GROUP), eq(SETTING_KEY),
                any(CompatibilitySettingContext.class)))
                .thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingContext context = createContext();

        CompatibilitySetting result = manager.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluateByGroupAndSetting(eq(SETTING_GROUP), eq(SETTING_KEY),
                any(CompatibilitySettingContext.class));
    }

    /**
     * Test getCompatibilitySettings builds context and evaluates.
     */
    @Test
    public void testGetCompatibilitySettingsBuildContextAndEvaluates() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));


        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySetting result = manager.getCompatibilitySettings(TENANT_DOMAIN);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluate(any(CompatibilitySettingContext.class));
    }

    /**
     * Test getCompatibilitySettings with settingGroup.
     */
    @Test
    public void testGetCompatibilitySettingsWithSettingGroup() throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));


        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluateByGroup(eq(SETTING_GROUP), any(CompatibilitySettingContext.class)))
                .thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySetting result = manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, SETTING_GROUP);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluateByGroup(eq(SETTING_GROUP), any(CompatibilitySettingContext.class));
    }

    /**
     * Test getCompatibilitySettings with settingGroup and setting.
     */
    @Test
    public void testGetCompatibilitySettingsWithSettingGroupAndSetting() throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));


        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluateByGroupAndSetting(eq(SETTING_GROUP), eq(SETTING_KEY),
                any(CompatibilitySettingContext.class)))
                .thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySetting result = manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP,
                SETTING_KEY);

        assertNotNull(result);
        verify(evaluator1, times(1)).evaluateByGroupAndSetting(eq(SETTING_GROUP), eq(SETTING_KEY),
                any(CompatibilitySettingContext.class));
    }

    /**
     * Test getCompatibilitySettings loads configuration from providers.
     */
    @Test
    public void testGetCompatibilitySettingsLoadsConfigurationFromProviders() throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));
        when(dataHolder.getConfigurationProviders()).thenReturn(Arrays.asList(configurationProvider));

        when(configurationProvider.getConfigurations(TENANT_DOMAIN)).thenReturn(createSettingsWithGroup1());
        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(new CompatibilitySetting());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        manager.getCompatibilitySettings(TENANT_DOMAIN);

        verify(configurationProvider, times(1)).getConfigurations(TENANT_DOMAIN);
    }

    /**
     * Test updateCompatibilitySettings validates and calls provider.
     */
    @Test
    public void testUpdateCompatibilitySettingsValidatesAndCallsProvider() throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));
        when(dataHolder.getConfigurationProviders()).thenReturn(Arrays.asList(configurationProvider));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluate(any(CompatibilitySettingContext.class))).thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySetting settingsToUpdate = createSettingsWithGroup1();

        manager.updateCompatibilitySettings(TENANT_DOMAIN, settingsToUpdate);

        verify(configurationProvider, times(1)).updateConfiguration(eq(settingsToUpdate), eq(TENANT_DOMAIN));
    }

    /**
     * Test updateCompatibilitySettings throws exception for unsupported setting group.
     */
    @Test
    public void testUpdateCompatibilitySettingsThrowsExceptionForUnsupportedGroup()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySettingGroup unsupportedGroup = new CompatibilitySettingGroup();
        unsupportedGroup.setSettingGroup("unsupportedGroup");
        unsupportedGroup.addSetting("someSetting", "someValue");

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(unsupportedGroup);

        try {
            manager.updateCompatibilitySettings(TENANT_DOMAIN, settings);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test updateCompatibilitySettings throws exception for unsupported setting.
     */
    @Test
    public void testUpdateCompatibilitySettingsThrowsExceptionForUnsupportedSetting()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySettingGroup groupWithUnsupportedSetting = new CompatibilitySettingGroup();
        groupWithUnsupportedSetting.setSettingGroup(SETTING_GROUP);
        groupWithUnsupportedSetting.addSetting("unsupportedSetting", "someValue");

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(groupWithUnsupportedSetting);

        try {
            manager.updateCompatibilitySettings(TENANT_DOMAIN, settings);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING.getCode()));
        }
    }

    /**
     * Test updateCompatibilitySettings throws exception for null settings.
     */
    @Test
    public void testUpdateCompatibilitySettingsThrowsExceptionForNullSettings()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.updateCompatibilitySettings(TENANT_DOMAIN, null);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING.getCode()));
        }
    }

    /**
     * Test updateCompatibilitySettings throws exception for empty settings.
     */
    @Test
    public void testUpdateCompatibilitySettingsThrowsExceptionForEmptySettings()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.updateCompatibilitySettings(TENANT_DOMAIN, new CompatibilitySetting());
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING.getCode()));
        }
    }

    /**
     * Test updateCompatibilitySettings with settingGroup validates and calls provider.
     */
    @Test
    public void testUpdateCompatibilitySettingsWithGroupValidatesAndCallsProvider()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));
        when(dataHolder.getCompatibilitySettingsEvaluators()).thenReturn(Arrays.asList(evaluator1));
        when(dataHolder.getConfigurationProviders()).thenReturn(Arrays.asList(configurationProvider));

        when(evaluator1.canHandle(any())).thenReturn(true);
        when(evaluator1.evaluateByGroup(eq(SETTING_GROUP), any(CompatibilitySettingContext.class)))
                .thenReturn(createSettingsWithGroup1());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        CompatibilitySettingGroup groupToUpdate = createSettingsGroup1();

        manager.updateCompatibilitySettingsGroup(TENANT_DOMAIN, SETTING_GROUP, groupToUpdate);

        verify(configurationProvider, times(1)).updateConfigurationGroup(eq(SETTING_GROUP), eq(groupToUpdate),
                eq(TENANT_DOMAIN));
    }

    /**
     * Test updateCompatibilitySettings with settingGroup throws exception for unsupported group.
     */
    @Test
    public void testUpdateCompatibilitySettingsWithGroupThrowsExceptionForUnsupportedGroup()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        CompatibilitySettingGroup unsupportedGroup = new CompatibilitySettingGroup();
        unsupportedGroup.setSettingGroup("unsupportedGroup");
        unsupportedGroup.addSetting("someSetting", "someValue");

        try {
            manager.updateCompatibilitySettingsGroup(TENANT_DOMAIN, "unsupportedGroup", unsupportedGroup);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test updateCompatibilitySettings with settingGroup throws exception for null group.
     */
    @Test
    public void testUpdateCompatibilitySettingsWithGroupThrowsExceptionForNullGroup()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.updateCompatibilitySettingsGroup(TENANT_DOMAIN, SETTING_GROUP, null);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test validation throws exception when supported settings not configured.
     */
    @Test
    public void testValidationThrowsExceptionWhenSupportedSettingsNotConfigured()
            throws CompatibilitySettingException {

        when(dataHolder.getMetaDataProviders()).thenReturn(new ArrayList<>());

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.updateCompatibilitySettings(TENANT_DOMAIN, createSettingsWithGroup1());
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertTrue(e.getErrorCode().contains(
                    ErrorMessages.ERROR_CODE_SUPPORTED_SETTINGS_NOT_CONFIGURED.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with null setting group throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithNullSettingGroupThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, (String) null);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with empty setting group throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithEmptySettingGroupThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, "");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with unsupported setting group throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithUnsupportedSettingGroupThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, "unsupportedGroup");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with setting group and null setting throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithNullSettingThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP, null);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with setting group and empty setting throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithEmptySettingThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP, "");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with unsupported setting throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithUnsupportedSettingThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP, "unsupportedSetting");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with setting group and unsupported group throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithUnsupportedGroupForSettingThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, "unsupportedGroup", SETTING_KEY);
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with whitespace-only setting group throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithWhitespaceSettingGroupThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, "   ");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with whitespace-only setting throws exception.
     */
    @Test
    public void testGetCompatibilitySettingsWithWhitespaceSettingThrowsException()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP, "   ");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings validates before building context.
     */
    @Test
    public void testGetCompatibilitySettingsValidatesBeforeBuildingContext()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroup(TENANT_DOMAIN, "unsupportedGroup");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode()));
        }
    }

    /**
     * Test getCompatibilitySettings with setting validates before building context.
     */
    @Test
    public void testGetCompatibilitySettingsWithSettingValidatesBeforeBuildingContext()
            throws CompatibilitySettingException {

        CompatibilitySettingMetaData metaData = createMetaData();
        when(metaDataProvider.getMetaData()).thenReturn(metaData);
        when(dataHolder.getMetaDataProviders()).thenReturn(Arrays.asList(metaDataProvider));

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();

        try {
            manager.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN, SETTING_GROUP, "unsupportedSetting");
            fail("Expected CompatibilitySettingClientException to be thrown");
        } catch (CompatibilitySettingClientException e) {
            assertTrue(e.getErrorCode().contains(ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING.getCode()));
        }
    }

    /**
     * Create a basic compatibility setting context.
     *
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContext() {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        return context;
    }

    /**
     * Create metadata with a single setting.
     *
     * @return CompatibilitySettingMetaData.
     */
    private CompatibilitySettingMetaData createMetaData() {

        CompatibilitySettingMetaDataEntry entry = new CompatibilitySettingMetaDataEntry();
        entry.setTimestampReference(Instant.now());
        entry.setTargetValue("targetValue");
        entry.setDefaultValue("defaultValue");

        CompatibilitySettingMetaDataGroup group = new CompatibilitySettingMetaDataGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSettingMetaData(SETTING_KEY, entry);

        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        metaData.addSettingMetaDataGroup(SETTING_GROUP, group);

        return metaData;
    }

    /**
     * Create metadata with multiple setting groups.
     *
     * @return CompatibilitySettingMetaData.
     */
    private CompatibilitySettingMetaData createMetaDataWithMultipleGroups() {

        CompatibilitySettingMetaDataEntry entry1 = new CompatibilitySettingMetaDataEntry();
        entry1.setTimestampReference(Instant.now());
        entry1.setTargetValue("targetValue1");
        entry1.setDefaultValue("defaultValue1");

        CompatibilitySettingMetaDataGroup group1 = new CompatibilitySettingMetaDataGroup();
        group1.setSettingGroup(SETTING_GROUP);
        group1.addSettingMetaData(SETTING_KEY, entry1);

        CompatibilitySettingMetaDataEntry entry2 = new CompatibilitySettingMetaDataEntry();
        entry2.setTimestampReference(Instant.now());
        entry2.setTargetValue("targetValue2");
        entry2.setDefaultValue("defaultValue2");

        CompatibilitySettingMetaDataGroup group2 = new CompatibilitySettingMetaDataGroup();
        group2.setSettingGroup(SETTING_GROUP_2);
        group2.addSettingMetaData(SETTING_KEY_2, entry2);

        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        metaData.addSettingMetaDataGroup(SETTING_GROUP, group1);
        metaData.addSettingMetaDataGroup(SETTING_GROUP_2, group2);

        return metaData;
    }

    /**
     * Create a compatibility setting group.
     *
     * @return CompatibilitySettingGroup.
     */
    private CompatibilitySettingGroup createSettingsGroup1() {

        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);
        return group;
    }

    /**
     * Create compatibility settings with group 1.
     *
     * @return CompatibilitySetting.
     */
    private CompatibilitySetting createSettingsWithGroup1() {

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(createSettingsGroup1());
        return settings;
    }

    /**
     * Create compatibility settings with group 2.
     *
     * @return CompatibilitySetting.
     */
    private CompatibilitySetting createSettingsWithGroup2() {

        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP_2);
        group.addSetting(SETTING_KEY_2, SETTING_VALUE_2);

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(group);
        return settings;
    }
}
