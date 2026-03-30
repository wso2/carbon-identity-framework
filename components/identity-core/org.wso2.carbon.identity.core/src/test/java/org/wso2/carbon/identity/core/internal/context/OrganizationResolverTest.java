/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.internal.context;

import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class OrganizationResolverTest {

    private static final String ORG_CONTEXTS_TENANT_QUALIFIED_PATHS_CONFIG =
            "OrgContextsToRewriteInTenantQualifiedPaths.WebApp.Context.SubPaths.Path";

    private static final List<String> CONFIGURED_SUB_PATHS = Arrays.asList(
            "/api/server/v1/validation-rules",
            "/api/server/v1/branding-preference",
            "/api/server/v1/offline-invite-link"
    );

    private MockedStatic<IdentityConfigParser> identityConfigParser;

    @BeforeMethod
    public void setUp() {

        identityConfigParser = mockStatic(IdentityConfigParser.class);
        IdentityConfigParser mockParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockParser);

        Map<String, Object> config = new HashMap<>();
        config.put(ORG_CONTEXTS_TENANT_QUALIFIED_PATHS_CONFIG, CONFIGURED_SUB_PATHS);
        when(mockParser.getConfiguration()).thenReturn(config);
    }

    @AfterMethod
    public void tearDown() {

        identityConfigParser.close();
    }

    @DataProvider(name = "tenantQualifiedOrgPathProvider")
    public Object[][] tenantQualifiedOrgPathProvider() {

        return new Object[][] {
                // Exact matches — should be recognized as tenant-qualified org paths.
                { "/t/acme.com/o/abc123/api/server/v1/validation-rules", true },
                { "/t/acme.com/o/abc123/api/server/v1/branding-preference", true },
                { "/t/acme.com/o/abc123/api/server/v1/offline-invite-link", true },

                // Nested sub-paths — valid extensions, should match.
                { "/t/acme.com/o/abc123/api/server/v1/validation-rules/sub", true },
                { "/t/acme.com/o/abc123/api/server/v1/branding-preference/", true },

                // Prefix-only matches — must NOT match (the boundary fix).
                { "/t/acme.com/o/abc123/api/server/v1/validation-rules-v2", false },
                { "/t/acme.com/o/abc123/api/server/v1/branding-preference-extra", false },
                { "/t/acme.com/o/abc123/api/server/v1/offline-invite-link-copy", false },

                // Paths that don't start with /t/.../o/... — should not match.
                { "/api/server/v1/validation-rules", false },
                { "/o/abc123/api/server/v1/validation-rules", false },

                // Blank / null URI.
                { "", false },
                { "   ", false },

                // Completely unrelated path.
                { "/t/acme.com/o/abc123/someother/path", false },
        };
    }

    @Test(dataProvider = "tenantQualifiedOrgPathProvider")
    public void testIsTenantQualifiedOrgPath(String requestURI, boolean expected) throws Exception {

        Method method = OrganizationResolver.class.getDeclaredMethod("isTenantQualifiedOrgPath", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(OrganizationResolver.getInstance(), requestURI);

        assertEquals(result, expected,
                "Unexpected result for URI: " + requestURI);
    }

    @Test
    public void testIsTenantQualifiedOrgPath_noConfiguredPaths() throws Exception {

        // Override config to have no configured paths.
        IdentityConfigParser mockParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockParser);
        when(mockParser.getConfiguration()).thenReturn(new HashMap<>());

        Method method = OrganizationResolver.class.getDeclaredMethod("isTenantQualifiedOrgPath", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(
                OrganizationResolver.getInstance(), "/t/acme.com/o/abc123/api/server/v1/validation-rules");

        assertEquals(result, false, "Should return false when no paths are configured.");
    }

    @Test
    public void testIsTenantQualifiedOrgPath_singleStringConfig() throws Exception {

        // Simulate config returning a single String instead of a List.
        IdentityConfigParser mockParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockParser);

        Map<String, Object> config = new HashMap<>();
        config.put(ORG_CONTEXTS_TENANT_QUALIFIED_PATHS_CONFIG, "/api/server/v1/validation-rules");
        when(mockParser.getConfiguration()).thenReturn(config);

        Method method = OrganizationResolver.class.getDeclaredMethod("isTenantQualifiedOrgPath", String.class);
        method.setAccessible(true);

        assertEquals(method.invoke(OrganizationResolver.getInstance(),
                "/t/acme.com/o/abc123/api/server/v1/validation-rules"), true);
        assertEquals(method.invoke(OrganizationResolver.getInstance(),
                "/t/acme.com/o/abc123/api/server/v1/validation-rules-v2"), false);
    }
}
