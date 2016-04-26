/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.count.cache;

//ToDo extend proper cache key class or define cache relevant methods
public class CountRetrieverCacheKey {

    private static final long serialVersionUID = -1414485745666304223L;

    private String countRetrieverKey;
    private int tenantDomain;

    /**
     * @param provisioningConnectorKey
     * @param tenantDomain
     */
    public CountRetrieverCacheKey(String provisioningConnectorKey, int tenantDomain) {
        this.countRetrieverKey = provisioningConnectorKey;
        this.tenantDomain = tenantDomain;
    }

    /**
     * @return
     */
    public String getCountRetrieverKey() {
        return countRetrieverKey;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CountRetrieverCacheKey that = (CountRetrieverCacheKey) o;

        if (!countRetrieverKey.equals(that.countRetrieverKey)) return false;
        if (tenantDomain != that.tenantDomain) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + countRetrieverKey.hashCode();
        result = 31 * result + tenantDomain;
        return result;
    }
}
