/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.test;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.cors.mgt.core.internal.util.HeaderUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * Unit test cases for HeaderUtil.
 */
public class HeaderUtilTests {

    @Test
    public void testFormatCanonical1() {

        assertEquals(HeaderUtils.formatCanonical("content-type"), "Content-Type");
    }

    @Test
    public void testFormatCanonical2() {

        assertEquals(HeaderUtils.formatCanonical("CONTENT-TYPE"), "Content-Type");
    }

    @Test
    public void testFormatCanonical3() {

        assertEquals(HeaderUtils.formatCanonical("X-type"), "X-Type");
    }

    @Test
    public void testFormatCanonical4() {

        assertEquals(HeaderUtils.formatCanonical("Origin"), "Origin");
    }

    @Test
    public void testFormatCanonical5() {

        assertEquals(HeaderUtils.formatCanonical("A"), "A");
    }

    @Test
    public void testFormatCanonical6() {

        try {
            assertEquals(HeaderUtils.formatCanonical(""), "");
            fail("Failed to raise IllegalArgumentException on empty string");

        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}
