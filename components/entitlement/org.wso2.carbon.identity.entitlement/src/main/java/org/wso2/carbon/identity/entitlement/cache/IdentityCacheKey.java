/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.cache;

import java.io.Serializable;

/**
 * Identity Cache key which wraps the identity related cache key values
 */
public class IdentityCacheKey implements Serializable {

    private static final long serialVersionUID = 3413834923591132863L;

    private int tenantId;
    private String key;

    public IdentityCacheKey(int tenantId, String key) {
        this.tenantId = tenantId;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getTenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityCacheKey)) return false;

        IdentityCacheKey that = (IdentityCacheKey) o;

        if (tenantId != that.tenantId) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tenantId;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
