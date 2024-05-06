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

package org.wso2.carbon.identity.entitlement.policy.finder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderResult;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.cache.PolicyStatus;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.ConfigDAO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.policy.collection.SimplePolicyCollection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Policy finder of the WSO2 entitlement engine.  This an implementation of <code>PolicyFinderModule</code>
 * of Balana engine. Extensions can be plugged with this.
 */
public class CarbonPolicyFinder extends org.wso2.balana.finder.PolicyFinderModule {

    private static Log log = LogFactory.getLog(CarbonPolicyFinder.class);
    public PolicyReader policyReader;
    private List<PolicyFinderModule> finderModules = null;
    private PolicyCollection policyCollection;

    private List<PolicyDTO> policyCollectionOrder = new ArrayList<PolicyDTO>();

    private PolicyFinder finder;
    /**
     * this is a flag to keep whether init it has finished or not.
     */
    private volatile boolean initFinish;
    private LinkedHashMap<URI, AbstractPolicy> policyReferenceCache = null;
    private int maxReferenceCacheEntries = PDPConstants.MAX_NO_OF_IN_MEMORY_POLICIES;


    @Override
    public void init(PolicyFinder finder) {
        initFinish = false;
        this.finder = finder;
        try {
            init();
        } catch (EntitlementException e) {
            throw new RuntimeException(e);
        }
        policyReferenceCache.clear();
    }

    private synchronized void init() throws EntitlementException {

        if (initFinish) {
            return;
        }

        log.info("Initializing of policy store is started at :  " + new Date());

        String maxEntries = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.MAX_POLICY_REFERENCE_ENTRIES);

        if (maxEntries != null) {
            try {
                maxReferenceCacheEntries = Integer.parseInt(maxEntries.trim());
            } catch (Exception e) {
                //ignore
            }
        }

        policyReferenceCache = new LinkedHashMap<URI, AbstractPolicy>() {

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                // oldest entry of the cache would be removed when max cache size become, i.e 50
                return size() > maxReferenceCacheEntries;
            }

        };

        PolicyCombiningAlgorithm policyCombiningAlgorithm = null;
        // get registered finder modules
        Map<PolicyFinderModule, Properties> finderModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyFinderModules();

        if (finderModules != null) {
            this.finderModules = new ArrayList<PolicyFinderModule>(finderModules.keySet());
        }

        PolicyCollection tempPolicyCollection = null;

        // get policy collection
        Map<PolicyCollection, Properties> policyCollections = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyCollections();
        if (policyCollections != null && policyCollections.size() > 0) {
            tempPolicyCollection = policyCollections.entrySet().iterator().next().getKey();
        } else {
            tempPolicyCollection = new SimplePolicyCollection();
        }

        // get policy reader
        policyReader = PolicyReader.getInstance(finder);

        if (this.finderModules != null && this.finderModules.size() > 0) {
            // find policy combining algorithm.

            ConfigDAO config = new RegistryConfigDAOImpl();
            policyCombiningAlgorithm = config.getGlobalPolicyAlgorithm();

            tempPolicyCollection.setPolicyCombiningAlgorithm(policyCombiningAlgorithm);

            for (PolicyFinderModule finderModule : this.finderModules) {
                log.info("Start retrieving policies from " + finderModule + " at : " + new Date());
                String[] policies = finderModule.getActivePolicies();
                for (int a = 0; a < policies.length; a++) {
                    String policy = policies[a];
                    AbstractPolicy abstractPolicy = policyReader.getPolicy(policy);
                    if (abstractPolicy != null) {
                        PolicyDTO policyDTO = new PolicyDTO();
                        policyDTO.setPolicyId(abstractPolicy.getId().toString());
                        policyDTO.setPolicyOrder(a);
                        policyCollectionOrder.add(policyDTO);
                        tempPolicyCollection.addPolicy(abstractPolicy);
                    }
                }
                log.info("Finish retrieving policies from " + finderModule + " at : " + new Date());
            }
        } else {
            log.warn("No Carbon policy finder modules are registered");

        }

        policyCollection = tempPolicyCollection;
        initFinish = true;
        log.info("Initializing of policy store is finished at :  " + new Date());
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier();
    }

    @Override
    public boolean isRequestSupported() {
        return true;
    }

    @Override
    public boolean isIdReferenceSupported() {
        return true;
    }


    private void orderPolicyCache() {
        LinkedHashMap<URI, AbstractPolicy> policyMap = policyCollection.getPolicyMap();
        Collections.sort(policyCollectionOrder, new PolicyOrderComparator());
        LinkedHashMap<URI, AbstractPolicy> newPolicyMap = new LinkedHashMap<URI, AbstractPolicy>();
        Iterator<PolicyDTO> policyDTOIterator = policyCollectionOrder.iterator();
        while (policyDTOIterator.hasNext()) {
            try {
                URI policyURI = new URI(policyDTOIterator.next().getPolicyId());
                newPolicyMap.put(policyURI, policyMap.get(policyURI));

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        if (EntitlementEngine.getInstance().getPolicyCache().isInvalidate()) {

            init(this.finder);
            policyReferenceCache.clear();
            EntitlementEngine.getInstance().clearDecisionCache();
            EntitlementEngine.getInstance().resetCacheInvalidateState();
            if (log.isDebugEnabled()) {
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                log.debug("Invalidation cache message is received. " +
                          "Re-initialized policy finder module of current node and invalidate decision " +
                          "caching for tenantId : " + tenantId);
            }
        } else {
            Collection<PolicyStatus> policies =
                    EntitlementEngine.getInstance().getPolicyCache().getInvalidatedPolicies();
            if (policies != null) {
                if (policies.size() > 0) {
                    synchronized (policies) {
                        boolean isReorder = false;
                        policyReferenceCache.clear();
                        EntitlementEngine.getInstance().clearDecisionCache();
                        for (PolicyStatus policyStatus : policies) {

                            if (EntitlementConstants.PolicyPublish.ACTION_DELETE
                                    .equals(policyStatus.getPolicyAction())) {
                                policyCollection.deletePolicy(policyStatus.getPolicyId());
                                policyCollectionOrder.remove(new PolicyDTO(policyStatus.getPolicyId()));
                            } else if (EntitlementConstants.PolicyPublish.ACTION_UPDATE
                                    .equals(policyStatus.getPolicyAction())) {
                                AbstractPolicy abstractPolicy = loadPolicy(policyStatus.getPolicyId());
                                policyCollection.addPolicy(abstractPolicy);
                            } else if (EntitlementConstants.PolicyPublish.ACTION_CREATE
                                    .equals(policyStatus.getPolicyAction())) {
                                AbstractPolicy abstractPolicy = loadPolicy(policyStatus.getPolicyId());
                                policyCollection.addPolicy(abstractPolicy);
                                isReorder = true;
                            } else if (EntitlementConstants.PolicyPublish.ACTION_ORDER
                                    .equals(policyStatus.getPolicyAction())) {
                                int order = getPolicyOrder(policyStatus.getPolicyId());
                                if (order != -1) {
                                    PolicyDTO policyDTO = new PolicyDTO(policyStatus.getPolicyId());
                                    if (policyCollectionOrder.indexOf(policyDTO) != -1) {
                                        policyCollectionOrder.get(policyCollectionOrder.indexOf(policyDTO))
                                                .setPolicyOrder(order);
                                        isReorder = true;
                                    }
                                }
                            }

                        }
                        if (isReorder) {
                            orderPolicyCache();
                        }
                        policies.clear();
                    }


                }
            }
        }

        try {
            AbstractPolicy policy = policyCollection.getEffectivePolicy(context);
            if (policy == null) {
                return new PolicyFinderResult();
            } else {
                return new PolicyFinderResult(policy);
            }
        } catch (EntitlementException e) {
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, e.getMessage());
            return new PolicyFinderResult(status);
        }
    }


    private AbstractPolicy loadPolicy(String policyId) {
        if (this.finderModules != null) {
            for (PolicyFinderModule finderModule : this.finderModules) {
                String policyString = finderModule.getPolicyString(policyId);
                if (policyString != null) {
                    AbstractPolicy policy = policyReader.getPolicy(policyString);
                    if (policy != null) {
                        return policy;
                    }
                }
            }
        }
        return null;
    }

    private int getPolicyOrder(String policyId) {
        int order = -1;
        if (this.finderModules != null) {

            for (PolicyFinderModule finderModule : this.finderModules) {
                if ((order = finderModule.getPolicyOrder(policyId)) != -1) {
                    break;
                }
            }
        }
        return order;
    }

    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {

        AbstractPolicy policy = policyReferenceCache.get(idReference);

        if (policy == null) {
            if (this.finderModules != null) {
                for (PolicyFinderModule finderModule : this.finderModules) {
                    String policyString = finderModule.getReferencedPolicy(idReference.toString());
                    if (policyString != null) {
                        policy = policyReader.getPolicy(policyString);
                        if (policy != null) {
                            policyReferenceCache.put(idReference, policy);
                            break;
                        }
                    }
                }
            }
        }

        if (policy != null) {
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy) {
                    return new PolicyFinderResult(policy);
                }
            } else {
                if (policy instanceof PolicySet) {
                    return new PolicyFinderResult(policy);
                }
            }
        }

        return new PolicyFinderResult();
    }

    public void clearPolicyCache() {
        EntitlementEngine.getInstance().getPolicyCache().clear();
    }
}
