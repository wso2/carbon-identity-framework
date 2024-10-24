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
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the Hybrid Subscriber Persistence Manager class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class HybridSubscriberPersistenceManagerTest extends SubscriberPersistenceManagerTest {

    JDBCSubscriberPersistenceManager jdbcSubscriberPersistenceManager;
    RegistrySubscriberPersistenceManager registrySubscriberPersistenceManager;

    public SubscriberPersistenceManager createSubscriberPersistenceManager() {

        jdbcSubscriberPersistenceManager = new JDBCSubscriberPersistenceManager();
        registrySubscriberPersistenceManager = new RegistrySubscriberPersistenceManager();
        return new HybridSubscriberPersistenceManager();
    }

    @Test(priority = 5)
    public void testAddSubscriberViaHybridImpl() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        assertTrue(jdbcSubscriberPersistenceManager.isSubscriberExists(SAMPLE_SUBSCRIBER_ID_1));
        assertFalse(registrySubscriberPersistenceManager.isSubscriberExists(SAMPLE_SUBSCRIBER_ID_1));
    }

    @Test(priority = 6)
    public void testGetSubscriberInDatabase() throws Exception {

        jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder1);
        verifyGetSubscriberFromStorage();
    }

    @Test(priority = 7)
    public void testGetSubscriberInRegistry() throws Exception {

        registrySubscriberPersistenceManager.addSubscriber(sampleHolder1);
        verifyGetSubscriberFromStorage();
    }

    private void verifyGetSubscriberFromStorage() throws EntitlementException {

        PublisherDataHolder subscriberFromRegistry =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false);
        assertEquals(subscriberFromRegistry.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue());
        assertEquals(subscriberFromRegistry.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue());
        assertEquals(subscriberFromRegistry.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue());
        assertEquals(subscriberFromRegistry.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_ENCRYPTED_PASSWORD1);

        // Retrieve the subscriber with the decrypted secrets.
        PublisherDataHolder decryptedSubscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, true);
        assertEquals(decryptedSubscriberFromStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_SUBSCRIBER_PASSWORD_1);
    }

    @Test(priority = 8)
    public void listSubscriberIdsInDatabase() throws Exception {

        jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder1);
        jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder2);
        verifyListSubscriberIdsFromStorage();
    }

    @Test(priority = 9)
    public void listSubscriberIdsInRegistry() throws Exception {

        registrySubscriberPersistenceManager.addSubscriber(sampleHolder1);
        registrySubscriberPersistenceManager.addSubscriber(sampleHolder2);
        verifyListSubscriberIdsFromStorage();
    }

    private void verifyListSubscriberIdsFromStorage() throws EntitlementException {

        List<String> allSubscriberIds = subscriberPersistenceManager.listSubscriberIds("*");
        assertEquals(allSubscriberIds.size(), 2);

        List<String> filteredSubscriberIds1 = subscriberPersistenceManager.listSubscriberIds(SAMPLE_SUBSCRIBER_ID_1);
        assertEquals(filteredSubscriberIds1.size(), 1);

        List<String> filteredSubscriberIds2 = subscriberPersistenceManager.listSubscriberIds("test");
        assertEquals(filteredSubscriberIds2.size(), 0);
    }

    @Test(priority = 10)
    public void testUpdateSubscriberInDatabase() throws Exception {

        jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.updateSubscriber(updatedSampleHolder1);
        verifyUpdatedSubscriber(jdbcSubscriberPersistenceManager, registrySubscriberPersistenceManager);
    }

    @Test(priority = 11)
    public void testUpdateSubscriberInRegistry() throws Exception {

        registrySubscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.updateSubscriber(updatedSampleHolder1);
        verifyUpdatedSubscriber(registrySubscriberPersistenceManager, jdbcSubscriberPersistenceManager);
    }

    private void verifyUpdatedSubscriber(SubscriberPersistenceManager usedSubscriberManager,
                                         SubscriberPersistenceManager unusedSubscriberManager)
            throws EntitlementException {

        PublisherDataHolder subscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false);
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue(),
                updatedSampleHolder1.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue(),
                updatedSampleHolder1.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_ENCRYPTED_PASSWORD2);

        // Verify weather the subscriber was updated in the correct storage.
        PublisherDataHolder subscriberFromUsedStorage =
                usedSubscriberManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false);
        assertEquals(subscriberFromUsedStorage.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue());
        assertEquals(subscriberFromUsedStorage.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue(),
                updatedSampleHolder1.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue());
        assertEquals(subscriberFromUsedStorage.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue(),
                updatedSampleHolder1.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue());
        assertEquals(subscriberFromUsedStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_ENCRYPTED_PASSWORD2);

        // Verify weather the subscriber was not updated in the other storage.
        assertThrows(EntitlementException.class,
                () -> unusedSubscriberManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false));
    }

    @Test(priority = 12)
    public void testRemoveSubscriberInDatabase() throws Exception {

        jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.removeSubscriber(SAMPLE_SUBSCRIBER_ID_1);
        verifyRemoveSubscriberFromStorage();
    }

    @Test(priority = 13)
    public void testRemoveSubscriberInRegistry() throws Exception {

        registrySubscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.removeSubscriber(SAMPLE_SUBSCRIBER_ID_1);
        verifyRemoveSubscriberFromStorage();
    }

    private void verifyRemoveSubscriberFromStorage() throws EntitlementException {

        assertThrows(EntitlementException.class,
                () -> subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false));
        assertFalse(jdbcSubscriberPersistenceManager.isSubscriberExists(SAMPLE_SUBSCRIBER_ID_1));
        assertFalse(registrySubscriberPersistenceManager.isSubscriberExists(SAMPLE_SUBSCRIBER_ID_1));
    }
}
