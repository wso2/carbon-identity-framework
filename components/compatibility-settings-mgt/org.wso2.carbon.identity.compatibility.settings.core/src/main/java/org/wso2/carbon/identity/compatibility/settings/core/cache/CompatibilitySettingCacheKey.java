/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

import java.util.Objects;

/**
 * Cache key for compatibility settings.
 * Used to uniquely identify cached compatibility settings for a tenant.
 */
public class CompatibilitySettingCacheKey extends CacheKey {

    private static final long serialVersionUID = -2864579037465920472L;

    private final String tenantDomain;

    /**
     * Constructor for CompatibilitySettingCacheKey.
     *
     * @param tenantDomain Tenant domain.
     */
    public CompatibilitySettingCacheKey(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Get the tenant domain.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        CompatibilitySettingCacheKey that = (CompatibilitySettingCacheKey) o;
        return Objects.equals(tenantDomain, that.tenantDomain);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + (tenantDomain != null ? tenantDomain.hashCode() : 0);
        return result;
    }
}
