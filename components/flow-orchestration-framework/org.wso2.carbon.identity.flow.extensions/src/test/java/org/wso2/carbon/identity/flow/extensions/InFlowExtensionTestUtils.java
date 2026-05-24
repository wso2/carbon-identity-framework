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

package org.wso2.carbon.identity.flow.extensions;

import org.wso2.carbon.identity.flow.extensions.model.FlowContextHandoverConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test-only helper that builds {@link FlowContextHandoverConfig} instances using the
 * inflow module's own factory method — no reflection, no engine class dependencies.
 */
public final class InFlowExtensionTestUtils {

    /**
     * Commonly used attribute sets that cover all fields exposed by the filter.
     */
    public static final Set<String> ALL_CONTEXT_ATTRS = new HashSet<>(Arrays.asList(
            "tenantDomain", "applicationId", "flowType", "callbackUrl", "portalUrl",
            "properties", "contextIdentifier"));

    public static final Set<String> ALL_USER_ATTRS = new HashSet<>(Arrays.asList(
            "username", "id", "userStoreDomain", "claims", "userCredentials"));

    private InFlowExtensionTestUtils() {

    }

    /**
     * Construct a permissive {@link FlowContextHandoverConfig} covering all known fields.
     */
    public static FlowContextHandoverConfig permissiveConfig() {

        return configOf(ALL_CONTEXT_ATTRS, ALL_USER_ATTRS);
    }

    /**
     * Construct a {@link FlowContextHandoverConfig} with explicit allow-lists.
     */
    public static FlowContextHandoverConfig configOf(Set<String> attrs, Set<String> userAttrs) {

        return FlowContextHandoverConfig.of(attrs, userAttrs);
    }
}
