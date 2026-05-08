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

package org.wso2.carbon.identity.flow.execution.engine.config;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExecutionContextFilter}.
 *
 * <p>Uses the {@link FlowContextHandoverConfig#permissive()} factory and the private
 * constructor variant (accessed via a local helper) to avoid touching
 * {@code IdentityConfigParser} — no OSGi or Mockito static setup required.</p>
 */
public class FlowExecutionContextFilterTest {

    // ========================= null guards =========================

    @Test
    public void testNullOriginalReturnsNull() {

        assertNull(FlowExecutionContextFilter.filter(null, FlowContextHandoverConfig.permissive()));
    }

    @Test
    public void testNullConfigFallsBackToPermissive() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, null);

        // Permissive fallback — every whitelisted field should round-trip.
        assertEquals(copy.getTenantDomain(), "carbon.super");
        assertEquals(copy.getApplicationId(), "app-1");
        assertEquals(copy.getFlowType(), "REGISTRATION");
        assertEquals(copy.getCallbackUrl(), "https://callback");
        assertEquals(copy.getPortalUrl(), "https://portal");
        assertEquals(copy.getFlowUser().getUserId(), "user-1");
        assertEquals(copy.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "u@e.com");
        assertEquals(copy.getProperties().get("p1"), "v1");
    }

    // ========================= contextIdentifier =========================

    @Test
    public void testContextIdentifierAlwaysCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();

        // Even with an empty allow-list contextIdentifier must round-trip.
        FlowContextHandoverConfig strict = configFrom(new HashSet<>(), new HashSet<>());
        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, strict);

        assertEquals(copy.getContextIdentifier(), "ctx-1");
    }

    // ========================= flow-branch toggles =========================

    @Test
    public void testAllFlowAttrsExcludedWhenNoneInAllowList() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        // Allow-list has only user attrs — no flow attrs.
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("username", "claims")),
                new HashSet<>(Arrays.asList("username", "claims")));

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        assertNull(copy.getTenantDomain());
        assertNull(copy.getApplicationId());
        assertNull(copy.getFlowType());
        assertNull(copy.getCallbackUrl());
        assertNull(copy.getPortalUrl());
    }

    @Test
    public void testFlowAttrSelective() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("tenantDomain", "flowType", "portalUrl")),
                new HashSet<>());

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        assertEquals(copy.getTenantDomain(), "carbon.super");
        assertNull(copy.getApplicationId());
        assertEquals(copy.getFlowType(), "REGISTRATION");
        assertNull(copy.getCallbackUrl());
        assertEquals(copy.getPortalUrl(), "https://portal");
    }

    // ========================= user-branch toggles =========================

    @Test
    public void testUserAbsentFromAllowListYieldsEmptyFlowUser() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        // No "flowUser" and no user attrs — user branch omitted.
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("tenantDomain")),
                new HashSet<>());

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        // FlowUser must be a non-null empty shell so downstream code doesn't NPE.
        assertNotNull(copy.getFlowUser());
        assertNull(copy.getFlowUser().getUserId());
        assertNull(copy.getFlowUser().getUserStoreDomain());
        assertTrue(copy.getFlowUser().getClaims() == null || copy.getFlowUser().getClaims().isEmpty());
        assertTrue(copy.getFlowUser().getUserCredentials() == null
                || copy.getFlowUser().getUserCredentials().isEmpty());
    }

    @Test
    public void testPerLeafUserToggles() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("flowUser")),     // flowUser in context attrs
                new HashSet<>(Arrays.asList("userStoreDomain", "claims"))); // userId excluded

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        // fullUserPassthrough == true because "flowUser" is in includedAttributes
        // → entire user is passed through regardless of includedUserAttributes
        assertEquals(copy.getFlowUser().getUserId(), "user-1");
        assertEquals(copy.getFlowUser().getUserStoreDomain(), "PRIMARY");
        assertEquals(copy.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "u@e.com");
    }

    @Test
    public void testIndividualUserAttrSelection() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        // No "flowUser" — use individual user attr list.
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("tenantDomain")),    // no "flowUser"
                new HashSet<>(Arrays.asList("userStoreDomain", "claims")));

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        assertNull(copy.getFlowUser().getUserId());
        assertEquals(copy.getFlowUser().getUserStoreDomain(), "PRIMARY");
        assertEquals(copy.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "u@e.com");
        assertTrue(copy.getFlowUser().getUserCredentials() == null
                || copy.getFlowUser().getUserCredentials().isEmpty());
    }

    // ========================= defensive copies =========================

    @Test
    public void testCredentialsAreDeepCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        char[] origPwd = orig.getFlowUser().getUserCredentials().get("password");
        char[] copyPwd = copy.getFlowUser().getUserCredentials().get("password");

        assertNotSame(origPwd, copyPwd);
        assertEquals(copyPwd, "secret".toCharArray());

        // Wiping the copy must not affect the original.
        Arrays.fill(copyPwd, '\0');
        assertEquals(origPwd, "secret".toCharArray());
    }

    @Test
    public void testClaimsMapIsDefensivelyCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        copy.getFlowUser().addClaim("http://wso2.org/claims/leak", "oops");
        assertNull(orig.getFlowUser().getClaims().get("http://wso2.org/claims/leak"));
    }

    @Test
    public void testPropertiesAreDefensivelyCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        copy.setProperty("p2", "leak");
        assertNull(orig.getProperty("p2"));
    }

    // ========================= properties branch =========================

    @Test
    public void testPropertiesExcludedWhenNotInAllowList() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        // "properties" not in allow-list.
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("tenantDomain")),
                new HashSet<>());

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        assertTrue(copy.getProperties() == null || copy.getProperties().isEmpty());
    }

    @Test
    public void testPropertiesIncludedWhenInAllowList() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig cfg = configFrom(
                new HashSet<>(Arrays.asList("properties")),
                new HashSet<>());

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, cfg);

        assertEquals(copy.getProperty("p1"), "v1");
    }

    // ========================= original untouched =========================

    @Test
    public void testOriginalUntouchedAcrossAllToggles() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverConfig strict = configFrom(new HashSet<>(), new HashSet<>());

        FlowExecutionContextFilter.filter(orig, strict);

        assertEquals(orig.getTenantDomain(), "carbon.super");
        assertEquals(orig.getApplicationId(), "app-1");
        assertEquals(orig.getFlowType(), "REGISTRATION");
        assertEquals(orig.getCallbackUrl(), "https://callback");
        assertEquals(orig.getPortalUrl(), "https://portal");
        assertEquals(orig.getFlowUser().getUserId(), "user-1");
        assertEquals(orig.getProperty("p1"), "v1");
    }

    // ========================= helpers =========================

    /**
     * Build a minimal {@link FlowContextHandoverConfig} from raw sets without touching
     * {@code IdentityConfigParser}. Uses the permissive factory as a baseline and overrides
     * via reflection would be heavy — instead we rely on the fact that
     * {@code FlowContextHandoverConfig.permissive()} already covers the all-on case, and
     * create targeted configs via a thin wrapper that directly exercises the filter with
     * controlled allow-lists by passing them to the filter via the engine's {@code permissive()}
     * overridden in a subclass if needed.
     *
     * <p>Since {@link FlowContextHandoverConfig} has no public constructor taking sets, we
     * call {@code permissive()} as the base and then exercise filter behaviour by passing
     * allow-list information through a thin anonymous subclass. However, the class is
     * {@code final}, so we instead test the filter contract indirectly by constructing real
     * configs through the only available factory that doesn't read identity.xml.</p>
     *
     * <p>For selective allow-list tests we use a package-private test helper in the same
     * package ({@code engine.config}) that exposes the private constructor.</p>
     */
    private static FlowContextHandoverConfig configFrom(Set<String> attrs, Set<String> userAttrs) {

        return FlowContextHandoverConfigTestHelper.of(attrs, userAttrs);
    }

    private FlowExecutionContext createFullyPopulatedContext() {

        FlowExecutionContext ctx = new FlowExecutionContext();
        ctx.setContextIdentifier("ctx-1");
        ctx.setTenantDomain("carbon.super");
        ctx.setApplicationId("app-1");
        ctx.setFlowType("REGISTRATION");
        ctx.setCallbackUrl("https://callback");
        ctx.setPortalUrl("https://portal");

        FlowUser user = new FlowUser();
        user.setUserId("user-1");
        user.setUserStoreDomain("PRIMARY");
        user.addClaim("http://wso2.org/claims/email", "u@e.com");

        Map<String, char[]> creds = new HashMap<>();
        creds.put("password", "secret".toCharArray());
        user.setUserCredentials(creds);

        ctx.setFlowUser(user);
        ctx.setProperty("p1", "v1");
        return ctx;
    }
}
