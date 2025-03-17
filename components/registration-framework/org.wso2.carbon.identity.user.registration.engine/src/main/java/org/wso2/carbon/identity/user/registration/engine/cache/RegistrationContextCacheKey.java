/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for RegistrationContext.
 */
public class RegistrationContextCacheKey extends CacheKey {

    private static final long serialVersionUID = 3829856934531448120L;

    private final String contextId;

    public RegistrationContextCacheKey(String contextId) {

        this.contextId = contextId;
    }

    public String getContextId() {

        return contextId;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof RegistrationContextCacheKey)) {
            return false;
        }
        return contextId.equals(((RegistrationContextCacheKey) o).getContextId());
    }

    @Override
    public int hashCode() {

        return contextId.hashCode();
    }
}
