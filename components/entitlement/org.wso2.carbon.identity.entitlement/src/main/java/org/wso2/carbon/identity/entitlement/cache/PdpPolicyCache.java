/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

/**
 * Cache implementation for PAP policies.
 * Cache entry: <policy id, policy store DTO>
 */
public class PdpPolicyCache extends BaseCache<String, PolicyStoreDTO> {

    private static final String CACHE_NAME = "PdpPolicyCache";
    private static final PdpPolicyCache instance = new PdpPolicyCache();

    private PdpPolicyCache() {

        super(CACHE_NAME);
    }

    public static PdpPolicyCache getInstance() {

        return instance;
    }

    @Override
    public void addToCache(String key, PolicyStoreDTO policyStoreDTO, int tenantId) {

        if (policyStoreDTO != null){
            PolicyStoreDTO policyStoreDTOCopy = new PolicyStoreDTO(policyStoreDTO);
            super.addToCache(key, policyStoreDTOCopy, tenantId);
        }
    }

    @Override
    public PolicyStoreDTO getValueFromCache(String key, int tenantId) {

        PolicyStoreDTO policyStoreDTO = super.getValueFromCache(key, tenantId);
        PolicyStoreDTO policyStoreDTOCopy = null;
        if (policyStoreDTO != null) {
            policyStoreDTOCopy = new PolicyStoreDTO(policyStoreDTO);
        }
        return policyStoreDTOCopy;
    }
}
