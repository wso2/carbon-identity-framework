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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.cache.BaseCache;

public class LongWaitResultCache extends BaseCache<LongWaitResultCacheKey, LongWaitResultCacheEntry> {

    private static final String LONG_WAIT_RESULT_CACHE_NAME = "LongWaitResultCache";
    private static final Log log = LogFactory.getLog(LongWaitResultCache.class);

    private static volatile LongWaitResultCache instance = new LongWaitResultCache();

    private LongWaitResultCache() {

        super(LONG_WAIT_RESULT_CACHE_NAME);
    }

    public static LongWaitResultCache getInstance() {

        return instance;
    }

    @Override
    public void addToCache(LongWaitResultCacheKey key, LongWaitResultCacheEntry entry) {

        super.addToCache(key, entry);
    }

    @Override
    public LongWaitResultCacheEntry getValueFromCache(LongWaitResultCacheKey key) {

        return super.getValueFromCache(key);
    }

    @Override
    public void clearCacheEntry(LongWaitResultCacheKey key) {

        super.clearCacheEntry(key);
    }
}
