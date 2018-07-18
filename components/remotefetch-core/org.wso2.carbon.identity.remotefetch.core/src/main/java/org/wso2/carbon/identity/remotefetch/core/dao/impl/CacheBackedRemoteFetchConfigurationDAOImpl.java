/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.core.dao.impl;

import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.cache.RemoteFetchConfigurationByIdCache;
import org.wso2.carbon.identity.remotefetch.core.cache.RemoteFetchConfigurationByIdCacheEntry;
import org.wso2.carbon.identity.remotefetch.core.cache.RemoteFetchConfigurationByIdCacheKey;
import org.wso2.carbon.identity.remotefetch.core.dao.RemoteFetchConfigurationDAO;

import java.util.List;

public class CacheBackedRemoteFetchConfigurationDAOImpl implements RemoteFetchConfigurationDAO {

    RemoteFetchConfigurationDAO remoteFetchConfigurationDAO = new RemoteFetchConfigurationDAOImpl();
    RemoteFetchConfigurationByIdCache IdCache = RemoteFetchConfigurationByIdCache.getInstance();

    /**
     * @param configuration
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public int createRemoteFetchConfiguration(RemoteFetchConfiguration configuration) throws RemoteFetchCoreException {

        int id = this.remoteFetchConfigurationDAO.createRemoteFetchConfiguration(configuration);
        configuration.setRemoteFetchConfigurationId(id);

        RemoteFetchConfigurationByIdCacheKey cacheKey = new RemoteFetchConfigurationByIdCacheKey(id);
        RemoteFetchConfigurationByIdCacheEntry cacheEntry = new RemoteFetchConfigurationByIdCacheEntry(configuration);

        this.IdCache.clearCacheEntry(cacheKey);
        this.IdCache.addToCache(cacheKey, cacheEntry);
        return id;
    }

    /**
     * @param configurationId
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public RemoteFetchConfiguration getRemoteFetchConfiguration(int configurationId) throws RemoteFetchCoreException {

        RemoteFetchConfigurationByIdCacheKey cacheKey = new RemoteFetchConfigurationByIdCacheKey(configurationId);
        RemoteFetchConfigurationByIdCacheEntry cacheEntry = this.IdCache.getValueFromCache(cacheKey);

        if (cacheEntry != null) {
            return cacheEntry.getRemoteFetchConfiguration();
        } else {
            return this.remoteFetchConfigurationDAO.getRemoteFetchConfiguration(configurationId);
        }
    }

    /**
     * @param configuration
     * @throws RemoteFetchCoreException
     */
    @Override
    public void updateRemoteFetchConfiguration(RemoteFetchConfiguration configuration) throws RemoteFetchCoreException {

        this.remoteFetchConfigurationDAO.updateRemoteFetchConfiguration(configuration);

        RemoteFetchConfigurationByIdCacheKey cacheKey = new RemoteFetchConfigurationByIdCacheKey(
                configuration.getRemoteFetchConfigurationId());
        RemoteFetchConfigurationByIdCacheEntry cacheEntry = new RemoteFetchConfigurationByIdCacheEntry(configuration);

        this.IdCache.addToCache(cacheKey, cacheEntry);

    }

    /**
     * @param configurationId
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deleteRemoteFetchConfiguration(int configurationId) throws RemoteFetchCoreException {

        RemoteFetchConfigurationByIdCacheKey cacheKey = new RemoteFetchConfigurationByIdCacheKey(configurationId);

        this.remoteFetchConfigurationDAO.deleteRemoteFetchConfiguration(configurationId);

        this.IdCache.clearCacheEntry(cacheKey);
    }

    /**
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public List<RemoteFetchConfiguration> getAllRemoteFetchConfigurations() throws RemoteFetchCoreException {

        return this.remoteFetchConfigurationDAO.getAllRemoteFetchConfigurations();
    }
}
