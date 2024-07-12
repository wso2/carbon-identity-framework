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
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.FIRST_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ONLY_ONE_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_COMBINING_PREFIX_1;
import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_COMBINING_PREFIX_3;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.CONFIG_KEY;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.CONFIG_VALUE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_POLICY_COMBINING_ALGORITHM_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_POLICY_COMBINING_ALGORITHM_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.UPDATE_POLICY_COMBINING_ALGORITHM_SQL;

public class JDBCConfigDAOImpl implements ConfigDAO {

    private static final Log LOG = LogFactory.getLog(JDBCConfigDAOImpl.class);

    /**
     * Gets the policy combining algorithm of the PDP.
     *
     * @return policy combining algorithm.
     */
    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);

        try {
            String algorithm = getPolicyCombiningAlgorithm(connection, tenantId);

            if (StringUtils.isBlank(algorithm)) {
                // read algorithm from entitlement.properties file
                algorithm = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                        getProperty(PDPConstants.PDP_GLOBAL_COMBINING_ALGORITHM);
                LOG.info(String.format(
                        "Using Global policy combining algorithm that is defined in configuration file in " +
                                "tenant %s.", IdentityTenantUtil.getTenantDomain(tenantId)));
            } else {
                if (FIRST_APPLICABLE.equals(algorithm) || ONLY_ONE_APPLICABLE.equals(algorithm)) {
                    algorithm = POLICY_COMBINING_PREFIX_1 + algorithm;
                } else {
                    algorithm = POLICY_COMBINING_PREFIX_3 + algorithm;
                }
            }
            return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);

        } catch (EntitlementException e) {
            LOG.warn(e);
        }

        LOG.warn("Global policy combining algorithm is not defined. Therefore using default one");
        return new DenyOverridesPolicyAlg();
    }

    /**
     * Persists the policy combining algorithm into the data store.
     *
     * @param policyCombiningAlgorithm policy combining algorithm name to persist.
     * @throws EntitlementException throws if fails.
     */
    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection connection = IdentityDatabaseUtil.getDBConnection(true);

        try {
            // Check the existence of the algorithm
            String algorithm = getPolicyCombiningAlgorithm(connection, tenantId);
            String query = StringUtils.isNotBlank(algorithm)
                    ? UPDATE_POLICY_COMBINING_ALGORITHM_SQL
                    : CREATE_POLICY_COMBINING_ALGORITHM_SQL;

            try (NamedPreparedStatement setPolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                    query)) {
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_VALUE, policyCombiningAlgorithm);
                setPolicyCombiningAlgoPrepStmt.setInt(TENANT_ID, tenantId);
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_KEY, PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM);
                setPolicyCombiningAlgoPrepStmt.executeUpdate();
            }

            // performing cache invalidation
            EntitlementEngine.getInstance().invalidatePolicyCache();

            IdentityDatabaseUtil.commitTransaction(connection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while updating combing algorithm in policy store", e);
        }
    }

    /**
     * Gets the policy combining algorithm name of the PDP.
     *
     * @return policy combining algorithm name.
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {

        String algorithm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);

        algorithm = getPolicyCombiningAlgorithm(connection, tenantId);

        // set default
        return (StringUtils.isNotBlank(algorithm)) ? algorithm : DENY_OVERRIDES;
    }

    private String getPolicyCombiningAlgorithm(Connection connection, int tenantId) {

        String algorithm = null;

        try (NamedPreparedStatement getPolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                GET_POLICY_COMBINING_ALGORITHM_SQL)) {
            getPolicyCombiningAlgoPrepStmt.setInt(TENANT_ID, tenantId);
            getPolicyCombiningAlgoPrepStmt.setString(CONFIG_KEY, PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM);

            try (ResultSet rs = getPolicyCombiningAlgoPrepStmt.executeQuery()) {
                if (rs.next()) {
                    algorithm = rs.getString(CONFIG_VALUE);
                }
            }

        } catch (SQLException e) {
            LOG.debug("Error while getting Global Policy Combining Algorithm from policy data store.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return algorithm;
    }
}
