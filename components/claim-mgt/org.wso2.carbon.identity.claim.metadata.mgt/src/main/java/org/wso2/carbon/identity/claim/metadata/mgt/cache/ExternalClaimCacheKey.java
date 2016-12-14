/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import java.io.Serializable;

/**
 * Cache key for ExternalClaimCache
 */
public class ExternalClaimCacheKey implements Serializable {

    private static final long serialVersionUID = 1504522344376716137L;
    private String externalDialectURI;
    private Integer tenantId;

    public ExternalClaimCacheKey(String externalDialectURI, int tenantId) {
        this.externalDialectURI = externalDialectURI;
        this.tenantId = tenantId;
    }

    public String getExternalDialectURI() {
        return externalDialectURI;
    }

    public void setExternalDialectURI(String externalDialectURI) {
        this.externalDialectURI = externalDialectURI;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalClaimCacheKey that = (ExternalClaimCacheKey) o;

        if (!externalDialectURI.equals(that.externalDialectURI)) {
            return false;
        }

        if (!tenantId.equals(that.tenantId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + externalDialectURI.hashCode();
        result = 31 * result + tenantId.hashCode();
        return result;
    }
}
