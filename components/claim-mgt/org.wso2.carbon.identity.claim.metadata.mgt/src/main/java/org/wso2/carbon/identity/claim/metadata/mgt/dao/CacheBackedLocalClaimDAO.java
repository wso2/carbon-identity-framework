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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimCacheEntry;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;

/**
 *
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO
 *
 */
public class CacheBackedLocalClaimDAO {

    private static Log log = LogFactory.getLog(CacheBackedLocalClaimDAO.class);

    LocalClaimDAO localClaimDAO;
    LocalClaimCache localClaimCache = LocalClaimCache.getInstance();

    public CacheBackedLocalClaimDAO(LocalClaimDAO localClaimDAO) {
        this.localClaimDAO = localClaimDAO;
    }


    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaims;

        LocalClaimCacheEntry localClaimCacheEntry = localClaimCache.getValueFromCache();

        if (localClaimCacheEntry != null) {

            if (log.isDebugEnabled()) {
                log.debug("Received local claim list from the cache");
            }
            localClaims = localClaimCacheEntry.getLocalClaimList();

        } else {

            localClaims = localClaimDAO.getLocalClaims(tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Updating local claim list on the cache");
            }
            localClaimCache.addToCache(new LocalClaimCacheEntry(localClaims));
        }

        return localClaims;
    }

    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.addLocalClaim(localClaim, tenantId);
        clearExternalClaimCache();
    }

    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.updateLocalClaim(localClaim, tenantId);
        clearExternalClaimCache();
    }

    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        localClaimDAO.removeLocalClaim(localClaimURI, tenantId);
        clearExternalClaimCache();
    }

    private void clearExternalClaimCache() {

        if (log.isDebugEnabled()) {
            log.debug("Clearing claim list on the cache");
        }
        localClaimCache.clearCacheEntry();
    }
}
