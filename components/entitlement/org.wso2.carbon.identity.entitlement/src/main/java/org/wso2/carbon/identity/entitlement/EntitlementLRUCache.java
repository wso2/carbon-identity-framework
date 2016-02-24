/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a simple LRU cache, based on <code>LinkedHashMap</code>. If the cache is full and another
 * entry is added, the least recently used entry is dropped.
 */
public class EntitlementLRUCache<String, Set> extends LinkedHashMap<String, Set> {

    private static final long serialVersionUID = -1308554805704597171L;
    private final static int INITIAL_CACHE_CAPACITY = 16;
    private final static float LOAD_FACTOR = 75f;
    private int cacheSize;

    public EntitlementLRUCache(int cacheSize) {
        super(INITIAL_CACHE_CAPACITY, LOAD_FACTOR, true);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        // oldest entry of the cache would be removed when max cache size become
        return size() == this.cacheSize;
    }

}
