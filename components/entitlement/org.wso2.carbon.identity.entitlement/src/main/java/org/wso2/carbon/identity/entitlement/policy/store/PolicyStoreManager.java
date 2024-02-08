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

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.PDPPolicyStoreModule;
import org.wso2.carbon.identity.entitlement.dao.PolicyDataStoreModule;
import org.wso2.carbon.identity.entitlement.dao.RegistryPDPPolicyStore;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This manages the storing and reading of policies and policy metadata that is related
 * with PDP component. This instance is not tenant aware. But you can make this tenant aware by
 * returning data that is relevant to the given tenant. Tenant domain or id can be available via
 * CarbonContext which can be call by extension module.
 */
public class PolicyStoreManager {

    private final PDPPolicyStoreModule policyStore;

    private final PolicyDataStoreModule policyDataStore;


    public PolicyStoreManager(PolicyDataStoreModule policyDataStore) {

        Map<PDPPolicyStoreModule, Properties> policyCollections = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyStore();
        if (policyCollections != null && !policyCollections.isEmpty()) {
            policyStore = policyCollections.entrySet().iterator().next().getKey();
        } else {
            policyStore = new RegistryPDPPolicyStore();
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
        dto.setVersion(policyDTO.getVersion());

        if (policyStore.isPolicyExist(policyDTO.getPolicyId())) {
            dto.setSetActive(false);
            dto.setSetOrder(false);
        } else {
            dto.setSetOrder(true);
            dto.setSetActive(true);
        }

        policyStore.addPolicy(dto);
        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);
        RegistryPDPPolicyStore
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_UPDATE);
    }


    public void updatePolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy does not exist in the Policy Store : PolicyId " +
                    policyDTO.getPolicyId());
        }

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setActive(policyDTO.isActive());
        dto.setPolicyOrder(policyDTO.getPolicyOrder());
        dto.setAttributeDTOs(policyDTO.getAttributeDTOs());
        dto.setVersion(policyDTO.getVersion());
        dto.setSetActive(false);
        dto.setSetOrder(false);

        policyStore.updatePolicy(dto);
        RegistryPDPPolicyStore
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_UPDATE);
    }


    public void enableDisablePolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy does not exist in the Policy Store : PolicyId " +
                    policyDTO.getPolicyId());
        }

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setActive(policyDTO.isActive());
        dto.setVersion(policyDTO.getVersion());
        dto.setSetActive(true);

        if (policyStore.isPolicyDeActivationSupport()) {
            policyStore.updatePolicy(dto);
        }

        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);

        if (policyDTO.isActive()) {
            RegistryPDPPolicyStore
                    .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_ENABLE);
        } else {
            RegistryPDPPolicyStore
                    .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_DISABLE);
        }
    }


    public void orderPolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy does not exist in the Policy Store : PolicyId " +
                    policyDTO.getPolicyId());
        }

        PolicyStoreDTO dto = new PolicyStoreDTO();
        dto.setPolicyId(policyDTO.getPolicyId());
        dto.setPolicy(policyDTO.getPolicy());
        dto.setPolicyOrder(policyDTO.getPolicyOrder());
        dto.setVersion(policyDTO.getVersion());
        dto.setSetOrder(true);

        if (policyStore.isPolicyOrderingSupport()) {
            policyStore.updatePolicy(dto);
        }

        policyDataStore.setPolicyData(policyDTO.getPolicyId(), dto);

        RegistryPDPPolicyStore
                .invalidateCache(dto.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_ORDER);
    }


    public void removePolicy(PolicyDTO policyDTO) throws EntitlementException {

        if (!policyStore.isPolicyExist(policyDTO.getPolicyId())) {
            throw new EntitlementException("Policy does not exist in the Policy Store : PolicyId " +
                    policyDTO.getPolicyId());
        }
        policyStore.deletePolicy(policyDTO.getPolicyId());
        policyDataStore.removePolicyData(policyDTO.getPolicyId());
        RegistryPDPPolicyStore
                .invalidateCache(policyDTO.getPolicyId(), EntitlementConstants.PolicyPublish.ACTION_DELETE);
    }


    public PolicyDTO getPolicy(String policyId) {

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setPolicyId(policyId);
        String policy = policyStore.getPolicy(policyId);
        PolicyStoreDTO storeDTO = policyDataStore.getPolicyData(policyId);
        if (policy != null) {
            policyDTO.setPolicy(policy);
            policyDTO.setActive(storeDTO.isActive());
            policyDTO.setPolicyOrder(storeDTO.getPolicyOrder());
        }
        return policyDTO;
    }


    public String[] getPolicyIds() {
        return policyStore.getOrderedPolicyIdentifiers();
    }


    public PolicyDTO[] getLightPolicies() {

        List<PolicyDTO> policyDTOs = new ArrayList<>();
        String[] policies = policyStore.getOrderedPolicyIdentifiers();
        if (policies != null) {
            for (String policy : policies) {
                PolicyDTO policyDTO = new PolicyDTO();
                policyDTO.setPolicyId(policy);

                PolicyStoreDTO storeDTO = policyDataStore.getPolicyData(policy);

                policyDTO.setActive(storeDTO.isActive());
                policyDTO.setPolicyOrder(storeDTO.getPolicyOrder());
                policyDTO.setPolicyType(storeDTO.getPolicyType());
                policyDTOs.add(policyDTO);
            }
        }
        return policyDTOs.toArray(new PolicyDTO[0]);
    }


    public PolicyStoreDTO[] getAllPolicyData() {
        return policyDataStore.getPolicyData();
    }

}
