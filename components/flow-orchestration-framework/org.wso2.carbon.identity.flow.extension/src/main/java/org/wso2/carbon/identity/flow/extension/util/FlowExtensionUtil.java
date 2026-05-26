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

package org.wso2.carbon.identity.flow.extension.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.HandoverPolicy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * General-purpose utilities for the Flow Extension module.
 *
 * <p>Currently hosts {@link #filterContext(FlowExecutionContext)}, which builds a filtered
 * defensive copy of a {@link FlowExecutionContext} containing only the attributes whitelisted
 * by {@link HandoverPolicy#INCLUDED_ATTRIBUTES} and {@link HandoverPolicy#INCLUDED_USER_ATTRIBUTES}.
 * The whitelist is governed at the code level, in {@code FlowExtensionConstants.HandoverPolicy}.</p>
 *
 * <p>The filtering implementation mirrors the engine's {@code FlowExecutionContextFilter} but
 * lives in the flow-extension module to avoid a cross-bundle dependency on
 * {@code engine.config.*}. When the toml-based dynamic configuration PR is merged, the filter
 * helper can be removed and the engine's filter used directly.</p>
 *
 * <p>Attributes are copied via explicit per-field mappings in
 * {@link #copyFlowContext(FlowExecutionContext, FlowExecutionContext)} and
 * {@link #copyFlowUser(FlowUser, FlowUser)}. The original context is never mutated;
 * non-permitted attributes are left null/empty on the copy.</p>
 *
 * <p>The {@code userCredentials} field receives a per-entry {@code char[]} clone so that the
 * request builder's post-extraction wipe zeroes the copy, not the source.</p>
 */
public final class FlowExtensionUtil {

    private static final Log LOG = LogFactory.getLog(FlowExtensionUtil.class);

    private FlowExtensionUtil() {

    }

    /**
     * Build a filtered copy of {@code original} using the code-level handover whitelist
     * declared in {@link HandoverPolicy}.
     *
     * @param original the source context (untouched).
     * @return a new {@link FlowExecutionContext} carrying only whitelisted attributes,
     *         or {@code null} if {@code original} is {@code null}.
     */
    public static FlowExecutionContext filterContext(FlowExecutionContext original) {

        if (original == null) {
            return null;
        }

        FlowExecutionContext copy = new FlowExecutionContext();

        // contextIdentifier is engine-internal and always propagated regardless of policy.
        copy.setContextIdentifier(original.getContextIdentifier());

        copyFlowContext(original, copy);

        // User attributes — a fresh non-null FlowUser is always set on the copy so that
        // request builders / response processors don't need to null-guard the user object.
        FlowUser dstUser = new FlowUser();
        copy.setFlowUser(dstUser);
        FlowUser srcUser = original.getFlowUser();
        if (srcUser != null) {
            copyFlowUser(srcUser, dstUser);
        }

        return copy;
    }

    /**
     * Copy the whitelisted top-level {@link FlowExecutionContext} fields from {@code src} to
     * {@code dst}. {@code contextIdentifier} and {@code flowUser} are skipped here because
     * they are handled by {@link #filterContext(FlowExecutionContext)} directly —
     * {@code contextIdentifier} is propagated unconditionally and {@code flowUser} is copied
     * via {@link #copyFlowUser(FlowUser, FlowUser)} so the destination always has a non-null
     * user.
     */
    private static void copyFlowContext(FlowExecutionContext src, FlowExecutionContext dst) {

        for (String name : HandoverPolicy.INCLUDED_ATTRIBUTES) {
            switch (name) {
                case "contextIdentifier":
                case "flowUser":
                    break;
                case "tenantDomain":
                    dst.setTenantDomain(src.getTenantDomain());
                    break;
                case "applicationId":
                    dst.setApplicationId(src.getApplicationId());
                    break;
                case "flowType":
                    dst.setFlowType(src.getFlowType());
                    break;
                case "callbackUrl":
                    dst.setCallbackUrl(src.getCallbackUrl());
                    break;
                case "portalUrl":
                    dst.setPortalUrl(src.getPortalUrl());
                    break;
                default:
                    LOG.warn("Skipping unmapped handover context attribute: " + name
                            + ". Add a case in FlowExtensionUtil.copyFlowContext to handle it.");
            }
        }
    }

    /**
     * Copy the whitelisted {@link FlowUser} fields from {@code src} to {@code dst}. Explicit
     * per-field mapping is used instead of JavaBean reflection because {@code FlowUser}'s
     * accessors do not all follow the standard getter/setter naming required by
     * {@link java.beans.Introspector} (notably {@code claims} is read-only and {@code userId}
     * does not match a whitelist entry of {@code "id"}).
     *
     * <p>The {@link HandoverPolicy#INCLUDED_USER_ATTRIBUTES} whitelist remains the source of
     * truth: only names listed there are copied, and an unrecognised entry is logged at WARN
     * so the missing mapping is visible.</p>
     */
    private static void copyFlowUser(FlowUser src, FlowUser dst) {

        for (String name : HandoverPolicy.INCLUDED_USER_ATTRIBUTES) {
            switch (name) {
                case "id":
                    dst.setUserId(src.getUserId());
                    break;
                case "username":
                    dst.setUsername(src.getUsername());
                    break;
                case "userStoreDomain":
                    dst.setUserStoreDomain(src.getUserStoreDomain());
                    break;
                case "claims":
                    dst.addClaims(new HashMap<>(src.getClaims()));
                    break;
                case "userCredentials":
                    Map<String, char[]> srcCreds = src.getUserCredentials();
                    Map<String, char[]> clonedCreds = new LinkedHashMap<>();
                    for (Map.Entry<String, char[]> entry : srcCreds.entrySet()) {
                        char[] v = entry.getValue();
                        clonedCreds.put(entry.getKey(), v == null ? null : v.clone());
                    }
                    dst.setUserCredentials(clonedCreds);
                    break;
                default:
                    LOG.warn("Skipping unmapped handover user attribute: " + name
                            + ". Add a case in FlowExtensionUtil.copyFlowUser to handle it.");
            }
        }
    }

}
