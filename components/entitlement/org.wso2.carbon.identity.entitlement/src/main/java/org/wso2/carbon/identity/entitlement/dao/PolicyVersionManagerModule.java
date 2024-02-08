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

import java.util.Properties;

/**
 * This manages policy versions
 */
public interface PolicyVersionManagerModule {

    /**
     * init policy version handler
     */
    void init(Properties properties);


    /**
     * Returns requested policy version
     */
    PolicyDTO getPolicy(String policyId, String version) throws EntitlementException;


    /**
     * Returns versions of the given policy
     */
    String[] getVersions(String policyId) throws EntitlementException;


    /**
     * Creates policy versions
     */
    String createVersion(PolicyDTO policyDTO) throws EntitlementException;


    /**
     * Removes policy versions
     */
    void deletePolicy(String policyId) throws EntitlementException;

}
