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

package org.wso2.carbon.identity.entitlement.policy.finder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.persistence.HybridPolicyPersistenceManager;
import org.wso2.carbon.identity.entitlement.persistence.JDBCPolicyPersistenceManager;
import org.wso2.carbon.identity.entitlement.persistence.RegistryPolicyPersistenceManager;

import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * This class tests the behavior of the JDBC Policy Persistence Manager class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class HybridPolicyPersistenceManagerTest extends PolicyPersistenceManagerTest {

    private JDBCPolicyPersistenceManager jdbcPolicyPersistenceManager;
    private RegistryPolicyPersistenceManager registryPolicyPersistenceManager;

    @BeforeMethod
    public void setUp() throws Exception {

        Properties storeProps = new Properties();
        policyPersistenceManager = new HybridPolicyPersistenceManager();
        policyPersistenceManager.init(storeProps);
        jdbcPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
        registryPolicyPersistenceManager = new RegistryPolicyPersistenceManager();
        registryPolicyPersistenceManager.init(storeProps);
    }

    @Test
    public void testGetPolicyIdentifiersInDb() throws Exception {

        jdbcPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        jdbcPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        jdbcPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        String[] policyIdentifiersBeforePublishing = ((AbstractPolicyFinderModule) policyPersistenceManager)
                .getPolicyIdentifiers();
        assertEquals(policyIdentifiersBeforePublishing.length, 0);
        String[] policyIdentifiersBeforePublishingInDb = ((AbstractPolicyFinderModule) jdbcPolicyPersistenceManager)
                .getPolicyIdentifiers();
        assertEquals(policyIdentifiersBeforePublishingInDb.length, 0);

        jdbcPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        jdbcPolicyPersistenceManager.addPolicy(samplePDPPolicy2);
        jdbcPolicyPersistenceManager.addPolicy(samplePDPPolicy3);

        String[] policyIdentifiersAfterPublishing = ((AbstractPolicyFinderModule) policyPersistenceManager).
                getPolicyIdentifiers();
        assertEquals(policyIdentifiersAfterPublishing.length, 3);
        String[] policyIdentifiersAfterPublishingInDb = ((AbstractPolicyFinderModule) jdbcPolicyPersistenceManager).
                getPolicyIdentifiers();
        assertEquals(policyIdentifiersAfterPublishingInDb.length, 3);
    }

    @Test
    public void testGetPolicyIdentifiersInRegistry() throws Exception {

        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        String[] policyIdentifiersBeforePublishing = ((AbstractPolicyFinderModule) policyPersistenceManager)
                .getPolicyIdentifiers();
        assertEquals(policyIdentifiersBeforePublishing.length, 0);
        String[] policyIdentifiersBeforePublishingInRegistry = ((AbstractPolicyFinderModule) policyPersistenceManager)
                .getPolicyIdentifiers();
        assertEquals(policyIdentifiersBeforePublishingInRegistry.length, 0);

        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy2);
        registryPolicyPersistenceManager.addPolicy(samplePDPPolicy3);

        String[] policyIdentifiersAfterPublishing = ((AbstractPolicyFinderModule) registryPolicyPersistenceManager).
                getPolicyIdentifiers();
        assertEquals(policyIdentifiersAfterPublishing.length, 3);
        String[] policyIdentifiersAfterPublishingInRegistry =
                ((AbstractPolicyFinderModule) registryPolicyPersistenceManager).
                        getPolicyIdentifiers();
        assertEquals(policyIdentifiersAfterPublishingInRegistry.length, 3);
    }
}
