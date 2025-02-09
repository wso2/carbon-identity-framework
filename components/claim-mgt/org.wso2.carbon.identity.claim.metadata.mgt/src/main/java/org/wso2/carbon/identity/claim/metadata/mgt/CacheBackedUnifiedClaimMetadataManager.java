/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.AssociatedClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ClaimDialectCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheKey;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CacheBackedUnifiedClaimMetadataManager extends UnifiedClaimMetadataManager {

    private static final Log log = LogFactory.getLog(CacheBackedUnifiedClaimMetadataManager.class);

    ClaimDialectCache claimDialectCache = ClaimDialectCache.getInstance();
    LocalClaimCache localClaimCache = LocalClaimCache.getInstance();
    ExternalClaimCache externalClaimCache = ExternalClaimCache.getInstance();
    AssociatedClaimCache associatedClaimCache = AssociatedClaimCache.getInstance();

    @Override
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialectList = claimDialectCache.getClaimDialects(tenantId);
        if (claimDialectList != null && !claimDialectList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for claim dialect list for tenant: " + tenantId + ". Claim dialect list size: "
                        + claimDialectList.size());
            }
            return claimDialectList;
        }

        claimDialectList = super.getClaimDialects(tenantId);
        claimDialectCache.putClaimDialects(tenantId, claimDialectList);

        if (log.isDebugEnabled()) {
            log.debug("Cache miss for claim dialect list for tenant: " + tenantId + ". Updated cache with claim " +
                    "dialect list. Claim dialect list size: " + claimDialectList.size());
        }
        return claimDialectList;
    }

    @Override
    public Optional<ClaimDialect> getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        return getClaimDialects(tenantId).stream()
                .filter(claimDialect -> claimDialectURI.equals(claimDialect.getClaimDialectURI()))
                .findFirst();
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        super.addClaimDialect(claimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + claimDialect.getClaimDialectURI() + " is added for tenant: " + tenantId +
                    ". Invalidated ClaimDialectCache.");
        }
    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        super.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        removeExternalClaimCache(oldClaimDialect.getClaimDialectURI(), tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + oldClaimDialect.getClaimDialectURI() + " is renamed to new claim dialect: "
                    + newClaimDialect.getClaimDialectURI() + " for tenant: " + tenantId + ". Invalidated " +
                    "ClaimDialectCache." );
        }

    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        super.removeClaimDialect(claimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        removeExternalClaimCache(claimDialect.getClaimDialectURI(), tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + claimDialect.getClaimDialectURI() + " is removed for tenant: " + tenantId +
                    ". Invalidated ClaimDialectCache.");
        }
    }

    @Override
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimList = localClaimCache.getValueFromCache(tenantId, tenantId);

        if (localClaimList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for local claim list for tenant: " + tenantId);
            }
            localClaimList = super.getLocalClaims(tenantId);
            localClaimCache.addToCache(tenantId, new ArrayList<>(localClaimList), tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for local claim list for tenant: " + tenantId);
            }
        }

        return localClaimList;
    }

    @Override
    public Optional<LocalClaim> getLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return getLocalClaims(tenantId).stream()
                .filter(localClaim -> localClaimURI.equals(localClaim.getClaimURI()))
                .findFirst();
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        super.addLocalClaim(localClaim, tenantId);
        localClaimCache.clearCacheEntry(tenantId, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Local claim: " + localClaim.getClaimURI() + " is added for tenant: " + tenantId +
                    ". Invalidated LocalClaimCache.");
        }
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        super.updateLocalClaim(localClaim, tenantId);
        localClaimCache.clearCacheEntry(tenantId, tenantId);
        associatedClaimCache.clearCacheEntry(localClaim.getClaimURI(), tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Local claim: " + localClaim.getClaimURI() + " is updated in tenant: " + tenantId +
                    ". Invalidated LocalClaimCache.");
        }
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        super.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);
        localClaimCache.clearCacheEntry(tenantId, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim mappings for user-store domain: " + userStoreDomain + " is updated in tenant: " +
                    tenantId + ". Invalidated LocalClaimCache.");
        }
    }

    @Override
    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        super.removeLocalClaim(localClaimURI, tenantId);
        localClaimCache.clearCacheEntry(tenantId, tenantId);
        associatedClaimCache.clearCacheEntry(localClaimURI, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Local claim: " + localClaimURI + " is deleted in tenant: " + tenantId +
                    ". Invalidated LocalClaimCache.");
        }
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        List<ExternalClaim> externalClaimList = externalClaimCache.getValueFromCache(cacheKey, tenantId);

        if (externalClaimList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for external claim list for dialect: " + externalClaimDialectURI + " in tenant: " +
                        tenantId);
            }
            externalClaimList = super.getExternalClaims(externalClaimDialectURI, tenantId);
            externalClaimCache.addToCache(cacheKey, new ArrayList<>(externalClaimList), tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for external claim list for dialect: " + externalClaimDialectURI + " in tenant: " +
                        tenantId);
            }
        }

        return externalClaimList;
    }

    @Override
    public Optional<ExternalClaim> getExternalClaim(String externalClaimDialectURI, String externalClaimURI,
                                                    int tenantId) throws ClaimMetadataException {

        return getExternalClaims(externalClaimDialectURI, tenantId).stream()
                .filter(externalClaim -> externalClaimURI.equals(externalClaim.getClaimURI()))
                .findFirst();
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        super.addExternalClaim(externalClaim, tenantId);
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaim.getClaimDialectURI());
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        associatedClaimCache.clearCacheEntry(externalClaim.getMappedLocalClaim(), tenantId);
        if (log.isDebugEnabled()) {
            log.debug("External claim: " + externalClaim.getClaimDialectURI() + ":" + externalClaim.getClaimURI() +
                    " is added for tenant: " + tenantId + ". Invalidated ExternalClaimCache.");
        }
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        super.updateExternalClaim(externalClaim, tenantId);
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaim.getClaimDialectURI());
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        associatedClaimCache.clearCacheEntry(externalClaim.getMappedLocalClaim(), tenantId);
        if (log.isDebugEnabled()) {
            log.debug("External claim: " + externalClaim.getClaimDialectURI() + ":" + externalClaim.getClaimURI() +
                    " is updated in tenant: " + tenantId + ". Invalidated ExternalClaimCache.");
        }
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsList = getExternalClaims(externalClaimDialectURI, tenantId);
        String mappedLocalClaim = null;
        if (externalClaimsList != null) {
            for (ExternalClaim externalClaim : externalClaimsList) {
                if (externalClaim.getClaimURI().equals(externalClaimURI)) {
                    mappedLocalClaim = externalClaim.getMappedLocalClaim();
                }
            }
        }
        super.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        ExternalClaimCacheKey cacheKey = new ExternalClaimCacheKey(externalClaimDialectURI);
        externalClaimCache.clearCacheEntry(cacheKey, tenantId);
        if (StringUtils.isNotBlank(mappedLocalClaim)) {
            associatedClaimCache.clearCacheEntry(mappedLocalClaim, tenantId);
        }
        if (log.isDebugEnabled()) {
            log.debug("External claim: " + externalClaimDialectURI + ":" + externalClaimURI +
                    " is deleted in tenant: " + tenantId + ". Invalidated ExternalClaimCache.");
        }
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        super.removeClaimMappingAttributes(tenantId, userstoreDomain);
        localClaimCache.clearCacheEntry(tenantId, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim mappings for user-store domain: " + userstoreDomain + " is removed in tenant: " +
                    tenantId + ". Invalidated LocalClaimCache.");
        }
    }

    @Override
    public void removeAllClaimDialects(int tenantId) throws ClaimMetadataException {

        super.removeAllClaimDialects(tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        localClaimCache.clear(tenantId);
        externalClaimCache.clear(tenantId);
        associatedClaimCache.clear(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("All claim dialects are removed for tenant: " + tenantId +
                    ". Invalidated ClaimDialectCache, LocalClaimCache and ExternalClaimCache.");
        }
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        List<Claim> associatedLocalClaims = associatedClaimCache.getValueFromCache(localClaimURI, tenantId);
        if (associatedLocalClaims == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache miss for associated claims of local claim:%s in tenant:%s ",
                        localClaimURI, tenantId));
            }
            associatedLocalClaims = super.getMappedExternalClaims(localClaimURI, tenantId);
            associatedClaimCache.addToCache(localClaimURI, new ArrayList<>(associatedLocalClaims), tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache hit for associated claims of local claim:%s in tenant:%s ",
                        localClaimURI, tenantId));
            }
        }
        return associatedLocalClaims;
    }

    private void removeExternalClaimCache(String externalClaimDialectURI, int tenantId) throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsList;
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
