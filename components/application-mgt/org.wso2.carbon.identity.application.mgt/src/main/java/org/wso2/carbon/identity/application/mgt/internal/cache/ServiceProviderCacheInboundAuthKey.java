/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.application.common.cache.CacheKey;

public class ServiceProviderCacheInboundAuthKey extends CacheKey {

    private static final long serialVersionUID = -2977524029670977142L;
    private String ServiceProvideCacheInboundAuthKey;
    private String ServiceProvideCacheInboundAuthType;

    public ServiceProviderCacheInboundAuthKey(String serviceProvideCacheInboundAuthKey, String
            serviceProvideCacheInboundAuthType, String tenantDomain) {

        ServiceProvideCacheInboundAuthKey = serviceProvideCacheInboundAuthKey;
        ServiceProvideCacheInboundAuthType = serviceProvideCacheInboundAuthType;
        this.tenantDomain = tenantDomain;
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

        ServiceProviderCacheInboundAuthKey that = (ServiceProviderCacheInboundAuthKey) o;

        if (!ServiceProvideCacheInboundAuthKey.equals(that.ServiceProvideCacheInboundAuthKey)) return false;
        if (!ServiceProvideCacheInboundAuthType.equals(that.ServiceProvideCacheInboundAuthType)) return false;
        return tenantDomain.equals(that.tenantDomain);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + ServiceProvideCacheInboundAuthKey.hashCode();
        result = 31 * result + ServiceProvideCacheInboundAuthType.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
