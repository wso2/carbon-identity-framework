/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.base;

import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test Class for the IdentityValidationUtil
 */
public class IdentityValidationUtilTest {

    private static final String DIGITS = IdentityValidationUtil.ValidatorPattern.DIGITS_ONLY.name();
    private static final String EMAIL = IdentityValidationUtil.ValidatorPattern.EMAIL.name();
    private static final String URL = IdentityValidationUtil.ValidatorPattern.URL.name();
    private static final String ALPHABETIC = IdentityValidationUtil.ValidatorPattern.ALPHABETIC_ONLY.name();

    private static final String SIMPLE_LETTERS_KEY = "SIMPLE_LETTERS_ONLY";
    private static final String SIMPLE_LETTERS_REGEX = "^[a-zA-Z]+$";

    @DataProvider
    public Object[][] getWhileListTestData() {
        return new Object[][]{
                {"", new String[]{DIGITS}, true},
                {"1241234", new String[]{DIGITS}, true},
                {"12fqwe34", new String[]{DIGITS}, false},
                {"12fqwe34", new String[]{DIGITS, EMAIL}, false},
                {"12fqwe34@foo.com", new String[]{DIGITS, EMAIL}, true},
                {"test@", new String[]{EMAIL}, false},
        };
    }

    @Test(dataProvider = "getWhileListTestData")
    public void testIsValidOverWhiteListPatterns(String patternToTest, String[] patterns, boolean expected) throws
            Exception {
        boolean isValid = IdentityValidationUtil.isValidOverWhiteListPatterns(patternToTest, patterns);
        assertEquals(isValid, expected, String.format("Pattern: %s, expected: %s, actual: %s", patternToTest,
                expected, isValid));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsValidOverWhiteListPatternsNoPatterns() throws Exception {
        IdentityValidationUtil.isValidOverWhiteListPatterns("foo");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsValidOverWhiteListPatternsInvalidPatterns() throws Exception {
        IdentityValidationUtil.isValidOverWhiteListPatterns("foo", "FOO_PATTERN");
    }

    @DataProvider
    public Object[][] getBlackListTestData() {
        return new Object[][]{
                {"", new String[]{DIGITS}, true},
                {"1241234", new String[]{DIGITS}, false},
                {"12fqwe34", new String[]{DIGITS}, true},
                {"12fqwe34", new String[]{DIGITS, EMAIL}, true},
                {"test@foo.com", new String[]{EMAIL}, false},
                {"test@", new String[]{EMAIL}, true},
        };
    }

    @Test(dataProvider = "getBlackListTestData")
    public void testIsValidOverBlackListPatterns(String patternToTest, String[] patterns, boolean expected) throws
            Exception {
        boolean isValid = IdentityValidationUtil.isValidOverBlackListPatterns(patternToTest, patterns);
        assertEquals(isValid, expected, String.format("Pattern: %s, expected: %s, actual: %s", patternToTest,
                expected, isValid));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsValidOverBlackListPatternsInvalidPatterns() throws Exception {
        IdentityValidationUtil.isValidOverBlackListPatterns("foo");
    }

    @DataProvider
    public Object[][] getValidationTestData() {
        return new Object[][]{
                {"", new String[]{DIGITS}, new String[]{URL}, true},
                {"12354", new String[]{ALPHABETIC}, new String[]{DIGITS}, false},
                {"12354", new String[]{DIGITS}, new String[]{DIGITS}, true},
                {"123dqw54", new String[]{DIGITS}, new String[]{EMAIL}, true},
                {"https://example.com", new String[]{DIGITS, ALPHABETIC}, new String[]{URL, EMAIL}, false},
        };
    }

    @Test(dataProvider = "getValidationTestData")
    public void testIsValid(String patternToTest, String[] whiteListPatterns, String[] blackListPatterns, boolean
            expected) throws Exception {
        boolean isValid = IdentityValidationUtil.isValid(patternToTest, whiteListPatterns, blackListPatterns);
        assertEquals(isValid, expected, String.format("Pattern: %s, expected: %s, actual: %s", patternToTest,
                expected, isValid));
    }

    @DataProvider
    public Object[][] getValidationNegativeTestData() {
        return new Object[][]{
                {"12354", new String[]{}, new String[]{}},
                {"12354", new String[]{DIGITS}, new String[]{}},
                {"12354", new String[]{}, new String[]{URL}},
                {"12354", new String[]{}, null},
                {"12354", null, null},
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "getValidationNegativeTestData")
    public void testIsValidWithInvalidWhiteListPatterns(String patternToTest, String[] whiteListPatterns, String[]
            blackListPatterns) throws Exception {
        IdentityValidationUtil.isValid(patternToTest, whiteListPatterns, blackListPatterns);
    }

    @Test
    public void testGetValidInputOverWhiteListPatterns() throws
            Exception {
        assertEquals("123", IdentityValidationUtil.getValidInputOverWhiteListPatterns("123", DIGITS));
        assertEquals("", IdentityValidationUtil.getValidInputOverWhiteListPatterns("", EMAIL));
    }

    @Test(expectedExceptions = IdentityValidationException.class)
    public void testGetValidInputOverWhiteListPatternsInvalidInput() throws
            Exception {
        IdentityValidationUtil.getValidInputOverWhiteListPatterns("123", EMAIL);
    }

    @Test
    public void testGetValidInputOverBlackListPatterns() throws Exception {
        assertEquals("123", IdentityValidationUtil.getValidInputOverBlackListPatterns("123", EMAIL));
        assertEquals("", IdentityValidationUtil.getValidInputOverBlackListPatterns("", ALPHABETIC));
    }

    @Test(expectedExceptions = IdentityValidationException.class)
    public void testGetValidInputOverBlackListPatternsInvalidInput() throws Exception {
        assertEquals("123", IdentityValidationUtil.getValidInputOverBlackListPatterns("123", DIGITS, EMAIL));
    }

    @Test
    public void testGetValidInput() throws Exception {
        assertEquals("123", IdentityValidationUtil.getValidInput("123", new String[]{EMAIL}, new
                String[]{ALPHABETIC}));
        assertEquals("", IdentityValidationUtil.getValidInput("", new String[]{EMAIL}, new
                String[]{ALPHABETIC}));
    }

    @Test(expectedExceptions = IdentityValidationException.class)
    public void testGetValidInputForInvalidInput() throws Exception {
        IdentityValidationUtil.getValidInput("123", new String[]{EMAIL}, new String[]{DIGITS});
    }

    @Test
    public void testAddPattern() throws Exception {
        IdentityValidationUtil.addPattern(SIMPLE_LETTERS_KEY, SIMPLE_LETTERS_REGEX);
        assertTrue(IdentityValidationUtil.patternExists(SIMPLE_LETTERS_KEY));
    }

    @DataProvider
    public Object[][] getAddPatternNegativeTestData() {
        return new Object[][]{
                {"", SIMPLE_LETTERS_REGEX},
                {"EMPTY", ""},
                {DIGITS, "Already Contains"},
                {"INVALID_PATTERN", "{)!\\"}
        };
    }

    @Test(dataProvider = "getAddPatternNegativeTestData", expectedExceptions = IllegalArgumentException.class)
    public void testAddPatternNegative(String key, String pattern) throws Exception {
        IdentityValidationUtil.addPattern(key, pattern);
    }

    @Test
    public void testPatternExists() throws Exception {
        assertTrue(IdentityValidationUtil.patternExists(DIGITS));
        assertTrue(IdentityValidationUtil.patternExists(SIMPLE_LETTERS_KEY));
    }

    @Test(dependsOnMethods = {"testAddPattern", "testPatternExists"})
    public void testRemovePattern() throws Exception {
        IdentityValidationUtil.removePattern(SIMPLE_LETTERS_KEY);
        assertFalse(IdentityValidationUtil.patternExists(SIMPLE_LETTERS_KEY));
    }

    @AfterClass
    public void tearDown() {
        //This may not have been deleted if testPatternExists(), or testRemovePattern() is failed
        IdentityValidationUtil.removePattern(SIMPLE_LETTERS_KEY);
    }
}