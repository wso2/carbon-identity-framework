/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache to maintain the inbound auth key - application resource id.
 */
public class ApplicationResourceIDByInboundAuthCache extends
        BaseCache<ApplicationResourceIDCacheInboundAuthKey, ApplicationResourceIDCacheInboundAuthEntry> {

    public static final String SP_CACHE_NAME = "ApplicationResourceIDCache.InboundAuth";

    private static volatile ApplicationResourceIDByInboundAuthCache instance;

    private ApplicationResourceIDByInboundAuthCache() {

        super(SP_CACHE_NAME);
    }

    public static ApplicationResourceIDByInboundAuthCache getInstance() {

        if (instance == null) {
            synchronized (ApplicationResourceIDByInboundAuthCache.class) {
                if (instance == null) {
                    instance = new ApplicationResourceIDByInboundAuthCache();
                }
            }
        }
        return instance;
    }
}
