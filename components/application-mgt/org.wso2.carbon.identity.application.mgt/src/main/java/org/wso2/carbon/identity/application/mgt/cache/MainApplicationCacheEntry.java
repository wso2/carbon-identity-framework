/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache entry holding the main application id for a shared application. A {@code null} mainAppId is a valid,
 * cached (negative) result meaning the application is not a shared/fragment application.
 */
public class MainApplicationCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 3050276134193513032L;

    private final String mainAppId;

    public MainApplicationCacheEntry(String mainAppId) {

        this.mainAppId = mainAppId;
    }

    public String getMainAppId() {

        return mainAppId;
    }
}
