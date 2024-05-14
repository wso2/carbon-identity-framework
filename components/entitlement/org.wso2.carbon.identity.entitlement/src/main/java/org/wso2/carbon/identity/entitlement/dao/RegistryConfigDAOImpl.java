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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This implementation handles the Global PolicyDAO Combining Algorithm management in the Registry.
 */
public class RegistryConfigDAOImpl implements ConfigDAO {

    public static final String POLICY_COMBINING_PREFIX_1 = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:";
    public static final String POLICY_COMBINING_PREFIX_3 = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:";
    // The logger that is used for all messages
    private static final Log log = LogFactory.getLog(RegistryConfigDAOImpl.class);
    private static final String GLOBAL_POLICY_COMBINING_ALGORITHM = "globalPolicyCombiningAlgorithm";
    private final String policyDataCollection = PDPConstants.ENTITLEMENT_POLICY_DATA;
    private final Registry registry;

    public RegistryConfigDAOImpl() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }

    /**
     * Gets the global policy combining algorithm.
     *
     * @return global policy combining algorithm
     */
    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        String algorithm = null;
        try {
            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty(GLOBAL_POLICY_COMBINING_ALGORITHM);
            }

            if (algorithm == null || algorithm.trim().isEmpty()) {
                // Reads the algorithm from entitlement.properties file
                algorithm = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                        getProperty(PDPConstants.PDP_GLOBAL_COMBINING_ALGORITHM);
                log.info("The global policy combining algorithm which is defined in the configuration file, " +
                        "is used.");
                try {
                    return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
                } catch (Exception e) {
                    log.debug(e);
                }
            }

            if (algorithm != null && !algorithm.trim().isEmpty()) {
                if ("first-applicable".equals(algorithm) || "only-one-applicable".equals(algorithm)) {
                    algorithm = POLICY_COMBINING_PREFIX_1 + algorithm;
                } else {
                    algorithm = POLICY_COMBINING_PREFIX_3 + algorithm;
                }
                return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
            }

        } catch (RegistryException | EntitlementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while getting global policy combining algorithm from policy data store.", e);
            }
        }

        log.warn("Global policy combining algorithm is not defined. Therefore the default algorithm is used.");
        return new DenyOverridesPolicyAlg();
    }

    /**
     * Sets the global policy combining algorithm.
     *
     * @param policyCombiningAlgorithm policy combining algorithm name
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        try {
            Collection policyCollection;
            if (registry.resourceExists(policyDataCollection)) {
                policyCollection = (Collection) registry.get(policyDataCollection);
            } else {
                policyCollection = registry.newCollection();
            }

            policyCollection.setProperty(GLOBAL_POLICY_COMBINING_ALGORITHM, policyCombiningAlgorithm);
            registry.put(policyDataCollection, policyCollection);

            // Performs cache invalidation
            EntitlementEngine.getInstance().invalidatePolicyCache();

        } catch (RegistryException e) {
            log.error("Error while updating global policy combining algorithm in policy store ", e);
            throw new EntitlementException("Error while updating global policy combining algorithm in policy store");
        }
    }

    /**
     * Gets the policy combining algorithm name.
     *
     * @return global policy combining algorithm name
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {

        String algorithm = null;
        try {
            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty(GLOBAL_POLICY_COMBINING_ALGORITHM);
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }

        // set default
        if (algorithm == null) {
            algorithm = "deny-overrides";
        }

        return algorithm;
    }

}
