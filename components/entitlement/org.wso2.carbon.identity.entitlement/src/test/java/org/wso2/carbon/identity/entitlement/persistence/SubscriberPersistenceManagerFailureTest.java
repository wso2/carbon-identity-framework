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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.persistence.cache.CacheBackedSubscriberDAO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

/**
 * This class tests the failure scenarios of Database or Registry in Subscriber Persistence Manager implementations.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
public class SubscriberPersistenceManagerFailureTest {

    static final String SUBSCRIBER_MODULE_NAME = "Carbon Basic Auth Policy Publisher Module";
    static final String SUBSCRIBER_ID_KEY = "subscriberId";
    static final String SUBSCRIBER_ID_DISPLAY_NAME = "Subscriber Id";
    static final String SAMPLE_SUBSCRIBER_ID_1 = "Subscriber1";
    static final String SUBSCRIBER_URL_KEY = "subscriberURL";
    static final String SUBSCRIBER_URL_DISPLAY_NAME = "Subscriber URL";
    static final String SAMPLE_SUBSCRIBER_URL_1 = "https://localhost:9443/subscriber1";
    static final String SUBSCRIBER_USERNAME_KEY = "subscriberUserName";
    static final String SUBSCRIBER_USERNAME_DISPLAY_NAME = "Subscriber User Name";
    static final String SAMPLE_SUBSCRIBER_USERNAME_1 = "admin_user1";
    static final String SUBSCRIBER_PASSWORD_DISPLAY_NAME = "Subscriber Password";
    static final String SUBSCRIBER_PASSWORD_KEY = "subscriberPassword";
    static final String SAMPLE_SUBSCRIBER_PASSWORD_1 = "admin_password1";
    static final String SAMPLE_ENCRYPTED_PASSWORD1 = "encrypted_admin_password1";

    public PublisherDataHolder sampleHolder1;

    @Mock
    private CacheBackedSubscriberDAO mockedSubscriberDAO;

    @Mock
    private Registry mockedRegistry;

    private JDBCSubscriberPersistenceManager jdbcSubscriberPersistenceManager;
    private RegistrySubscriberPersistenceManager registrySubscriberPersistenceManager;

    MockedStatic<EntitlementServiceComponent> entitlementServiceComponent;
    private CryptoUtil mockCryptoUtil;
    private MockedStatic<CryptoUtil> cryptoUtil;

    @BeforeClass
    public void setUpClass() throws Exception {

        cryptoUtil = mockStatic(CryptoUtil.class);
        mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);
        when(mockCryptoUtil.encryptAndBase64Encode(SAMPLE_ENCRYPTED_PASSWORD1.getBytes(Charsets.UTF_8))).thenReturn(
                SAMPLE_ENCRYPTED_PASSWORD1);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        entitlementServiceComponent = mockStatic(EntitlementServiceComponent.class);
        entitlementServiceComponent.when(() -> EntitlementServiceComponent.getGovernanceRegistry(anyInt()))
                .thenReturn(mockedRegistry);

        registrySubscriberPersistenceManager = new RegistrySubscriberPersistenceManager();
        jdbcSubscriberPersistenceManager = new JDBCSubscriberPersistenceManager();
        setPrivateStaticFinalField(JDBCSubscriberPersistenceManager.class, "subscriberDAO", mockedSubscriberDAO);

        sampleHolder1 =
                createSampleHolder(SAMPLE_SUBSCRIBER_ID_1, SAMPLE_SUBSCRIBER_URL_1, SAMPLE_SUBSCRIBER_USERNAME_1,
                        SAMPLE_SUBSCRIBER_PASSWORD_1);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        entitlementServiceComponent.close();
        setPrivateStaticFinalField(JDBCSubscriberPersistenceManager.class, "subscriberDAO",
                CacheBackedSubscriberDAO.getInstance());
    }

    @Test
    public void testAddSubscriberWhenDatabaseErrorHappened() throws Exception {

        doThrow(new EntitlementException("")).when(mockedSubscriberDAO).insertSubscriber(anyString(), any(), anyInt());
        assertThrows(EntitlementException.class, () -> jdbcSubscriberPersistenceManager.addSubscriber(sampleHolder1));
    }

    @Test
    public void testUpdateSubscriberWhenDatabaseErrorHappened() throws Exception {

        when(mockedSubscriberDAO.isSubscriberExists(anyString(), anyInt())).thenReturn(false);
        assertThrows(EntitlementException.class,
                () -> jdbcSubscriberPersistenceManager.updateSubscriber(sampleHolder1));
    }

    @Test
    public void testGetSubscriberIdsWhenDatabaseErrorHappened() throws Exception {

        when(mockedSubscriberDAO.getSubscriber(anyString(), anyInt())).thenThrow(new EntitlementException(""));
        assertThrows(EntitlementException.class,
                () -> jdbcSubscriberPersistenceManager.getSubscriber(SUBSCRIBER_ID_KEY, false));
    }

    @Test
    public void testListSubscriberWhenDatabaseErrorHappened() throws Exception {

        when(mockedSubscriberDAO.getSubscriberIds(anyInt())).thenThrow(new EntitlementException(""));
        assertThrows(EntitlementException.class,
                () -> jdbcSubscriberPersistenceManager.listSubscriberIds(SUBSCRIBER_ID_KEY));
    }

    @Test
    public void testIsSubscriberExistsWhenDatabaseErrorHappened() throws Exception {

        when(mockedSubscriberDAO.isSubscriberExists(anyString(), anyInt())).thenThrow(new EntitlementException(""));
        assertThrows(EntitlementException.class,
                () -> jdbcSubscriberPersistenceManager.isSubscriberExists(SUBSCRIBER_ID_KEY));
    }

    @Test
    public void testRemoveSubscriberWhenDatabaseErrorHappened() throws Exception {

        doThrow(new EntitlementException("")).when(mockedSubscriberDAO).deleteSubscriber(anyString(), anyInt());
        assertThrows(EntitlementException.class,
                () -> jdbcSubscriberPersistenceManager.removeSubscriber(SUBSCRIBER_ID_KEY));
    }

    @Test
    public void testAddSubscriberWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registrySubscriberPersistenceManager.addSubscriber(sampleHolder1));
    }

    @Test
    public void testUpdateSubscriberWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registrySubscriberPersistenceManager.updateSubscriber(sampleHolder1));
    }

    @Test
    public void testGetSubscriberWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        when(mockedRegistry.get(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registrySubscriberPersistenceManager.getSubscriber(SUBSCRIBER_ID_KEY, false));
    }

    @Test
    public void testListSubscriberIdsWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        when(mockedRegistry.get(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class, () -> registrySubscriberPersistenceManager.listSubscriberIds("*"));
    }

    @Test
    public void testIsSubscriberExistsWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registrySubscriberPersistenceManager.isSubscriberExists(SUBSCRIBER_ID_KEY));
    }

    @Test
    public void testRemoveSubscriberWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        doThrow(new RegistryException("")).when(mockedRegistry).delete(anyString());
        assertThrows(EntitlementException.class,
                () -> registrySubscriberPersistenceManager.removeSubscriber(SUBSCRIBER_ID_KEY));
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

        PublisherDataHolder holder = new PublisherDataHolder();
        holder.setModuleName(SUBSCRIBER_MODULE_NAME);
        holder.setPropertyDTOs(new PublisherPropertyDTO[]{idProperty, urlProperty, usernameProperty, passwordProperty});
        return holder;
    }

    private PublisherPropertyDTO getPublisherPropertyDTO(String id, String value, String displayName, boolean secret) {

        PublisherPropertyDTO dto = new PublisherPropertyDTO();
        dto.setId(id);
        dto.setValue(value);
        dto.setDisplayName(displayName);
        dto.setSecret(secret);
        return dto;
    }

    private static void setPrivateStaticFinalField(Class<?> clazz, String fieldName, Object newValue)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
