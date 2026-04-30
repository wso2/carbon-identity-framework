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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FlowContextHandoverPolicyTest {

    @Test
    public void testDefaultBuilderHasEverythingDisabled() {

        FlowContextHandoverPolicy p = FlowContextHandoverPolicy.builder().build();

        assertFalse(p.isRedirectionEnabled());
        assertFalse(p.isUserEnabled());
        assertFalse(p.isUserUserId());
        assertFalse(p.isUserUserStoreDomain());
        assertFalse(p.isUserClaims());
        assertFalse(p.isUserCredentials());
        assertFalse(p.isFlowEnabled());
        assertFalse(p.isFlowTenantDomain());
        assertFalse(p.isFlowApplicationId());
        assertFalse(p.isFlowFlowType());
        assertFalse(p.isFlowCallbackUrl());
        assertFalse(p.isFlowPortalUrl());
        assertFalse(p.isPropertiesEnabled());
    }

    @Test
    public void testPermissiveAllowsEverything() {

        FlowContextHandoverPolicy p = FlowContextHandoverPolicy.permissive();

        assertTrue(p.isRedirectionEnabled());
        assertTrue(p.isUserEnabled());
        assertTrue(p.isUserUserId());
        assertTrue(p.isUserUserStoreDomain());
        assertTrue(p.isUserClaims());
        assertTrue(p.isUserCredentials());
        assertTrue(p.isFlowEnabled());
        assertTrue(p.isFlowTenantDomain());
        assertTrue(p.isFlowApplicationId());
        assertTrue(p.isFlowFlowType());
        assertTrue(p.isFlowCallbackUrl());
        assertTrue(p.isFlowPortalUrl());
        assertTrue(p.isPropertiesEnabled());
    }

    @Test
    public void testStartingFromInheritsThenOverrides() {

        // Mimic deployment.toml: per-flow-type override that says "redirection.enable = false"
        // but inherits everything else from the permissive default.
        FlowContextHandoverPolicy base = FlowContextHandoverPolicy.permissive();
        FlowContextHandoverPolicy override = FlowContextHandoverPolicy.builder()
                .startingFrom(base)
                .redirectionEnabled(false)
                .userClaims(false)
                .build();

        // Overridden:
        assertFalse(override.isRedirectionEnabled());
        assertFalse(override.isUserClaims());

        // Inherited:
        assertTrue(override.isUserEnabled());
        assertTrue(override.isUserUserStoreDomain());
        assertTrue(override.isFlowFlowType());
        assertTrue(override.isFlowPortalUrl());
        assertTrue(override.isPropertiesEnabled());
    }

    @Test
    public void testBuilderIndependentOfBaseAfterStartingFrom() {

        // Calling startingFrom must copy values, not retain a reference. Mutating the builder
        // afterwards must not affect the base policy.
        FlowContextHandoverPolicy base = FlowContextHandoverPolicy.permissive();
        FlowContextHandoverPolicy.Builder b = FlowContextHandoverPolicy.builder().startingFrom(base);

        b.redirectionEnabled(false);
        FlowContextHandoverPolicy mutated = b.build();

        assertFalse(mutated.isRedirectionEnabled());
        assertTrue(base.isRedirectionEnabled());  // base unchanged
    }
}
