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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;

/**
 * HybridConfigDAOImpl is a hybrid implementation of ConfigDAO. It uses both JDBC and Registry implementations to
 * handle configuration data. Adding or updating a configuration will migrate the configuration to the database.
 */
public class HybridConfigDAOImpl implements ConfigDAO {

    private final JDBCConfigDAOImpl jdbcConfigDAO = new JDBCConfigDAOImpl();
    private final RegistryConfigDAOImpl registryConfigDAO = new RegistryConfigDAOImpl();
    private static final Log LOG = LogFactory.getLog(HybridConfigDAOImpl.class);

    @Override
    public String getGlobalPolicyAlgorithmName() {

        // TODO: revisit in caching, make sure it's not skipped
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String algorithm = null;
        try {
            algorithm = jdbcConfigDAO.getPolicyCombiningAlgorithm(tenantId);
        } catch (EntitlementException e) {
            LOG.debug(String.format("Error while getting Global Policy Combining Algorithm name from JDBC in tenant " +
                            "%s.", tenantId), e);
        }
        if (StringUtils.isBlank(algorithm)) {
            algorithm = registryConfigDAO.getGlobalPolicyAlgorithmName();
        }
        return algorithm;
    }

    @Override
    public boolean addOrUpdateGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        boolean isUpdate = jdbcConfigDAO.addOrUpdateGlobalPolicyAlgorithm(policyCombiningAlgorithm);
        if (isUpdate) {
            try {
                registryConfigDAO.deleteGlobalPolicyAlgorithm();
            } catch (EntitlementException e) {
                LOG.debug("Error while deleting global policy combining algorithm from registry", e);
            }
        }
        return isUpdate;
    }
}
