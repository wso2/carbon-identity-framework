/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.circuitbreaker;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link DefaultPolicyConfigurationLoader}.
 *
 * <p>Integration tests (getStaticPolicy / getRuntimePolicy) verify the values loaded from
 * identity.xml match the configured values. Reflection-based tests exercise the four private
 * parse helpers directly, injecting values into the live {@link IdentityUtil} configuration map
 * to cover blank-input defaults, clamping, invalid-format fallbacks, and whitespace trimming.
 */
public class DefaultPolicyConfigurationLoaderTest {

    private static final String TEST_KEY = "CircuitBreaker._test_";

    private static Map<String, Object> identityConfig;

    @BeforeClass
    public void setUpClass() throws Exception {

        URL root = getClass().getClassLoader().getResource(".");
        System.setProperty("carbon.home", new File(root.getPath()).getAbsolutePath());
        IdentityConfigParser.getInstance(new File(root.getPath(), "identity.xml").getAbsolutePath());
        IdentityUtil.populateProperties();

        Field configField = IdentityUtil.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        identityConfig = (Map<String, Object>) configField.get(null);
    }

    @AfterMethod
    public void tearDown() {

        identityConfig.remove(TEST_KEY);
    }

    // ─────────────────────────── getStaticPolicy() ───────────────────────────

    @Test
    public void testGetStaticPolicyReturnsNonNull() {

        assertNotNull(DefaultPolicyConfigurationLoader.getStaticPolicy());
    }

    @Test
    public void testGetStaticPolicyReturnsSameInstance() {

        assertSame(DefaultPolicyConfigurationLoader.getStaticPolicy(),
                DefaultPolicyConfigurationLoader.getStaticPolicy());
    }

    @Test
    public void testGetStaticPolicyEnabled() {

        assertTrue(DefaultPolicyConfigurationLoader.getStaticPolicy().isEnabled());
    }

    @Test
    public void testGetStaticPolicyCacheCapacity() {

        assertEquals(DefaultPolicyConfigurationLoader.getStaticPolicy().getTenantServiceCacheCapacity(), 2);
    }

    @Test
    public void testGetStaticPolicyEvictionThreshold() {

        assertEquals(DefaultPolicyConfigurationLoader.getStaticPolicy().getTenantServiceEvictionThreshold(), 1);
    }

    @Test
    public void testGetStaticPolicyEntryIdleTimeout() {

        assertEquals(DefaultPolicyConfigurationLoader.getStaticPolicy().getTenantServiceEntryIdleTimeout(), 600000L);
    }

    // ─────────────────────────── getRuntimePolicy() ───────────────────────────

    @Test
    public void testGetRuntimePolicyReturnsNonNull() {

        assertNotNull(DefaultPolicyConfigurationLoader.getRuntimePolicy());
    }

    @Test
    public void testGetRuntimePolicyReturnsSameInstance() {

        assertSame(DefaultPolicyConfigurationLoader.getRuntimePolicy(),
                DefaultPolicyConfigurationLoader.getRuntimePolicy());
    }

    @Test
    public void testGetRuntimePolicyWindowSize() {

        assertEquals(DefaultPolicyConfigurationLoader.getRuntimePolicy().getWindowSize(), 2);
    }

    @Test
    public void testGetRuntimePolicyMinCallsToEvaluate() {

        assertEquals(DefaultPolicyConfigurationLoader.getRuntimePolicy().getMinCallsToEvaluate(), 2);
    }

    @Test
    public void testGetRuntimePolicyFailureRateThreshold() {

        assertEquals(DefaultPolicyConfigurationLoader.getRuntimePolicy().getFailureRateThreshold(), 0.50, 0.001);
    }

    @Test
    public void testGetRuntimePolicyOpenDuration() {

        assertEquals(DefaultPolicyConfigurationLoader.getRuntimePolicy().getOpenDuration(), 600000L);
    }

    @Test
    public void testGetRuntimePolicyMaxInFlight() {

        assertEquals(DefaultPolicyConfigurationLoader.getRuntimePolicy().getMaxInFlight(), 2);
    }

    // ─────────────────────────── parseBoolean ───────────────────────────

    @Test
    public void testParseBooleanReturnsDefaultWhenKeyAbsent() throws Exception {

        assertFalse(invokeParseBoolean(TEST_KEY, false));
    }

    @Test
    public void testParseBooleanReturnsDefaultWhenBlank() throws Exception {

        identityConfig.put(TEST_KEY, "   ");

        assertTrue(invokeParseBoolean(TEST_KEY, true));
    }

    @Test
    public void testParseBooleanParsesTrue() throws Exception {

        identityConfig.put(TEST_KEY, "true");

        assertTrue(invokeParseBoolean(TEST_KEY, false));
    }

    @Test
    public void testParseBooleanParsesFalse() throws Exception {

        identityConfig.put(TEST_KEY, "false");

        assertFalse(invokeParseBoolean(TEST_KEY, true));
    }

    @Test
    public void testParseBooleanTrimsWhitespace() throws Exception {

        identityConfig.put(TEST_KEY, "  true  ");

        assertTrue(invokeParseBoolean(TEST_KEY, false));
    }

    @Test
    public void testParseBooleanCaseInsensitive() throws Exception {

        identityConfig.put(TEST_KEY, "TRUE");

        assertTrue(invokeParseBoolean(TEST_KEY, false));
    }

    // ─────────────────────────── parseInt ───────────────────────────

    @Test
    public void testParseIntReturnsDefaultWhenKeyAbsent() throws Exception {

        assertEquals(invokeParseInt(TEST_KEY, 10, 1), 10);
    }

    @Test
    public void testParseIntReturnsDefaultWhenBlank() throws Exception {

        identityConfig.put(TEST_KEY, "  ");

        assertEquals(invokeParseInt(TEST_KEY, 7, 1), 7);
    }

    @Test
    public void testParseIntParsesValidValue() throws Exception {

        identityConfig.put(TEST_KEY, "42");

        assertEquals(invokeParseInt(TEST_KEY, 1, 1), 42);
    }

    @Test
    public void testParseIntClampsValueBelowMin() throws Exception {

        identityConfig.put(TEST_KEY, "0");

        assertEquals(invokeParseInt(TEST_KEY, 5, 3), 3);
    }

    @Test
    public void testParseIntReturnsDefaultOnInvalidFormat() throws Exception {

        identityConfig.put(TEST_KEY, "notanumber");

        assertEquals(invokeParseInt(TEST_KEY, 8, 1), 8);
    }

    @Test
    public void testParseIntTrimsWhitespace() throws Exception {

        identityConfig.put(TEST_KEY, "  15  ");

        assertEquals(invokeParseInt(TEST_KEY, 1, 1), 15);
    }

    // ─────────────────────────── parseLong ───────────────────────────

    @Test
    public void testParseLongReturnsDefaultWhenKeyAbsent() throws Exception {

        assertEquals(invokeParseLong(TEST_KEY, 100L, 1L), 100L);
    }

    @Test
    public void testParseLongReturnsDefaultWhenBlank() throws Exception {

        identityConfig.put(TEST_KEY, "  ");

        assertEquals(invokeParseLong(TEST_KEY, 50L, 1L), 50L);
    }

    @Test
    public void testParseLongParsesValidValue() throws Exception {

        identityConfig.put(TEST_KEY, "9999");

        assertEquals(invokeParseLong(TEST_KEY, 1L, 1L), 9999L);
    }

    @Test
    public void testParseLongClampsValueBelowMin() throws Exception {

        identityConfig.put(TEST_KEY, "0");

        assertEquals(invokeParseLong(TEST_KEY, 5L, 3L), 3L);
    }

    @Test
    public void testParseLongReturnsDefaultOnInvalidFormat() throws Exception {

        identityConfig.put(TEST_KEY, "not_a_long");

        assertEquals(invokeParseLong(TEST_KEY, 20L, 1L), 20L);
    }

    @Test
    public void testParseLongTrimsWhitespace() throws Exception {

        identityConfig.put(TEST_KEY, "  500  ");

        assertEquals(invokeParseLong(TEST_KEY, 1L, 1L), 500L);
    }

    // ─────────────────────────── parseDouble ───────────────────────────

    @Test
    public void testParseDoubleReturnsDefaultWhenKeyAbsent() throws Exception {

        assertEquals(invokeParseDouble(TEST_KEY, 0.5, 0.01, 1.0), 0.5, 0.0001);
    }

    @Test
    public void testParseDoubleReturnsDefaultWhenBlank() throws Exception {

        identityConfig.put(TEST_KEY, "  ");

        assertEquals(invokeParseDouble(TEST_KEY, 0.7, 0.01, 1.0), 0.7, 0.0001);
    }

    @Test
    public void testParseDoubleParsesValidValue() throws Exception {

        identityConfig.put(TEST_KEY, "0.75");

        assertEquals(invokeParseDouble(TEST_KEY, 0.5, 0.01, 1.0), 0.75, 0.0001);
    }

    @Test
    public void testParseDoubleClampsToMin() throws Exception {

        identityConfig.put(TEST_KEY, "0.001");

        assertEquals(invokeParseDouble(TEST_KEY, 0.5, 0.01, 1.0), 0.01, 0.0001);
    }

    @Test
    public void testParseDoubleClampsToMax() throws Exception {

        identityConfig.put(TEST_KEY, "1.5");

        assertEquals(invokeParseDouble(TEST_KEY, 0.5, 0.01, 1.0), 1.0, 0.0001);
    }

    @Test
    public void testParseDoubleReturnsDefaultOnInvalidFormat() throws Exception {

        identityConfig.put(TEST_KEY, "notadouble");

        assertEquals(invokeParseDouble(TEST_KEY, 0.6, 0.01, 1.0), 0.6, 0.0001);
    }

    @Test
    public void testParseDoubleTrimsWhitespace() throws Exception {

        identityConfig.put(TEST_KEY, "  0.33  ");

        assertEquals(invokeParseDouble(TEST_KEY, 0.5, 0.01, 1.0), 0.33, 0.0001);
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private boolean invokeParseBoolean(String key, boolean defaultValue) throws Exception {

        Method method = DefaultPolicyConfigurationLoader.class.getDeclaredMethod(
                "parseBoolean", String.class, boolean.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, key, defaultValue);
    }

    private int invokeParseInt(String key, int defaultValue, int min) throws Exception {

        Method method = DefaultPolicyConfigurationLoader.class.getDeclaredMethod(
                "parseInt", String.class, int.class, int.class);
        method.setAccessible(true);
        return (Integer) method.invoke(null, key, defaultValue, min);
    }

    private long invokeParseLong(String key, long defaultValue, long min) throws Exception {

        Method method = DefaultPolicyConfigurationLoader.class.getDeclaredMethod(
                "parseLong", String.class, long.class, long.class);
        method.setAccessible(true);
        return (Long) method.invoke(null, key, defaultValue, min);
    }

    private double invokeParseDouble(String key, double defaultValue, double min, double max) throws Exception {

        Method method = DefaultPolicyConfigurationLoader.class.getDeclaredMethod(
                "parseDouble", String.class, double.class, double.class, double.class);
        method.setAccessible(true);
        return (Double) method.invoke(null, key, defaultValue, min, max);
    }
}
