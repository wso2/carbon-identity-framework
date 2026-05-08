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

package org.wso2.carbon.identity.flow.inflow.extensions;

import org.wso2.carbon.identity.flow.execution.engine.config.FlowContextHandoverConfig;
import org.wso2.carbon.identity.flow.execution.engine.config.FlowContextHandoverConstants;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test-only helper that builds {@link FlowContextHandoverConfig} instances without
 * touching {@code IdentityConfigParser} or triggering {@code FlowExecutionContextFilter}'s
 * static initialiser (which fails in the inflow test classpath due to missing
 * authentication-framework dependencies).
 *
 * <p>Uses reflection to call the private 3-arg constructor on
 * {@link FlowContextHandoverConfig} directly.</p>
 */
public final class InFlowExtensionTestUtils {

    /**
     * Commonly used attribute sets that cover all fields the filter exposes without needing
     * {@code FlowExecutionContextFilter.getKnownContextAttributes()}.
     */
    public static final Set<String> ALL_CONTEXT_ATTRS = new HashSet<>(Arrays.asList(
            "tenantDomain", "applicationId", "flowType", "callbackUrl", "portalUrl",
            "properties", "contextIdentifier"));

    public static final Set<String> ALL_USER_ATTRS = new HashSet<>(Arrays.asList(
            "username", "userId", "userStoreDomain", "claims", "userCredentials"));

    private InFlowExtensionTestUtils() {

    }

    /**
     * Construct a permissive {@link FlowContextHandoverConfig} without triggering
     * {@code FlowContextHandoverConfig.permissive()} (which calls
     * {@code FlowExecutionContextFilter}'s static initialiser).
     */
    public static FlowContextHandoverConfig permissiveConfig() {

        return configOf(ALL_CONTEXT_ATTRS, ALL_USER_ATTRS);
    }

    /**
     * Construct a {@link FlowContextHandoverConfig} with explicit allow-lists.
     */
    public static FlowContextHandoverConfig configOf(Set<String> attrs, Set<String> userAttrs) {

        try {
            Constructor<FlowContextHandoverConfig> ctor =
                    FlowContextHandoverConfig.class.getDeclaredConstructor(
                            Set.class, Set.class, boolean.class);
            ctor.setAccessible(true);
            boolean fullPassthrough = attrs.contains(FlowContextHandoverConstants.ATTR_FLOW_USER);
            return ctor.newInstance(attrs, userAttrs, fullPassthrough);
        } catch (Exception e) {
            throw new RuntimeException("Cannot construct FlowContextHandoverConfig for test", e);
        }
    }
}
