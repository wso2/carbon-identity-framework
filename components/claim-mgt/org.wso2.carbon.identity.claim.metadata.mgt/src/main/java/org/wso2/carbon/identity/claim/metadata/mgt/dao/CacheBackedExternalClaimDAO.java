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
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheEntry;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;

import java.util.List;

/**
 *
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO
 *
 */
public class CacheBackedExternalClaimDAO {

    private static Log log = LogFactory.getLog(CacheBackedExternalClaimDAO.class);

    ExternalClaimDAO externalClaimDAO;
    ExternalClaimCache externalClaimCache = ExternalClaimCache.getInstance();

    public CacheBackedExternalClaimDAO(ExternalClaimDAO externalClaimDAO) {
        this.externalClaimDAO = externalClaimDAO;
    }


    public List<ExternalClaim> getExternalClaims(String externalDialectURI, int tenantId) throws
            ClaimMetadataException {

        List<ExternalClaim> externalClaims;

        ExternalClaimCacheEntry externalClaimCacheEntry = externalClaimCache.getValueFromCache(externalDialectURI);

        if (externalClaimCacheEntry != null) {

            if (log.isDebugEnabled()) {
                log.debug("Received claim list from the cache for dialect: " + externalDialectURI);
            }
            externalClaims =  externalClaimCacheEntry.getExternalClaimList();

        } else {

            externalClaims = externalClaimDAO.getExternalClaims(externalDialectURI, tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Updating claim list on the cache for dialect: " + externalDialectURI);
            }
            externalClaimCache.addToCache(externalDialectURI, new ExternalClaimCacheEntry(externalClaims));

        }

        return externalClaims;
    }

    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
        clearExternalClaimCache(externalClaim.getClaimDialectURI());
    }

    public void updateLocalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        externalClaimDAO.updateLocalClaim(externalClaim, tenantId);
        clearExternalClaimCache(externalClaim.getClaimDialectURI());
    }


    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId) throws
            ClaimMetadataException {

        externalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        clearExternalClaimCache(externalClaimDialectURI);
    }

    public boolean isMappedLocalClaim(String mappedLocalClaimURI, int tenantId) throws
            ClaimMetadataException {

        //Need different type of cache
        return externalClaimDAO.isMappedLocalClaim(mappedLocalClaimURI, tenantId);
    }

    private void clearExternalClaimCache(String claimDialectURI) {

        if (log.isDebugEnabled()) {
            log.debug("Clearing claim list on the cache for dialect: " + claimDialectURI);
        }
        externalClaimCache.clearCacheEntry(claimDialectURI);
    }

}
