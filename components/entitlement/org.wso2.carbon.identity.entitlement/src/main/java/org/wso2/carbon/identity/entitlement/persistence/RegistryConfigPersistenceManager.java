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

package org.wso2.carbon.identity.entitlement.persistence;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.wso2.carbon.identity.entitlement.PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM;

/**
 * This implementation handles the Global Policy Combining Algorithm management in the Registry.
 */
public class RegistryConfigPersistenceManager implements ConfigPersistenceManager {

    // The logger that is used for all messages
    private static final Log LOG = LogFactory.getLog(RegistryConfigPersistenceManager.class);
    private static final String POLICY_DATA_COLLECTION = PDPConstants.ENTITLEMENT_POLICY_DATA;
    private final Registry registry;

    public RegistryConfigPersistenceManager() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }

    /**
     * Sets the global policy combining algorithm.
     *
     * @param policyCombiningAlgorithm policy combining algorithm name.
     * @return true if the policy combining algorithm is updated, false if the policy combining algorithm is added.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addOrUpdateGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        try {
            Collection policyCollection;
            if (registry.resourceExists(POLICY_DATA_COLLECTION)) {
                policyCollection = (Collection) registry.get(POLICY_DATA_COLLECTION);
            } else {
                policyCollection = registry.newCollection();
            }
            policyCollection.setProperty(GLOBAL_POLICY_COMBINING_ALGORITHM, policyCombiningAlgorithm);
            registry.put(POLICY_DATA_COLLECTION, policyCollection);
        } catch (RegistryException e) {
            throw new EntitlementException("Error while updating global policy combining algorithm in policy store", e);
        }
    }

    /**
     * Gets the policy combining algorithm name.
     *
     * @return global policy combining algorithm name
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {

        String algorithm = getGlobalPolicyAlgorithmValue();

        // set default
        if (algorithm == null) {
            algorithm = PDPConstants.Algorithms.DENY_OVERRIDES;
        }

        return algorithm;
    }

    private String getGlobalPolicyAlgorithmValue() {

        String algorithm = null;
        try {
            if (registry.resourceExists(POLICY_DATA_COLLECTION)) {
                Collection collection = (Collection) registry.get(POLICY_DATA_COLLECTION);
                algorithm = collection.getProperty(GLOBAL_POLICY_COMBINING_ALGORITHM);
            }
        } catch (RegistryException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
        return algorithm;
    }

    /**
     * Deletes the global policy combining algorithm.
     *
     * @throws EntitlementException If an error occurs
     */
    public void deleteGlobalPolicyAlgorithm() throws EntitlementException {

        try {
            if (registry.resourceExists(POLICY_DATA_COLLECTION)) {
                registry.delete(POLICY_DATA_COLLECTION);
            }
        } catch (RegistryException e) {
            throw new EntitlementException("Error while deleting global policy combining algorithm in policy store", e);
        }
    }

    boolean isGlobalPolicyAlgorithmExist() {

        return getGlobalPolicyAlgorithmValue() != null;

    }
}
