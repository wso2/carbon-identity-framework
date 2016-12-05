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
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimInvalidationCache;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO
 *
 */
public class CacheBackedLocalClaimDAO {

    private static Log log = LogFactory.getLog(CacheBackedLocalClaimDAO.class);

    LocalClaimDAO localClaimDAO;

    LocalClaimInvalidationCache localClaimInvalidationCache = LocalClaimInvalidationCache.getInstance();

    public CacheBackedLocalClaimDAO(LocalClaimDAO localClaimDAO) {
        this.localClaimDAO = localClaimDAO;
    }


    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimList = localClaimInvalidationCache.getLocalClaims(tenantId);

        if (localClaimList == null || localClaimInvalidationCache.isInvalid(tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for local claim list for tenant: " + tenantId);
            }
            localClaimList = localClaimDAO.getLocalClaims(tenantId);
            localClaimInvalidationCache.setLocalClaims(tenantId, localClaimList);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for local claim list for tenant: " + tenantId);
            }
        }

        return localClaimList;
    }

    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.addLocalClaim(localClaim, tenantId);
        invalidate(tenantId);
    }

    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.updateLocalClaim(localClaim, tenantId);
        invalidate(tenantId);
    }

    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        localClaimDAO.removeLocalClaim(localClaimURI, tenantId);
        invalidate(tenantId);
    }

    private void invalidate(int tenantId) throws ClaimMetadataException {

        if (log.isDebugEnabled()) {
            log.debug("Updating local claim list for tenant: " + tenantId);
        }

        List<LocalClaim> localClaimList = localClaimDAO.getLocalClaims(tenantId);
        localClaimInvalidationCache.setLocalClaims(tenantId, localClaimList);
        localClaimInvalidationCache.invalidate(tenantId);
    }
}
