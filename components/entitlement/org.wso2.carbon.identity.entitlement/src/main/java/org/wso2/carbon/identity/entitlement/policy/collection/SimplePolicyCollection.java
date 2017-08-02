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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.EntitlementException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * simple implementation of Policy collection interface. This uses in-memory map to maintain policies
 * policy versions are not maintained by this
 */
public class SimplePolicyCollection implements PolicyCollection {

    private static Log log = LogFactory.getLog(SimplePolicyCollection.class);
    /**
     * the actual collection of policies
     * to maintain the order of the policies, <code>LinkedHashMap</code> has been used.
     * Map with  policy identifier policy as <code>AbstractPolicy</code> object
     */
    private LinkedHashMap<URI, AbstractPolicy> policyCollection = new LinkedHashMap<URI, AbstractPolicy>();
    /**
     * the optional combining algorithm used when wrapping multiple policies
     * if no algorithm is defined, only one applicable algorithm is used
     */
    private PolicyCombiningAlgorithm combiningAlg;
    /**
     * the optional policy id used when wrapping multiple policies
     */
    private URI parentId;

    @Override
    public void init(Properties properties) throws Exception {
        String parentIdProperty = properties.getProperty("parentId");
        if (parentIdProperty != null) {
            parentId = new URI(parentIdProperty);
        }
    }

    @Override
    public boolean addPolicy(AbstractPolicy policy) {
        return addPolicy(policy.getId(), policy);
    }

    @Override
    public AbstractPolicy getEffectivePolicy(EvaluationCtx context) throws EntitlementException {

        // setup a list of matching policies
        ArrayList<AbstractPolicy> list = new ArrayList<AbstractPolicy>();

        for (Map.Entry<URI, AbstractPolicy> entry : policyCollection.entrySet()) {

            AbstractPolicy policy = entry.getValue();

            // see if we match
            MatchResult match = policy.match(context);
            int result = match.getResult();

            // if there was an error, we stop right away
            if (result == MatchResult.INDETERMINATE) {
                log.error(match.getStatus().getMessage());
                throw new EntitlementException(match.getStatus().getMessage());
            }

            // if we matched, we keep track of the matching policy...
            if (result == MatchResult.MATCH) {
                // ...first checking if this is the first match and if
                // we automatically nest policies

                if (log.isDebugEnabled()) {
                    log.debug("Matching XACML policy found " + policy.getId().toString());
                }

                if ((combiningAlg == null) && (list.size() > 0)) {
                    log.error("Too many applicable top-level policies");
                    throw new EntitlementException("Too many applicable top-level policies");
                }

                list.add(policy);
            }
        }

        // no errors happened during the search, so now take the right
        // action based on how many policies we found
        switch (list.size()) {
            case 0:
                if (log.isDebugEnabled()) {
                    log.debug("No matching XACML policy found");
                }
                return null;
            case 1:
                return ((AbstractPolicy) (list.get(0)));
            default:
                return new PolicySet(parentId, combiningAlg, null, list);
        }

    }

    @Override
    public AbstractPolicy getPolicy(URI policyId) {
        return policyCollection.get(policyId);
    }

    @Override
    public AbstractPolicy getPolicy(URI identifier, int type, VersionConstraints constraints) {

        AbstractPolicy policy = policyCollection.get(identifier);

        if (policy != null) {
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy) {
                    return policy;
                }
            } else {
                if (policy instanceof PolicySet) {
                    return policy;
                }
            }
        }

        return null;
    }

    private synchronized boolean addPolicy(URI identifier, AbstractPolicy policy) {
        return policyCollection.put(identifier, policy) != null;
    }

    @Override
    public void setPolicyCombiningAlgorithm(PolicyCombiningAlgorithm algorithm) {
        this.combiningAlg = algorithm;
    }

    @Override
    public boolean deletePolicy(String policyId) {
        try {
            return this.policyCollection.remove(new URI(policyId)) != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    @Override
    public LinkedHashMap getPolicyMap() {
        return this.policyCollection;
    }

    @Override
    public void setPolicyMap(LinkedHashMap policyMap) {
        this.policyCollection = policyMap;
    }
}
