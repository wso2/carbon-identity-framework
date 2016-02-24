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

import org.wso2.carbon.identity.application.common.cache.CacheKey;

public class IdentityServiceProviderCacheKey extends CacheKey {

    /**
     *
     */
    private static final long serialVersionUID = 8263255365985309443L;

    private String serviceProviderKey;

    /**
     * @param serviceProviderName
     * @param tenantDomain
     */
    public IdentityServiceProviderCacheKey(String serviceProviderName, String tenantDomain) {
        this.serviceProviderKey = serviceProviderName;
        this.tenantDomain = tenantDomain.toLowerCase();
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

        if (!serviceProviderKey.equals(that.serviceProviderKey)) return false;
        if (!tenantDomain.equals(that.tenantDomain)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serviceProviderKey.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
