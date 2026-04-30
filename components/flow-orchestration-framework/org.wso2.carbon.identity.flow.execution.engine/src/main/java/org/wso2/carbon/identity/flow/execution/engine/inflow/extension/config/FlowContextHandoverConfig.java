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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and exposes the {@link FlowContextHandoverPolicy} for each known flow type from
 * {@code identity.xml} (which is itself templated from {@code deployment.toml}). The j2
 * template performs deep-merge inheritance from the {@code default} section using chained
 * {@code default(...)} Jinja filters, so by the time this loader runs every leaf is fully
 * resolved and the loader just reads the literal value.
 *
 * <p>Per-flow-type policies are keyed by {@code FlowTypes.getType()} (e.g.,
 * {@code "REGISTRATION"}). Unknown flow types fall through to {@link #getDefaultPolicy()}.</p>
 */
public class FlowContextHandoverConfig {

    private static final Log LOG = LogFactory.getLog(FlowContextHandoverConfig.class);

    // Section names under <InFlowExtensionContextHandover> in identity.xml.
    private static final String ROOT = "InFlowExtensionContextHandover";
    private static final String SECTION_DEFAULT = "Default";
    private static final String SECTION_REGISTRATION = "Registration";
    private static final String SECTION_PASSWORD_RECOVERY = "PasswordRecovery";
    private static final String SECTION_INVITED_USER_REGISTRATION = "InvitedUserRegistration";

    // Mapping XML section name → FlowTypes enum value (the keys we'll look up at runtime).
    private static final Map<String, String> XML_SECTION_TO_FLOW_TYPE;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(SECTION_REGISTRATION, "REGISTRATION");
        map.put(SECTION_PASSWORD_RECOVERY, "PASSWORD_RECOVERY");
        map.put(SECTION_INVITED_USER_REGISTRATION, "INVITED_USER_REGISTRATION");
        XML_SECTION_TO_FLOW_TYPE = Collections.unmodifiableMap(map);
    }

    private final FlowContextHandoverPolicy defaultPolicy;
    private final Map<String, FlowContextHandoverPolicy> perFlowTypePolicies;

    /**
     * Eager-load all policies from {@code IdentityUtil}. Call once at component startup.
     */
    public FlowContextHandoverConfig() {

        this.defaultPolicy = readPolicy(SECTION_DEFAULT);
        Map<String, FlowContextHandoverPolicy> perType = new HashMap<>();
        for (Map.Entry<String, String> entry : XML_SECTION_TO_FLOW_TYPE.entrySet()) {
            perType.put(entry.getValue(), readPolicy(entry.getKey()));
        }
        this.perFlowTypePolicies = Collections.unmodifiableMap(perType);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded In-Flow Extension context handover policies: default + "
                    + perFlowTypePolicies.keySet());
        }
    }

    /**
     * Resolve the policy for the given flow type. Falls back to the default policy if the
     * flow type is unknown or null.
     */
    public FlowContextHandoverPolicy resolve(String flowType) {

        if (flowType == null) {
            return defaultPolicy;
        }
        FlowContextHandoverPolicy policy = perFlowTypePolicies.get(flowType);
        return policy != null ? policy : defaultPolicy;
    }

    public FlowContextHandoverPolicy getDefaultPolicy() {

        return defaultPolicy;
    }

    private FlowContextHandoverPolicy readPolicy(String section) {

        String prefix = ROOT + "." + section + ".";
        return FlowContextHandoverPolicy.builder()
                .redirectionEnabled(readBool(prefix + "Redirection.Enable", true))
                .userEnabled(readBool(prefix + "User.Enable", true))
                .userUserId(readBool(prefix + "User.UserId", false))
                .userUserStoreDomain(readBool(prefix + "User.UserStoreDomain", true))
                .userClaims(readBool(prefix + "User.Claims", true))
                .userCredentials(readBool(prefix + "User.Credentials", false))
                .flowEnabled(readBool(prefix + "Flow.Enable", true))
                .flowTenantDomain(readBool(prefix + "Flow.TenantDomain", true))
                .flowApplicationId(readBool(prefix + "Flow.ApplicationId", true))
                .flowFlowType(readBool(prefix + "Flow.FlowType", true))
                .flowCallbackUrl(readBool(prefix + "Flow.CallbackUrl", true))
                .flowPortalUrl(readBool(prefix + "Flow.PortalUrl", true))
                .propertiesEnabled(readBool(prefix + "Properties.Enable", true))
                .build();
    }

    private static boolean readBool(String key, boolean fallback) {

        String value = IdentityUtil.getProperty(key);
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
