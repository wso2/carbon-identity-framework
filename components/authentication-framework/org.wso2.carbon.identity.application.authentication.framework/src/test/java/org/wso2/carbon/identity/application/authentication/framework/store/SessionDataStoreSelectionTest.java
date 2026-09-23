/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for the selection of {@link SessionDataStore} and for the session data store registry of
 * {@link FrameworkServiceDataHolder}.
 * <p>
 * The relational store is left out on purpose: constructing it starts the session clean up task, and it
 * is already exercised by {@link SessionDataStoreTest}.
 */
public class SessionDataStoreSelectionTest {

    private static final String SESSION_STORAGE_TYPE_PROPERTY = "SessionStorage.Type";

    private final List<SessionDataStore> registeredStores = new ArrayList<>();

    @BeforeMethod
    public void clearSelectedStore() throws Exception {

        setSelectedStore(null);
    }

    @AfterMethod
    public void deregisterStores() throws Exception {

        for (SessionDataStore store : registeredStores) {
            FrameworkServiceDataHolder.getInstance().removeSessionDataStore(store);
        }
        registeredStores.clear();
        setSelectedStore(null);
    }

    /**
     * A registered alternative store is selected by the configured name, matched case insensitively in
     * both directions: the registered name and the configured one.
     */
    @Test
    public void testRegisteredStoreIsSelectedByTheConfiguredName() {

        SessionDataStore registered = register("Redis");

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY)).thenReturn("REDIS");

            assertSame(SessionDataStore.getInstance(), registered);
        }
    }

    /**
     * The store is resolved once and kept, so that a selection is not repeated on every session write.
     */
    @Test
    public void testSelectedStoreIsResolvedOnceAndCached() {

        SessionDataStore registered = register("redis");

        SessionDataStore first;
        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY)).thenReturn("redis");
            first = SessionDataStore.getInstance();
        }
        assertSame(first, registered);

        // The configuration is gone, but the already resolved store is kept rather than resolved again.
        FrameworkServiceDataHolder.getInstance().removeSessionDataStore(registered);
        assertSame(SessionDataStore.getInstance(), registered);
    }

    /**
     * If the configured store is not registered the selection fails, rather than falling back to the
     * relational store, so that session data is never split between two stores.
     */
    @Test
    public void testUnregisteredStoreFailsInsteadOfFallingBack() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY)).thenReturn("redis");

            try {
                SessionDataStore.getInstance();
                fail("Expected the selection of an unregistered session data store to fail");
            } catch (IdentityRuntimeException e) {
                assertTrue(e.getMessage().contains("redis"), "Unexpected message: " + e.getMessage());
            }
        }
    }

    /**
     * A store is looked up under the name it registered, whatever the case of either name.
     */
    @Test
    public void testRegistryLookupIsCaseInsensitive() {

        SessionDataStore registered = register("  Redis  ");

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        assertSame(dataHolder.getSessionDataStore("redis"), registered);
        assertSame(dataHolder.getSessionDataStore("REDIS"), registered);
        assertSame(dataHolder.getSessionDataStore(" Redis "), registered);
    }

    /**
     * A store that does not name itself is not selectable, and registering it is ignored rather than
     * failing the component that carries it.
     */
    @Test
    public void testStoreWithoutAStoreNameIsNotRegistered() {

        SessionDataStore unnamed = mock(SessionDataStore.class);
        when(unnamed.getStoreName()).thenReturn(null);

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        dataHolder.addSessionDataStore(unnamed);
        dataHolder.addSessionDataStore(null);
        dataHolder.removeSessionDataStore(unnamed);
        dataHolder.removeSessionDataStore(null);

        assertNull(dataHolder.getSessionDataStore(null));
    }

    /**
     * A deregistered store stops being selectable, so an alternative store that goes away is not used
     * after its bundle is stopped.
     */
    @Test
    public void testDeregisteredStoreIsNoLongerSelectable() {

        SessionDataStore registered = register("redis");
        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        assertSame(dataHolder.getSessionDataStore("redis"), registered);

        dataHolder.removeSessionDataStore(registered);

        assertNull(dataHolder.getSessionDataStore("redis"));
    }

    private SessionDataStore register(String storeName) {

        SessionDataStore store = mock(SessionDataStore.class);
        when(store.getStoreName()).thenReturn(storeName);
        FrameworkServiceDataHolder.getInstance().addSessionDataStore(store);
        registeredStores.add(store);
        return store;
    }

    private static void setSelectedStore(SessionDataStore store) throws Exception {

        Field selectedStore = SessionDataStore.class.getDeclaredField("selectedStore");
        selectedStore.setAccessible(true);
        selectedStore.set(null, store);
    }
}
