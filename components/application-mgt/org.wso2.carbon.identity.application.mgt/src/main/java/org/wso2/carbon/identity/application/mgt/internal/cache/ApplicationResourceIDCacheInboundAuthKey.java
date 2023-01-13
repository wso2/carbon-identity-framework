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
 * Cache key used to access application based on authenticated application information for the request.
 */
public class ApplicationResourceIDCacheInboundAuthKey extends CacheKey {

    private static final long serialVersionUID = 5197091237662341491L;
    private String applicationCacheInboundAuthKey;
    private String applicationCacheInboundAuthType;
    private String tenantDomain;

    public ApplicationResourceIDCacheInboundAuthKey(String applicationCacheInboundAuthKey, String
            applicationCacheInboundAuthType, String tenantDomain) {

        this.applicationCacheInboundAuthKey = applicationCacheInboundAuthKey;
        this.applicationCacheInboundAuthType = applicationCacheInboundAuthType;
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

        ApplicationResourceIDCacheInboundAuthKey that = (ApplicationResourceIDCacheInboundAuthKey) o;

        if (applicationCacheInboundAuthKey == null || applicationCacheInboundAuthType == null ||
                tenantDomain == null) {
            return false;
        }

        if (!tenantDomain.equals(that.tenantDomain)) {
            return false;
        }
        if (!applicationCacheInboundAuthKey.equals(that.applicationCacheInboundAuthKey)) {
            return false;
        }
        return applicationCacheInboundAuthType.equals(that.applicationCacheInboundAuthType);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + applicationCacheInboundAuthType.hashCode();
        result = 31 * result + applicationCacheInboundAuthKey.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return applicationCacheInboundAuthKey + " : " + applicationCacheInboundAuthType + " : " + tenantDomain;
    }
}
