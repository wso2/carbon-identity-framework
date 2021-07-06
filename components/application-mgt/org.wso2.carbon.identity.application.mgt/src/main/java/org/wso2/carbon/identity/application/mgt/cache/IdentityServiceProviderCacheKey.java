/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for lookup Application (aka. Service Provieder) from the cache.
 */
public class IdentityServiceProviderCacheKey extends CacheKey {

    private static final long serialVersionUID = 8263255365985309443L;

    private String serviceProviderKey;

    /**
     * @param serviceProviderName
     */
    public IdentityServiceProviderCacheKey(String serviceProviderName) {

        this.serviceProviderKey = serviceProviderName;
    }

    /**
     * @return
     */
    public String getServiceProviderKey() {

        return serviceProviderKey;
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

        IdentityServiceProviderCacheKey that = (IdentityServiceProviderCacheKey) o;

        return serviceProviderKey.equals(that.serviceProviderKey);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + serviceProviderKey.hashCode();
        return result;
    }
}
