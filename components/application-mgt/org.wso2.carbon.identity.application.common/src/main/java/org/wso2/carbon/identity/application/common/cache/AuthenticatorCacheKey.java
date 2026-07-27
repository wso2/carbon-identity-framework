/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for the user defined local application authenticator configurations.
 */
public class AuthenticatorCacheKey extends CacheKey {

    private static final long serialVersionUID = -2897123859023483921L;

    private final String authenticatorName;

    public AuthenticatorCacheKey(String authenticatorName) {

        this.authenticatorName = authenticatorName;
    }

    public String getAuthenticatorName() {

        return authenticatorName;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof AuthenticatorCacheKey)) {
            return false;
        }
        return authenticatorName.equals(((AuthenticatorCacheKey) o).getAuthenticatorName());
    }

    @Override
    public int hashCode() {

        return authenticatorName.hashCode();
    }
}
