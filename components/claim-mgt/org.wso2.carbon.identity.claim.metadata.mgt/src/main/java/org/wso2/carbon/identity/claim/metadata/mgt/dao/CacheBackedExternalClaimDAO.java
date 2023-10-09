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
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheKey;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Caching wrapper for org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO
 *
 */
public class CacheBackedExternalClaimDAO {

    private static final Log log = LogFactory.getLog(CacheBackedExternalClaimDAO.class);

    ExternalClaimDAO externalClaimDAO;
    ExternalClaimCache externalClaimCache = ExternalClaimCache.getInstance();
    AssociatedClaimCache associatedClaimCache = AssociatedClaimCache.getInstance();

    public CacheBackedExternalClaimDAO(ExternalClaimDAO externalClaimDAO) {
        this.externalClaimDAO = externalClaimDAO;
    }


    public List<ExternalClaim> getExternalClaims(String externalDialectURI, int tenantId) throws
            ClaimMetadataException {

        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalDialectURI);
        List<ExternalClaim> externalClaimList = externalClaimCache.getValueFromCache(cacheKey, tenantId);

        if (externalClaimList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for external claim list for dialect: " + externalDialectURI + " in tenant: " +
                        tenantId);
            }
            externalClaimList = externalClaimDAO.getExternalClaims(externalDialectURI, tenantId);
            externalClaimCache.addToCache(cacheKey, new ArrayList<>(externalClaimList), tenantId);
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
        String externalClaimDialectURI = externalClaim.getClaimDialectURI();
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        associatedClaimCache.clearCacheEntry(externalClaim.getMappedLocalClaim(), tenantId);
    }
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        externalClaimDAO.updateExternalClaim(externalClaim, tenantId);
        String externalClaimDialectURI = externalClaim.getClaimDialectURI();
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        associatedClaimCache.clearCacheEntry(externalClaim.getMappedLocalClaim(), tenantId);
    }
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId) throws
            ClaimMetadataException {

        List<ExternalClaim> externalClaimsList = getExternalClaims(externalClaimDialectURI, tenantId);
        String mappedLocalClaim = null;
        if (externalClaimsList != null) {
            for (ExternalClaim externalClaim : externalClaimsList) {
                if (externalClaim.getClaimURI().equals(externalClaimURI)) {
                    mappedLocalClaim = externalClaim.getMappedLocalClaim();
                }
            }
        }
        externalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        if (StringUtils.isNotBlank(mappedLocalClaim)) {
            associatedClaimCache.clearCacheEntry(mappedLocalClaim, tenantId);
        }
    }

    public boolean isMappedLocalClaim(String mappedLocalClaimURI, int tenantId) throws
            ClaimMetadataException {

        //Need different type of cache
        return externalClaimDAO.isMappedLocalClaim(mappedLocalClaimURI, tenantId);
    }

    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaimURI, String externalClaimDialectURI,
                                                   int tenantId) throws ClaimMetadataException {

        return externalClaimDAO.isLocalClaimMappedWithinDialect(mappedLocalClaimURI, externalClaimDialectURI, tenantId);
    }

    /**
     * Remove mapped external claims at post removing claim dialect.
     *
     * @param externalClaimDialectURI External claim dialect uri
     * @param tenantId                Tenant Id
     */
    public void removeExternalClaimCache(String externalClaimDialectURI, int tenantId) throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsList = null;
        List<String> mappedLocalClaim = new ArrayList<>();
        externalClaimsList = getExternalClaims(externalClaimDialectURI, tenantId);
        if (externalClaimsList != null) {
            for (ExternalClaim externalClaim : externalClaimsList) {
                mappedLocalClaim.add(externalClaim.getMappedLocalClaim());
            }
        }
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        for (String localClaim : mappedLocalClaim) {
            associatedClaimCache.clearCacheEntry(localClaim, tenantId);
        }
    }
}
