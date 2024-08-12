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

import java.util.ArrayList;

/**
 * Cache implementation for PDP policy list.
 * Cache entry: <constant key, policy store DTO list>
 */
public class PdpPolicyListCache extends BaseCache<String, PolicyStoreDTO[]> {

    private static final String CACHE_NAME = "PdpPolicyListCache";
    private static final PdpPolicyListCache instance = new PdpPolicyListCache();

    private PdpPolicyListCache() {

        super(CACHE_NAME);
    }

    public static PdpPolicyListCache getInstance() {

        return instance;
    }

    @Override
    public void addToCache(String key, PolicyStoreDTO[] policyDTOs, int tenantId) {

        PolicyStoreDTO[] policyDTOList = createCopy(policyDTOs);
        super.addToCache(key, policyDTOList, tenantId);
    }

    @Override
    public PolicyStoreDTO[] getValueFromCache(String key, int tenantId) {

        PolicyStoreDTO[] policyDTOs = super.getValueFromCache(key, tenantId);
        return createCopy(policyDTOs);
    }

    private PolicyStoreDTO[] createCopy(PolicyStoreDTO[] policyDTOs) {

        if (policyDTOs == null) {
            return null;
        }
        ArrayList<PolicyStoreDTO> policyDTOList = new ArrayList<>();
        for (PolicyStoreDTO policyDTO : policyDTOs) {
            if (policyDTO != null) {
                policyDTOList.add(new PolicyStoreDTO(policyDTO));
            }
        }
        return policyDTOList.toArray(new PolicyStoreDTO[0]);
    }
}
