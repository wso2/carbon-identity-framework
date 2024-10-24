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
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.Properties;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the Registry Policy Persistence Manager class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class RegistryPolicyPersistenceManagerTest extends PolicyPersistenceManagerTest {

    public PolicyPersistenceManager createPolicyPersistenceManager() {

        Properties storeProps = new Properties();
        policyPersistenceManager = new RegistryPolicyPersistenceManager();
        policyPersistenceManager.init(storeProps);
        return policyPersistenceManager;
    }

    @Test
    public void testIsPolicyExistsInPap() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        assertTrue(((RegistryPolicyPersistenceManager) policyPersistenceManager).
                isPolicyExistsInPap(samplePAPPolicy1.getPolicyId()));
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());

        assertFalse(((RegistryPolicyPersistenceManager) policyPersistenceManager).isPolicyExistsInPap(null));
    }
}
