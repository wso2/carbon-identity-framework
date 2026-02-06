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

package org.wso2.carbon.identity.compatibility.settings.core.evaluator;

import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link OrganizationalCreationTimeBasedEvaluator}.
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationalCreationTimeBasedEvaluatorTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String SETTING_GROUP = "testSettingGroup";
    private static final String SETTING_GROUP_2 = "testSettingGroup2";
    private static final String SETTING_KEY = "testSetting";
    private static final String SETTING_KEY_2 = "testSetting2";
    private static final String TARGET_VALUE = "targetValue";
    private static final String DEFAULT_VALUE = "defaultValue";

    private OrganizationalCreationTimeBasedEvaluator evaluator;
    private MockedStatic<IdentityCompatibilitySettingsDataHolder> dataHolderMockedStatic;
    private MockedStatic<IdentityCompatibilitySettingsUtil> utilMockedStatic;
    private IdentityCompatibilitySettingsDataHolder dataHolder;

    @BeforeMethod
    public void setUp() {

        evaluator = new OrganizationalCreationTimeBasedEvaluator();
        dataHolder = mock(IdentityCompatibilitySettingsDataHolder.class);

        dataHolderMockedStatic = mockStatic(IdentityCompatibilitySettingsDataHolder.class);
        utilMockedStatic = mockStatic(IdentityCompatibilitySettingsUtil.class);

        dataHolderMockedStatic.when(IdentityCompatibilitySettingsDataHolder::getInstance).thenReturn(dataHolder);
    }

    @AfterMethod
    public void tearDown() {

        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
        if (utilMockedStatic != null) {
            utilMockedStatic.close();
        }
    }

    /**
     * Test getExecutionOrderId returns the expected execution order.
     */
    @Test
    public void testGetExecutionOrderId() {

        assertEquals(evaluator.getExecutionOrderId(), 10);
    }

    /**
     * Test isEnabled returns true.
     */
    @Test
    public void testIsEnabled() {

        assertTrue(evaluator.isEnabled());
    }

    /**
     * Test canHandle returns false when context is null.
     */
    @Test
    public void testCanHandleWithNullContext() throws CompatibilitySettingException {

        assertFalse(evaluator.canHandle(null));
    }

    /**
     * Test canHandle returns false when metadata is null.
     */
    @Test
    public void testCanHandleWithNullMetadata() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setCompatibilitySettingMetaData(null);

        assertFalse(evaluator.canHandle(context));
    }

    /**
     * Test canHandle returns false when metadata settings are empty.
     */
    @Test
    public void testCanHandleWithEmptyMetadata() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        context.setCompatibilitySettingMetaData(metaData);

        assertFalse(evaluator.canHandle(context));
    }

    /**
     * Test canHandle returns true when metadata has settings.
     */
    @Test
    public void testCanHandleWithValidMetadata() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        CompatibilitySettingMetaData metaData = createMetaDataWithSingleSetting();
        context.setCompatibilitySettingMetaData(metaData);

        assertTrue(evaluator.canHandle(context));
    }

    /**
     * Test evaluate returns target value when organization creation time is before timestamp reference.
     */
    @Test
    public void testEvaluateReturnsTargetValueWhenOrgCreatedBeforeTimestamp() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.minus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), TARGET_VALUE);
    }

    /**
     * Test evaluate returns default value when organization creation time is after timestamp reference.
     */
    @Test
    public void testEvaluateReturnsDefaultValueWhenOrgCreatedAfterTimestamp() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.plus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), DEFAULT_VALUE);
    }

    /**
     * Test evaluate returns default value when organization creation time equals timestamp reference.
     */
    @Test
    public void testEvaluateReturnsDefaultValueWhenOrgCreatedAtExactTimestamp() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference;

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), DEFAULT_VALUE);
    }

    /**
     * Test evaluate returns default value when timestamp reference is null.
     */
    @Test
    public void testEvaluateReturnsDefaultValueWhenTimestampReferenceIsNull() throws Exception {

        CompatibilitySettingContext context = createContext(null);
        Instant orgCreationTime = Instant.now();

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), DEFAULT_VALUE);
    }

    /**
     * Test evaluate with multiple setting groups.
     */
    @Test
    public void testEvaluateWithMultipleSettingGroups() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.minus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContextWithMultipleGroups(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 2);

        CompatibilitySettingGroup group1 = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(group1);
        assertEquals(group1.getSettingValue(SETTING_KEY), TARGET_VALUE);

        CompatibilitySettingGroup group2 = result.getCompatibilitySetting(SETTING_GROUP_2);
        assertNotNull(group2);
        assertEquals(group2.getSettingValue(SETTING_KEY_2), TARGET_VALUE);
    }

    /**
     * Test evaluate with multiple settings in same group.
     */
    @Test
    public void testEvaluateWithMultipleSettingsInSameGroup() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.minus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContextWithMultipleSettingsInGroup(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettings().size(), 2);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), TARGET_VALUE);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY_2), TARGET_VALUE);
    }

    /**
     * Test evaluate method throws CompatibilitySettingServerException when
     * OrganizationManagementException occurs during organization creation time retrieval.
     */
    @Test
    public void testEvaluateThrowsServerExceptionOnOrganizationManagementException() throws Exception {

        CompatibilitySettingContext context = mock(CompatibilitySettingContext.class);
        CompatibilitySettingMetaData metaData = mock(CompatibilitySettingMetaData.class);

        when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        when(context.getCompatibilitySettingMetaData()).thenReturn(metaData);

        OrganizationManagementException orgException = new OrganizationManagementException("Test org error");
        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenThrow(orgException);

        CompatibilitySettingServerException expectedException = new CompatibilitySettingServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode(),
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getMessage(),
                orgException);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME,
                orgException, TENANT_DOMAIN)).thenReturn(expectedException);

        try {
            evaluator.evaluate(context);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode());
            assertEquals(e.getCause(), orgException);
        }
    }

    /**
     * Test evaluate with settingGroup returns target value when org created before timestamp.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsTargetValue() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.minus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluateByGroup(SETTING_GROUP, context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), TARGET_VALUE);
    }

    /**
     * Test evaluate with settingGroup returns default value when org created after timestamp.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsDefaultValue() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.plus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluateByGroup(SETTING_GROUP, context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), DEFAULT_VALUE);
    }

    /**
     * Test evaluate with settingGroup returns empty result when setting group not found.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsEmptyWhenGroupNotFound() throws Exception {

        CompatibilitySettingContext context = createContext(Instant.now());

        CompatibilitySetting result = evaluator.evaluateByGroup("nonExistentGroup", context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate method with settingGroup parameter throws CompatibilitySettingServerException
     * when OrganizationManagementException occurs.
     */
    @Test
    public void testEvaluateWithSettingGroupThrowsServerExceptionOnOrganizationManagementException()
            throws Exception {

        CompatibilitySettingContext context = mock(CompatibilitySettingContext.class);
        CompatibilitySettingMetaData metaData = mock(CompatibilitySettingMetaData.class);
        CompatibilitySettingMetaDataGroup metaDataGroup = mock(CompatibilitySettingMetaDataGroup.class);

        when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        when(context.getCompatibilitySettingMetaData()).thenReturn(metaData);
        when(metaData.getSettingMetaDataGroup(SETTING_GROUP)).thenReturn(metaDataGroup);

        OrganizationManagementException orgException = new OrganizationManagementException("Test org error");
        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenThrow(orgException);

        CompatibilitySettingServerException expectedException = new CompatibilitySettingServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode(),
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getMessage(),
                orgException);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME,
                orgException, TENANT_DOMAIN)).thenReturn(expectedException);

        try {
            evaluator.evaluateByGroup(SETTING_GROUP, context);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode());
            assertEquals(e.getCause(), orgException);
        }
    }

    /**
     * Test evaluate with settingGroup and setting returns target value when org created before timestamp.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsTargetValue() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.minus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), TARGET_VALUE);
    }

    /**
     * Test evaluate with settingGroup and setting returns default value when org created after timestamp.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsDefaultValue() throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.plus(1, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), DEFAULT_VALUE);
    }

    /**
     * Test evaluate with settingGroup and setting returns empty result when setting not found.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsEmptyWhenSettingNotFound() throws Exception {

        CompatibilitySettingContext context = createContext(Instant.now());

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, "nonExistentSetting", context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup and setting returns empty result when group not found.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsEmptyWhenGroupNotFound() throws Exception {

        CompatibilitySettingContext context = createContext(Instant.now());

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting("nonExistentGroup", SETTING_KEY, context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate method with settingGroup and setting parameters throws
     * CompatibilitySettingServerException when OrganizationManagementException occurs.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingThrowsServerExceptionOnOrganizationManagementException()
            throws Exception {

        CompatibilitySettingContext context = mock(CompatibilitySettingContext.class);
        CompatibilitySettingMetaData metaData = mock(CompatibilitySettingMetaData.class);
        CompatibilitySettingMetaDataEntry metaDataEntry = mock(CompatibilitySettingMetaDataEntry.class);

        when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        when(context.getCompatibilitySettingMetaData()).thenReturn(metaData);
        when(metaData.getSettingMetaDataEntry(SETTING_GROUP, SETTING_KEY)).thenReturn(metaDataEntry);

        OrganizationManagementException orgException = new OrganizationManagementException("Test org error");
        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenThrow(orgException);

        CompatibilitySettingServerException expectedException = new CompatibilitySettingServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode(),
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getMessage(),
                orgException);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME,
                orgException, TENANT_DOMAIN)).thenReturn(expectedException);

        try {
            evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME.getCode());
            assertEquals(e.getCause(), orgException);
        }
    }

    /**
     * Data provider for timestamp comparison scenarios.
     *
     * @return Test data with org creation time offset and expected value.
     */
    @DataProvider(name = "timestampComparisonProvider")
    public Object[][] timestampComparisonProvider() {

        return new Object[][]{

                {-1, TARGET_VALUE},   // Created 1 day before reference.
                {-30, TARGET_VALUE},  // Created 30 days before reference.
                {-365, TARGET_VALUE}, // Created 1 year before reference.
                {0, DEFAULT_VALUE},   // Created at exact reference time.
                {1, DEFAULT_VALUE},   // Created 1 day after reference.
                {30, DEFAULT_VALUE},  // Created 30 days after reference.
                {365, DEFAULT_VALUE}  // Created 1 year after reference.
        };
    }

    /**
     * Test evaluate with various timestamp comparison scenarios.
     *
     * @param orgCreationTimeOffsetDays Offset in days from timestamp reference.
     * @param expectedValue Expected setting value.
     */
    @Test(dataProvider = "timestampComparisonProvider")
    public void testEvaluateWithVariousTimestampScenarios(int orgCreationTimeOffsetDays, String expectedValue)
            throws Exception {

        Instant timestampReference = Instant.now();
        Instant orgCreationTime = timestampReference.plus(orgCreationTimeOffsetDays, ChronoUnit.DAYS);

        CompatibilitySettingContext context = createContext(timestampReference);

        utilMockedStatic.when(() -> IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(
                anyString(), anyBoolean())).thenReturn(orgCreationTime);

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), expectedValue);
    }

    /**
     * Create a compatibility setting context with a single setting.
     *
     * @param timestampReference Timestamp reference for evaluation.
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContext(Instant timestampReference) {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettingMetaData(createMetaDataWithSingleSetting(timestampReference));
        return context;
    }

    /**
     * Create metadata with a single setting.
     *
     * @return CompatibilitySettingMetaData.
     */
    private CompatibilitySettingMetaData createMetaDataWithSingleSetting() {

        return createMetaDataWithSingleSetting(Instant.now());
    }

    /**
     * Create metadata with a single setting.
     *
     * @param timestampReference Timestamp reference for the setting.
     * @return CompatibilitySettingMetaData.
     */
    private CompatibilitySettingMetaData createMetaDataWithSingleSetting(Instant timestampReference) {

        CompatibilitySettingMetaDataEntry entry = new CompatibilitySettingMetaDataEntry();
        entry.setTimestampReference(timestampReference);
        entry.setTargetValue(TARGET_VALUE);
        entry.setDefaultValue(DEFAULT_VALUE);

        CompatibilitySettingMetaDataGroup group = new CompatibilitySettingMetaDataGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSettingMetaData(SETTING_KEY, entry);

        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        metaData.addSettingMetaDataGroup(SETTING_GROUP, group);

        return metaData;
    }

    /**
     * Create context with multiple setting groups.
     *
     * @param timestampReference Timestamp reference for the settings.
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContextWithMultipleGroups(Instant timestampReference) {

        CompatibilitySettingMetaDataEntry entry1 = new CompatibilitySettingMetaDataEntry();
        entry1.setTimestampReference(timestampReference);
        entry1.setTargetValue(TARGET_VALUE);
        entry1.setDefaultValue(DEFAULT_VALUE);

        CompatibilitySettingMetaDataGroup group1 = new CompatibilitySettingMetaDataGroup();
        group1.setSettingGroup(SETTING_GROUP);
        group1.addSettingMetaData(SETTING_KEY, entry1);

        CompatibilitySettingMetaDataEntry entry2 = new CompatibilitySettingMetaDataEntry();
        entry2.setTimestampReference(timestampReference);
        entry2.setTargetValue(TARGET_VALUE);
        entry2.setDefaultValue(DEFAULT_VALUE);

        CompatibilitySettingMetaDataGroup group2 = new CompatibilitySettingMetaDataGroup();
        group2.setSettingGroup(SETTING_GROUP_2);
        group2.addSettingMetaData(SETTING_KEY_2, entry2);

        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        metaData.addSettingMetaDataGroup(SETTING_GROUP, group1);
        metaData.addSettingMetaDataGroup(SETTING_GROUP_2, group2);

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettingMetaData(metaData);
        return context;
    }

    /**
     * Create context with multiple settings in the same group.
     *
     * @param timestampReference Timestamp reference for the settings.
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContextWithMultipleSettingsInGroup(Instant timestampReference) {

        CompatibilitySettingMetaDataEntry entry1 = new CompatibilitySettingMetaDataEntry();
        entry1.setTimestampReference(timestampReference);
        entry1.setTargetValue(TARGET_VALUE);
        entry1.setDefaultValue(DEFAULT_VALUE);

        CompatibilitySettingMetaDataEntry entry2 = new CompatibilitySettingMetaDataEntry();
        entry2.setTimestampReference(timestampReference);
        entry2.setTargetValue(TARGET_VALUE);
        entry2.setDefaultValue(DEFAULT_VALUE);

        CompatibilitySettingMetaDataGroup group = new CompatibilitySettingMetaDataGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSettingMetaData(SETTING_KEY, entry1);
        group.addSettingMetaData(SETTING_KEY_2, entry2);

        CompatibilitySettingMetaData metaData = new CompatibilitySettingMetaData();
        metaData.addSettingMetaDataGroup(SETTING_GROUP, group);

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettingMetaData(metaData);
        return context;
    }
}
