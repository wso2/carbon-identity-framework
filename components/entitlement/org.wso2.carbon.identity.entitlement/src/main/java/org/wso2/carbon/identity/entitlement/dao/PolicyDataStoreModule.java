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

package org.wso2.carbon.identity.entitlement.dao;

import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.util.Properties;

/**
 * This is the entitlement policy data store that is used to persist metadata of the policies
 * Such as global policy combining algorithm.
 */
public interface PolicyDataStoreModule {


    /**
     * initializes the PolicyDataStoreModule
     */
    void init(Properties properties) throws EntitlementException;


    /**
     * Gets the policy combining algorithm of the PDP
     */
    PolicyCombiningAlgorithm getGlobalPolicyAlgorithm();


    /**
     * Persists the policy combining algorithm into the data store
     */
    void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException;


    /**
     * Gets the policy combining algorithm name of the PDP
     */
    String getGlobalPolicyAlgorithmName();


    /**
     * Gets all supported policy combining algorithm name of the PDP
     */
    String[] getAllGlobalPolicyAlgorithmNames();


    /**
     * Gets policy data for given policy id
     */
    PolicyStoreDTO getPolicyData(String policyId);


    /**
     * Gets all policy data
     */
    PolicyStoreDTO[] getPolicyData();


    /**
     * Sets policy data for give policy id
     */
    void setPolicyData(String policyId, PolicyStoreDTO policyDataDTO) throws EntitlementException;


    /**
     * Removes policy data for given policy id
     */
    void removePolicyData(String policyId) throws EntitlementException;

}
