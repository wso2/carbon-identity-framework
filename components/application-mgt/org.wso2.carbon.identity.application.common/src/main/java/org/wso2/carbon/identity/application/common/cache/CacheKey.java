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

package org.wso2.carbon.identity.application.common.cache;

import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.Serializable;

/**
 * Cache key class. Any value that acts as a cache key must encapsulated with a class overriding
 * from this class.
 */
public abstract class CacheKey implements Serializable {

    private static final long serialVersionUID = 659646764466611338L;

    protected String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CacheKey key = (CacheKey) o;

        if (tenantDomain != null && !tenantDomain.equals(key.tenantDomain)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (tenantDomain != null) {
            return tenantDomain.hashCode();
        } else {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.hashCode();
        }
    }

    public String getTenantDomain() {
        return tenantDomain;
    }
}
