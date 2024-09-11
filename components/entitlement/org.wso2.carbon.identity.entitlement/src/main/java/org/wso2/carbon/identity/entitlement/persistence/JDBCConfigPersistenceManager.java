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
import org.wso2.carbon.identity.entitlement.persistence.cache.CacheBackedConfigDAO;

import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;

/**
 * This class handles the JDBC operations related to the global policy combining algorithm.
 */
public class JDBCConfigPersistenceManager implements ConfigPersistenceManager {

    private static final Log LOG = LogFactory.getLog(JDBCConfigPersistenceManager.class);
    private static final CacheBackedConfigDAO configDAO = CacheBackedConfigDAO.getInstance();

    /**
     * Gets the policy combining algorithm name of the PDP.
     *
     * @return policy combining algorithm name.
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String algorithm = null;
        try {
            algorithm = configDAO.getPolicyCombiningAlgorithm(tenantId);
        } catch (EntitlementException e) {
            LOG.debug(String.format("Error while getting Global Policy Combining Algorithm name from JDBC in tenant " +
                    "%s. Default algorithm name will be returned.", tenantId), e);
        }
        if (StringUtils.isBlank(algorithm)) {
            algorithm = DENY_OVERRIDES;
        }

        return algorithm;
    }

    /**
     * Persists the policy combining algorithm into the data store.
     *
     * @param policyCombiningAlgorithm policy combining algorithm name to persist.
     * @return true if the policy combining algorithm is updated, false if the policy combining algorithm is added.
     * @throws EntitlementException throws if fails.
     */
    @Override
    public void addOrUpdateGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        // Check the existence of the algorithm
        String algorithm = null;
        try {
            algorithm = configDAO.getPolicyCombiningAlgorithm(tenantId);
        } catch (EntitlementException e) {
            LOG.debug(String.format("Error while getting Global Policy Combining Algorithm name from JDBC in tenant " +
                    "%s.", tenantId), e);
        }
        if (StringUtils.isBlank(algorithm)) {
            configDAO.insertPolicyCombiningAlgorithm(policyCombiningAlgorithm, tenantId);
        } else {
            configDAO.updatePolicyCombiningAlgorithm(policyCombiningAlgorithm, tenantId);
        }
    }
}
