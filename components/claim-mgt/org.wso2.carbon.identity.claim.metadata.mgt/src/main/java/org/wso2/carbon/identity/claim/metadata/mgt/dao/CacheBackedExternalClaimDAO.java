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
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimInvalidationCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheKey;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO
 *
 */
public class CacheBackedExternalClaimDAO {

    private static Log log = LogFactory.getLog(CacheBackedExternalClaimDAO.class);

    ExternalClaimDAO externalClaimDAO;

    ExternalClaimInvalidationCache externalClaimInvalidationCache = ExternalClaimInvalidationCache.getInstance();

    public CacheBackedExternalClaimDAO(ExternalClaimDAO externalClaimDAO) {
        this.externalClaimDAO = externalClaimDAO;
    }


    public List<ExternalClaim> getExternalClaims(String externalDialectURI, int tenantId) throws
            ClaimMetadataException {

        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalDialectURI, tenantId);
        List<ExternalClaim> externalClaimList = externalClaimInvalidationCache.getExternalClaims(cacheKey);

        if (externalClaimList == null || externalClaimInvalidationCache.isInvalid(externalDialectURI, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for external claim list for dialect: " + externalDialectURI + " in tenant: " +
                        tenantId);
            }
            externalClaimList = externalClaimDAO.getExternalClaims(externalDialectURI, tenantId);
            externalClaimInvalidationCache.setExternalClaims(cacheKey, externalClaimList);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for external claim list for dialect: " + externalDialectURI + " in tenant: " +
                        tenantId);
            }
        }

        return externalClaimList;
    }

    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
        invalidate(externalClaim.getClaimDialectURI(), tenantId);
    }

    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        externalClaimDAO.updateExternalClaim(externalClaim, tenantId);
        invalidate(externalClaim.getClaimDialectURI(), tenantId);
    }


    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId) throws
            ClaimMetadataException {

        externalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        invalidate(externalClaimDialectURI, tenantId);
    }

    public boolean isMappedLocalClaim(String mappedLocalClaimURI, int tenantId) throws
            ClaimMetadataException {

        //Need different type of cache
        return externalClaimDAO.isMappedLocalClaim(mappedLocalClaimURI, tenantId);
    }

    private void invalidate(String externalDialectURI, int tenantId) throws ClaimMetadataException {

        if (log.isDebugEnabled()) {
            log.debug("Updating external claim list for dialect: " + externalDialectURI + " in tenant: " + tenantId);
        }

        List<ExternalClaim> localClaimList = externalClaimDAO.getExternalClaims(externalDialectURI, tenantId);
        externalClaimInvalidationCache.setExternalClaims(new ExternalClaimCacheKey(externalDialectURI, tenantId), localClaimList);
        externalClaimInvalidationCache.invalidate(externalDialectURI, tenantId);
    }

}
