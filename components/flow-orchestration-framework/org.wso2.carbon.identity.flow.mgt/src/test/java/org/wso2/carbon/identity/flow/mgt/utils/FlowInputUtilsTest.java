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
import org.wso2.carbon.identity.flow.mgt.Constants.FlowInputConfigs;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;

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
public class FlowInputUtilsTest {

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
                {FlowInputDataType.STRING, "gold", "gold"},
                {FlowInputDataType.STRING, "<script>alert(1)</script>", null},
                {FlowInputDataType.STRING, "${jndi:ldap://x}", null},
                {FlowInputDataType.STRING, "line\nbreak", null},
                {FlowInputDataType.INTEGER, "42", 42L},
                {FlowInputDataType.INTEGER, "-7", -7L},
                {FlowInputDataType.INTEGER, "4.2", null},
                {FlowInputDataType.INTEGER, "1e3", null},
                {FlowInputDataType.DECIMAL, "-1.5", -1.5},
                {FlowInputDataType.DECIMAL, "3", 3.0},
                {FlowInputDataType.DECIMAL, "NaN", null},
                {FlowInputDataType.BOOLEAN, "TRUE", true},
                {FlowInputDataType.BOOLEAN, "yes", null},
                {FlowInputDataType.STRING_ARRAY, "[\"a\",\"b\"]", Arrays.asList("a", "b")},
                {FlowInputDataType.STRING_ARRAY, "[\"a\",1]", null},
                {FlowInputDataType.STRING_ARRAY, "a,b", null},
                {FlowInputDataType.NUMBER_ARRAY, "[1, 2.5]", Arrays.asList(1.0, 2.5)},
                {FlowInputDataType.NUMBER_ARRAY, "[\"1\"]", null},
        };
    }

    @Test(dataProvider = "texts")
    public void testParse(FlowInputDataType type, String text, Object expected) {

        assertEquals(FlowInputUtils.parse(type, text), expected);
    }

    /**
     * Each header line or repeated query parameter is one entry: a comma inside it does not split it.
     */
    @Test
    public void testParseEachKeepsEachTextAsOneEntry() {

        assertEquals(FlowInputUtils.parseEach(FlowInputDataType.STRING_ARRAY,
                Collections.singletonList("kusal,kamindu,jeewan")), Collections.singletonList("kusal,kamindu,jeewan"));
        assertEquals(FlowInputUtils.parseEach(FlowInputDataType.STRING_ARRAY, Arrays.asList("ashley", "hezzer")),
                Arrays.asList("ashley", "hezzer"));
        assertEquals(FlowInputUtils.parseEach(FlowInputDataType.NUMBER_ARRAY, Collections.singletonList("18")),
                Collections.singletonList(18.0));
    }

    @Test
    public void testParseEachRejectsTheWholeValueWhenAnEntryIsInvalid() {

        assertNull(FlowInputUtils.parseEach(FlowInputDataType.NUMBER_ARRAY, Arrays.asList("18", "eighteen")));
        assertNull(FlowInputUtils.parseEach(FlowInputDataType.STRING_ARRAY, Arrays.asList("a", "<b>")));
        assertNull(FlowInputUtils.parseEach(FlowInputDataType.STRING, Collections.singletonList("a")));
    }

    @Test
    public void testParseEachRejectsMoreEntriesThanAllowed() {

        List<String> texts = new ArrayList<>();
        for (int i = 0; i <= FlowInputConfigs.DEFAULT_MAX_ARRAY_SIZE; i++) {
            texts.add("v" + i);
        }
        assertNull(FlowInputUtils.parseEach(FlowInputDataType.STRING_ARRAY, texts));
    }

    /**
     * JSON keeps an integer as whatever fits, so a value read back from the stored context is put back in the form
     * its type keeps.
     */
    @Test
    public void testToValueRestoresTheFormOfTheType() {

        assertEquals(FlowInputUtils.toValue(FlowInputDataType.INTEGER, 3), 3L);
        assertEquals(FlowInputUtils.toValue(FlowInputDataType.DECIMAL, 3), 3.0);
        assertEquals(FlowInputUtils.toValue(FlowInputDataType.NUMBER_ARRAY, Arrays.asList(1, 2.5)),
                Arrays.asList(1.0, 2.5));
        assertNull(FlowInputUtils.toValue(FlowInputDataType.INTEGER, 3.5));
        assertNull(FlowInputUtils.toValue(FlowInputDataType.BOOLEAN, "true"));
    }

    @Test
    public void testFromInputRejectsNonFiniteNumbers() {

        assertNull(FlowInputUtils.fromInput(FlowInputDataType.DECIMAL, Double.NaN));
        assertNull(FlowInputUtils.fromInput(FlowInputDataType.DECIMAL, Double.POSITIVE_INFINITY));
        assertNull(FlowInputUtils.fromInput(FlowInputDataType.STRING, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testToTextWritesNumbersWithoutTrailingZeros() {

        assertEquals(FlowInputUtils.toText(18.0), "18");
        assertEquals(FlowInputUtils.toText(21.50), "21.5");
        assertEquals(FlowInputUtils.toText(7L), "7");
    }

    @Test
    public void testConfiguredMaxValueLengthApplies() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.MAX_VALUE_LENGTH)).thenReturn("5");

        assertEquals(FlowInputUtils.parse(FlowInputDataType.STRING, "abcde"), "abcde");
        assertNull(FlowInputUtils.parse(FlowInputDataType.STRING, "abcdef"));
    }

    @Test
    public void testDefaultExclusions() {

        assertTrue(FlowInputUtils.isExcludedHeader("Authorization"));
        assertTrue(FlowInputUtils.isExcludedHeader("X-Forwarded-For"));
        assertTrue(FlowInputUtils.isExcludedHeader("proxy-authorization"));
        assertFalse(FlowInputUtils.isExcludedHeader("x-ab-variant"));
        assertTrue(FlowInputUtils.isExcludedQueryParam("sessionDataKey"));
        assertFalse(FlowInputUtils.isExcludedQueryParam("SessionDataKey"));
        assertFalse(FlowInputUtils.isExcludedQueryParam("campaign"));
    }

    @Test
    public void testConfiguredExclusionsReplaceTheDefaults() {

        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(FlowInputConfigs.EXCLUDED_HEADERS))
                .thenReturn(Collections.singletonList("x-internal-*"));

        assertTrue(FlowInputUtils.isExcludedHeader("X-Internal-Token"));
        assertFalse(FlowInputUtils.isExcludedHeader("authorization"));
    }
}
