/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.utils;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableConfigs;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests that every defined value reaching the flow execution context is checked and kept in the one form its type
 * allows.
 */
public class FlowVariableUtilsTest {

    private MockedStatic<IdentityUtil> identityUtilMock;

    @BeforeMethod
    public void setUp() {

        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
    }

    @AfterMethod
    public void tearDown() {

        identityUtilMock.close();
    }

    @DataProvider
    public Object[][] texts() {

        return new Object[][]{
                {FlowVariableDataType.STRING, "gold", "gold"},
                {FlowVariableDataType.STRING, "<script>alert(1)</script>", null},
                {FlowVariableDataType.STRING, "${jndi:ldap://x}", null},
                {FlowVariableDataType.STRING, "line\nbreak", null},
                {FlowVariableDataType.INTEGER, "42", 42L},
                {FlowVariableDataType.INTEGER, "-7", -7L},
                {FlowVariableDataType.INTEGER, "4.2", null},
                {FlowVariableDataType.INTEGER, "1e3", null},
                {FlowVariableDataType.DECIMAL, "-1.5", -1.5},
                {FlowVariableDataType.DECIMAL, "3", 3.0},
                {FlowVariableDataType.DECIMAL, "NaN", null},
                {FlowVariableDataType.BOOLEAN, "TRUE", true},
                {FlowVariableDataType.BOOLEAN, "yes", null},
                {FlowVariableDataType.STRING_ARRAY, "[\"a\",\"b\"]", Arrays.asList("a", "b")},
                {FlowVariableDataType.STRING_ARRAY, "[\"a\",1]", null},
                {FlowVariableDataType.STRING_ARRAY, "a,b", null},
                {FlowVariableDataType.NUMBER_ARRAY, "[1, 2.5]", Arrays.asList(1.0, 2.5)},
                {FlowVariableDataType.NUMBER_ARRAY, "[\"1\"]", null},
        };
    }

    @Test(dataProvider = "texts")
    public void testParse(FlowVariableDataType type, String text, Object expected) {

        assertEquals(FlowVariableUtils.parse(type, text), expected);
    }

    /**
     * Each header line is one entry: a comma inside it does not split it.
     */
    @Test
    public void testParseEachKeepsEachTextAsOneEntry() {

        assertEquals(FlowVariableUtils.parseEach(FlowVariableDataType.STRING_ARRAY,
                Collections.singletonList("kusal,kamindu,jeewan")), Collections.singletonList("kusal,kamindu,jeewan"));
        assertEquals(FlowVariableUtils.parseEach(FlowVariableDataType.STRING_ARRAY, Arrays.asList("ashley", "hezzer")),
                Arrays.asList("ashley", "hezzer"));
        assertEquals(FlowVariableUtils.parseEach(FlowVariableDataType.NUMBER_ARRAY, Collections.singletonList("18")),
                Collections.singletonList(18.0));
    }

    @Test
    public void testParseEachRejectsTheWholeValueWhenAnEntryIsInvalid() {

        assertNull(FlowVariableUtils.parseEach(FlowVariableDataType.NUMBER_ARRAY, Arrays.asList("18", "eighteen")));
        assertNull(FlowVariableUtils.parseEach(FlowVariableDataType.STRING_ARRAY, Arrays.asList("a", "<b>")));
        assertNull(FlowVariableUtils.parseEach(FlowVariableDataType.STRING, Collections.singletonList("a")));
    }

    @Test
    public void testParseEachRejectsMoreEntriesThanAllowed() {

        List<String> texts = new ArrayList<>();
        for (int i = 0; i <= FlowVariableConfigs.DEFAULT_MAX_ARRAY_SIZE; i++) {
            texts.add("v" + i);
        }
        assertNull(FlowVariableUtils.parseEach(FlowVariableDataType.STRING_ARRAY, texts));
    }

    /**
     * JSON keeps an integer as whatever fits, so a value read back from the stored context is put back in the form
     * its type keeps.
     */
    @Test
    public void testToValueRestoresTheFormOfTheType() {

        assertEquals(FlowVariableUtils.toValue(FlowVariableDataType.INTEGER, 3), 3L);
        assertEquals(FlowVariableUtils.toValue(FlowVariableDataType.DECIMAL, 3), 3.0);
        assertEquals(FlowVariableUtils.toValue(FlowVariableDataType.NUMBER_ARRAY, Arrays.asList(1, 2.5)),
                Arrays.asList(1.0, 2.5));
        assertNull(FlowVariableUtils.toValue(FlowVariableDataType.INTEGER, 3.5));
        assertNull(FlowVariableUtils.toValue(FlowVariableDataType.BOOLEAN, "true"));
    }

    @Test
    public void testFromInputRejectsNonFiniteNumbers() {

        assertNull(FlowVariableUtils.fromInput(FlowVariableDataType.DECIMAL, Double.NaN));
        assertNull(FlowVariableUtils.fromInput(FlowVariableDataType.DECIMAL, Double.POSITIVE_INFINITY));
        assertNull(FlowVariableUtils.fromInput(FlowVariableDataType.STRING, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testFromInputKeepsTheDigitsOfALargeInteger() {

        String digits = "123456789012345678901234567890";
        assertEquals(FlowVariableUtils.fromInput(FlowVariableDataType.STRING, new BigInteger(digits)), digits);
    }

    @Test
    public void testToTextWritesNumbersWithoutTrailingZeros() {

        assertEquals(FlowVariableUtils.toText(18.0), "18");
        assertEquals(FlowVariableUtils.toText(21.50), "21.5");
        assertEquals(FlowVariableUtils.toText(7L), "7");
    }

    @Test
    public void testConfiguredMaxValueLengthApplies() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.MAX_VALUE_LENGTH)).thenReturn("5");

        assertEquals(FlowVariableUtils.parse(FlowVariableDataType.STRING, "abcde"), "abcde");
        assertNull(FlowVariableUtils.parse(FlowVariableDataType.STRING, "abcdef"));
    }

    @Test
    public void testDefaultExclusions() {

        assertTrue(FlowVariableUtils.isExcludedHeader("Authorization"));
        assertTrue(FlowVariableUtils.isExcludedHeader("X-Forwarded-For"));
        assertTrue(FlowVariableUtils.isExcludedHeader("proxy-authorization"));
        assertFalse(FlowVariableUtils.isExcludedHeader("x-ab-variant"));
        assertTrue(FlowVariableUtils.isExcludedHeader("COOKIE"));
    }

    @Test
    public void testConfiguredExclusionsReplaceTheDefaults() {

        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(FlowVariableConfigs.EXCLUDED_HEADERS))
                .thenReturn(Collections.singletonList("x-internal-*"));

        assertTrue(FlowVariableUtils.isExcludedHeader("X-Internal-Token"));
        assertFalse(FlowVariableUtils.isExcludedHeader("authorization"));
    }
}
