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
import org.wso2.carbon.identity.entitlement.EntitlementUtil;

/**
 * This interface supports the management of policy configuration data.
 */
public interface ConfigDAO {

    /**
     * Gets the global policy combining algorithm.
     *
     * @return global policy combining algorithm
     */
    default PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        String algorithm = getGlobalPolicyAlgorithmName();
        return EntitlementUtil.resolveGlobalPolicyAlgorithm(algorithm);
    }

    /**
     * Gets the policy combining algorithm name.
     *
     * @return global policy combining algorithm name
     */
    String getGlobalPolicyAlgorithmName();

    /**
     * Sets the global policy combining algorithm.
     *
     * @param policyCombiningAlgorithm policy combining algorithm name
     * @throws EntitlementException If an error occurs
     */
    void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException;
}
