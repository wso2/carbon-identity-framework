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

/**
 * HybridConfigPersistenceManager is a hybrid implementation of ConfigPersistenceManager. It uses both JDBC and Registry
 * implementations to handle configuration data. Adding or updating a configuration will migrate the
 * configuration to the database.
 */
public class HybridConfigPersistenceManager implements ConfigPersistenceManager {

    private final JDBCConfigPersistenceManager jdbcConfigPersistenceManager = new JDBCConfigPersistenceManager();
    private final RegistryConfigPersistenceManager registryConfigPersistenceManager =
            new RegistryConfigPersistenceManager();
    private static final CacheBackedConfigDAO configDAO = CacheBackedConfigDAO.getInstance();
    private static final Log LOG = LogFactory.getLog(HybridConfigPersistenceManager.class);

    @Override
    public String getGlobalPolicyAlgorithmName() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String algorithm = null;
        try {
            algorithm = configDAO.getPolicyCombiningAlgorithm(tenantId);
        } catch (EntitlementException e) {
            LOG.debug(String.format("Error while getting Global Policy Combining Algorithm name from JDBC in tenant " +
                    "%s.", tenantId), e);
        }
        if (StringUtils.isBlank(algorithm)) {
            algorithm = registryConfigPersistenceManager.getGlobalPolicyAlgorithmName();
        }
        return algorithm;
    }

    @Override
    public boolean addOrUpdateGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        boolean isUpdate = jdbcConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyCombiningAlgorithm);
        if (!isUpdate) {
            try {
                registryConfigPersistenceManager.deleteGlobalPolicyAlgorithm();
            } catch (EntitlementException e) {
                LOG.debug("Error while deleting global policy combining algorithm from registry", e);
            }
        }
        return isUpdate;
    }
}
