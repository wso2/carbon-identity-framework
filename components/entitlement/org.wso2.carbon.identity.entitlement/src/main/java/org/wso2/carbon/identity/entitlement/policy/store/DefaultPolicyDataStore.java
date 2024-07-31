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

import org.apache.commons.lang.NotImplementedException;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dao.PolicyDAO;
import org.wso2.carbon.identity.entitlement.dao.RegistryPolicyDAOImpl;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is the default implementation of PolicyDataStore
 */
public class DefaultPolicyDataStore implements PolicyDataStore {

    private final PolicyDAO policyDAO = new RegistryPolicyDAOImpl();

    @Override
    public void init(Properties properties) throws EntitlementException {

        policyDAO.init(properties);
    }

    /**
     * This method is not implemented since the data is already being
     * retrieved with {@link org.wso2.carbon.identity.entitlement.dao.ConfigDAO#getGlobalPolicyAlgorithm()}
     */
    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        throw new NotImplementedException();
    }

    /**
     * This method is not implemented since the data is already being
     * set with {@link org.wso2.carbon.identity.entitlement.dao.ConfigDAO#setGlobalPolicyAlgorithm(String)}
     */
    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        throw new NotImplementedException();
    }

    /**
     * This method is not implemented since the data is already being
     * retrieved with {@link org.wso2.carbon.identity.entitlement.dao.ConfigDAO#getGlobalPolicyAlgorithmName()}
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {

        throw new NotImplementedException();
    }

    /**
     * This method is not implemented since the data is already being retrieved with
     * {@link org.wso2.carbon.identity.entitlement.EntitlementUtil#getAllGlobalPolicyAlgorithmNames()}
     */
    @Override
    public String[] getAllGlobalPolicyAlgorithmNames() {

        throw new NotImplementedException();
    }

    /**
     * Gets policy data for given policy id.
     *
     * @param policyId policy id as <code>String</code>.
     * @return policy data such as order and so on <code>PolicyStoreDTO</code>.
     */
    @Override
    public PolicyStoreDTO getPolicyData(String policyId) {

        PolicyDTO policyDTO = policyDAO.getPublishedPolicy(policyId);
        PolicyStoreDTO dataDTO = new PolicyStoreDTO();
        dataDTO.setPolicyOrder(policyDTO.getPolicyOrder());
        dataDTO.setActive(policyDTO.isActive());
        dataDTO.setPolicyId(policyDTO.getPolicyId());
        return dataDTO;
    }

    /**
     * Gets all policy data.
     *
     * @return <code>Array</code> of <code>PolicyStoreDTO</code>.
     */
    @Override
    public PolicyStoreDTO[] getPolicyData() {

        String[] publishedPolicyIds = policyDAO.getOrderedPolicyIdentifiers();
        List<PolicyStoreDTO> policyStoreDTOs = new ArrayList<>();
        if (publishedPolicyIds != null) {
            for (String policyId : publishedPolicyIds) {
                policyStoreDTOs.add(getPolicyData(policyId));
            }
        }
        return policyStoreDTOs.toArray(new PolicyStoreDTO[0]);
    }

    /**
     * This method is not implemented since the data is already being
     * set with {@link PolicyStoreManageModule#updatePolicy(PolicyStoreDTO)}
     *
     * @param policyId      policy id
     * @param policyDataDTO policy data
     * @throws EntitlementException if an error occurs
     */
    @Override
    public void setPolicyData(String policyId, PolicyStoreDTO policyDataDTO) throws EntitlementException {

        // No default implementation provided.
    }

    /**
     * This method is not implemented since the data is already being
     * removed with {@link PolicyStoreManageModule#deletePolicy(String)}
     *
     * @param policyId policy id
     * @throws EntitlementException if an error occurs
     */
    @Override
    public void removePolicyData(String policyId) throws EntitlementException {

        // No default implementation provided.
    }
}
