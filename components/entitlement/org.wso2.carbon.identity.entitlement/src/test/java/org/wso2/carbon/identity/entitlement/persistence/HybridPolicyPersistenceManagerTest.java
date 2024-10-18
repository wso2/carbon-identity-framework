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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the Hybrid Policy Persistence Manager class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/h2.sql"})
public class HybridPolicyPersistenceManagerTest extends PolicyPersistenceManagerTest {

    private JDBCPolicyPersistenceManager jdbcPolicyPersistenceManager;
    private RegistryPolicyPersistenceManager registryPolicyPersistenceManager;

    public PolicyPersistenceManager createPolicyPersistenceManager() {

        Properties storeProps = new Properties();
        policyPersistenceManager = new HybridPolicyPersistenceManager();
        policyPersistenceManager.init(storeProps);
        jdbcPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
        registryPolicyPersistenceManager = new RegistryPolicyPersistenceManager();
        registryPolicyPersistenceManager.init(storeProps);
        return policyPersistenceManager;
    }

    @Test(priority = 13)
    public void testAddPAPPolicyInDb() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);

        PolicyDTO policyFromStorage = jdbcPolicyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(policyFromStorage.getPolicy(), samplePAPPolicy1.getPolicy());
        assertEquals(policyFromStorage.getPolicyId(), samplePAPPolicy1.getPolicyId());
        assertEquals(policyFromStorage.getVersion(), "1");
        assertTrue(jdbcPolicyPersistenceManager.isPolicyExistsInPap(samplePAPPolicy1.getPolicyId()));
        assertFalse(registryPolicyPersistenceManager.isPolicyExistsInPap(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 14)
    public void testDeletePAPPolicyInDb() throws Exception {

        jdbcPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());
        assertNull(policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
        assertNull(jdbcPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
        assertNull(registryPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 15)
    public void testDeletePAPPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());
        assertNull(policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
        assertNull(registryPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 16)
    public void testUpdatePAPPolicyInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(sampleUpdatedPAPPolicy1, true);

        // Verify weather the get policy method returning the updated policy.
        PolicyDTO updatedPolicy = policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(updatedPolicy.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        assertEquals(updatedPolicy.getPolicyId(), sampleUpdatedPAPPolicy1.getPolicyId());
        assertEquals(updatedPolicy.getVersion(), "2");

        // Verify weather the policy was updated in the database.
        PolicyDTO updatedPolicyFromDb = jdbcPolicyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromDb.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        assertEquals(updatedPolicyFromDb.getPolicyId(), sampleUpdatedPAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromDb.getVersion(), "2");

        // Verify weather get policy by version method returns the correct policy.
        PolicyDTO oldPolicy = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "1");
        assertEquals(oldPolicy.getPolicy(), samplePAPPolicy1.getPolicy());
        PolicyDTO oldPolicyFromDb = jdbcPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "1");
        assertEquals(oldPolicyFromDb.getPolicy(), samplePAPPolicy1.getPolicy());

        PolicyDTO newPolicy = policyPersistenceManager.getPolicy(sampleUpdatedPAPPolicy1.getPolicyId(), "2");
        assertEquals(newPolicy.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        PolicyDTO newPolicyFromDb = jdbcPolicyPersistenceManager.getPolicy(sampleUpdatedPAPPolicy1.getPolicyId(), "2");
        assertEquals(newPolicyFromDb.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());

        // Verify weather the total number of versions are correct.
        String[] policyVersions = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersions.length, 2);
        String[] policyVersionsFromDb = jdbcPolicyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersionsFromDb.length, 2);
    }

    @Test(priority = 17)
    public void testUpdatePAPPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(sampleUpdatedPAPPolicy1, true);

        // Verify weather the get policy method returning the updated policy.
        PolicyDTO updatedPolicy = policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(updatedPolicy.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        assertEquals(updatedPolicy.getPolicyId(), sampleUpdatedPAPPolicy1.getPolicyId());
        assertEquals(updatedPolicy.getVersion(), "2");

        // Verify weather the policy was updated in the registry.
        PolicyDTO updatedPolicyFromRegistry =
                registryPolicyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromRegistry.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        assertEquals(updatedPolicyFromRegistry.getPolicyId(), sampleUpdatedPAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromRegistry.getVersion(), "2");

        // Verify weather get policy by version method returns the correct policy.
        PolicyDTO policyVersion1 = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "1");
        assertEquals(policyVersion1.getPolicy(), samplePAPPolicy1.getPolicy());
        PolicyDTO policyVersion1FromRegistry =
                registryPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "1");
        assertEquals(policyVersion1FromRegistry.getPolicy(), samplePAPPolicy1.getPolicy());

        PolicyDTO policyVersion2 = policyPersistenceManager.getPolicy(sampleUpdatedPAPPolicy1.getPolicyId(), "2");
        assertEquals(policyVersion2.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        PolicyDTO policyVersion2FromRegistry =
                registryPolicyPersistenceManager.getPolicy(sampleUpdatedPAPPolicy1.getPolicyId(), "2");
        assertEquals(policyVersion2FromRegistry.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());

        // Verify weather the total number of versions are correct.
        String[] versions = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(versions.length, 2);
        String[] versionsFromRegistry = registryPolicyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(versionsFromRegistry.length, 2);
    }

    @Test(priority = 19)
    public void testAddPDPPolicyInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        assertTrue(jdbcPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        PolicyStoreDTO policyFromDb = jdbcPolicyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyFromDb.getPolicy(), samplePDPPolicy1.getPolicy());
        assertEquals(policyFromDb.getPolicyId(), samplePDPPolicy1.getPolicyId());

        policyPersistenceManager.deletePolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(jdbcPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 20)
    public void testAddPDPPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);

        assertTrue(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        // Verify weather the policy was added to the registry.
        assertTrue(registryPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));

        PolicyStoreDTO policyFromStorage = policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyFromStorage.getPolicy(), samplePDPPolicy1.getPolicy());
        assertEquals(policyFromStorage.getPolicyId(), samplePDPPolicy1.getPolicyId());
        PolicyStoreDTO policyFromRegistry =
                registryPolicyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyFromRegistry.getPolicy(), samplePDPPolicy1.getPolicy());
        assertEquals(policyFromRegistry.getPolicyId(), samplePDPPolicy1.getPolicyId());

        policyPersistenceManager.deletePolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        assertFalse(registryPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 21)
    public void testDeletePDPPolicyInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.deletePolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        assertFalse(jdbcPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 22)
    public void testDeletePDPPolicy() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.deletePolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        assertFalse(registryPolicyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 23)
    public void testGetReferencedPolicyInDb() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy2);

        // Verify the policies that are not active.
        assertNull(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy1.getPolicyId()));
        assertNull(jdbcPolicyPersistenceManager.getReferencedPolicy(samplePDPPolicy1.getPolicyId()));

        assertEquals(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy2.getPolicyId()),
                samplePDPPolicy2.getPolicy());
        assertEquals(jdbcPolicyPersistenceManager.getReferencedPolicy(samplePDPPolicy2.getPolicyId()),
                samplePDPPolicy2.getPolicy());
    }

    @Test(priority = 24)
    public void testGetReferencedPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);

        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy2);

        // Verify the policies that are not active.
        assertNull(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy1.getPolicyId()));
        assertNull(registryPolicyPersistenceManager.getReferencedPolicy(samplePDPPolicy1.getPolicyId()));

        assertEquals(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy2.getPolicyId()),
                samplePDPPolicy2.getPolicy());
        assertEquals(registryPolicyPersistenceManager.getReferencedPolicy(samplePDPPolicy2.getPolicyId()),
                samplePDPPolicy2.getPolicy());
    }

    @Test(priority = 25)
    public void testGetPolicyOrderInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the policy order.
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()), 0);
        assertEquals(jdbcPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()), 0);
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy3.getPolicyId()),
                samplePDPPolicy3.getPolicyOrder());
        assertEquals(jdbcPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy3.getPolicyId()),
                samplePDPPolicy3.getPolicyOrder());
    }

    @Test(priority = 26)
    public void testGetPolicyOrderInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the policy order.
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()), 0);
        assertEquals(registryPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()), 0);

        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy3.getPolicyId()),
                samplePDPPolicy3.getPolicyOrder());
        assertEquals(registryPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy3.getPolicyId()),
                samplePDPPolicy3.getPolicyOrder());
    }

    @Test(priority = 27)
    public void testListPDPPolicyInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy2);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the number of published policies.
        List<String> policyIds = policyPersistenceManager.listPublishedPolicyIds();
        assertEquals(policyIds.size(), 3);
        List<String> dbPolicyIds = jdbcPolicyPersistenceManager.listPublishedPolicyIds();
        assertEquals(dbPolicyIds.size(), 3);

        // Verify the number of ordered policy identifiers.
        String[] orderedPolicyIdentifiers = policyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicyIdentifiers.length, 3);
        String[] orderedPolicyIdentifiersFromDb = jdbcPolicyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicyIdentifiersFromDb.length, 3);

        // Verify the number of active policies.
        String[] activePolicies = policyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 2);
        String[] activePoliciesFromDb = jdbcPolicyPersistenceManager.getActivePolicies();
        assertEquals(activePoliciesFromDb.length, 2);
    }

    @Test(priority = 28)
    public void testListPDPPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy2);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the number of published policies.
        List<String> policyIds = policyPersistenceManager.listPublishedPolicyIds();
        assertEquals(policyIds.size(), 3);
        List<String> regPolicyIds = registryPolicyPersistenceManager.listPublishedPolicyIds();
        assertEquals(regPolicyIds.size(), 3);

        // Verify the number of ordered policy identifiers.
        String[] orderedPolicyIdentifiers = policyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicyIdentifiers.length, 3);
        String[] orderedPolicyIdentifiersFromRegistry = registryPolicyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicyIdentifiersFromRegistry.length, 3);

        // Verify the number of active policies.
        String[] activePolicies = policyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 2);
        String[] activePoliciesFromRegistry = registryPolicyPersistenceManager.getActivePolicies();
        assertEquals(activePoliciesFromRegistry.length, 2);
    }

    @Test(priority = 29)
    public void testUpdatePDPPolicyInDatabase() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        // Update Policy order.
        policyPersistenceManager.updatePolicy(orderedSamplePDPPolicy1);
        policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());
        assertEquals(jdbcPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());

        // Update Policy active status.
        policyPersistenceManager.updatePolicy(inactiveSamplePDPPolicy1);
        PolicyStoreDTO updatedPDPPolicy = policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(updatedPDPPolicy.isActive());
        PolicyStoreDTO updatedPDPPolicyFromDb =
                jdbcPolicyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(updatedPDPPolicyFromDb.isActive());
    }

    @Test(priority = 30)
    public void testUpdatePDPPolicyInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);

        // Update Policy order.
        policyPersistenceManager.updatePolicy(orderedSamplePDPPolicy1);
        policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());
        assertEquals(registryPolicyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());

        // Update Policy active status.
        policyPersistenceManager.updatePolicy(inactiveSamplePDPPolicy1);
        PolicyStoreDTO updatedPDPPolicy = policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(updatedPDPPolicy.isActive());
        PolicyStoreDTO updatedPDPPolicy1FromRegistry =
                registryPolicyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(updatedPDPPolicy1FromRegistry.isActive());
    }
}
