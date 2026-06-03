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

/**
 * Utility for constructing and parsing per-tenant service keys.
 */
public final class TenantKeyUtil {

    private static final char KEY_SEGMENT_SEPARATOR = ':';

    private TenantKeyUtil() {

    }

    /**
     * Tenant domain and service name extracted from a composite tenant-service key.
     *
     * @param tenantDomain Tenant domain segment.
     * @param serviceName  Service name segment.
     */
    public record TenantKeyParts(String tenantDomain, String serviceName) {

    }

    /**
     * Builds a composite key of the form {@code "<tenantDomain>:<service>"}. Callers must
     * ensure both arguments are non-null and non-blank before invoking.
     *
     * @param tenantDomain Tenant domain.
     * @param service      Service name.
     * @return Composite tenant-service key.
     */
    public static String buildTenantServiceKey(String tenantDomain, String service) {

        return tenantDomain.trim() + KEY_SEGMENT_SEPARATOR + service.trim();
    }

    /**
     * Parses a composite key produced by {@link #buildTenantServiceKey(String, String)} and
     * returns both the tenant domain and service name.
     *
     * @param tenantKey Composite tenant-service key.
     * @return {@link TenantKeyParts} holding the tenant domain and service name.
     */
    public static TenantKeyParts parse(String tenantKey) {

        int idx = tenantKey.indexOf(KEY_SEGMENT_SEPARATOR);
        return new TenantKeyParts(tenantKey.substring(0, idx), tenantKey.substring(idx + 1));
    }
}
