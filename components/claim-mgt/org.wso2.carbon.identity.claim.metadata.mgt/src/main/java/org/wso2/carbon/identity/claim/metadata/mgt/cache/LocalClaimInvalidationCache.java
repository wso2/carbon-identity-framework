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

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Invalidation Cache implementation for LocalClaim
 */
public class LocalClaimInvalidationCache {

    private static Log log = LogFactory.getLog(LocalClaimInvalidationCache.class);

    private static final String CACHE_NAME = "LocalClaimInvalidationCache";
    private static final BaseCache<Integer, String> invalidationCache = new BaseCache<>(CACHE_NAME);

    private static final LocalClaimInvalidationCache instance = new LocalClaimInvalidationCache();
    private Map<Integer, List<LocalClaim>> localClaims = new HashMap<>();
    private String localUUID = UUID.randomUUID().toString();

    private LocalClaimInvalidationCache() {
    }

    public static LocalClaimInvalidationCache getInstance() {
        return instance;
    }

    public boolean isInvalid(int tenantId) {

        String shareUUID = invalidationCache.getValueFromCache(tenantId);
        if (shareUUID != null && !localUUID.equals(shareUUID)) {
            if (log.isDebugEnabled()) {
                log.debug("isInvalid: true for local UUID: " + localUUID + ", wrt shared UUID: " + shareUUID +
                        " for tenant: " + tenantId);
            }
            localUUID = shareUUID;
            return true;
        }
        return false;
    }


    public void invalidate(int tenantId) {

        String newUUID = UUID.randomUUID().toString();

        if (log.isDebugEnabled()) {
            log.debug("Invalidating local UUID: " + localUUID + ", with new UUID: " + newUUID + " for tenant: " +
                    tenantId);
        }

        localUUID = newUUID;
        invalidationCache.addToCache(tenantId, localUUID);
    }

    public List<LocalClaim> getLocalClaims(int tenantId) {
        return localClaims.get(tenantId);
    }

    public void setLocalClaims(int tenantId, List<LocalClaim> localClaims) {
        this.localClaims.put(tenantId, localClaims);
    }
}
