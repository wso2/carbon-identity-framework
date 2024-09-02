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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;

/**
 * This class tests the behavior of the HybridConfigPersistenceManager class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/h2.sql"})
public class HybridConfigPersistenceManagerTest extends ConfigPersistenceManagerTest {

    private JDBCConfigPersistenceManager jdbcConfigPersistenceManager;
    private RegistryConfigPersistenceManager registryConfigPersistenceManager;

    @BeforeMethod
    public void setUp() throws Exception {

        configPersistenceManager = new HybridConfigPersistenceManager();
        jdbcConfigPersistenceManager = new JDBCConfigPersistenceManager();
        registryConfigPersistenceManager = new RegistryConfigPersistenceManager();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        deletePolicyCombiningAlgorithmInDatabase();
        registryConfigPersistenceManager.deleteGlobalPolicyAlgorithm();
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testGetGlobalPolicyAlgorithmNameFromRegistry(String policyAlgorithmNameFromStorage) throws Exception {

        // Add the global policy combining algorithm.
        registryConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmNameFromStorage);
        String policyAlgorithmFromRegistry = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmFromRegistry, policyAlgorithmNameFromStorage);
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testGetGlobalPolicyAlgorithmNameFromDatabase(String policyAlgorithmNameFromStorage) throws Exception {

        // Add the global policy combining algorithm.
        jdbcConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmNameFromStorage);
        String policyAlgorithmFromDatabase = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmFromDatabase, policyAlgorithmNameFromStorage);
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testUpdateGlobalPolicyAlgorithmInRegistry(String policyAlgorithmNameFromStorage) throws Exception {

        registryConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(DENY_OVERRIDES);
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmNameFromStorage);
        // Verify that the global policy combining algorithm value was deleted from the registry.
        assertFalse(registryConfigPersistenceManager.isGlobalPolicyAlgorithmExist());

        String policyAlgorithmFromDatabase = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmFromDatabase, policyAlgorithmNameFromStorage);
    }

    @Test(dataProvider = "globalPolicyAlgorithmData")
    public void testUpdateGlobalPolicyAlgorithmInDatabase(String policyAlgorithmNameFromStorage) throws Exception {

        // Add the global policy combining algorithm.
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(DENY_OVERRIDES);
        assertFalse(registryConfigPersistenceManager.isGlobalPolicyAlgorithmExist());
        configPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(policyAlgorithmNameFromStorage);
        assertFalse(registryConfigPersistenceManager.isGlobalPolicyAlgorithmExist());
        String policyAlgorithmFromDatabase = configPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(policyAlgorithmFromDatabase, policyAlgorithmNameFromStorage);
    }
}
