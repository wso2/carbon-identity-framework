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

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.util.Properties;


/**
 * This interface supports the management of XACML policies.
 */
public interface PolicyStore {


    default void init(Properties properties) {

    }


    /**
     * Adds or updates the given policy
     */
    void addOrUpdatePolicy(PolicyDTO policy) throws EntitlementException;


    /**
     * Gets the requested policy
     */
    PolicyDTO getPolicy(String policyId) throws EntitlementException;


    /**
     * Gets the requested policy version
     */
    PolicyDTO getPolicy(String policyId, String version) throws EntitlementException;


    /**
     * Gets all policy IDs
     */
    String[] listPolicyIds() throws EntitlementException;


    /**
     * Gets all policies
     */
    //TODO add filters
    PolicyDTO[] listPolicies() throws EntitlementException;


    /**
     * Gets all versions of the given policy ID
     */
    String[] getVersions(String policyId);


    /**
     * Removes the given policy
     */
    void removePolicy(String policyId) throws EntitlementException;


    /**
     * Checks whether the given policy is published or not
     */
    boolean isPublished(String policyId);


    /**
     * Gets all published policy IDs
     */
    String[] listPublishedPolicyIds() throws EntitlementException;


    /**
     * Publishes the given policy
     */
    void publishPolicy(PolicyStoreDTO policy) throws EntitlementException;


    /**
     * Unpublishes the policy
     */
    void unpublishPolicy(String policyId) throws EntitlementException;


    /**
     * Sets the global policy combining algorithm
     */
    void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException;


    /**
     * Gets the policy combining algorithm
     */
    PolicyCombiningAlgorithm getGlobalPolicyAlgorithm();


    /**
     * Gets the policy combining algorithm name
     */
    String getGlobalPolicyAlgorithmName();


    /**
     * Gets all supported policy combining algorithm names
     */
    default String[] getAllGlobalPolicyAlgorithmNames() {
        return new String[] {"deny-overrides", "permit-overrides", "first-applicable", "ordered-deny-overrides",
                "ordered-permit-overrides", "only-one-applicable"};
    }

}
