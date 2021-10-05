/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.template.mgt.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Cache implementation for config store based template cache.
 */
public class ConfigStoreBasedTemplateCache extends BaseCache<ConfigStoreBasedTemplateCacheKey,
        ConfigStoreBasedTemplateCacheEntry> {

    private static final String CACHE_NAME = "TemplateCacheById";

    private static final ConfigStoreBasedTemplateCache instance = new
            ConfigStoreBasedTemplateCache();

    private ConfigStoreBasedTemplateCache() {

        super(CACHE_NAME);
    }

    public static ConfigStoreBasedTemplateCache getInstance() {

        CarbonUtils.checkSecurity();
        return instance;
    }

}
