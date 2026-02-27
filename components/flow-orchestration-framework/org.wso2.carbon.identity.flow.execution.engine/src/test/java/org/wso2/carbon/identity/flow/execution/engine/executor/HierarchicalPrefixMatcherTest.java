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

package org.wso2.carbon.identity.flow.execution.engine.executor;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.HierarchicalPrefixMatcher;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.HierarchicalPrefixMatcher.ContextArea;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link HierarchicalPrefixMatcher}.
 */
public class HierarchicalPrefixMatcherTest {

    // ========================= identifyContextArea =========================

    @DataProvider(name = "contextAreaPaths")
    public Object[][] contextAreaPaths() {

        return new Object[][] {
                { "/user/claims/http://wso2.org/claims/email", ContextArea.USER_CLAIMS },
                { "/user/claims/", ContextArea.USER_CLAIMS },
                { "/user/credentials/password", ContextArea.USER_CREDENTIALS },
                { "/user/credentials/", ContextArea.USER_CREDENTIALS },
                { "/user/federatedAssociations/google", ContextArea.USER_FEDERATED },
                { "/user/federatedAssociations/", ContextArea.USER_FEDERATED },
                { "/user/userId", ContextArea.USER_SCALAR },
                { "/user/username", ContextArea.USER_SCALAR },
                { "/user/userStoreDomain", ContextArea.USER_SCALAR },
                { "/properties/riskScore", ContextArea.PROPERTIES },
                { "/properties/", ContextArea.PROPERTIES },
                { "/input/someField", ContextArea.INPUT },
                { "/input/", ContextArea.INPUT },
                { "/flow/tenantDomain", ContextArea.FLOW },
                { "/flow/applicationId", ContextArea.FLOW },
                { "/flow/", ContextArea.FLOW },
                { "/graph/currentNode/id", ContextArea.GRAPH },
                { "/graph/", ContextArea.GRAPH },
        };
    }

    @Test(dataProvider = "contextAreaPaths")
    public void testIdentifyContextArea(String path, ContextArea expected) {

        assertEquals(HierarchicalPrefixMatcher.identifyContextArea(path), expected);
    }

    @DataProvider(name = "unknownPaths")
    public Object[][] unknownPaths() {

        return new Object[][] {
                { null },
                { "" },
                { "/unknown/path" },
                { "noSlash" },
                { "/other/" },
        };
    }

    @Test(dataProvider = "unknownPaths")
    public void testIdentifyContextAreaReturnsNullForUnknownPaths(String path) {

        assertNull(HierarchicalPrefixMatcher.identifyContextArea(path));
    }

    // ========================= extractKey =========================

    @DataProvider(name = "extractKeyData")
    public Object[][] extractKeyData() {

        return new Object[][] {
                { "/properties/riskScore", "/properties/", "riskScore" },
                { "/properties/nested/field", "/properties/", "nested" },
                { "/user/claims/http://wso2.org/claims/email", "/user/claims/", "http:" },
                { "/input/fieldName", "/input/", "fieldName" },
        };
    }

    @Test(dataProvider = "extractKeyData")
    public void testExtractKey(String path, String prefix, String expected) {

        assertEquals(HierarchicalPrefixMatcher.extractKey(path, prefix), expected);
    }

    @DataProvider(name = "extractKeyNullCases")
    public Object[][] extractKeyNullCases() {

        return new Object[][] {
                { null, "/properties/" },
                { "/properties/", "/properties/" },
                { "/user/claims/email", "/properties/" },
        };
    }

    @Test(dataProvider = "extractKeyNullCases")
    public void testExtractKeyReturnsNullForInvalidInput(String path, String prefix) {

        assertNull(HierarchicalPrefixMatcher.extractKey(path, prefix));
    }

    // ========================= getSubPath =========================

    @DataProvider(name = "subPathData")
    public Object[][] subPathData() {

        return new Object[][] {
                { "/properties/nested/field", "/properties/", "nested/field" },
                { "/properties/riskScore", "/properties/", "riskScore" },
                { "/user/claims/http://wso2.org/claims/email", "/user/claims/", "http://wso2.org/claims/email" },
        };
    }

    @Test(dataProvider = "subPathData")
    public void testGetSubPath(String path, String prefix, String expected) {

        assertEquals(HierarchicalPrefixMatcher.getSubPath(path, prefix), expected);
    }

    @Test
    public void testGetSubPathReturnsNullForNullPath() {

        assertNull(HierarchicalPrefixMatcher.getSubPath(null, "/properties/"));
    }

    @Test
    public void testGetSubPathReturnsNullForMismatchedPrefix() {

        assertNull(HierarchicalPrefixMatcher.getSubPath("/user/claims/x", "/properties/"));
    }

    // ========================= buildPath =========================

    @Test
    public void testBuildPath() {

        assertEquals(HierarchicalPrefixMatcher.buildPath("/properties/", "riskScore"),
                "/properties/riskScore");
    }

    @Test
    public void testBuildPathWithTrailingSlash() {

        assertEquals(HierarchicalPrefixMatcher.buildPath("/user/claims/", "http://wso2.org/claims/email"),
                "/user/claims/http://wso2.org/claims/email");
    }

    @Test
    public void testBuildPathReturnsNullForNullInputs() {

        assertNull(HierarchicalPrefixMatcher.buildPath(null, "key"));
        assertNull(HierarchicalPrefixMatcher.buildPath("/properties/", null));
    }

    // ========================= isReadOnly =========================

    @DataProvider(name = "readOnlyPaths")
    public Object[][] readOnlyPaths() {

        return new Object[][] {
                { "/flow/tenantDomain", true },
                { "/flow/applicationId", true },
                { "/flow/", true },
                { "/graph/currentNode/id", true },
                { "/graph/", true },
                { "/properties/riskScore", false },
                { "/user/claims/email", false },
                { "/input/field", false },
                { null, false },
        };
    }

    @Test(dataProvider = "readOnlyPaths")
    public void testIsReadOnly(String path, boolean expected) {

        assertEquals(HierarchicalPrefixMatcher.isReadOnly(path), expected);
    }

    // ========================= requiresSystemKeyValidation =========================

    @DataProvider(name = "systemKeyPaths")
    public Object[][] systemKeyPaths() {

        return new Object[][] {
                { "/user/claims/http://wso2.org/claims/email", true },
                { "/user/credentials/password", true },
                { "/user/federatedAssociations/google", true },
                { "/properties/score", false },
                { "/input/field", false },
                { "/flow/tenantDomain", false },
                { "/user/userId", false },
        };
    }

    @Test(dataProvider = "systemKeyPaths")
    public void testRequiresSystemKeyValidation(String path, boolean expected) {

        assertEquals(HierarchicalPrefixMatcher.requiresSystemKeyValidation(path), expected);
    }

    // ========================= matchesAnyExpose =========================

    @Test
    public void testMatchesAnyExposePathStartsWithPrefix() {

        List<String> expose = Arrays.asList("/user/", "/properties/");
        assertTrue(HierarchicalPrefixMatcher.matchesAnyExpose("/user/claims/email", expose));
        assertTrue(HierarchicalPrefixMatcher.matchesAnyExpose("/properties/riskScore", expose));
    }

    @Test
    public void testMatchesAnyExposePrefixStartsWithPath() {

        // Bidirectional: path is parent of a prefix in the list.
        List<String> expose = Arrays.asList("/user/claims/http://wso2.org/claims/email");
        assertTrue(HierarchicalPrefixMatcher.matchesAnyExpose("/user/", expose));
        assertTrue(HierarchicalPrefixMatcher.matchesAnyExpose("/user/claims/", expose));
    }

    @Test
    public void testMatchesAnyExposeNoMatch() {

        List<String> expose = Arrays.asList("/flow/", "/graph/");
        assertFalse(HierarchicalPrefixMatcher.matchesAnyExpose("/properties/score", expose));
        assertFalse(HierarchicalPrefixMatcher.matchesAnyExpose("/user/claims/email", expose));
    }

    @Test
    public void testMatchesAnyExposeNullPath() {

        assertFalse(HierarchicalPrefixMatcher.matchesAnyExpose(null, Arrays.asList("/user/")));
    }

    @Test
    public void testMatchesAnyExposeEmptyList() {

        assertFalse(HierarchicalPrefixMatcher.matchesAnyExpose("/user/claims/email", Collections.emptyList()));
    }

    @Test
    public void testMatchesAnyExposeNullList() {

        assertFalse(HierarchicalPrefixMatcher.matchesAnyExpose("/user/claims/email", null));
    }

    @Test
    public void testMatchesAnyExposeMultiplePrefixesOneMatches() {

        List<String> expose = Arrays.asList("/flow/", "/graph/", "/properties/");
        assertTrue(HierarchicalPrefixMatcher.matchesAnyExpose("/properties/score", expose));
    }

    // ========================= normalizePath =========================

    @Test
    public void testNormalizePathLegacyUserInputs() {

        assertEquals(HierarchicalPrefixMatcher.normalizePath("/userInputs/field"), "/input/field");
    }

    @Test
    public void testNormalizePathUnchanged() {

        assertEquals(HierarchicalPrefixMatcher.normalizePath("/properties/score"), "/properties/score");
        assertEquals(HierarchicalPrefixMatcher.normalizePath("/user/claims/email"), "/user/claims/email");
    }

    @Test
    public void testNormalizePathNull() {

        assertNull(HierarchicalPrefixMatcher.normalizePath(null));
    }

    // ========================= DEFAULT_EXPOSE =========================

    @Test
    public void testDefaultExposeContainsAllAreas() {

        List<String> defaultExpose = HierarchicalPrefixMatcher.DEFAULT_EXPOSE;
        assertNotNull(defaultExpose);
        assertEquals(defaultExpose.size(), 5);
        assertTrue(defaultExpose.contains("/user/"));
        assertTrue(defaultExpose.contains("/properties/"));
        assertTrue(defaultExpose.contains("/input/"));
        assertTrue(defaultExpose.contains("/flow/"));
        assertTrue(defaultExpose.contains("/graph/"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testDefaultExposeIsUnmodifiable() {

        HierarchicalPrefixMatcher.DEFAULT_EXPOSE.add("/new/");
    }

    // ========================= ContextArea enum =========================

    @Test
    public void testContextAreaPrefix() {

        assertEquals(ContextArea.USER_CLAIMS.getPrefix(), "/user/claims/");
        assertEquals(ContextArea.PROPERTIES.getPrefix(), "/properties/");
        assertEquals(ContextArea.INPUT.getPrefix(), "/input/");
        assertEquals(ContextArea.FLOW.getPrefix(), "/flow/");
        assertEquals(ContextArea.GRAPH.getPrefix(), "/graph/");
    }

    @Test
    public void testContextAreaHasSystemConfiguredKeys() {

        assertTrue(ContextArea.USER_CLAIMS.hasSystemConfiguredKeys());
        assertTrue(ContextArea.USER_CREDENTIALS.hasSystemConfiguredKeys());
        assertTrue(ContextArea.USER_FEDERATED.hasSystemConfiguredKeys());
        assertFalse(ContextArea.USER_SCALAR.hasSystemConfiguredKeys());
        assertFalse(ContextArea.PROPERTIES.hasSystemConfiguredKeys());
        assertFalse(ContextArea.INPUT.hasSystemConfiguredKeys());
        assertFalse(ContextArea.FLOW.hasSystemConfiguredKeys());
        assertFalse(ContextArea.GRAPH.hasSystemConfiguredKeys());
    }
}
