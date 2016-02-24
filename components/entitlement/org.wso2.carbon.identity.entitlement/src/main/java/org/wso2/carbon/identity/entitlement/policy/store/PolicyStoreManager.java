/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.policy.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This manages the storing and reading of policies and policy meta data that is related
 * with PDP component. This instance is not tenant aware. But you can make this tenant aware by
 * returning data that is relevant to the given tenant. Tenant domain or id can be available via
 * CarbonContext which can be call by extension module.
 */
public class PolicyStoreManager {

    private PolicyStoreManageModule policyStoreStore = null;
    private PolicyDataStore policyDataStore = null;

    private static Log log = LogFactory.getLog(PolicyStoreManager.class);

    public PolicyStoreManager(PolicyDataStore policyDataStore) {
        // get policy collection
        Map<PolicyStoreManageModule, Properties> policyCollections = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyStore();
        if (policyCollections != null && policyCollections.size() > 0) {
            policyStoreStore = policyCollections.entrySet().iterator().next().getKey();
        } else {
            policyStoreStore = new RegistryPolicyStoreManageModule();
        }
        this.policyDataStore = policyDataStore;
    }

    public void addPolicy(PolicyDTO policyDTO) throws EntitlementException {

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setActive(policyDTO.isActive());
        dto.setPolicyOrder(policyDTO.getPolicyOrder());
        dto.setAttributeDTOs(policyDTO.getAttributeDTOs());
        if (policyStoreStore.isPolicyExist(policyDTO.getPolicyId())) {
            dto.setSetActive(false);
            dto.setSetOrder(false);
        } else {
            dto.setSetOrder(true);
            dto.setSetActive(true);
        }
        policyStoreStore.addPolicy(dto);
        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);
        RegistryPolicyStoreManageModule
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_UPDATE);
    }

    public void updatePolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStoreStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy is not exist in the Policy Store : PolicyId " +
                                           policyDTO.getPolicyId());
        }
        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setActive(policyDTO.isActive());
        dto.setPolicyOrder(policyDTO.getPolicyOrder());
        dto.setAttributeDTOs(policyDTO.getAttributeDTOs());
        dto.setSetActive(false);
        dto.setSetOrder(false);
        policyStoreStore.updatePolicy(dto);
        RegistryPolicyStoreManageModule
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_UPDATE);
    }

    public void enableDisablePolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStoreStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy is not exist in the Policy Store : PolicyId " +
                                           policyDTO.getPolicyId());
        }

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setActive(policyDTO.isActive());
        dto.setSetActive(true);
        if (policyStoreStore.isPolicyDeActivationSupport()) {
            policyStoreStore.updatePolicy(dto);
        }
        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);
        if (policyDTO.isActive()) {
            RegistryPolicyStoreManageModule
                    .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_ENABLE);
        } else {
            RegistryPolicyStoreManageModule
                    .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_DISABLE);
        }
    }

    public void orderPolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStoreStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy is not exist in the Policy Store : PolicyId " +
                                           policyDTO.getPolicyId());
        }

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setPolicyOrder(policyDTO.getPolicyOrder());
        dto.setSetOrder(true);
        if (policyStoreStore.isPolicyOrderingSupport()) {
            policyStoreStore.updatePolicy(dto);
        }
        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);
        RegistryPolicyStoreManageModule
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_ORDER);
    }


    public void removePolicy(PolicyDTO policyDTO) throws EntitlementException {
        if (!policyStoreStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy is not exist in the Policy Store : PolicyId " +
                                           policyDTO.getPolicyId());
        }
        policyStoreStore.deletePolicy(policyDTO.getPolicyId());
        policyDataStore.removePolicyData(policyDTO.getPolicyId());
        RegistryPolicyStoreManageModule
                .invalidateCache(policyDTO.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_DELETE);
    }

    public PolicyDTO getPolicy(String policyId) {

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setPolicyId(policyId);
        String policy = policyStoreStore.getPolicy(policyId);
        PolicyStoreDTO storeDTO = policyDataStore.getPolicyData(policyId);
        if (policy != null) {
            policyDTO.setPolicy(policy);
            policyDTO.setActive(storeDTO.isActive());
            policyDTO.setPolicyOrder(storeDTO.getPolicyOrder());
        }
        return policyDTO;
    }

    public String[] getPolicyIds() {
        return policyStoreStore.getOrderedPolicyIdentifiers();
    }

    public PolicyDTO[] getLightPolicies() {

        List<PolicyDTO> policyDTOs = new ArrayList<PolicyDTO>();
        String[] policies = policyStoreStore.getOrderedPolicyIdentifiers();
        if (policies != null) {
            for (String policy : policies) {
                PolicyDTO policyDTO = new PolicyDTO();
                policyDTO.setPolicyId(policy);
                PolicyStoreDTO storeDTO = policyDataStore.getPolicyData(policy);
                policyDTO.setActive(storeDTO.isActive());
                policyDTO.setPolicyOrder(storeDTO.getPolicyOrder());
                policyDTOs.add(policyDTO);
            }
        }
        return policyDTOs.toArray(new PolicyDTO[policyDTOs.size()]);
    }

    public PolicyStoreDTO[] getAllPolicyData() {
        return policyDataStore.getPolicyData();
    }
}
