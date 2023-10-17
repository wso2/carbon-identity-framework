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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.AssociatedClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO
 */
public class CacheBackedLocalClaimDAO {

    private static final Log log = LogFactory.getLog(CacheBackedLocalClaimDAO.class);

    LocalClaimDAO localClaimDAO;

    LocalClaimCache localClaimInvalidationCache = LocalClaimCache.getInstance();
    AssociatedClaimCache associatedClaimCache = AssociatedClaimCache.getInstance();


    public CacheBackedLocalClaimDAO(LocalClaimDAO localClaimDAO) {
        this.localClaimDAO = localClaimDAO;
    }


    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimList = localClaimInvalidationCache.getValueFromCache(tenantId, tenantId);

        if (localClaimList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for local claim list for tenant: " + tenantId);
            }
            localClaimList = localClaimDAO.getLocalClaims(tenantId);
            localClaimInvalidationCache.addToCache(tenantId, new ArrayList<>(localClaimList), tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for local claim list for tenant: " + tenantId);
            }
        }

        return localClaimList;
    }

    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.addLocalClaim(localClaim, tenantId);
        localClaimInvalidationCache.clearCacheEntry(tenantId, tenantId);
    }

    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        localClaimDAO.updateLocalClaim(localClaim, tenantId);
        localClaimInvalidationCache.clearCacheEntry(tenantId, tenantId);
        associatedClaimCache.clearCacheEntry(localClaim.getClaimURI(), tenantId);
    }

    /**
     * Update attribute claim mappings related to tenant id and domain in bulk.
     *
     * @param localClaimList  List of local claims.
     * @param tenantId        Tenant Id.
     * @param userStoreDomain Domain name.
     * @throws ClaimMetadataException If an error occurred while updating local claims.
     */
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        localClaimDAO.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);
        localClaimInvalidationCache.clearCacheEntry(tenantId, tenantId);
    }

    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        localClaimDAO.removeLocalClaim(localClaimURI, tenantId);
        localClaimInvalidationCache.clearCacheEntry(tenantId, tenantId);
        associatedClaimCache.clearCacheEntry(localClaimURI, tenantId);
    }

    /**
     * Remove attribute claim mappings related to tenant id and domain.
     *
     * @param tenantId        Tenant Id
     * @param userstoreDomain Domain name
     * @throws UserStoreException If an error occurred while removing local claims
     */
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws UserStoreException {

        if (StringUtils.isEmpty(userstoreDomain)) {
            String message = ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_TENANT_DOMAIN.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new UserStoreException(message);
        }
        localClaimDAO.deleteClaimMappingAttributes(tenantId, userstoreDomain);
        localClaimInvalidationCache.clearCacheEntry(tenantId, tenantId);
    }

    /**
     * Fetch mapped external claims of a local claim.
     *
     * @param tenantId      Tenant Id.
     * @param localClaimURI URI of local claim.
     * @throws ClaimMetadataException If an error occurred while getting mapped external claim for local claim.
     */
    public List<Claim> fetchMappedExternalClaims(String localClaimURI, int tenantId)
            throws ClaimMetadataException {

        List<Claim> associatedLocalClaims = associatedClaimCache.getValueFromCache(localClaimURI, tenantId);
        if (associatedLocalClaims == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache miss for associated claims of local claim:%s in tenant:%s ",
                        localClaimURI, tenantId));
            }
            associatedLocalClaims = localClaimDAO.fetchMappedExternalClaims(localClaimURI, tenantId);
            associatedClaimCache.addToCache(localClaimURI, new ArrayList<>(associatedLocalClaims), tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache hit for associated claims of local claim:%s in tenant:%s ",
                        localClaimURI, tenantId));
            }
        }
        return associatedLocalClaims;
    }
}
