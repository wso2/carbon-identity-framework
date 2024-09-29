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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the JDBC Policy Persistence Manager class.
 */
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class JDBCPolicyPersistenceManagerTest extends PolicyPersistenceManagerTest {

    public PolicyPersistenceManager createPolicyPersistenceManager() {

        return new JDBCPolicyPersistenceManager();
    }

    @Test
    public void testIsPolicyExistsInPap() throws Exception {

        assertFalse(((JDBCPolicyPersistenceManager) policyPersistenceManager).isPolicyExistsInPap(null));
        assertFalse(((JDBCPolicyPersistenceManager) policyPersistenceManager).isPolicyExistsInPap(" "));
        assertFalse(((JDBCPolicyPersistenceManager) policyPersistenceManager).isPolicyExistsInPap(
                samplePAPPolicy1.getPolicyId()));

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        assertTrue(((JDBCPolicyPersistenceManager) policyPersistenceManager).
                isPolicyExistsInPap(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 3)
    public void testAddPAPPolicyNotFromPAP() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, false);
        assertNull(policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId()));
    }
}
