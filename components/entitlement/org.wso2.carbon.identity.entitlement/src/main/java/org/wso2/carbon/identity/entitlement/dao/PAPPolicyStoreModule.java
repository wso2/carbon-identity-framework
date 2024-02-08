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

/**
 * This interface supports data retrieving from the PAP
 */
public interface PAPPolicyStoreModule {


    /**
     * Returns all policy ids
     */
    String[] getAllPolicyIds() throws EntitlementException;


    /**
     * Returns the given policy as a PolicyDTO
     */
    PolicyDTO getPolicy(String policyId) throws EntitlementException;


    /**
     * Returns the given policy version as a PolicyDTO
     */
    PolicyDTO getPolicy(String policyId, String version) throws EntitlementException;


    /**
     * Returns all policies
     */
    PolicyDTO[] getAllPolicies() throws EntitlementException;


    /**
     * Adds or Updates the given policy
     */
    void addOrUpdatePolicy(PolicyDTO policy, Boolean toPAP) throws EntitlementException;


    /**
     * Removes the given policy from the policy store
     */
    void removePolicy(String policyId) throws EntitlementException;


    /**
     * Removes the given policy version from the policy store
     */
    void removePolicy(String policyId, int version) throws EntitlementException;


}
