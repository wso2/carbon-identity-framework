/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.circuitbreaker;

import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

/**
 * Resolves the effective {@link RuntimePolicy} for a new {@link TenantBreakerEntry} by applying
 * the registered {@link RuntimePolicyLoader} and {@link RuntimePolicyExtender} in order.
 */
public class RuntimePolicyResolver {

    private static final RuntimePolicyResolver INSTANCE = new RuntimePolicyResolver();

    /**
     * Returns the singleton instance.
     *
     * @return the {@link RuntimePolicyResolver} instance.
     */
    public static RuntimePolicyResolver getInstance() {

        return INSTANCE;
    }

    private RuntimePolicyResolver() {

    }

    /**
     * Resolves the effective runtime policy for the entry identified by {@code tenantKey}.
     *
     * @param tenantKey     The composite tenant-service key.
     * @param defaultPolicy The server-wide default policy.
     * @return The resolved {@link RuntimePolicy} for the new entry.
     */
    public RuntimePolicy resolve(String tenantKey, RuntimePolicy defaultPolicy) {

        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantKey);
        String tenantDomain = parts.tenantDomain();
        TenantService service = TenantService.valueOf(parts.serviceName());

        RuntimePolicy current = defaultPolicy;

        IdentityCoreServiceDataHolder dataHolder = IdentityCoreServiceDataHolder.getInstance();

        RuntimePolicyLoader loader = dataHolder.getRuntimePolicyLoader(service);
        if (loader != null) {
            RuntimePolicy loaded = loader.load(tenantDomain, service, current);
            if (loaded != null) {
                current = loaded;
            }
        }

        RuntimePolicyExtender extender = dataHolder.getRuntimePolicyExtender();
        if (extender != null) {
            RuntimePolicy extended = extender.extend(tenantDomain, service, current);
            if (extended != null) {
                current = extended;
            }
        }

        return current;
    }
}
