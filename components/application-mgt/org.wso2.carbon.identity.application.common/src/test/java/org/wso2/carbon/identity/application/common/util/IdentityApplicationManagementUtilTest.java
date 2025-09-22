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
 * Unit tests for IdentityApplicationManagementUtil.isArrayFillPresentInAdaptiveAuthScript.
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
}

