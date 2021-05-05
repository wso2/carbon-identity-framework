/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class ApplicationBasicInfoResourceIdCacheKey extends CacheKey {

    private static final long serialVersionUID = -731159352431745665L;
    private final String resourceId;

    public ApplicationBasicInfoResourceIdCacheKey(String resourceId) {

        this.resourceId = resourceId;
    }

    public String getResourceId() {

        return resourceId;
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

        ApplicationBasicInfoResourceIdCacheKey that = (ApplicationBasicInfoResourceIdCacheKey) o;
        return resourceId.equalsIgnoreCase(that.resourceId);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + resourceId.hashCode();
        return result;
    }
}
