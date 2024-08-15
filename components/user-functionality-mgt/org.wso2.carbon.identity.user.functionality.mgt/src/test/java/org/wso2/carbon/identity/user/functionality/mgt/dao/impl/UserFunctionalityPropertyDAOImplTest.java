/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.functionality.mgt.dao.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityManager;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityManagerImpl;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityMgtConstants;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityPropertyDAO;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.internal.UserFunctionalityManagerComponentDataHolder;
import org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils.spyConnection;

@Listeners(MockitoTestNGListener.class)
public class UserFunctionalityPropertyDAOImplTest {

    @Mock
    private RealmService realmService;
    @Mock
    private UserRealm userRealm;
    @Mock
    private UniqueIDUserStoreManager userStoreManager;
    @Mock
    private UserFunctionalityManagerComponentDataHolder mockUserFunctionalityManagerComponentDataHolder;
    private UserFunctionalityPropertyDAO userFunctionalityPropertyDAO = new UserFunctionalityPropertyDAOImpl();
    private UserFunctionalityManager userFunctionalityManager = new UserFunctionalityManagerImpl();

    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<UserFunctionalityManagerComponentDataHolder> userFunctionalityManagerComponentDataHolder;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        identityUtil = mockStatic(IdentityUtil.class);
        userFunctionalityManagerComponentDataHolder = mockStatic(UserFunctionalityManagerComponentDataHolder.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityUtil.when(() -> IdentityUtil
                        .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                .thenReturn("true");
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
        identityUtil.close();
        userFunctionalityManagerComponentDataHolder.close();
        identityTenantUtil.close();
    }

    @DataProvider(name = "TestFunctionalityLockPropertiesData")
    public Object[][] testFunctionalityLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // functionalityIdentifier
                // propertyName
                // propertyValue
                {"user1", 1, "functionalityIdentifier1", properties},
                {"user1", 2, "functionalityIdentifier1", properties},
                {"user2", 2, "functionalityIdentifier2", properties},
                {"user3", 3, "functionalityIdentifier3", properties}
        };
    }

    @Test(dataProvider = "TestFunctionalityLockPropertiesData")
    public void testAddFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                   Map<String, String> lockProperties) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, lockProperties);
                Map<String, String> properties =
                        userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);
                assertEquals(properties, lockProperties);
            } catch (SQLException | UserFunctionalityManagementServerException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @Test(dataProvider = "TestFunctionalityLockPropertiesData")
    public void testGetAllFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                      Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, properties);
                Map<String, String> propertiesMap = userFunctionalityPropertyDAO
                        .getAllProperties(userId, tenantId, functionalityIdentifier);
                assertEquals(properties, propertiesMap);
            } catch (SQLException | UserFunctionalityManagementServerException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @DataProvider(name = "TestUpdateFunctionalityLockPropertyData")
    public Object[][] testUpdateFunctionalityLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Map<String, String> propertiesToUpdate = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // functionalityIdentifier
                // properties
                // propertiesToUpdate
                {"user1", 1, "functionalityIdentifier1", properties, propertiesToUpdate},
                {"user1", 2, "functionalityIdentifier1", properties, propertiesToUpdate},
                {"user2", 2, "functionalityIdentifier2", properties, propertiesToUpdate},
                {"user3", 3, "functionalityIdentifier3", properties, propertiesToUpdate}
        };
    }

    @Test(dataProvider = "TestUpdateFunctionalityLockPropertyData")
    public void testUpdateFunctionalityLockProperty(String userId, int tenantId, String functionalityIdentifier,
                                                    Map<String, String> properties,
                                                    Map<String, String> propertiesToUpdate) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, properties);
                userFunctionalityPropertyDAO
                        .updateProperties(userId, tenantId, functionalityIdentifier, propertiesToUpdate);
                Map<String, String> updatedProperties =
                        userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);
                assertEquals(updatedProperties, propertiesToUpdate);
            } catch (SQLException | UserFunctionalityManagementServerException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @Test(dataProvider = "TestFunctionalityLockPropertiesData")
    public void testDeleteAllFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                         Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                mockUser(userId);
                userFunctionalityManager.setProperties(userId, tenantId, functionalityIdentifier, properties);
                userFunctionalityPropertyDAO.deleteAllPropertiesForUser(userId, tenantId, functionalityIdentifier);
                Map<String, String> functionalityLockProperties =
                        userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);
                assertTrue(functionalityLockProperties.isEmpty());
            } catch (SQLException | UserFunctionalityManagementException | UserStoreException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @DataProvider(name = "TestDeleteFunctionalityLockPropertiesData")
    public Object[][] testDeleteFunctionalityLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Set<String> propertiesToDelete1 = Stream.of("k1", "k2").collect(Collectors.toSet());
        Set<String> propertiesToDelete2 = Stream.of("k1", "k2", "k3").collect(Collectors.toSet());
        Set<String> propertiesToDelete3 = Stream.of("").collect(Collectors.toSet());
        Set<String> propertiesToDelete4 = Stream.of("invalid", "k2").collect(Collectors.toSet());

        return new Object[][]{
                // userId
                // tenantId
                // functionalityIdentifier
                // propertyName
                // propertyValue
                // expected
                {"user1", 1, "functionalityIdentifier1", properties, propertiesToDelete1,
                        Stream.of("k3").collect(Collectors.toCollection(HashSet::new))},
                {"user1", 2, "functionalityIdentifier1", properties, propertiesToDelete2, Collections.emptySet()},
                {"user2", 2, "functionalityIdentifier2", properties, propertiesToDelete3,
                        Stream.of("k1", "k2", "k3").collect(Collectors.toCollection(HashSet::new))},
                {"user3", 3, "functionalityIdentifier3", properties, propertiesToDelete4,
                        Stream.of("k1", "k3").collect(Collectors.toCollection(HashSet::new))}
        };
    }

    @Test(dataProvider = "TestDeleteFunctionalityLockPropertiesData")
    public void testDeleteFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                      Map<String, String> properties, Set<String> propertiesToDelete,
                                                      Set<String> expected) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, properties);
                userFunctionalityPropertyDAO
                        .deletePropertiesForUser(userId, tenantId, functionalityIdentifier, propertiesToDelete);
                Map<String, String> functionalityLockProperties =
                        userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);

                assertEquals(functionalityLockProperties.keySet(), expected);
            } catch (SQLException | UserFunctionalityManagementServerException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @Test
    public void testDeletePropertiesForTenant() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};
        String[] functionalityIdentifiers = {"functionality1", "functionality2", "functionality3"};
        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

            try (Connection connection = getConnection()) {
                Connection spyConnection = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                for (String functionalityIdentifier : functionalityIdentifiers) {
                    userFunctionalityPropertyDAO.addProperties("user", 1, functionalityIdentifier, properties);
                }

                userFunctionalityPropertyDAO.deleteAllPropertiesForTenant(1);

                for (String functionalityIdentifier : functionalityIdentifiers) {
                    Map<String, String> functionalityLockProperties =
                            userFunctionalityPropertyDAO.getAllProperties("user", 1, functionalityIdentifier);
                    assertEquals(functionalityLockProperties.keySet(), Collections.emptySet());
                }
            } catch (SQLException | UserFunctionalityManagementServerException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    private void mockUser(String userId) throws UserStoreException {


        userFunctionalityManagerComponentDataHolder.when(
                UserFunctionalityManagerComponentDataHolder::getInstance).thenReturn(mockUserFunctionalityManagerComponentDataHolder);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("carbon.super");
        TestUtils.mockUserStoreManager(mockUserFunctionalityManagerComponentDataHolder, realmService, userRealm, userStoreManager);
        when(userStoreManager.isExistingUserWithID(anyString())).thenReturn(true);
    }
}
