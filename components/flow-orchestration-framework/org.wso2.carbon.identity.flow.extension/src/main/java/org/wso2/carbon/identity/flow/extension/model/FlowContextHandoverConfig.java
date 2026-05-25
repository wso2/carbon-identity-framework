/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extension.model;

import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable snapshot of which {@code FlowExecutionContext} attributes are allowed to be
 * handed over to the action framework during an in-flow extension execution.
 *
 * <p>The default policy is sourced from compile-time constants in
 * {@link FlowExtensionConstants.HandoverPolicy}. When the dynamic toml-based
 * configuration PR is merged, these constants will serve as the documented defaults.</p>
 */
public final class FlowContextHandoverConfig {

    private final Set<String> includedAttributes;
    private final Set<String> includedUserAttributes;
    private final boolean fullUserPassthrough;

    private FlowContextHandoverConfig(Set<String> includedAttributes,
                                      Set<String> includedUserAttributes,
                                      boolean fullUserPassthrough) {

        this.includedAttributes = Collections.unmodifiableSet(includedAttributes);
        this.includedUserAttributes = Collections.unmodifiableSet(includedUserAttributes);
        this.fullUserPassthrough = fullUserPassthrough;
    }

    /**
     * Construct a config from explicit attribute sets.
     * {@code fullUserPassthrough} is true when {@link FlowExtensionConstants.HandoverPolicy#ATTR_FLOW_USER}
     * is present in {@code attrs}, meaning the entire FlowUser passes through without per-field filtering.
     *
     * @param attrs     top-level context attributes to include; null is treated as empty.
     * @param userAttrs FlowUser attributes to include; null is treated as empty.
     * @return a new immutable {@link FlowContextHandoverConfig}.
     */
    public static FlowContextHandoverConfig of(Set<String> attrs, Set<String> userAttrs) {

        Set<String> resolvedAttrs = (attrs != null) ? attrs : Collections.emptySet();
        Set<String> resolvedUserAttrs = (userAttrs != null) ? userAttrs : Collections.emptySet();
        boolean fullPassthrough = resolvedAttrs.contains(
                FlowExtensionConstants.HandoverPolicy.ATTR_FLOW_USER);
        return new FlowContextHandoverConfig(resolvedAttrs, resolvedUserAttrs, fullPassthrough);
    }

    /**
     * Returns the default handover policy built from compile-time constants defined in
     * {@link FlowExtensionConstants.HandoverPolicy}.
     *
     * <p>This is the factory method called at runtime by the executor and the context tree
     * service. To change the effective policy, update the constants in
     * {@link FlowExtensionConstants.HandoverPolicy}.</p>
     *
     * @return a new {@link FlowContextHandoverConfig} reflecting the default policy.
     */
    public static FlowContextHandoverConfig defaultPolicy() {

        return of(
                FlowExtensionConstants.HandoverPolicy.INCLUDED_ATTRIBUTES,
                FlowExtensionConstants.HandoverPolicy.INCLUDED_USER_ATTRIBUTES
        );
    }

    /**
     * Returns the set of top-level {@code FlowExecutionContext} attribute names that may be
     * handed over to the action framework.
     *
     * @return unmodifiable set of allowed attribute names.
     */
    public Set<String> getIncludedAttributes() {

        return includedAttributes;
    }

    /**
     * Returns the set of {@code FlowUser} attribute names that may be handed over when
     * {@link #isFullUserPassthrough()} is false.
     *
     * @return unmodifiable set of allowed user attribute names.
     */
    public Set<String> getIncludedUserAttributes() {

        return includedUserAttributes;
    }

    /**
     * Returns true when {@link FlowExtensionConstants.HandoverPolicy#ATTR_FLOW_USER} is
     * present in {@link #getIncludedAttributes()}, meaning the entire {@code FlowUser} object
     * is passed through without per-field inspection.
     *
     * @return true if the full FlowUser should pass through; false if per-field filtering applies.
     */
    public boolean isFullUserPassthrough() {

        return fullUserPassthrough;
    }
}
