/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.idp.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.CacheKey;

public class IdPAuthPropertyCacheKey extends CacheKey {

    private static final long serialVersionUID = 5800275605577468290L;

    private String name;
    private String value;

    public IdPAuthPropertyCacheKey(String name, String value, String tenantDomain) {
        this.name = name;
        this.value = value;
        this.tenantDomain = tenantDomain.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
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

        IdPAuthPropertyCacheKey that = (IdPAuthPropertyCacheKey) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!tenantDomain.equals(that.tenantDomain)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
