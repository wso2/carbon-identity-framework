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

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.wso2.carbon.identity.application.authentication.framework.cache.LongWaitResultCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.LongWaitResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.LongWaitResultCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.dao.LongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;

import java.sql.Timestamp;

public class CacheBackedLongWaitStatusDAO implements LongWaitStatusDAO {

    private LongWaitStatusDAO waitStatusDAO;

    public CacheBackedLongWaitStatusDAO(LongWaitStatusDAO waitStatusDAO) {

        this.waitStatusDAO = waitStatusDAO;
    }

    public void addWaitStatus(int tenantId, String waitKey, LongWaitStatus status, Timestamp createdTime, Timestamp
            expireTime) throws FrameworkException {

        if (waitKey != null) {
            // Add to database.
            waitStatusDAO.addWaitStatus(tenantId, waitKey, status, createdTime, expireTime);

            // Add to cache.
            LongWaitResultCacheKey cacheKey = new LongWaitResultCacheKey(waitKey);
            LongWaitResultCacheEntry cacheEntry = new LongWaitResultCacheEntry(status);
            LongWaitResultCache.getInstance().addToCache(cacheKey, cacheEntry);
        }
    }

    public void removeWaitStatus(String waitKey) throws FrameworkException {

        waitStatusDAO.removeWaitStatus(waitKey);

        // Add status as completed.
        LongWaitResultCacheKey cacheKey = new LongWaitResultCacheKey(waitKey);
        LongWaitStatus status = new LongWaitStatus();
        status.setStatus(LongWaitStatus.Status.UNKNOWN);
        LongWaitResultCacheEntry cacheEntry = new LongWaitResultCacheEntry(status);
        LongWaitResultCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    public LongWaitStatus getWaitStatus(String waitKey) throws FrameworkException {

        LongWaitStatus status = null;
        LongWaitResultCacheEntry valueFromCache = LongWaitResultCache.getInstance().getValueFromCache(new
                LongWaitResultCacheKey(waitKey));

        if (valueFromCache != null) {
            status = valueFromCache.getWaitStatus();
        }
        if (status == null) {
            status = waitStatusDAO.getWaitStatus(waitKey);

            LongWaitResultCacheKey cacheKey = new LongWaitResultCacheKey(waitKey);
            LongWaitResultCacheEntry cacheEntry = new LongWaitResultCacheEntry(status);
            LongWaitResultCache.getInstance().addToCache(cacheKey, cacheEntry);
        }
        return status;
    }
}
