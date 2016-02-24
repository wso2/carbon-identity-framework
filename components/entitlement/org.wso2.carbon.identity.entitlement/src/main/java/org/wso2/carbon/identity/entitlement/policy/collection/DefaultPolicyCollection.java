/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.xacml2.Target;
import org.wso2.balana.xacml2.TargetSection;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementLRUCache;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class DefaultPolicyCollection implements PolicyCollection {

    // default target that matches anything, used in wrapping policies
    private static final AbstractTarget target;
    private static Log log = LogFactory.getLog(DefaultPolicyCollection.class);
    /**
     * This static initializer just sets up the default target, which is used by all wrapping policy
     * sets.
     */
    static {
        target = new Target(new TargetSection(null, TargetMatch.SUBJECT,
                XACMLConstants.XACML_VERSION_2_0), new TargetSection(null, TargetMatch.RESOURCE,
                XACMLConstants.XACML_VERSION_2_0), new TargetSection(null, TargetMatch.ACTION,
                XACMLConstants.XACML_VERSION_2_0), new TargetSection(null, TargetMatch.ENVIRONMENT,
                XACMLConstants.XACML_VERSION_2_0));
    }
    // the actual collection of policies
    private LinkedHashMap<String, TreeSet<AbstractPolicy>> policies;
    // the single instance of the comparator we'll use for managing versions
    private VersionComparator versionComparator = new VersionComparator();
    // the optional combining algorithm used when wrapping multiple policies
    private PolicyCombiningAlgorithm combiningAlg;
    // the optional policy id used when wrapping multiple policies
    private URI parentId;
    private int maxInMemoryPolicies;

    ;

    /**
     * Creates a new <code>DefaultPolicyCollection</code> that will return errors when multiple policies
     * match for a given request.
     *
     * @param combiningAlg        Policy combining Algorithm
     * @param maxInMemoryPolicies maximum no of policies that keeps in memory
     */
    public DefaultPolicyCollection(PolicyCombiningAlgorithm combiningAlg, int maxInMemoryPolicies) {
        policies = new EntitlementLRUCache<String, TreeSet<AbstractPolicy>>(maxInMemoryPolicies);
        this.maxInMemoryPolicies = maxInMemoryPolicies;
        this.combiningAlg = combiningAlg;
    }

    /**
     * Creates a new <code>DefaultPolicyCollection</code> that will return errors when multiple policies
     * match for a given request.
     *
     * @param combiningAlg Policy combining Algorithm
     */
    public DefaultPolicyCollection(PolicyCombiningAlgorithm combiningAlg) {
        policies = new LinkedHashMap<String, TreeSet<AbstractPolicy>>();
        this.combiningAlg = combiningAlg;
    }

    /**
     * Creates a new <code>DefaultPolicyCollection</code> that will create a new top-level PolicySet when
     * multiple policies match for a given request.
     *
     * @param combiningAlg   the algorithm to use in a new PolicySet when more than one policy applies
     * @param parentPolicyId the identifier to use for the new PolicySet
     */
    public DefaultPolicyCollection(PolicyCombiningAlgorithm combiningAlg, URI parentPolicyId) {
        policies = new LinkedHashMap<String, TreeSet<AbstractPolicy>>();
        this.combiningAlg = combiningAlg;
        this.parentId = parentPolicyId;
    }

    @Override
    public void init(Properties properties) throws Exception {
        String parentIdProperty = properties.getProperty("parentId");
        if (parentIdProperty != null) {
            parentId = new URI(parentIdProperty);
        }
    }

    /**
     * Adds a new policy to the collection, and uses the policy's identifier as the reference
     * identifier. If this identifier already exists in the collection, and this policy does not
     * represent a new version of the policy, then the policy is not added.
     *
     * @param policy the policy to add
     * @return true if the policy was added, false otherwise
     */
    public boolean addPolicy(AbstractPolicy policy) {
        return addPolicy(policy, policy.getId().toString());
    }

    /**
     * *Adds a new policy to the collection using the given identifier as the reference identifier.
     * If this identifier already exists in the collection, and this policy does not represent a new
     * version of the policy, then the policy is not added.
     *
     * @param policy
     * @param identifier
     * @return
     */
    public boolean addPolicy(AbstractPolicy policy, String identifier) {
        if (policies.containsKey(identifier)) {
            // this identifier is already is use, so see if this version is
            // already in the set
            TreeSet<AbstractPolicy> set = policies.get(identifier);
            return set.add(policy);
        } else {
            // this identifier isn't already being used, so create a new
            // set in the map for it, and add the policy
            TreeSet<AbstractPolicy> set = new TreeSet<AbstractPolicy>(versionComparator);
            policies.put(identifier, set);
            return set.add(policy);
        }
    }

    /**
     * Attempts to retrieve a policy based on the given context. If multiple policies match then
     * this will either throw an exception or wrap the policies under a new PolicySet (depending on
     * how this instance was constructed). If no policies match, then this will return null. See the
     * comment in the class header about how this behaves when multiple versions of the same policy
     * exist.
     *
     * @param context
     * @return
     * @throws EntitlementException
     */
    public AbstractPolicy getEffectivePolicy(EvaluationCtx context) throws EntitlementException {
        // setup a list of matching policies
        ArrayList<AbstractPolicy> list = new ArrayList<AbstractPolicy>();
        // get an iterator over all the identifiers
        Iterator<TreeSet<AbstractPolicy>> it = policies.values().iterator();

        while (it.hasNext()) {
            // for each identifier, get only the most recent policy
            AbstractPolicy policy = it.next().first();

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
                    ArrayList<String> code = new ArrayList<String>();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    Status status = new Status(code, "too many applicable top-level policies");
                    //throw new EntitlementException(status);     // TODO
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

    /**
     * Get Policy using policyId
     *
     * @param policyId policyId as a URI
     * @return AbstractPolicy
     */
    public AbstractPolicy getPolicy(URI policyId) {
        if (policies.containsKey(policyId.toString())) {
            return policies.get(policyId.toString()).first();
        }
        return null;
    }

    /**
     * Get Policy using policyId
     *
     * @param policyId policyId as a String
     * @return AbstractPolicy
     */
    public AbstractPolicy getPolicy(String policyId) {
        if (policies.containsKey(policyId)) {
            return policies.get(policyId).first();
        }
        return null;
    }

    /**
     * get All policies
     *
     * @return LinkedHashMap of policies
     */
    public LinkedHashMap<String, TreeSet<AbstractPolicy>> getPolicies() {
        return policies;
    }

    /**
     * Get Policy or Policy Set for given applicable policies
     *
     * @param policies applicable policies as array list
     * @return Policy or Policy Set as AbstractPolicy
     * @throws EntitlementException throws if no policy combiningAlg is defined
     */
    public AbstractPolicy getEffectivePolicy(ArrayList<AbstractPolicy> policies) throws EntitlementException {

        if ((combiningAlg == null) && (policies.size() > 0)) {
            log.error("Too many applicable top-level policies");
            throw new EntitlementException("Too many applicable top-level policies");
        }

        switch (policies.size()) {
            case 0:
                if (log.isDebugEnabled()) {
                    log.debug("No matching XACML policy found");
                }
                return null;
            case 1:
                return ((AbstractPolicy) (policies.get(0)));
            default:
                return new PolicySet(parentId, combiningAlg, target, policies);
        }
    }


    /**
     * Attempts to retrieve a policy based on the given identifier and other constraints. If there
     * are multiple versions of the identified policy that meet the version constraints, then the
     * most recent version is returned.
     *
     * @param identifier
     * @param type
     * @param constraints
     * @return
     */
    public AbstractPolicy getPolicy(URI identifier, int type, VersionConstraints constraints) {

        TreeSet<AbstractPolicy> set = policies.get(identifier.toString());

        // if we don't know about this identifier then there's nothing to do
        if (set == null)
            return null;

        // walk through the set starting with the most recent version, looking
        // for a match until we exhaust all known versions
        Iterator<AbstractPolicy> it = set.iterator();
        while (it.hasNext()) {
            AbstractPolicy policy = (AbstractPolicy) (it.next());
            if (constraints.meetsConstraint(policy.getVersion())) {
                // we found a valid version, so see if it's the right kind,
                // and if it is then we return it
                if (type == PolicyReference.POLICY_REFERENCE) {
                    if (policy instanceof Policy)
                        return policy;
                } else {
                    if (policy instanceof PolicySet)
                        return policy;
                }
            }
        }

        // we didn't find a match
        return null;
    }

    @Override
    public void setPolicyCombiningAlgorithm(PolicyCombiningAlgorithm algorithm) {

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == this.policies ? 0 : this.policies.hashCode());
        hash = 31 * hash + (null == this.combiningAlg ? 0 : this.combiningAlg.hashCode());
        return hash;
    }

    /**
     * A Comparator that is used within this class to maintain ordering amongst different versions
     * of the same policy. Note that it actually maintains reverse-ordering, since we want to
     * traverse the sets in decreasing, not increasing order.
     * <p/>
     * Note that this comparator is only used when there are multiple versions of the same policy,
     * which in practice will probably happen far less (from this class' point of view) than
     * additions or fetches.
     */
    static class VersionComparator implements Serializable, Comparator<AbstractPolicy> {

        private static final long serialVersionUID = 1136846256293162005L;

        public int compare(AbstractPolicy o1, AbstractPolicy o2) {
            // we swap the parameters so that sorting goes largest to smallest
            String v1 = ((AbstractPolicy) o2).getVersion();
            String v2 = ((AbstractPolicy) o1).getVersion();

            // do a quick check to see if the strings are equal (note that
            // even if the strings aren't equal, the versions can still
            // be equal)
            if (v1.equals(v2))
                return 0;

            // setup tokenizers, and walk through both strings one set of
            // numeric values at a time
            StringTokenizer tok1 = new StringTokenizer(v1, ".");
            StringTokenizer tok2 = new StringTokenizer(v2, ".");

            while (tok1.hasMoreTokens()) {
                // if there's nothing left in tok2, then v1 is bigger
                if (!tok2.hasMoreTokens())
                    return 1;

                // get the next elements in the version, convert to numbers,
                // and compare them (continuing with the loop only if the
                // two values were equal)
                int num1 = Integer.parseInt(tok1.nextToken());
                int num2 = Integer.parseInt(tok2.nextToken());

                if (num1 > num2)
                    return 1;

                if (num1 < num2)
                    return -1;
            }

            // if there's still something left in tok2, then it's bigger
            if (tok2.hasMoreTokens())
                return -1;

            // if we got here it means both versions had the same number of
            // elements and all the elements were equal, so the versions
            // are in fact equal
            return 0;
        }
    }
@Override
    public boolean deletePolicy(String policyId) {
        return false;
    }
@Override
    public LinkedHashMap getPolicyMap() {
        return this.policies;
    }

    @Override
    public void setPolicyMap(LinkedHashMap policyMap) {
        this.policies = policyMap ;
    }
}
