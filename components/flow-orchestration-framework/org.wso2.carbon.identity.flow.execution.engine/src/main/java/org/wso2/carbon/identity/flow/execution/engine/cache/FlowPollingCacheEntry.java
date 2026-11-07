/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.execution.engine.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache entry for a polling entry: its status and when it expires.
 */
public class FlowPollingCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 5126947381604937215L;

    private final String status;
    private final long expiresAt;

    /**
     * @param status    Status of the polling entry.
     * @param expiresAt Time the polling entry expires at, in milliseconds since the epoch.
     */
    public FlowPollingCacheEntry(String status, long expiresAt) {

        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getStatus() {

        return status;
    }

    public long getExpiresAt() {

        return expiresAt;
    }

    /**
     * Whether the polling entry has expired at the given time.
     *
     * @param now Time in milliseconds since the epoch.
     * @return True if the entry has expired.
     */
    public boolean isExpired(long now) {

        return now >= expiresAt;
    }
}
