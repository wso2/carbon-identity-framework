/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.cors.mgt.core.internal.cache;

import java.io.Serializable;

/**
 * CORS origin container cache key.
 */
public class CORSOriginByAppIdCacheKey implements Serializable {

    private static final long serialVersionUID = 6298593317563873934L;

    private final int applicationId;
    private final int tenantId;

    public CORSOriginByAppIdCacheKey(int applicationId, int tenantId) {

        this.applicationId = applicationId;
        this.tenantId = tenantId;
    }

    public int getApplicationId() {

        return applicationId;
    }

    public int getTenantId() {

        return tenantId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CORSOriginByAppIdCacheKey)) {
            return false;
        }

        CORSOriginByAppIdCacheKey that = (CORSOriginByAppIdCacheKey) o;
        return tenantId == that.tenantId && applicationId == that.applicationId;
    }

    @Override
    public int hashCode() {

        return 31 * applicationId + tenantId;
    }
}
