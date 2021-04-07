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
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSConfigurationCache;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSConfigurationCacheEntry;
import org.wso2.carbon.identity.cors.mgt.core.internal.cache.CORSConfigurationCacheKey;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

/**
 * Cached DAO layer for CORS Configurations. All the DAO access has to be happen through this layer to ensure
 * single point of caching.
 */
public class CacheBackedCORSConfigurationDAO extends CORSConfigurationDAOImpl {

    private static final Log log = LogFactory.getLog(CacheBackedCORSConfigurationDAO.class);

    private final CORSConfigurationDAO corsConfigurationDAO;

    public CacheBackedCORSConfigurationDAO(CORSConfigurationDAO corsConfigurationDAO) {

        this.corsConfigurationDAO = corsConfigurationDAO;
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
    public CORSConfiguration getCORSConfigurationByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException {

        CORSConfiguration cachedResult = getCORSConfigurationFromCache(tenantDomain);
        if (cachedResult != null) {
            return cachedResult;
        }

        CORSConfiguration corsConfiguration = corsConfigurationDAO.getCORSConfigurationByTenantDomain(tenantDomain);
        addCORSConfigurationToCache(corsConfiguration, tenantDomain);
        return corsConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCORSConfigurationByTenantDomain(CORSConfiguration corsConfiguration, String tenantDomain)
            throws CORSManagementServiceServerException {

        clearCaches(tenantDomain);
        corsConfigurationDAO.setCORSConfigurationByTenantDomain(corsConfiguration, tenantDomain);
    }

    /**
     * Add CORS configurations to the cache.
     *
     * @param corsConfiguration The cors configuration that should be added to the cache.
     * @param tenantDomain      The tenant domain specific to the cache entry.
     */
    private void addCORSConfigurationToCache(CORSConfiguration corsConfiguration, String tenantDomain) {

        CORSConfigurationCacheKey cacheKey = new CORSConfigurationCacheKey(tenantDomain);
        CORSConfigurationCacheEntry cacheEntry = new CORSConfigurationCacheEntry(corsConfiguration);

        if (log.isDebugEnabled()) {
            log.debug("Adding CORS configuration to Cache with Key: " + tenantDomain);
        }

        CORSConfigurationCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * Get CORS configuration from the cache.
     *
     * @param tenantDomain The tenant domain specific to the cache entry.
     * @return Returns an instance of {@code CORSConfiguration}(s) if the cached CORS configuration is found for the
     * tenant. Else return {@code null}.
     */
    private CORSConfiguration getCORSConfigurationFromCache(String tenantDomain) {

        CORSConfigurationCacheKey cacheKey = new CORSConfigurationCacheKey(tenantDomain);
        CORSConfigurationCache cache = CORSConfigurationCache.getInstance();
        CORSConfigurationCacheEntry cacheEntry = cache.getValueFromCache(cacheKey);

        if (cacheEntry != null && cacheEntry.getCorsConfiguration() != null) {
            return cacheEntry.getCorsConfiguration();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for cache key :" + tenantDomain);
            }
            return null;
        }
    }

    /**
     * Clear CORS configuration caches of a particular tenant.
     *
     * @param tenantDomain The domain of the tenant.
     */
    private void clearCaches(String tenantDomain) {

        CORSConfigurationCacheKey cacheKey = new CORSConfigurationCacheKey(tenantDomain);
        CORSConfigurationCache.getInstance().clearCacheEntry(cacheKey);
    }
}
