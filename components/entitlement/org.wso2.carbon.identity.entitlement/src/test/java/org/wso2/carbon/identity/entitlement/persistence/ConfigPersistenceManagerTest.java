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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.cache.ConfigCache;

import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.FIRST_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ONLY_ONE_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ORDERED_DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ORDERED_PERMIT_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.PERMIT_OVERRIDES;

/**
 * This is the parent test class for the Config Persistence Manager test classes.
 */
public abstract class ConfigPersistenceManagerTest {

    ConfigPersistenceManager configPersistenceManager;

    @Test
    public void testGetDefaultGlobalPolicyAlgorithmName() {

        String globalPolicyAlgorithmName = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(globalPolicyAlgorithmName, DENY_OVERRIDES);

        //Get policy object from the storage.
        PolicyCombiningAlgorithm globalPolicyAlgorithm = configPersistenceManager.getGlobalPolicyAlgorithm();
        PolicyCombiningAlgorithm expectedPolicyCombiningAlgorithm =
                EntitlementUtil.resolveGlobalPolicyAlgorithm(globalPolicyAlgorithmName);
        assertEquals(globalPolicyAlgorithm.getIdentifier(), expectedPolicyCombiningAlgorithm.getIdentifier());
    }

    @DataProvider
    public Object[][] globalPolicyAlgorithmData() {

        return new Object[][]{
                {DENY_OVERRIDES},
                {PERMIT_OVERRIDES},
                {FIRST_APPLICABLE},
                {ONLY_ONE_APPLICABLE},
                {ORDERED_DENY_OVERRIDES},
                {ORDERED_PERMIT_OVERRIDES}
        };
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testAddGlobalPolicyAlgorithm(String policyAlgorithmName) throws Exception {

        // Add the first global policy combining algorithm.
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmName);
        String policyAlgorithmNameFromStorage = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmNameFromStorage, policyAlgorithmName);
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testGetGlobalPolicyAlgorithmWhenCacheMisses(String policyAlgorithmName) throws Exception {

        // Add the first global policy combining algorithm.
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmName);
        // Clear the cache.
        ConfigCache.getInstance().clear(-1234);

        String policyAlgorithmNameFromStorage = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmNameFromStorage, policyAlgorithmName);
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testUpdateGlobalPolicyAlgorithm(String policyAlgorithmName) throws Exception {

        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(DENY_OVERRIDES);
        // Update the global policy combining algorithm.
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmName);

        String policyAlgorithmNameFromStorage = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmNameFromStorage, policyAlgorithmName);
    }

    public void deletePolicyCombiningAlgorithmInDatabase() throws EntitlementException {

        ConfigCache configCache = ConfigCache.getInstance();
        configCache.clear(-1234);

        String DELETE_POLICY_COMBINING_ALGORITHMS_SQL = "DELETE FROM IDN_XACML_CONFIG";
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement removePolicyCombiningAlgoPrepStmt = new NamedPreparedStatement(connection,
                    DELETE_POLICY_COMBINING_ALGORITHMS_SQL)) {
                removePolicyCombiningAlgoPrepStmt.execute();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while removing global policy combining algorithm in policy store", e);
        }
    }
}
