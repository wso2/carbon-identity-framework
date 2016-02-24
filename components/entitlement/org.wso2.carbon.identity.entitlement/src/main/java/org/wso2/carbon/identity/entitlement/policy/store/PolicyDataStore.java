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

import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.util.Properties;

/**
 * This is the entitlement policy data store that is used to persist meta data of the policies
 * Such as global policy combining algorithm.
 */
public interface PolicyDataStore {


    /**
     * initializes the PolicyDataStore
     *
     * @param properties properties, that need to initialize the module.
     * @throws EntitlementException throws when initialization is failed
     */
    public void init(Properties properties) throws EntitlementException;

    /**
     * Gets the policy combining algorithm of the PDP
     *
     * @return policy combining algorithm as <code>PolicyCombiningAlgorithm</code>
     */
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm();

    /**
     * Persist the policy combining algorithm in to data store
     *
     * @param policyCombiningAlgorithm policy combining algorithm name to persist
     * @throws EntitlementException throws if fails
     */
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException;

    /**
     * Gets the policy combining algorithm name of the PDP
     *
     * @return policy combining algorithm name as <code>String</code>
     */
    public String getGlobalPolicyAlgorithmName();

    /**
     * Gets all supported policy combining algorithm name of the PDP
     *
     * @return policy combining algorithm names as <code>Array</code> of <code>String</code>
     */
    public String[] getAllGlobalPolicyAlgorithmNames();

    /**
     * Gets policy data for given policy id
     *
     * @param policyId policy id as <code>String</code>
     * @return policy data such as order and so on <code>PolicyStoreDTO</code>
     */
    public PolicyStoreDTO getPolicyData(String policyId);


    /**
     * Gets all policy data
     *
     * @return <code>Array</code> of <code>PolicyStoreDTO</code>
     */
    public PolicyStoreDTO[] getPolicyData();

    /**
     * Set policy data for give policy id
     *
     * @param policyId      policy id as <code>String</code>
     * @param policyDataDTO policy data such as order and so on <code>PolicyStoreDTO</code>
     * @throws EntitlementException if it is failed
     */
    public void setPolicyData(String policyId, PolicyStoreDTO policyDataDTO) throws EntitlementException;

    /**
     * Remove policy data for give policy id
     *
     * @param policyId policy id as <code>String</code>
     * @throws EntitlementException
     */
    public void removePolicyData(String policyId) throws EntitlementException;

}
