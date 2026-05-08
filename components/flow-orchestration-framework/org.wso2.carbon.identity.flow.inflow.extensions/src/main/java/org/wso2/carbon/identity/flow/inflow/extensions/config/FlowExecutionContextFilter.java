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

package org.wso2.carbon.identity.flow.inflow.extensions.config;

import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a filtered defensive copy of a {@link FlowExecutionContext} containing only the
 * fields permitted by the supplied {@link FlowContextHandoverPolicy}. Used by
 * {@code InFlowExtensionExecutor} to bound the surface area visible to the action framework.
 *
 * <p>Non-permitted fields are left null/empty on the copy. The original context is never
 * mutated. Only public getters/setters are used so the existing request-builder and
 * response-processor code that reads via {@code execCtx.getFoo()} continues to work — it
 * just gets {@code null} (or empty maps) for fields the deployment.toml whitelist omits.</p>
 */
public final class FlowExecutionContextFilter {

    private FlowExecutionContextFilter() {

    }

    /**
     * Build a filtered copy of {@code original} according to {@code policy}.
     *
     * @param original the original context (untouched).
     * @param policy   the per-flow handover policy.
     * @return a new {@link FlowExecutionContext} carrying only whitelisted fields.
     */
    public static FlowExecutionContext filter(FlowExecutionContext original,
                                              FlowContextHandoverPolicy policy) {

        if (original == null) {
            return null;
        }
        if (policy == null) {
            // Defensive: no policy → permissive (preserve current behaviour).
            policy = FlowContextHandoverPolicy.permissive();
        }

        FlowExecutionContext copy = new FlowExecutionContext();

        // contextIdentifier is engine-internal and always copied — not a user-data field.
        copy.setContextIdentifier(original.getContextIdentifier());

        // ---- Flow block ----
        if (policy.isFlowEnabled()) {
            if (policy.isFlowTenantDomain()) {
                copy.setTenantDomain(original.getTenantDomain());
            }
            if (policy.isFlowApplicationId()) {
                copy.setApplicationId(original.getApplicationId());
            }
            if (policy.isFlowFlowType()) {
                copy.setFlowType(original.getFlowType());
            }
            if (policy.isFlowCallbackUrl()) {
                copy.setCallbackUrl(original.getCallbackUrl());
            }
            if (policy.isFlowPortalUrl()) {
                copy.setPortalUrl(original.getPortalUrl());
            }
        }

        // ---- User block ----
        // Always set a non-null FlowUser on the copy. Pending claims/credentials/properties
        // collected by the response processor are stashed in FlowContext keys (not on FlowUser)
        // and applied to the *original* context by TaskExecutionNode after the executor returns,
        // so this empty FlowUser never leaks back into engine state. The empty instance exists
        // only as a defensive convenience: it lets the request builder call getClaims()/
        // getUserCredentials() without null-guarding the FlowUser itself.
        FlowUser filteredUser = new FlowUser();
        FlowUser originalUser = original.getFlowUser();
        if (policy.isUserEnabled() && originalUser != null) {
            if (policy.isUserUserId()) {
                filteredUser.setUserId(originalUser.getUserId());
            }
            if (policy.isUserUserStoreDomain()) {
                filteredUser.setUserStoreDomain(originalUser.getUserStoreDomain());
            }
            if (policy.isUserClaims() && originalUser.getClaims() != null) {
                // Defensive copy of the claims map.
                filteredUser.addClaims(new HashMap<>(originalUser.getClaims()));
            }
            if (policy.isUserCredentials() && originalUser.getUserCredentials() != null) {
                // Defensive copy of credentials — clone each char[] so the request builder's
                // post-extraction wipe (Arrays.fill(...,'\0')) zeroes the *copy*, not the original.
                Map<String, char[]> credentialsCopy = new HashMap<>();
                for (Map.Entry<String, char[]> entry : originalUser.getUserCredentials().entrySet()) {
                    char[] value = entry.getValue();
                    credentialsCopy.put(entry.getKey(), value == null ? null : value.clone());
                }
                filteredUser.setUserCredentials(credentialsCopy);
            }
        }
        copy.setFlowUser(filteredUser);

        // ---- Properties block ----
        if (policy.isPropertiesEnabled() && original.getProperties() != null) {
            copy.setProperties(new HashMap<>(original.getProperties()));
        }

        return copy;
    }
}
