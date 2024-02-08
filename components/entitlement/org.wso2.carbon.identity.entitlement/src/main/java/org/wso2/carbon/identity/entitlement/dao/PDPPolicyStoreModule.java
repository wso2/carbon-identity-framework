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
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.Properties;

/**
 * Carbon policy manage module is an extension point where XACML policies can be stored and
 * loaded in to the PDP from different sources. This is specially written for storing policies.
 * There can be only one policy store manage module in PDP
 */
public interface PDPPolicyStoreModule extends PolicyFinderModule {

    /**
     * init policy store module
     */
    void init(Properties properties);


    /**
     * Adds the policy into the store
     */
    void addPolicy(PolicyStoreDTO policy) throws EntitlementException;


    /**
     * Updates the policy in the store
     */
    void updatePolicy(PolicyStoreDTO policy) throws EntitlementException;


    /**
     * Deletes the policy from the store
     */
    void deletePolicy(String policyIdentifier) throws EntitlementException;


    /**
     * Checks whether policy exists or not
     */
    boolean isPolicyExist(String policyId);

}
