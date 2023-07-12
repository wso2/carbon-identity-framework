/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for Basic Service provider, when it is loaded with Application ID.
 */
public class LiteServiceProviderIDCacheKey extends CacheKey {

    private static final long serialVersionUID = 6638400636618465149L;
    private String serviceProviderKey;
    private String tenantDomain;


    public LiteServiceProviderIDCacheKey(String serviceProviderName, String tenantDomain) {

        this.serviceProviderKey = serviceProviderName;
        if (tenantDomain != null) {
            this.tenantDomain = tenantDomain.toLowerCase();
        }
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

        LiteServiceProviderIDCacheKey that = (LiteServiceProviderIDCacheKey) o;

        if (!serviceProviderKey.equals(that.serviceProviderKey)) {
            return false;
        }
        return tenantDomain.equals(that.tenantDomain);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + serviceProviderKey.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
