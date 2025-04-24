/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginByAppIdCache;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginByAppIdCacheEntry;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginByAppIdCacheKey;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginCache;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginCacheEntry;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSOriginCacheKey;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.util.Arrays;
import java.util.List;

/**
 * Cached DAO layer for CORS Origins. All the DAO accesses has to be happen through this layer to ensure
 * single point of caching.
 */
public class CacheBackedCORSOriginDAO extends CORSOriginDAOImpl {

    private static final Log log = LogFactory.getLog(CacheBackedCORSOriginDAO.class);

    private final CORSOriginDAO corsOriginDAO;

    public CacheBackedCORSOriginDAO(CORSOriginDAO corsOriginDAO) {

        this.corsOriginDAO = corsOriginDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getCORSOriginsByTenantId(int tenantId) throws CORSManagementServiceServerException {

        CORSOrigin[] cachedResult = getCORSOriginsFromCache(tenantId);
        if (cachedResult != null) {
            return Arrays.asList(cachedResult);
        }

        List<CORSOrigin> corsOrigins = corsOriginDAO.getCORSOriginsByTenantId(tenantId);
        addCORSOriginsToCache(corsOrigins.toArray(new CORSOrigin[0]), tenantId);
        return corsOrigins;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getCORSOriginsByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return getCORSOriginsByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getCORSOriginsByApplicationId(int applicationId, int tenantId)
            throws CORSManagementServiceServerException {

        CORSOrigin[] cachedResult = getCORSOriginsFromCache(applicationId, tenantId);
        if (cachedResult != null) {
            return Arrays.asList(cachedResult);
        }

        List<CORSOrigin> corsOrigins = corsOriginDAO.getCORSOriginsByApplicationId(applicationId, tenantId);
        addCORSOriginsToCache(corsOrigins.toArray(new CORSOrigin[0]), applicationId, tenantId);
        return corsOrigins;
    }

    /**
     * Set the CORS origins of an application. This will replace the existing CORS origin list of that application.
     *
     * @param applicationId The application ID.
     * @param corsOrigins   The CORS origins to be set, associated with the application.
     * @param tenantId      The tenant ID.
     * @throws CORSManagementServiceServerException
     */
    @Override
    public void setCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        clearCaches(applicationId, tenantId);
        corsOriginDAO.setCORSOrigins(applicationId, corsOrigins, tenantId);
    }

    /**
     * Add the CORS origins of an application. This will append the new origins to the existing CORS origin list
     * of that application.
     *
     * @param applicationId The application ID.
     * @param corsOrigins   The CORS origins to be add, associated with the application.
     * @param tenantId      The tenant ID.
     * @throws CORSManagementServiceServerException
     */
    @Override
    public void addCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        clearCaches(applicationId, tenantId);
        corsOriginDAO.addCORSOrigins(applicationId, corsOrigins, tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCORSOrigins(int applicationId, List<String> corsOriginIds, int tenantId)
            throws CORSManagementServiceServerException {

        clearCaches(applicationId, tenantId);
        corsOriginDAO.deleteCORSOrigins(applicationId, corsOriginIds, tenantId);
    }

    /**
     * Add CORS origins of a particular tenant to the cache.
     *
     * @param corsOrigins  The  origins that should be added to the cache.
     * @param tenantId The tenant domain specific to the cache entry.
     */
    private void addCORSOriginsToCache(CORSOrigin[] corsOrigins, int tenantId) {

        CORSOriginCacheKey cacheKey = new CORSOriginCacheKey(tenantId);
        CORSOriginCacheEntry cacheEntry = new CORSOriginCacheEntry(corsOrigins);

        if (log.isDebugEnabled()) {
            log.debug("Adding CORS origins to Cache with Key: " + tenantId);
        }

        CORSOriginCache.getInstance().addToCache(cacheKey, cacheEntry, tenantId);
    }

    /**
     * Add CORS origins of a particular application to the CORSOriginByAppId cache.
     *
     * @param corsOrigins The origins that should be added to the cache.
     * @param applicationId The id of the application.
     * @param tenantId The tenant domain specific to the cache entry.
     */
    private void addCORSOriginsToCache(CORSOrigin[] corsOrigins, int applicationId, int tenantId) {

        CORSOriginByAppIdCacheKey cacheKey = new CORSOriginByAppIdCacheKey(applicationId);
        CORSOriginByAppIdCacheEntry cacheEntry = new CORSOriginByAppIdCacheEntry(corsOrigins);
        if (log.isDebugEnabled()) {
            log.debug("Adding CORS origins to Cache for application id: " + applicationId +
                    ", tenant id: " + tenantId);
        }

        CORSOriginByAppIdCache.getInstance().addToCache(cacheKey, cacheEntry, tenantId);
    }

    /**
     * Get CORS origins from the cache.
     *
     * @param tenantId The tenant id specific to the cache entry.
     * @return Returns an array of {@code Origin}(s) if the cached origins are found for the tenant.
     * Else return {@code null}.
     */
    private CORSOrigin[] getCORSOriginsFromCache(int tenantId) {

        CORSOriginCacheKey cacheKey = new CORSOriginCacheKey(tenantId);
        CORSOriginCache cache = CORSOriginCache.getInstance();
        CORSOriginCacheEntry cacheEntry = cache.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null && cacheEntry.getValidatedOrigins() != null) {
            return cacheEntry.getValidatedOrigins();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for cache key:" + tenantId);
            }
            return null;
        }
    }

    /**
     * Get CORS origins from the cache by application id.
     *
     * @param applicationId The application id specific to the cache entry.
     * @param tenantId      The tenant id specific to the cache entry.
     * @return Returns an array of {@code Origin}(s) if the cached origins are found for the tenant.
     * Else return {@code null}.
     */
    private CORSOrigin[] getCORSOriginsFromCache(int applicationId, int tenantId) {

        CORSOriginByAppIdCacheKey cacheKey = new CORSOriginByAppIdCacheKey(applicationId);
        CORSOriginByAppIdCache cache = CORSOriginByAppIdCache.getInstance();
        CORSOriginByAppIdCacheEntry cacheEntry = cache.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null && cacheEntry.getValidatedOrigins() != null) {
            return cacheEntry.getValidatedOrigins();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for application id: " + applicationId + ", tenantId: " + tenantId);
            }
            return null;
        }
    }

    /**
     * Clear CORS origin caches of a particular application.
     *
     * @param applicationId The application id of the tenant.
     * @param tenantId      The id of the tenant.
     */
    private void clearCaches(int applicationId, int tenantId) {

        // Remove cache entry for the application.
        CORSOriginByAppIdCacheKey cacheKey = new CORSOriginByAppIdCacheKey(applicationId);
        CORSOriginByAppIdCache.getInstance().clearCacheEntry(cacheKey, tenantId);

        // Remove cache entry for the tenant.
        clearCaches(tenantId);
    }

    /**
     * Clear CORS origin caches of a particular tenant.
     *
     * @param tenantId The Id of the tenant.
     */
    private void clearCaches(int tenantId) {

        CORSOriginCacheKey cacheKey = new CORSOriginCacheKey(tenantId);
        CORSOriginCache.getInstance().clearCacheEntry(cacheKey, tenantId);
    }
}
