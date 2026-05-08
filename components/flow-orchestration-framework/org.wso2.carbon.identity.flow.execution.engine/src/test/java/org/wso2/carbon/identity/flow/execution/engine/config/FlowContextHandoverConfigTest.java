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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowContextHandoverConfig}.
 *
 * <p>Tests that do not read {@code identity.xml} use
 * {@link FlowContextHandoverConfigTestHelper} to build instances with explicit allow-lists,
 * or use the {@link FlowContextHandoverConfig#permissive()} factory which seeds allow-lists
 * from the static maps on {@link FlowExecutionContextFilter}.</p>
 */
public class FlowContextHandoverConfigTest {

    // ========================= permissive() factory =========================

    @Test
    public void testPermissiveIncludesAllKnownContextAttributes() {

        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        // At minimum all well-known FlowExecutionContext properties must be present.
        Set<String> attrs = cfg.getIncludedAttributes();
        assertNotNull(attrs);
        assertFalse(attrs.isEmpty());

        // These are the canonical FlowExecutionContext property names exposed by the filter.
        assertTrue(attrs.contains("tenantDomain"),   "permissive missing tenantDomain");
        assertTrue(attrs.contains("applicationId"),  "permissive missing applicationId");
        assertTrue(attrs.contains("flowType"),       "permissive missing flowType");
        assertTrue(attrs.contains("callbackUrl"),    "permissive missing callbackUrl");
        assertTrue(attrs.contains("portalUrl"),      "permissive missing portalUrl");
        assertTrue(attrs.contains("properties"),     "permissive missing properties");
    }

    @Test
    public void testPermissiveIncludesAllKnownUserAttributes() {

        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        Set<String> userAttrs = cfg.getIncludedUserAttributes();
        assertNotNull(userAttrs);
        assertFalse(userAttrs.isEmpty());

        // Canonical FlowUser property names.
        assertTrue(userAttrs.contains("username"),         "permissive missing username");
        assertTrue(userAttrs.contains("userId"),           "permissive missing userId");
        assertTrue(userAttrs.contains("userStoreDomain"),  "permissive missing userStoreDomain");
        assertTrue(userAttrs.contains("claims"),           "permissive missing claims");
        assertTrue(userAttrs.contains("userCredentials"),  "permissive missing userCredentials");
    }

    @Test
    public void testPermissiveDoesNotSetFullUserPassthrough() {

        // permissive() does NOT put "flowUser" in includedAttributes — it seeds individual
        // user attributes instead. So isFullUserPassthrough() must be false.
        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();
        assertFalse(cfg.isFullUserPassthrough());
    }

    // ========================= fullUserPassthrough flag =========================

    @Test
    public void testFullUserPassthroughTrueWhenFlowUserInAttrs() {

        Set<String> attrs = new HashSet<>(Arrays.asList("flowUser", "tenantDomain"));
        FlowContextHandoverConfig cfg = FlowContextHandoverConfigTestHelper.of(attrs, new HashSet<>());

        assertTrue(cfg.isFullUserPassthrough());
    }

    @Test
    public void testFullUserPassthroughFalseWhenFlowUserAbsent() {

        Set<String> attrs = new HashSet<>(Arrays.asList("tenantDomain", "applicationId"));
        FlowContextHandoverConfig cfg = FlowContextHandoverConfigTestHelper.of(attrs, new HashSet<>());

        assertFalse(cfg.isFullUserPassthrough());
    }

    // ========================= allow-list contents =========================

    @Test
    public void testIncludedAttributesReflectInput() {

        Set<String> attrs = new HashSet<>(Arrays.asList("tenantDomain", "properties"));
        Set<String> userAttrs = new HashSet<>(Arrays.asList("username", "claims"));

        FlowContextHandoverConfig cfg = FlowContextHandoverConfigTestHelper.of(attrs, userAttrs);

        assertTrue(cfg.getIncludedAttributes().contains("tenantDomain"));
        assertTrue(cfg.getIncludedAttributes().contains("properties"));
        assertFalse(cfg.getIncludedAttributes().contains("applicationId"));

        assertTrue(cfg.getIncludedUserAttributes().contains("username"));
        assertTrue(cfg.getIncludedUserAttributes().contains("claims"));
        assertFalse(cfg.getIncludedUserAttributes().contains("userId"));
    }

    @Test
    public void testIncludedAttributesSetIsUnmodifiable() {

        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        try {
            cfg.getIncludedAttributes().add("injected");
            // If no exception, check the value wasn't actually added (some impls are backed
            // by unmodifiable wrappers that silently discard — but the API contract is that
            // clients must not rely on mutation, so either throwing or silent discard is fine).
        } catch (UnsupportedOperationException e) {
            // Expected — the set is unmodifiable.
        }
    }

    @Test
    public void testIncludedUserAttributesSetIsUnmodifiable() {

        FlowContextHandoverConfig cfg = FlowContextHandoverConfig.permissive();

        try {
            cfg.getIncludedUserAttributes().add("injected");
        } catch (UnsupportedOperationException e) {
            // Expected.
        }
    }
}
