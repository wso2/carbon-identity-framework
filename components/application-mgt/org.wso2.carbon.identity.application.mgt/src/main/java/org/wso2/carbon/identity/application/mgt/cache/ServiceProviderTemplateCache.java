/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.application.common.model.SpTemplate;

/**
 * This class is used to cache the service provider template info.
 */
public class ServiceProviderTemplateCache extends BaseCache<ServiceProviderTemplateCacheKey, SpTemplate> {

    public static final String SP_CACHE_NAME = "ServiceProviderTemplateCache";
    private static volatile ServiceProviderTemplateCache instance;

    private ServiceProviderTemplateCache() {

        super(SP_CACHE_NAME);
    }

    public static ServiceProviderTemplateCache getInstance() {

        if (instance == null) {
            synchronized (ServiceProviderTemplateCache.class) {
                if (instance == null) {
                    instance = new ServiceProviderTemplateCache();
                }
            }
        }
        return instance;
    }
}
