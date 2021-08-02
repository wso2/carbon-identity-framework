/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key used to access Applications basic information.
 *
 */
public class ApplicationBasicInfoNameCacheKey extends CacheKey {

    private static final long serialVersionUID = 5373336682513425899L;
    private final String name;

    public ApplicationBasicInfoNameCacheKey(String name) {

        this.name = name;
    }

    public String getResourceId() {

        return name;
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

        ApplicationBasicInfoNameCacheKey that = (ApplicationBasicInfoNameCacheKey) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
