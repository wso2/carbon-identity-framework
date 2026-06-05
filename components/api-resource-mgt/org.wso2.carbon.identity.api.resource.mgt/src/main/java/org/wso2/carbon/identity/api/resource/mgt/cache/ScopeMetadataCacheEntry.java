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

package org.wso2.carbon.identity.api.resource.mgt.cache;

import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache entry holding the tenant scope metadata list (all scopes of a tenant, unfiltered).
 */
public class ScopeMetadataCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 4476546755970558142L;

    private List<Scope> scopes;

    public ScopeMetadataCacheEntry(List<Scope> scopes) {

        this.scopes = new ArrayList<>(scopes);
    }

    public List<Scope> getScopes() {

        // Return a copy so callers cannot mutate the cached list.
        return new ArrayList<>(scopes);
    }

    public void setScopes(List<Scope> scopes) {

        this.scopes = new ArrayList<>(scopes);
    }
}
