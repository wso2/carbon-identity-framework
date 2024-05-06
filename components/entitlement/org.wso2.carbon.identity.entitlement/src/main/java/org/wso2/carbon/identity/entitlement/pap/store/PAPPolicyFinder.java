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
package org.wso2.carbon.identity.entitlement.pap.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.OnlyOneApplicablePolicyAlg;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.PolicyFinderResult;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.policy.collection.DefaultPolicyCollection;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PAPPolicyFinder extends PolicyFinderModule {

    // the logger we'll use for all messages
    private static final Log log = LogFactory.getLog(PAPPolicyFinder.class);
    // the list of policy URLs passed to the constructor
    private final PAPPolicyStoreReader policyReader;
    // the map of policies
    private DefaultPolicyCollection policies;
    //keeps policy ids according to the order
    private List<String> policyIds;
    private PolicyFinder policyFinder;


    /**
     * Creates a PAPPolicyFinder that provides access to the given collection of policies.
     * Any policy that cannot be loaded will be noted in the log, but will not cause an error. The
     * schema file used to validate policies is defined by the property
     * PolicyRepository.POLICY_SCHEMA_PROPERTY. If the retrieved property is null, then no schema
     * validation will occur.
     *
     * @param policyReader Policy store repository
     */
    public PAPPolicyFinder(PAPPolicyStoreReader policyReader) {
        this.policyReader = policyReader;
    }

    /**
     * Always returns <code>true</code> since this module does support finding policies based on
     * reference.
     *
     * @return true
     */
    public boolean isIdReferenceSupported() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.PolicyFinderModule#isRequestSupported()
     */
    public boolean isRequestSupported() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.PolicyFinderModule#init(org.wso2.balana.finder.CarbonPolicyFinder)
     */
    public void init(PolicyFinder finder) {

        PolicyCombiningAlgorithm algorithm;
        this.policyFinder = finder;

        try {
            // for PAP policy store, Global policy combining algorithm is not needed. As we are
            // only evaluating one policy therefore using default algorithm.
            algorithm = new OnlyOneApplicablePolicyAlg();
            initPolicyIds();
            this.policies = new DefaultPolicyCollection(algorithm, 0);
        } catch (EntitlementException e) {
            log.error("Error while initializing PAPPolicyFinder", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.PolicyFinderModule#findPolicy(java.net.URI, int,
     * org.wso2.balana.VersionConstraints, org.wso2.balana.PolicyMetaData)
     */
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {

        // clear all current policies
        policies.getPolicies().clear();

        AbstractPolicy policy = null;

        try {
            AbstractPolicy policyFromStore = policyReader.readPolicy(idReference.toString(),
                    this.policyFinder);

            if (policyFromStore != null) {
                if (type == PolicyReference.POLICY_REFERENCE) {
                    if (policyFromStore instanceof Policy) {
                        policy = policyFromStore;
                        policies.addPolicy(policy);
                    }
                } else {
                    if (policyFromStore instanceof PolicySet) {
                        policy = policyFromStore;
                        policies.addPolicy(policy);
                    }
                }
            }
        } catch (EntitlementException e) {
            // ignore and just log the error.
            log.error(e);
        }

        if (policy == null) {
            return new PolicyFinderResult();
        } else {
            return new PolicyFinderResult(policy);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.PolicyFinderModule#findPolicy(org.wso2.balana.EvaluationCtx)
     */
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        // clear all current policies
        policies.getPolicies().clear();

        ArrayList<AbstractPolicy> list = new ArrayList<>();

        try {
            for (String policyId : policyIds) {

                if (list.size() == PDPConstants.DEFAULT_MAX_NO_OF_IN_MEMORY_POLICIES) {
                    break;
                }
                AbstractPolicy policy = null;

                try {
                    policy = policyReader.readPolicy(policyId, this.policyFinder);
                } catch (EntitlementException e) {
                    //log and ignore
                    log.error(e);
                }
                if (policy == null) {
                    continue;
                } else {
                    policies.addPolicy(policy);
                }
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
                    if (log.isDebugEnabled()) {
                        log.debug("Matching XACML policy found " + policy.getId().toString());
                    }
                    list.add(policy);
                }
            }

            AbstractPolicy policy = policies.getEffectivePolicy(list);
            if (policy == null) {
                return new PolicyFinderResult();
            } else {
                return new PolicyFinderResult(policy);
            }
        } catch (EntitlementException e) {
            ArrayList<String> code = new ArrayList<>();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, e.getMessage());
            return new PolicyFinderResult(status);
        }
    }


    /**
     * Sets polices ids that is evaluated
     *
     * @param policyIds policyIds
     */
    public void setPolicyIds(List<String> policyIds) {
        this.policyIds = policyIds;
    }

    public void initPolicyIds() throws EntitlementException {
        this.policyIds = new ArrayList<>();
        PolicyDTO[] policyDTOs = policyReader.readAllLightPolicyDTOs();
        for (PolicyDTO dto : policyDTOs) {
            if (dto.isActive()) {
                policyIds.add(dto.getPolicyId());
            }
        }
    }
}
