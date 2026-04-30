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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.config;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class FlowExecutionContextFilterTest {

    @Test
    public void testNullOriginalReturnsNull() {

        assertNull(FlowExecutionContextFilter.filter(null, FlowContextHandoverPolicy.permissive()));
    }

    @Test
    public void testNullPolicyFallsBackToPermissive() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, null);

        // Permissive fallback — every field should round-trip.
        assertEquals(copy.getTenantDomain(), "carbon.super");
        assertEquals(copy.getApplicationId(), "app-1");
        assertEquals(copy.getFlowType(), "REGISTRATION");
        assertEquals(copy.getCallbackUrl(), "https://callback");
        assertEquals(copy.getPortalUrl(), "https://portal");
        assertEquals(copy.getFlowUser().getUserId(), "user-1");
        assertEquals(copy.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "u@e.com");
        assertEquals(copy.getProperties().get("p1"), "v1");
    }

    @Test
    public void testContextIdentifierAlwaysCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();

        // Even with the most restrictive policy, contextIdentifier must round-trip — it's the
        // engine-internal flow id, not user data.
        FlowContextHandoverPolicy strict = FlowContextHandoverPolicy.builder().build();
        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, strict);

        assertEquals(copy.getContextIdentifier(), "ctx-1");
    }

    @Test
    public void testFlowDisabledOmitsAllFlowFields() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.builder()
                .startingFrom(FlowContextHandoverPolicy.permissive())
                .flowEnabled(false)
                .build();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        assertNull(copy.getTenantDomain());
        assertNull(copy.getApplicationId());
        assertNull(copy.getFlowType());
        assertNull(copy.getCallbackUrl());
        assertNull(copy.getPortalUrl());
    }

    @Test
    public void testPerLeafFlowToggles() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.builder()
                .flowEnabled(true)
                .flowTenantDomain(true)
                .flowApplicationId(false)  // off
                .flowFlowType(true)
                .flowCallbackUrl(false)    // off
                .flowPortalUrl(true)
                .build();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        assertEquals(copy.getTenantDomain(), "carbon.super");
        assertNull(copy.getApplicationId());
        assertEquals(copy.getFlowType(), "REGISTRATION");
        assertNull(copy.getCallbackUrl());
        assertEquals(copy.getPortalUrl(), "https://portal");
    }

    @Test
    public void testUserDisabledYieldsEmptyFlowUser() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.builder()
                .startingFrom(FlowContextHandoverPolicy.permissive())
                .userEnabled(false)
                .build();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        // Must be a fresh non-null FlowUser — the response processor relies on this when
        // applying pendingClaims/pendingCredentials on second-call SUCCESS responses.
        assertNotNull(copy.getFlowUser());
        assertNull(copy.getFlowUser().getUserId());
        assertNull(copy.getFlowUser().getUserStoreDomain());
        assertTrue(copy.getFlowUser().getClaims().isEmpty());
        assertTrue(copy.getFlowUser().getUserCredentials().isEmpty());
    }

    @Test
    public void testPerLeafUserToggles() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.builder()
                .userEnabled(true)
                .userUserId(false)         // off
                .userUserStoreDomain(true)
                .userClaims(true)
                .userCredentials(false)    // off
                .build();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        assertNull(copy.getFlowUser().getUserId());
        assertEquals(copy.getFlowUser().getUserStoreDomain(), "PRIMARY");
        assertEquals(copy.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "u@e.com");
        assertTrue(copy.getFlowUser().getUserCredentials().isEmpty());
    }

    @Test
    public void testCredentialsAreDeepCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        char[] origPwd = orig.getFlowUser().getUserCredentials().get("password");
        char[] copyPwd = copy.getFlowUser().getUserCredentials().get("password");

        assertNotSame(origPwd, copyPwd);
        assertEquals(copyPwd, "secret".toCharArray());

        // Wiping the copy must not affect the original — this is the desired post-extraction
        // sanitisation behaviour: the request builder's Arrays.fill(...,'\0') zeroes the copy,
        // not the original on the live FlowExecutionContext.
        Arrays.fill(copyPwd, '\0');
        assertEquals(origPwd, "secret".toCharArray());
    }

    @Test
    public void testClaimsMapIsDefensivelyCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        // Mutating the copy's claims must not leak to the original.
        copy.getFlowUser().addClaim("http://wso2.org/claims/leak", "oops");
        assertNull(orig.getFlowUser().getClaims().get("http://wso2.org/claims/leak"));
    }

    @Test
    public void testPropertiesDisabledOmitsAll() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.builder()
                .startingFrom(FlowContextHandoverPolicy.permissive())
                .propertiesEnabled(false)
                .build();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        // Properties default to an empty map on a fresh FlowExecutionContext.
        assertTrue(copy.getProperties() == null || copy.getProperties().isEmpty());
    }

    @Test
    public void testPropertiesAreDefensivelyCopied() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy policy = FlowContextHandoverPolicy.permissive();

        FlowExecutionContext copy = FlowExecutionContextFilter.filter(orig, policy);

        copy.setProperty("p2", "leak");
        assertNull(orig.getProperty("p2"));
    }

    @Test
    public void testOriginalUntouchedAcrossAllToggles() {

        FlowExecutionContext orig = createFullyPopulatedContext();
        FlowContextHandoverPolicy strict = FlowContextHandoverPolicy.builder().build();

        FlowExecutionContextFilter.filter(orig, strict);

        // Original must be byte-for-byte equivalent after filtering.
        assertEquals(orig.getTenantDomain(), "carbon.super");
        assertEquals(orig.getApplicationId(), "app-1");
        assertEquals(orig.getFlowType(), "REGISTRATION");
        assertEquals(orig.getCallbackUrl(), "https://callback");
        assertEquals(orig.getPortalUrl(), "https://portal");
        assertEquals(orig.getFlowUser().getUserId(), "user-1");
        assertEquals(orig.getProperty("p1"), "v1");
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
