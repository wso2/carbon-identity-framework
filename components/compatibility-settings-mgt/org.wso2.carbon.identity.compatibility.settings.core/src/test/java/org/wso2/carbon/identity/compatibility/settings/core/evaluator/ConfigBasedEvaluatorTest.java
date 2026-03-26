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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ConfigBasedEvaluator}.
 */
public class ConfigBasedEvaluatorTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String SETTING_GROUP = "testSettingGroup";
    private static final String SETTING_GROUP_2 = "testSettingGroup2";
    private static final String SETTING_KEY = "testSetting";
    private static final String SETTING_KEY_2 = "testSetting2";
    private static final String SETTING_VALUE = "testValue";
    private static final String SETTING_VALUE_2 = "testValue2";

    private ConfigBasedEvaluator evaluator;

    @BeforeMethod
    public void setUp() {

        evaluator = new ConfigBasedEvaluator();
    }

    /**
     * Test getExecutionOrderId returns the expected execution order.
     */
    @Test
    public void testGetExecutionOrderId() {

        assertEquals(evaluator.getExecutionOrderId(), 20);
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
     * Test canHandle returns false when compatibility settings are null.
     */
    @Test
    public void testCanHandleWithNullCompatibilitySettings() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setCompatibilitySettings(null);

        assertFalse(evaluator.canHandle(context));
    }

    /**
     * Test canHandle returns false when compatibility settings are empty.
     */
    @Test
    public void testCanHandleWithEmptyCompatibilitySettings() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setCompatibilitySettings(new CompatibilitySetting());

        assertFalse(evaluator.canHandle(context));
    }

    /**
     * Test canHandle returns true when compatibility settings are present.
     */
    @Test
    public void testCanHandleWithValidCompatibilitySettings() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        assertTrue(evaluator.canHandle(context));
    }

    /**
     * Test evaluate returns empty settings when context has null compatibility settings.
     */
    @Test
    public void testEvaluateReturnsEmptyWhenSettingsNull() throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(null);

        // Ideally this scenario should not occur as canHandle would return false.
        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate returns the compatibility settings from context.
     */
    @Test
    public void testEvaluateReturnsSettingsFromContext() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), SETTING_VALUE);
    }

    /**
     * Test evaluate returns the same reference as context settings.
     */
    @Test
    public void testEvaluateReturnsSameReferenceAsContextSettings() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();
        CompatibilitySetting contextSettings = context.getCompatibilitySettings();

        CompatibilitySetting result = evaluator.evaluate(context);

        assertEquals(result, contextSettings);
    }

    /**
     * Test evaluate with multiple setting groups.
     */
    @Test
    public void testEvaluateWithMultipleSettingGroups() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithMultipleGroups();

        CompatibilitySetting result = evaluator.evaluate(context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 2);

        CompatibilitySettingGroup group1 = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(group1);
        assertEquals(group1.getSettingValue(SETTING_KEY), SETTING_VALUE);

        CompatibilitySettingGroup group2 = result.getCompatibilitySetting(SETTING_GROUP_2);
        assertNotNull(group2);
        assertEquals(group2.getSettingValue(SETTING_KEY_2), SETTING_VALUE_2);
    }

    /**
     * Test evaluate with settingGroup returns empty when context settings are null.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsEmptyWhenSettingsNull()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(null);

        CompatibilitySetting result = evaluator.evaluateByGroup(SETTING_GROUP, context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup returns empty when group not found.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsEmptyWhenGroupNotFound()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        CompatibilitySetting result = evaluator.evaluateByGroup("nonExistentGroup", context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup returns the specified group.
     */
    @Test
    public void testEvaluateWithSettingGroupReturnsSpecifiedGroup() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithMultipleGroups();

        CompatibilitySetting result = evaluator.evaluateByGroup(SETTING_GROUP, context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), SETTING_VALUE);
    }

    /**
     * Test evaluate with settingGroup only returns the requested group from multiple groups.
     */
    @Test
    public void testEvaluateWithSettingGroupFiltersCorrectGroup() throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithMultipleGroups();

        CompatibilitySetting result = evaluator.evaluateByGroup(SETTING_GROUP_2, context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP_2);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY_2), SETTING_VALUE_2);
        assertNull(result.getCompatibilitySetting(SETTING_GROUP));
    }

    /**
     * Test evaluate with settingGroup and setting returns empty when context settings are null.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsEmptyWhenSettingsNull()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(null);

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup and setting returns empty when group not found.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsEmptyWhenGroupNotFound()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting("nonExistentGroup", SETTING_KEY, context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup and setting returns empty when setting not found.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsEmptyWhenSettingNotFound()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, "nonExistentSetting", context);

        assertNotNull(result);
        assertTrue(result.getCompatibilitySettings().isEmpty());
    }

    /**
     * Test evaluate with settingGroup and setting returns the specified setting.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingReturnsSpecifiedSetting()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithSettings();

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettings().size(), 1);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), SETTING_VALUE);
    }

    /**
     * Test evaluate with settingGroup and setting filters to single setting from multiple settings.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingFiltersCorrectSetting()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithMultipleSettingsInGroup();

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP, SETTING_KEY, context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettings().size(), 1);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY), SETTING_VALUE);
        assertNull(resultGroup.getSettingValue(SETTING_KEY_2));
    }

    /**
     * Test evaluate with settingGroup and setting from the second group.
     */
    @Test
    public void testEvaluateWithSettingGroupAndSettingFromSecondGroup()
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = createContextWithMultipleGroups();

        CompatibilitySetting result = evaluator.evaluateByGroupAndSetting(SETTING_GROUP_2, SETTING_KEY_2, context);

        assertNotNull(result);
        assertEquals(result.getCompatibilitySettings().size(), 1);
        CompatibilitySettingGroup resultGroup = result.getCompatibilitySetting(SETTING_GROUP_2);
        assertNotNull(resultGroup);
        assertEquals(resultGroup.getSettingValue(SETTING_KEY_2), SETTING_VALUE_2);
    }

    /**
     * Create a compatibility setting context with a single setting.
     *
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContextWithSettings() {

        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(group);

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(settings);
        return context;
    }

    /**
     * Create context with multiple setting groups.
     *
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContextWithMultipleGroups() {

        CompatibilitySettingGroup group1 = new CompatibilitySettingGroup();
        group1.setSettingGroup(SETTING_GROUP);
        group1.addSetting(SETTING_KEY, SETTING_VALUE);

        CompatibilitySettingGroup group2 = new CompatibilitySettingGroup();
        group2.setSettingGroup(SETTING_GROUP_2);
        group2.addSetting(SETTING_KEY_2, SETTING_VALUE_2);

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(group1);
        settings.addCompatibilitySetting(group2);

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(settings);
        return context;
    }

    /**
     * Create context with multiple settings in the same group.
     *
     * @return CompatibilitySettingContext.
     */
    private CompatibilitySettingContext createContextWithMultipleSettingsInGroup() {

        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);
        group.addSetting(SETTING_KEY_2, SETTING_VALUE_2);

        CompatibilitySetting settings = new CompatibilitySetting();
        settings.addCompatibilitySetting(group);

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setCompatibilitySettings(settings);
        return context;
    }
}
