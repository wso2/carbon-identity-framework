/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * IDP Cache against IDP resource ID.
 */
public class IdPCacheByResourceId extends BaseCache<IdPResourceIdCacheKey, IdPCacheEntry> {

    private static final String CACHE_NAME = "IdPCacheByResourceId";

    private static final IdPCacheByResourceId instance = new IdPCacheByResourceId();

    private IdPCacheByResourceId() {
        super(CACHE_NAME);
    }

    public static IdPCacheByResourceId getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }
}
