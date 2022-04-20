/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationError;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Key to cache {@link AuthenticationError}
 */
public class AuthenticationErrorCacheKey extends CacheEntry {

    private final String errorKey;

    public AuthenticationErrorCacheKey(String contextId) {

        this.errorKey = contextId;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
