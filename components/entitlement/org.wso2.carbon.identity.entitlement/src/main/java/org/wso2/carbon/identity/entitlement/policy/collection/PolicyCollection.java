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

package org.wso2.carbon.identity.entitlement.policy.collection;

import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.EntitlementException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Policy collection for all the policies defined in entitlement engine. This collection is created
 * by finding each and every policies that have been configured with each policy finder modules.
 * There can be different implementation to maintain policies.
 */
public interface PolicyCollection {

    /**
     * initializes policy finder collection
     *
     * @param properties Properties, that need to initialize the module
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * adds policy to policy collection
     *
     * @param policy policy as AbstractPolicy object of Balana
     * @return whether policy is added successfully or not
     */
    public boolean addPolicy(AbstractPolicy policy);

    /**
     * returns the effective policy for given XACML request
     *
     * @param context XACML request ctx
     * @return effective policy set as AbstractPolicy object of Balana
     * @throws EntitlementException if any error, while policy is retrieved
     */
    public AbstractPolicy getEffectivePolicy(EvaluationCtx context) throws EntitlementException;

    /**
     * returns policy by given identifier
     *
     * @param identifier policy identifier
     * @return policy as AbstractPolicy object of Balana
     */
    public AbstractPolicy getPolicy(URI identifier);

    /**
     * returns policy by identifier type and version
     *
     * @param identifier  policy identifier
     * @param type        policy type whether policy or policy set
     * @param constraints policy version constraints
     * @return policy as AbstractPolicy object of Balana
     */
    public AbstractPolicy getPolicy(URI identifier, int type, VersionConstraints constraints);

    /**
     * sets global policy combining algorithm
     *
     * @param algorithm PolicyCombiningAlgorithm object of Balana
     */
    public void setPolicyCombiningAlgorithm(PolicyCombiningAlgorithm algorithm);


    public boolean deletePolicy(String policyId);
    public LinkedHashMap getPolicyMap() ;
    public void setPolicyMap(LinkedHashMap policyMap) ;

}
