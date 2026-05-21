/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extensions.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionPathUtil}.
 */
public class InFlowExtensionPathUtilTest {

    // ========================= isReadOnly =========================

    @DataProvider(name = "readOnlyPaths")
    public Object[][] readOnlyPaths() {

        return new Object[][] {
                { "/flow/tenantDomain", true },
                { "/flow/applicationId", true },
                { "/flow/", true },
                { "/properties/riskScore", false },
                { "/user/claims/email", false },
                { null, false },
        };
    }

    @Test(dataProvider = "readOnlyPaths")
    public void testIsReadOnly(String path, boolean expected) {

        assertEquals(InFlowExtensionPathUtil.isReadOnly(path), expected);
    }

    // ========================= anyExposedUnder =========================

    @Test
    public void testAnyExposedUnderMatchesLeafUnderPrefix() {

        List<String> leafPaths = Arrays.asList(
                "/user/claims/http://wso2.org/claims/email",
                "/properties/riskScore");
        assertTrue(InFlowExtensionPathUtil.anyExposedUnder("/user/claims/", leafPaths));
        assertTrue(InFlowExtensionPathUtil.anyExposedUnder("/properties/", leafPaths));
    }

    @Test
    public void testAnyExposedUnderNoMatch() {

        List<String> leafPaths = Arrays.asList("/flow/tenantDomain", "/flow/applicationId");
        assertFalse(InFlowExtensionPathUtil.anyExposedUnder("/user/claims/", leafPaths));
        assertFalse(InFlowExtensionPathUtil.anyExposedUnder("/properties/", leafPaths));
    }

    @Test
    public void testAnyExposedUnderNullPrefix() {

        assertFalse(InFlowExtensionPathUtil.anyExposedUnder(null,
                Arrays.asList("/user/claims/email")));
    }

    @Test
    public void testAnyExposedUnderNullList() {

        assertFalse(InFlowExtensionPathUtil.anyExposedUnder("/user/claims/", null));
    }

    @Test
    public void testAnyExposedUnderEmptyList() {

        assertFalse(InFlowExtensionPathUtil.anyExposedUnder("/user/claims/",
                Collections.emptyList()));
    }

    @Test
    public void testAnyExposedUnderDoesNotMatchShortPath() {

        List<String> leafPaths = Collections.singletonList("/user/userId");
        assertFalse(InFlowExtensionPathUtil.anyExposedUnder("/user/claims/", leafPaths));
    }

    @Test
    public void testAnyExposedUnderMultipleLeafsOneMatches() {

        List<String> leafPaths = Arrays.asList(
                "/flow/tenantDomain",
                "/user/credentials/password");
        assertTrue(InFlowExtensionPathUtil.anyExposedUnder("/user/credentials/", leafPaths));
    }

    // ========================= isExposedPath =========================

    @Test
    public void testIsExposedPathExactMatch() {

        List<String> leafPaths = Arrays.asList(
                "/user/claims/http://wso2.org/claims/email",
                "/flow/tenantDomain",
                "/user/userId");
        assertTrue(InFlowExtensionPathUtil.isExposedPath(
                "/user/claims/http://wso2.org/claims/email", leafPaths));
        assertTrue(InFlowExtensionPathUtil.isExposedPath("/flow/tenantDomain", leafPaths));
        assertTrue(InFlowExtensionPathUtil.isExposedPath("/user/userId", leafPaths));
    }

    @Test
    public void testIsExposedPathNoMatch() {

        List<String> leafPaths = Arrays.asList("/flow/tenantDomain", "/user/userId");
        assertFalse(InFlowExtensionPathUtil.isExposedPath(
                "/user/claims/http://wso2.org/claims/email", leafPaths));
    }

    @Test
    public void testIsExposedPathNullPath() {

        assertFalse(InFlowExtensionPathUtil.isExposedPath(null,
                Arrays.asList("/user/userId")));
    }

    @Test
    public void testIsExposedPathNullList() {

        assertFalse(InFlowExtensionPathUtil.isExposedPath("/user/userId", null));
    }

    @Test
    public void testIsExposedPathEmptyList() {

        assertFalse(InFlowExtensionPathUtil.isExposedPath("/user/userId",
                Collections.emptyList()));
    }

    @Test
    public void testIsExposedPathPrefixNotSufficient() {

        List<String> leafPaths = Collections.singletonList("/user/claims/http://wso2.org/claims/email");
        assertFalse(InFlowExtensionPathUtil.isExposedPath("/user/claims/", leafPaths));
    }
}
