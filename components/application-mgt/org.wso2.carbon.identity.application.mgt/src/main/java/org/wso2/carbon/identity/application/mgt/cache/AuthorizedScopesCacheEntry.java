/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import java.util.List;

/**
 * Cache entry for authorized scopes of an application.
 */
public class AuthorizedScopesCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 6182846342958962501L;

    private List<AuthorizedScopes> authorizedScopes;

    public AuthorizedScopesCacheEntry(List<AuthorizedScopes> authorizedScopes) {

        this.authorizedScopes = authorizedScopes;
    }

    public List<AuthorizedScopes> getAuthorizedScopes() {

        return authorizedScopes;
    }

    public void setAuthorizedScopes(List<AuthorizedScopes> authorizedScopes) {

        this.authorizedScopes = authorizedScopes;
    }
}
