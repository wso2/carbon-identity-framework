/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link Utils#extractUserNameFromCodeResource(String)}.
 */
public class UtilsTest {

    private static final String CODE = "11111111-2222-3333-4444-555555555555";

    @DataProvider(name = "primaryResources")
    public Object[][] primaryResources() {

        return new Object[][]{
                {"2___normaluser___" + CODE, "normaluser"},
                // The username itself carries the delimiter.
                {"2___test___user___" + CODE, "test___user"},
                // The trailing underscore of the username merges into the delimiter.
                {"2___edge_user____" + CODE, "edge_user_"},
                {"2____leadinguser___" + CODE, "_leadinguser"},
                {"2___a___b___c___" + CODE, "a___b___c"},
                // Secondary user store users are qualified with their domain.
                {"2___domain/user___" + CODE, "domain/user"},
                {"2______" + CODE, ""},
        };
    }

    @Test(dataProvider = "primaryResources")
    public void testExtractUserNameFromPrimaryResource(String resource, String expected) {

        assertEquals(Utils.extractUserNameFromCodeResource(resource), expected,
                "Wrong username extracted from resource: " + resource);
    }

    @DataProvider(name = "nonPrimaryResources")
    public Object[][] nonPrimaryResources() {

        return new Object[][]{
                {null},
                {""},
                {"plaincollection"},
                // Secondary user store collections carry a single delimiter and no username.
                {"2___DOMAIN"},
                // A domain starting with an underscore makes the two delimiter matches overlap.
                {"2____DOMAIN"},
                {"2_____DOMAIN"},
        };
    }

    @Test(dataProvider = "nonPrimaryResources")
    public void testExtractUserNameFromNonPrimaryResource(String resource) {

        assertNull(Utils.extractUserNameFromCodeResource(resource),
                "Expected no username for resource: " + resource);
    }
}
