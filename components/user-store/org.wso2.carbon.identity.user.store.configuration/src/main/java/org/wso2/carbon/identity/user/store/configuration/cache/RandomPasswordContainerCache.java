/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.user.store.configuration.cache;

import org.wso2.carbon.identity.user.store.configuration.beans.MaskedProperty;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPassword;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPasswordContainer;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;

import javax.cache.Cache;
import javax.cache.Caching;

/**
 * Class to hold the reference for distributed cache object
 *
 * @see <a href="https://github.com/wso2/product-is/issues/6410">https://github.com/wso2/product-is/issues/6410</a>.
 * @deprecated {@link RandomPassword}, {@link RandomPasswordContainer} and
 * {@link org.wso2.carbon.identity.user.store.configuration.cache.RandomPasswordContainerCache} based approach in the
 * {@link org.wso2.carbon.identity.user.store.configuration.UserStoreConfigAdminService} is identified as problematic
 * in a clustered deployment. Therefore a new approach based {@link MaskedProperty} is used.
 */
public class RandomPasswordContainerCache {

    private static volatile RandomPasswordContainerCache randomPasswordContainerCache = null;

    private RandomPasswordContainerCache() {

    }

    /**
     * Singleton instance class which returns RandomPasswordContainerCache instance
     *
     * @return
     */
    public static RandomPasswordContainerCache getInstance() {
        if (randomPasswordContainerCache == null) {
            synchronized (RandomPasswordContainerCache.class) {
                if (randomPasswordContainerCache == null) { // Check 2
                    randomPasswordContainerCache = new RandomPasswordContainerCache();
                }
            }
        }
        return randomPasswordContainerCache;
    }

    /**
     * Get the cache which holds the RandomPasswordContainer cache
     *
     * @return Cache object of RandomPasswordContainerCache
     */
    public Cache<String, RandomPasswordContainer> getRandomPasswordContainerCache() {
        return Caching.getCacheManagerFactory().getCacheManager(
                UserStoreConfigurationConstant.SECONDARY_STORAGE_CACHE_MANAGER).
                getCache(UserStoreConfigurationConstant.RANDOM_PASSWORD_CONTAINER_CACHE);
    }


}
