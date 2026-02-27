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
 * Utility for constructing per-tenant service keys.
 */
public final class TenantKeyUtil {

    private static final char KEY_SEGMENT_SEPARATOR = ':';

    private TenantKeyUtil() {

    }

    public static String buildTenantServiceKey(String tenantDomain, String service) {

        String normalizedTenantDomain = normalize(tenantDomain, "tenantDomain");
        String normalizedService = normalize(service, "service");
        return normalizedTenantDomain + KEY_SEGMENT_SEPARATOR + normalizedService;
    }

    public static String extractTenantDomain(String tenantServiceKey) {

        KeySegments keySegments = parseTenantServiceKey(tenantServiceKey);
        return keySegments.tenantDomain;
    }

    public static String extractService(String tenantServiceKey) {

        KeySegments keySegments = parseTenantServiceKey(tenantServiceKey);
        return keySegments.service;
    }

    private static KeySegments parseTenantServiceKey(String tenantServiceKey) {

        String normalizedTenantServiceKey = normalize(tenantServiceKey, "tenantServiceKey");
        int separatorIndex = normalizedTenantServiceKey.indexOf(KEY_SEGMENT_SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex == normalizedTenantServiceKey.length() - 1) {
            throw new IllegalArgumentException("Invalid tenantServiceKey format");
        }

        String tenantDomain = normalizedTenantServiceKey.substring(0, separatorIndex);
        String service = normalizedTenantServiceKey.substring(separatorIndex + 1);
        if (isBlank(tenantDomain) || isBlank(service)) {
            throw new IllegalArgumentException("Invalid tenantServiceKey format");
        }

        return new KeySegments(tenantDomain, service);
    }

    private static String normalize(String value, String fieldName) {

        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }

    private static final class KeySegments {

        private final String tenantDomain;
        private final String service;

        private KeySegments(String tenantDomain, String service) {

            this.tenantDomain = tenantDomain;
            this.service = service;
        }
    }
}
