/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
  * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;

public class ProvisioningConnectorCache extends
        BaseCache<ProvisioningConnectorCacheKey, ProvisioningConnectorCacheEntry> {

    private static volatile ProvisioningConnectorCache instance;

    private ProvisioningConnectorCache() {
        super(IdentityProvisioningConstants.PropertyConfig.IDENTITY_PROVISIONING_CONNECTOR_CACHE_NAME);
    }

    public static ProvisioningConnectorCache getInstance() {
        if (instance == null) {
            synchronized (ProvisioningConnectorCache.class) {
                if (instance == null) {
                    instance = new ProvisioningConnectorCache();
                }
            }
        }
        return instance;
    }
}
