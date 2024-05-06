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
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.List;


/**
 * This interface supports the management of XACML policies.
 */
public interface PolicyDAO extends PolicyFinderModule {


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
     * Gets all versions of the given policy ID
     */
    String[] getVersions(String policyId);


    /**
     * Lists all policy IDs
     */
    List<String> listPolicyIds() throws EntitlementException;


    /**
     * Removes the given policy
     */
    void removePolicy(String policyId) throws EntitlementException;


    /**
     * Publishes the given policy
     */
    void publishPolicy(PolicyStoreDTO policy) throws EntitlementException;


    /**
     * Checks whether the given policy is published or not
     */
    boolean isPublished(String policyId);


    /**
     * Lists all published policy IDs
     */
    List<String> listPublishedPolicyIds() throws EntitlementException;


    /**
     * Un-publishes the policy
     */
    void unPublishPolicy(String policyId);

}
