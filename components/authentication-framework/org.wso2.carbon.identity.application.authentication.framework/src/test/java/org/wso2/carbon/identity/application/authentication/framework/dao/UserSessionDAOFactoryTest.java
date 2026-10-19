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

package org.wso2.carbon.identity.application.authentication.framework.dao;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtUtils;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

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
 * Unit tests for {@link UserSessionDAOFactory} and for the user session DAO registry of
 * {@link FrameworkServiceDataHolder}, which together select the DAO of the configured session store.
 */
public class UserSessionDAOFactoryTest {

    private static final String SESSION_STORAGE_TYPE_PROPERTY = "SessionStorage.Type";

    private final List<UserSessionDAO> registeredDAOs = new ArrayList<>();

    @AfterMethod
    public void deregisterDAOs() {

        for (UserSessionDAO userSessionDAO : registeredDAOs) {
            FrameworkServiceDataHolder.getInstance().removeUserSessionDAO(userSessionDAO);
        }
        registeredDAOs.clear();
    }

    @DataProvider(name = "relationalStoreConfigurations")
    public Object[][] relationalStoreConfigurations() {

        return new Object[][]{
                {null},
                {"JDBC"}
        };
    }

    /**
     * An unconfigured session storage, or the relational store configured by name, resolves to the
     * relational DAO, without needing anything to be registered.
     */
    @Test(dataProvider = "relationalStoreConfigurations")
    public void testUnconfiguredOrJdbcStoreResolvesToTheRelationalDAO(String configuredStoreName) {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY))
                    .thenReturn(configuredStoreName);

            assertTrue(UserSessionDAOFactory.getUserSessionDAO() instanceof UserSessionDAOImpl);
        }
    }

    /**
     * A registered alternative DAO is selected by the configured name, matched case insensitively in
     * both directions: the registered name and the configured one.
     */
    @Test
    public void testRegisteredDAOIsSelectedByTheConfiguredName() {

        UserSessionDAO registered = register("Redis");

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY)).thenReturn("REDIS");

            assertSame(UserSessionDAOFactory.getUserSessionDAO(), registered);
        }
    }

    /**
     * If the configured DAO is not registered the selection fails, rather than falling back to the
     * relational DAO, so that session data is never split between two stores.
     */
    @Test
    public void testUnregisteredStoreFailsInsteadOfFallingBack() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY)).thenReturn("redis");

            try {
                UserSessionDAOFactory.getUserSessionDAO();
                fail("Expected the selection of an unregistered user session DAO to fail");
            } catch (IdentityRuntimeException e) {
                assertTrue(e.getMessage().contains("redis"), "Unexpected message: " + e.getMessage());
            }
        }
    }

    /**
     * A DAO is looked up under the name it registered, whatever the case of either name.
     */
    @Test
    public void testRegistryLookupIsCaseInsensitive() {

        UserSessionDAO registered = register("  Redis  ");

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        assertSame(dataHolder.getUserSessionDAO("redis"), registered);
        assertSame(dataHolder.getUserSessionDAO("REDIS"), registered);
        assertSame(dataHolder.getUserSessionDAO(" Redis "), registered);
    }

    /**
     * A DAO that does not name a store is not selectable, and registering it is ignored rather than
     * failing the component that carries it.
     */
    @Test
    public void testDAOWithoutAStoreNameIsNotRegistered() {

        UserSessionDAO unnamed = mock(UserSessionDAO.class);
        when(unnamed.getStoreName()).thenReturn(null);

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        dataHolder.addUserSessionDAO(unnamed);
        dataHolder.addUserSessionDAO(null);
        dataHolder.removeUserSessionDAO(unnamed);
        dataHolder.removeUserSessionDAO(null);

        assertNull(dataHolder.getUserSessionDAO(null));
    }

    /**
     * A deregistered DAO stops being selectable, so an alternative store that goes away is not used
     * after its bundle is stopped.
     */
    @Test
    public void testDeregisteredDAOIsNoLongerSelectable() {

        UserSessionDAO registered = register("redis");
        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        assertSame(dataHolder.getUserSessionDAO("redis"), registered);

        dataHolder.removeUserSessionDAO(registered);

        assertNull(dataHolder.getUserSessionDAO("redis"));
    }

    /**
     * The relational DAO does not name a store, since it is the default rather than a registered one.
     */
    @Test
    public void testRelationalDAOIsNotSelectableByName() {

        assertNull(new UserSessionDAOImpl().getStoreName());
        assertNull(FrameworkServiceDataHolder.getInstance()
                .getUserSessionDAO(SessionMgtUtils.DEFAULT_SESSION_STORE_NAME));
    }

    private UserSessionDAO register(String storeName) {

        UserSessionDAO userSessionDAO = mock(UserSessionDAO.class);
        when(userSessionDAO.getStoreName()).thenReturn(storeName);
        FrameworkServiceDataHolder.getInstance().addUserSessionDAO(userSessionDAO);
        registeredDAOs.add(userSessionDAO);
        return userSessionDAO;
    }
}
