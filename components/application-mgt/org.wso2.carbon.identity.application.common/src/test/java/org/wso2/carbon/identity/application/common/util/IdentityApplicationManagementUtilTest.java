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

package org.wso2.carbon.identity.application.common.util;

import org.junit.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;


/**
 * Unit tests for IdentityApplicationManagementUtil.
 */
public class IdentityApplicationManagementUtilTest {

    /**
     * Test detection of Array(N).fill usage in script.
     */
    @Test
    public void testArrayFillPresent() {

        String script = "var arr = Array(100).fill(0);";
        assertTrue(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection with spaces and newlines.
     */
    @Test
    public void testArrayFillWithSpaces() {

        String script = "var arr = Array ( 10 ) . fill ( null );";
        assertTrue(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with Array(N) but no fill.
     */
    @Test
    public void testArrayWithoutFill() {

        String script = "var arr = Array(10);";
        assertFalse(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with fill but not on Array.
     */
    @Test
    public void testFillWithoutArray() {

        String script = "var arr = foo.fill(1);";
        assertFalse(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with Array(N).fill inside a comment.
     */
    @Test
    public void testArrayFillInComment() {

        String script = "// Array(100).fill(0)\nvar x = 1;";
        assertFalse(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with Array(N).fill inside a string.
     */
    @Test
    public void testArrayFillInString() {

        String script = "var s = \"Array(100).fill(0)\";";
        assertFalse(IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection of string.repeat() usage in script.
     */
    @Test
    public void testStringRepeatPresent() {

        String script = "var str = 'x'.repeat(100);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection of string.repeat() with variable.
     */
    @Test
    public void testVariableStringRepeatPresent() {

        String script = "var test = 'X'; test.repeat(100);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection with double quotes.
     */
    @Test
    public void testStringRepeatWithDoubleQuotes() {

        String script = "var result = \"hello\".repeat(5);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection with template literals.
     */
    @Test
    public void testStringRepeatWithTemplateLiterals() {

        String script = "var result = `world`.repeat(10);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection with spaces and newlines.
     */
    @Test
    public void testStringRepeatWithSpaces() {

        String script = "var str = someVar . repeat ( 50 );";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection of object property repeat.
     */
    @Test
    public void testObjectPropertyRepeat() {

        String script = "var result = obj.prop.repeat(5);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test detection of array element repeat.
     */
    @Test
    public void testArrayElementRepeat() {

        String script = "var result = array[0].repeat(2);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with repeat but not as method call.
     */
    @Test
    public void testRepeatNotAsMethod() {

        String script = "var repeat = 'test'; var x = repeat;";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with string.repeat() inside a comment.
     */
    @Test
    public void testStringRepeatInComment() {

        String script = "// 'x'.repeat(100)\nvar x = 1;";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with string.repeat() inside a string literal.
     */
    @Test
    public void testStringRepeatInString() {

        String script = "var s = \"test.repeat(10)\";";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with string.repeat() inside single quote string.
     */
    @Test
    public void testStringRepeatInSingleQuoteString() {

        String script = "var s = 'x.repeat(100)';";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with string.repeat() inside multi-line comment.
     */
    @Test
    public void testStringRepeatInMultiLineComment() {

        String script = "/* var str = 'x'.repeat(100); */\nvar x = 1;";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with escaped quotes and string.repeat().
     */
    @Test
    public void testStringRepeatWithEscapedQuotes() {

        String script = "var s = 'test\\'s'; s.repeat(5);";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test complex script with multiple constructs including string.repeat().
     */
    @Test
    public void testComplexScriptWithStringRepeat() {

        String script = "function test() {\n" +
                "    var arr = Array(10).fill(0);\n" +
                "    var str = 'x'.repeat(100);\n" +
                "    return str;\n" +
                "}";
        assertTrue(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test empty script.
     */
    @Test
    public void testEmptyScriptForStringRepeat() {

        String script = "";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }

    /**
     * Test script with only whitespace.
     */
    @Test
    public void testWhitespaceOnlyScriptForStringRepeat() {

        String script = "   \n\t  ";
        assertFalse(IdentityApplicationManagementUtil.isStringRepeatPresentInAdaptiveAuthScript(script));
    }
}
