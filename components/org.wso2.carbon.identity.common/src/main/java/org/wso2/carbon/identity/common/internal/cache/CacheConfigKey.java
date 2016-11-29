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
package org.wso2.carbon.identity.common.internal.cache;

/**
 * Cache configuration key.
 */
public class CacheConfigKey {

    private String cacheManagerName;
    private String cacheName;

    public CacheConfigKey(String cacheManagerName, String cacheName) {
        this.cacheManagerName = cacheManagerName;
        this.cacheName = cacheName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheConfigKey that = (CacheConfigKey) o;

        if (!cacheManagerName.equals(that.cacheManagerName)) {
            return false;
        }
        return cacheName.equals(that.cacheName);

    }

    @Override
    public int hashCode() {
        int result = cacheManagerName.hashCode();
        result = 31 * result + cacheName.hashCode();
        return result;
    }
}
