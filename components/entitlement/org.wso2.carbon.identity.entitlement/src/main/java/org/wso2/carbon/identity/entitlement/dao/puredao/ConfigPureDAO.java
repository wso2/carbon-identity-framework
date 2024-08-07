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

package org.wso2.carbon.identity.entitlement.dao.puredao;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.CONFIG_KEY;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.CONFIG_VALUE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_POLICY_COMBINING_ALGORITHM_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_POLICY_COMBINING_ALGORITHM_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.UPDATE_POLICY_COMBINING_ALGORITHM_SQL;

/**
 * This class handles the JDBC operations related to the global policy combining algorithm.
 */
public class ConfigPureDAO {

    private static final ConfigPureDAO instance = new ConfigPureDAO();

    public static ConfigPureDAO getInstance() {

        return instance;
    }

    /**
     * DAO method to get the policy combining algorithm from the data store.
     *
     * @return policy combining algorithm.
     */
    public String getPolicyCombiningAlgorithm(int tenantId) throws EntitlementException {

        String algorithm = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement getPolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                    GET_POLICY_COMBINING_ALGORITHM_SQL)) {
                getPolicyCombiningAlgoPrepStmt.setString(CONFIG_KEY, PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM);
                getPolicyCombiningAlgoPrepStmt.setInt(TENANT_ID, tenantId);
                try (ResultSet rs = getPolicyCombiningAlgoPrepStmt.executeQuery()) {
                    if (rs.next()) {
                        algorithm = rs.getString(CONFIG_VALUE);
                    }
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException(
                    "Error while getting Global Policy Combining Algorithm from policy data store.", e);
        }
        return algorithm;
    }

    /**
     * DAO method to set the policy combining algorithm in the data store.
     *
     * @param policyCombiningAlgorithm policy combining algorithm to set.
     * @param tenantId                 tenant id.
     * @throws EntitlementException throws if fails.
     */
    public void insertPolicyCombiningAlgorithm(String policyCombiningAlgorithm, int tenantId)
            throws EntitlementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement setPolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                    CREATE_POLICY_COMBINING_ALGORITHM_SQL)) {
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_KEY, PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM);
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_VALUE, policyCombiningAlgorithm);
                setPolicyCombiningAlgoPrepStmt.setInt(TENANT_ID, tenantId);
                setPolicyCombiningAlgoPrepStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while adding global policy combining algorithm in policy store", e);
        }
    }

    /**
     * DAO method to update the policy combining algorithm in the data store.
     *
     * @param policyCombiningAlgorithm policy combining algorithm to update.
     * @param tenantId                 tenant id.
     * @throws EntitlementException throws if fails.
     */
    public void updatePolicyCombiningAlgorithm(String policyCombiningAlgorithm, int tenantId)
            throws EntitlementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement setPolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                    UPDATE_POLICY_COMBINING_ALGORITHM_SQL)) {
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_KEY, PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM);
                setPolicyCombiningAlgoPrepStmt.setString(CONFIG_VALUE, policyCombiningAlgorithm);
                setPolicyCombiningAlgoPrepStmt.setInt(TENANT_ID, tenantId);
                setPolicyCombiningAlgoPrepStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while updating global policy combining algorithm in policy store", e);
        }
    }
}
