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
import org.wso2.carbon.identity.entitlement.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation of a policy finder module. This can be easily extended by any module
 * that support dynamic policy changes.
 */
public abstract class AbstractPolicyFinderModule implements PolicyFinderModule {

    private static Log log = LogFactory.getLog(AbstractPolicyFinderModule.class);

    /**
     * This method must be called by the module when its policies are updated
     */
    public static void invalidateCache(String policyId, String action) {
        EntitlementEngine.getInstance().getPolicyCache().invalidateCache(policyId, action);
        EntitlementEngine.getInstance().clearDecisionCache();
        EntitlementEngine.getInstance().getPolicySearch().clearCache();
    }

    public static void invalidateCache() {
        EntitlementEngine.getInstance().clearDecisionCache();
        EntitlementEngine.getInstance().invalidatePolicyCache();
        EntitlementEngine.getInstance().getPolicySearch().clearCache();
    }

    @Override
    public String[] getOrderedPolicyIdentifiers() {

        log.debug("Start retrieving ordered policy identifiers at : " + new Date());
        String[] policyIdentifiers = getPolicyIdentifiers();
        if (policyIdentifiers != null && !isPolicyOrderingSupport()) {
            PolicyStoreDTO[] policyDTOs = EntitlementAdminEngine.getInstance().
                    getPolicyStoreManager().getAllPolicyData();
            Arrays.sort(policyDTOs, new PolicyOrderComparator());
            List<String> list = new ArrayList<String>();
            List<String> finalList = new ArrayList<String>();
            // 1st put non -order items
            list.addAll(Arrays.asList(policyIdentifiers));
            for (PolicyStoreDTO dto : policyDTOs) {
                list.remove(dto.getPolicyId());
                finalList.add(dto.getPolicyId());
            }
            finalList.addAll(list);
            return finalList.toArray(new String[finalList.size()]);
        }
        log.debug("Finish retrieving ordered policy identifiers at : " + new Date());
        return policyIdentifiers;
    }

    @Override
    public String[] getActivePolicies() {
        log.debug("Start retrieving active policies at : " + new Date());
        List<String> policies = new ArrayList<String>();
        String[] policyIdentifiers = getOrderedPolicyIdentifiers();
        if (policyIdentifiers != null) {
            for (String identifier : policyIdentifiers) {
                if (!isPolicyDeActivationSupport()) {
                    PolicyStoreDTO data = EntitlementAdminEngine.getInstance().
                            getPolicyDataStore().getPolicyData(identifier);
                    if (data != null && data.isActive()) {
                        String policy = getPolicy(identifier);
                        if (policy != null) {
                            policies.add(policy);
                        }
                    }
                } else {
                    String policy = getPolicy(identifier);
                    if (policy != null) {
                        policies.add(policy);
                    }
                }
            }
        }
        log.debug("Finish retrieving active policies at : " + new Date());
        return policies.toArray(new String[policies.size()]);

    }


    @Override
    public boolean isDefaultCategoriesSupported() {
        return true;
    }

    @Override
    public boolean isPolicyOrderingSupport() {
        return false;
    }

    @Override
    public boolean isPolicyDeActivationSupport() {
        return false;
    }

    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier,
                                                              Set<AttributeDTO> givenAttribute) {
        return null;
    }

    @Override
    public int getSupportedSearchAttributesScheme() {
        return 0;
    }

    /**
     * @return
     */
    protected abstract String[] getPolicyIdentifiers();
}
