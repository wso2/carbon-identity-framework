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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads and exposes the global flow execution context handover filtering configuration from
 * {@code identity.xml} (templated from {@code deployment.toml}).
 *
 * <p>Two flat allow-lists are read:
 * <ul>
 *   <li>{@code FlowExecutionContextHandover.IncludedAttributes.IncludedAttribute} —
 *       top-level property names of {@link
 *       org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext}
 *       to expose.</li>
 *   <li>{@code FlowExecutionContextHandover.IncludedUserAttributes.IncludedUserAttribute} —
 *       property names of {@link
 *       org.wso2.carbon.identity.flow.execution.engine.model.FlowUser} to expose.</li>
 * </ul>
 *
 * <p>If {@code "flowUser"} is present in {@code includedAttributes} the entire
 * {@code FlowUser} is passed through and {@code includedUserAttributes} is ignored
 * ({@link #isFullUserPassthrough()} returns {@code true}).</p>
 *
 * <p>When the configuration is absent or empty a permissive fallback (all known attributes)
 * is used so that upgrading a server without updating deployment.toml preserves current
 * behaviour.</p>
 */
public final class FlowContextHandoverConfig {

    private static final Log LOG = LogFactory.getLog(FlowContextHandoverConfig.class);

    private final Set<String> includedAttributes;
    private final Set<String> includedUserAttributes;
    private final boolean fullUserPassthrough;

    /**
     * Construct by reading from the carbon configuration ({@code IdentityConfigParser}).
     * Call once during component activation; the result is immutable.
     */
    public FlowContextHandoverConfig() {

        Set<String> attrs = readList(FlowContextHandoverConstants.INCLUDED_ATTRIBUTES_KEY);
        Set<String> userAttrs = readList(FlowContextHandoverConstants.INCLUDED_USER_ATTRIBUTES_KEY);

        if (attrs.isEmpty()) {
            // Permissive fallback — no config present, expose everything.
            LOG.warn("No 'FlowExecutionContextHandover.IncludedAttributes' configured in identity.xml. "
                    + "Falling back to permissive mode (all attributes exposed).");
            FlowContextHandoverConfig permissive = permissive();
            this.includedAttributes = permissive.includedAttributes;
            this.includedUserAttributes = permissive.includedUserAttributes;
            this.fullUserPassthrough = permissive.fullUserPassthrough;
            return;
        }

        this.includedAttributes = Collections.unmodifiableSet(attrs);
        this.includedUserAttributes = Collections.unmodifiableSet(userAttrs);
        this.fullUserPassthrough = attrs.contains(FlowContextHandoverConstants.ATTR_FLOW_USER);

        if (LOG.isDebugEnabled()) {
            LOG.debug("FlowContextHandoverConfig loaded. includedAttributes=" + includedAttributes
                    + ", includedUserAttributes=" + includedUserAttributes
                    + ", fullUserPassthrough=" + fullUserPassthrough);
        }
    }

    /**
     * Private constructor used by {@link #permissive()}.
     */
    private FlowContextHandoverConfig(Set<String> includedAttributes,
                                      Set<String> includedUserAttributes,
                                      boolean fullUserPassthrough) {

        this.includedAttributes = Collections.unmodifiableSet(includedAttributes);
        this.includedUserAttributes = Collections.unmodifiableSet(includedUserAttributes);
        this.fullUserPassthrough = fullUserPassthrough;
    }

    /**
     * Factory that creates a permissive config allowing all known attributes on both
     * {@code FlowExecutionContext} and {@code FlowUser}. Used as a safe fallback when no
     * configuration is present, preserving behaviour for servers upgrading without updating
     * deployment.toml.
     *
     * @return a permissive {@link FlowContextHandoverConfig}.
     */
    public static FlowContextHandoverConfig permissive() {

        Set<String> allContext = new HashSet<>(FlowExecutionContextFilter.getKnownContextAttributes());
        Set<String> allUser = new HashSet<>(FlowExecutionContextFilter.getKnownUserAttributes());
        return new FlowContextHandoverConfig(allContext, allUser, false);
    }

    /**
     * Top-level property names of {@code FlowExecutionContext} that are included in the
     * filtered copy handed to downstream consumers.
     *
     * @return unmodifiable set of attribute names.
     */
    public Set<String> getIncludedAttributes() {

        return includedAttributes;
    }

    /**
     * Property names of {@code FlowUser} that are included in the filtered copy.
     * Ignored when {@link #isFullUserPassthrough()} is {@code true}.
     *
     * @return unmodifiable set of user attribute names.
     */
    public Set<String> getIncludedUserAttributes() {

        return includedUserAttributes;
    }

    /**
     * Returns {@code true} when {@code "flowUser"} is present in {@code includedAttributes},
     * meaning the entire {@code FlowUser} object is passed through and
     * {@code includedUserAttributes} is not consulted.
     *
     * @return whether the full FlowUser passes through unchanged.
     */
    public boolean isFullUserPassthrough() {

        return fullUserPassthrough;
    }

    // ---- private helpers ----

    @SuppressWarnings("unchecked")
    private static Set<String> readList(String key) {

        Object raw = IdentityConfigParser.getInstance().getConfiguration().get(key);
        List<String> items = new ArrayList<>();
        if (raw instanceof List) {
            items = (List<String>) raw;
        } else if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (!s.isEmpty()) {
                items.add(s);
            }
        }
        Set<String> result = new HashSet<>();
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                result.add(item.trim());
            }
        }
        return result;
    }
}
