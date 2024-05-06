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


/**
 * This interface supports the management of policy configuration data.
 */
public interface ConfigDAO {


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

}
