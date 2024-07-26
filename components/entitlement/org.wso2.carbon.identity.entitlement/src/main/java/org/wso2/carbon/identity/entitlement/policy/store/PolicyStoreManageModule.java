/*
 *  Copyright (c)  WSO2 LLC (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC licenses this file to you under the Apache License,
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
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.Properties;

/**
 * Carbon policy manage module is an extension point where XACML policies can be stored and
 * loaded in to the PDP from different sources. This is specially written for storing policies.
 * There can be only one policy store manage module in PDP
 */
public interface PolicyStoreManageModule extends PolicyFinderModule {

    /**
     * Init policy store module
     *
     * @param properties properties that are need to initialize the module.
     */
    public void init(Properties properties);

    /**
     * Add policy in to the store.
     *
     * @param policy policy as <code>PolicyStoreDTO</code>.
     */
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException;

    /**
     * Update policy in to the store.
     *
     * @param policy policy as <code>PolicyStoreDTO</code>.
     */
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException;

    /**
     * Delete policy from the store.
     *
     * @param policyIdentifier policy identifier as <code>String</code>.
     */
    public boolean deletePolicy(String policyIdentifier) throws EntitlementException;

    /**
     * Check whether policy is published or not.
     *
     * @param policyId policy id as <code>String</code>.
     * @return whether true or false.
     */
    public boolean isPolicyExist(String policyId);
}
