/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;

/**
 * Cache implementation for Flow Management.
 * This cache stores FlowDTO objects, which represent the flow configurations in the flow management system.
 */
public class FlowMgtCache extends BaseCache<FlowMgtCacheKey, FlowDTO> {

    private static final String CACHE_NAME = "FlowMgtCache";
    private static final FlowMgtCache INSTANCE = new FlowMgtCache();

    private FlowMgtCache() {

        super(CACHE_NAME);
    }

    public static FlowMgtCache getInstance() {

        return INSTANCE;
    }
}
