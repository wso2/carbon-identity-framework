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

import org.apache.commons.codec.Charsets;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.PDP_SUBSCRIBER_ID;

/**
 * This class tests the behavior of the Subscriber Persistence Manager class.
 */
public abstract class SubscriberPersistenceManagerTest {

    public SubscriberPersistenceManager subscriberPersistenceManager;
    private CryptoUtil mockCryptoUtil;
    private MockedStatic<CryptoUtil> cryptoUtil;

    static final String SUBSCRIBER_MODULE_NAME = "Carbon Basic Auth Policy Publisher Module";
    static final String SUBSCRIBER_ID_KEY = "subscriberId";
    static final String SUBSCRIBER_ID_DISPLAY_NAME = "Subscriber Id";
    static final String SAMPLE_SUBSCRIBER_ID_1 = "Subscriber1";
    static final String SAMPLE_SUBSCRIBER_ID_2 = "Subscriber2";
    static final String SUBSCRIBER_URL_KEY = "subscriberURL";
    static final String SUBSCRIBER_URL_DISPLAY_NAME = "Subscriber URL";
    static final String SAMPLE_SUBSCRIBER_URL_1 = "https://localhost:9443/subscriber1";
    static final String SAMPLE_SUBSCRIBER_URL_2 = "https://localhost:9443/subscriber2";
    static final String SUBSCRIBER_USERNAME_KEY = "subscriberUserName";
    static final String SUBSCRIBER_USERNAME_DISPLAY_NAME = "Subscriber User Name";
    static final String SAMPLE_SUBSCRIBER_USERNAME_1 = "admin_user1";
    static final String SAMPLE_SUBSCRIBER_USERNAME_2 = "admin_user2";
    static final String SUBSCRIBER_PASSWORD_DISPLAY_NAME = "Subscriber Password";
    static final String SUBSCRIBER_PASSWORD_KEY = "subscriberPassword";
    static final String SAMPLE_SUBSCRIBER_PASSWORD_1 = "admin_password1";
    static final String SAMPLE_SUBSCRIBER_PASSWORD_2 = "admin_password2";
    static final String SAMPLE_ENCRYPTED_PASSWORD1 = "encrypted_admin_password1";
    static final String SAMPLE_ENCRYPTED_PASSWORD2 = "encrypted_admin_password2";
    static final String NEW_MODULE_NAME = "New Updated Module";

    public PublisherDataHolder sampleHolder1;
    public PublisherDataHolder sampleHolder2;
    public PublisherDataHolder updatedSampleHolder1;
    private PublisherDataHolder moduleNameUpdatedSampleHolder1;
    public PublisherDataHolder invalidSampleHolder;

    @BeforeClass
    public void setUpClass() throws Exception {

        cryptoUtil = mockStatic(CryptoUtil.class);
        mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);
        mockSecretEncryption(SAMPLE_SUBSCRIBER_PASSWORD_1);
        mockSecretEncryption(SAMPLE_SUBSCRIBER_PASSWORD_2);
        mockSecretDecryption(SAMPLE_ENCRYPTED_PASSWORD1);
        mockSecretDecryption(SAMPLE_ENCRYPTED_PASSWORD2);
    }

    @BeforeMethod
    public void setUp() {

        // Reinitialize the subscriber manager and sample holders before each test.
        subscriberPersistenceManager = createSubscriberPersistenceManager();
        sampleHolder1 =
                createSampleHolder(SAMPLE_SUBSCRIBER_ID_1, SAMPLE_SUBSCRIBER_URL_1, SAMPLE_SUBSCRIBER_USERNAME_1,
                        SAMPLE_SUBSCRIBER_PASSWORD_1);
        sampleHolder2 =
                createSampleHolder(SAMPLE_SUBSCRIBER_ID_2, SAMPLE_SUBSCRIBER_URL_2, SAMPLE_SUBSCRIBER_USERNAME_2,
                        SAMPLE_SUBSCRIBER_PASSWORD_2);
        updatedSampleHolder1 =
                createSampleHolder(SAMPLE_SUBSCRIBER_ID_1, SAMPLE_SUBSCRIBER_URL_2, SAMPLE_SUBSCRIBER_USERNAME_2,
                        SAMPLE_SUBSCRIBER_PASSWORD_2);
        moduleNameUpdatedSampleHolder1 = createSampleHolder(SAMPLE_SUBSCRIBER_ID_1, SAMPLE_SUBSCRIBER_URL_1,
                SAMPLE_SUBSCRIBER_USERNAME_1, SAMPLE_SUBSCRIBER_PASSWORD_1);
        moduleNameUpdatedSampleHolder1.setModuleName(NEW_MODULE_NAME);
        invalidSampleHolder = createSampleHolder(null, null, null, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        subscriberPersistenceManager.removeSubscriber(SAMPLE_SUBSCRIBER_ID_1);
        subscriberPersistenceManager.removeSubscriber(SAMPLE_SUBSCRIBER_ID_2);
    }

    @AfterClass
    public void wrapUp() {

        cryptoUtil.close();
    }

    @Test(priority = 1)
    public void testAddSubscriber() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);

        PublisherDataHolder subscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false);
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_ID_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_URL_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue(),
                sampleHolder1.getPropertyDTO(SUBSCRIBER_USERNAME_KEY).getValue());
        assertEquals(subscriberFromStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_ENCRYPTED_PASSWORD1);

        // Retrieve the subscriber with the decrypted secrets.
        PublisherDataHolder decryptedSubscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, true);
        assertEquals(decryptedSubscriberFromStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_SUBSCRIBER_PASSWORD_1);
    }

    @Test(priority = 1)
    public void testAddInvalidSubscriber() throws Exception {

        assertThrows(EntitlementException.class, () -> subscriberPersistenceManager.addSubscriber(invalidSampleHolder));
    }

    @Test(priority = 1)
    public void testAddSubscriberWithDuplicateId() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        assertThrows(EntitlementException.class, () -> subscriberPersistenceManager.addSubscriber(sampleHolder1));
    }

    @Test(priority = 2)
    public void testListSubscriberIds() throws Exception {

        List<String> subscriberIds = subscriberPersistenceManager.listSubscriberIds("*");
        assertEquals(subscriberIds.size(), 0);

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.addSubscriber(sampleHolder2);

        List<String> allSubscriberIds = subscriberPersistenceManager.listSubscriberIds("*");
        assertEquals(allSubscriberIds.size(), 2);

        List<String> filteredSubscriberIds1 = subscriberPersistenceManager.listSubscriberIds(SAMPLE_SUBSCRIBER_ID_1);
        assertEquals(filteredSubscriberIds1.size(), 1);

        List<String> filteredSubscriberIds2 = subscriberPersistenceManager.listSubscriberIds("test");
        assertEquals(filteredSubscriberIds2.size(), 0);
    }

    @Test(priority = 3)
    public void testUpdateSubscriber() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.updateSubscriber(updatedSampleHolder1);

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

        PublisherDataHolder decryptedSubscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, true);
        assertEquals(decryptedSubscriberFromStorage.getPropertyDTO(SUBSCRIBER_PASSWORD_KEY).getValue(),
                SAMPLE_SUBSCRIBER_PASSWORD_2);
    }

    @Test(priority = 3)
    public void testUpdateSubscriberModuleName() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.updateSubscriber(moduleNameUpdatedSampleHolder1);

        PublisherDataHolder subscriberFromStorage =
                subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false);
        assertEquals(subscriberFromStorage.getModuleName(), moduleNameUpdatedSampleHolder1.getModuleName());
    }

    @Test(priority = 3)
    public void testUpdateInvalidSubscriber() throws Exception {

        assertThrows(EntitlementException.class,
                () -> subscriberPersistenceManager.updateSubscriber(invalidSampleHolder));
    }

    @Test(priority = 4)
    public void testRemoveSubscriber() throws Exception {

        subscriberPersistenceManager.addSubscriber(sampleHolder1);
        subscriberPersistenceManager.removeSubscriber(SAMPLE_SUBSCRIBER_ID_1);
        assertThrows(EntitlementException.class,
                () -> subscriberPersistenceManager.getSubscriber(SAMPLE_SUBSCRIBER_ID_1, false));
    }

    @Test(priority = 4)
    public void testRemoveInvalidSubscriber() {

        assertThrows(EntitlementException.class, () -> subscriberPersistenceManager.removeSubscriber(null));
        assertThrows(EntitlementException.class,
                () -> subscriberPersistenceManager.removeSubscriber(PDP_SUBSCRIBER_ID));
    }

    private void mockSecretEncryption(String secret) throws org.wso2.carbon.core.util.CryptoException {

        if (SAMPLE_SUBSCRIBER_PASSWORD_1.equals(secret)) {
            when(mockCryptoUtil.encryptAndBase64Encode(secret.getBytes(Charsets.UTF_8))).thenReturn(
                    SAMPLE_ENCRYPTED_PASSWORD1);
        } else {
            when(mockCryptoUtil.encryptAndBase64Encode(secret.getBytes(Charsets.UTF_8))).thenReturn(
                    SAMPLE_ENCRYPTED_PASSWORD2);
        }
    }

    private void mockSecretDecryption(String cipherText) throws org.wso2.carbon.core.util.CryptoException {

        if (SAMPLE_ENCRYPTED_PASSWORD1.equals(cipherText)) {
            when(mockCryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(SAMPLE_SUBSCRIBER_PASSWORD_1.getBytes());
        } else {
            when(mockCryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(SAMPLE_SUBSCRIBER_PASSWORD_2.getBytes());
        }
    }

    private PublisherDataHolder createSampleHolder(String id, String url, String username, String password) {

        PublisherPropertyDTO idProperty =
                getPublisherPropertyDTO(SUBSCRIBER_ID_KEY, id, SUBSCRIBER_ID_DISPLAY_NAME, false);
        PublisherPropertyDTO urlProperty =
                getPublisherPropertyDTO(SUBSCRIBER_URL_KEY, url, SUBSCRIBER_URL_DISPLAY_NAME, false);
        PublisherPropertyDTO usernameProperty =
                getPublisherPropertyDTO(SUBSCRIBER_USERNAME_KEY, username, SUBSCRIBER_USERNAME_DISPLAY_NAME, false);
        PublisherPropertyDTO passwordProperty =
                getPublisherPropertyDTO(SUBSCRIBER_PASSWORD_KEY, password, SUBSCRIBER_PASSWORD_DISPLAY_NAME, true);
        return getPublisherHolder(
                new PublisherPropertyDTO[]{idProperty, urlProperty, usernameProperty, passwordProperty});
    }

    private PublisherPropertyDTO getPublisherPropertyDTO(String id, String value, String displayName, boolean secret) {

        PublisherPropertyDTO dto = new PublisherPropertyDTO();
        dto.setId(id);
        dto.setValue(value);
        dto.setDisplayName(displayName);
        dto.setSecret(secret);
        return dto;
    }

    private PublisherDataHolder getPublisherHolder(PublisherPropertyDTO[] propertyDTOs) {

        PublisherDataHolder holder = new PublisherDataHolder();
        holder.setModuleName(SUBSCRIBER_MODULE_NAME);
        holder.setPropertyDTOs(propertyDTOs);
        return holder;
    }

    /**
     * Abstract method to create the subscriber persistence manager
     *
     * @return The subscriber persistence manager.
     */
    protected abstract SubscriberPersistenceManager createSubscriberPersistenceManager();
}
