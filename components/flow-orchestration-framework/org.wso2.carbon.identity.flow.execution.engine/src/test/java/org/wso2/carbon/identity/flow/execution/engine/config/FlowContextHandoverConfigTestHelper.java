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

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * Test-only helper that instantiates {@link FlowContextHandoverConfig} with explicit
 * allow-lists without touching {@code IdentityConfigParser}. Lives in the same package
 * as the production class so it can access the package-private constructor via reflection
 * from a predictable location.
 */
public final class FlowContextHandoverConfigTestHelper {

    private FlowContextHandoverConfigTestHelper() {

    }

    /**
     * Construct a {@link FlowContextHandoverConfig} with the given allow-lists.
     * {@code fullUserPassthrough} is derived from whether {@code "flowUser"} is in
     * {@code attrs}, mirroring the production constructor logic.
     */
    public static FlowContextHandoverConfig of(Set<String> attrs, Set<String> userAttrs) {

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
