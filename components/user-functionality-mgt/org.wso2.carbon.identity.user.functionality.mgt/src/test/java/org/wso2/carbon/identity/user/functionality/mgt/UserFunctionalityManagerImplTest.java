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

package org.wso2.carbon.identity.user.functionality.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityManagerDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityPropertyDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityManagerDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityPropertyDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.internal.UserFunctionalityManagerComponentDataHolder;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;
import org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class UserFunctionalityManagerImplTest {

    private static final Log log = LogFactory.getLog(UserFunctionalityManagerImplTest.class);
    private UserFunctionalityManagerDAO userFunctionalityManagerDAO = new UserFunctionalityManagerDAOImpl();
    private UserFunctionalityManager userFunctionalityManager = new UserFunctionalityManagerImpl();
    private UserFunctionalityPropertyDAO userFunctionalityPropertyDAO = new UserFunctionalityPropertyDAOImpl();

    @Mock
    private RealmService realmService;
    @Mock
    private UserRealm userRealm;
    @Mock
    private UniqueIDUserStoreManager userStoreManager;
    @Mock
    private UserFunctionalityManagerComponentDataHolder mockUserFunctionalityManagerComponentDataHolder;

    @BeforeMethod
    public void setUp() throws Exception {

        TestUtils.initiateH2Base();
        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");

            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);

                FunctionalityLockStatus functionalityLockStatus1 = new FunctionalityLockStatus(false, 0, null, null);
                FunctionalityLockStatus functionalityLockStatus2 = new FunctionalityLockStatus(false, 0, null, null);
                FunctionalityLockStatus functionalityLockStatus3 =
                        new FunctionalityLockStatus(true, System.currentTimeMillis() + 300000, "E001",
                                "Lock Reason 1");
                FunctionalityLockStatus functionalityLockStatus4 =
                        new FunctionalityLockStatus(true, System.currentTimeMillis() + 300000, "E002",
                                "Lock Reason 2");
                FunctionalityLockStatus functionalityLockStatus5 =
                        new FunctionalityLockStatus(true, Long.MAX_VALUE, "E002",
                                "Lock Reason 2");
                try {
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user1", 1, "FunctionalityType1", functionalityLockStatus1);
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user2", 1, "FunctionalityType2", functionalityLockStatus2);
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user3", 1, "FunctionalityType3", functionalityLockStatus3);
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user3", 2, "FunctionalityType3", functionalityLockStatus4);
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user5", 3, "FunctionalityType5", functionalityLockStatus5);
                } catch (UserFunctionalityManagementServerException e) {
                    log.error("Error while adding functionality", e);
                }
            }
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @DataProvider(name = "IsFunctionalityLockedData")
    public Object[][] isFunctionalityLockedData() {

        return new Object[][]{
                // functionalityType
                // userId
                // tenantId
                //expected
                {"FunctionalityType1", "user1", 1, false},
                {"FunctionalityType2", "user2", 1, false},
                {"FunctionalityType3", "user3", 1, true},
                {"FunctionalityType3", "user3", 2, true},
                {"FalseFunctionalityType", "null", 0, false},
                {"FunctionalityType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "IsFunctionalityLockedData")
    public void testIsFunctionalityLockedForUser(String functionalityIdentifier, String userId, int tenantId,
                                                 boolean expected) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");
            try {
                try (Connection connection = TestUtils.getConnection()) {
                    Connection spyConnection = TestUtils.spyConnection(connection);
                    when(dataSource.getConnection()).thenReturn(spyConnection);
                    try {
                        mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                        assertEquals(userFunctionalityManager
                                        .getLockStatus(userId, tenantId, functionalityIdentifier).getLockStatus(),
                                expected);
                    } catch (UserFunctionalityManagementException | UserStoreException e) {
                        log.error(String.format("Error while selecting functionality: %s", functionalityIdentifier), e);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
        }
    }

    @DataProvider(name = "TestGetFunctionalityLockPropertiesData")
    public Object[][] testGetAllPropertiesData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // functionalityIdentifier
                // properties
                {"user1", 1, "functionalityIdentifier1", properties},
                {"user1", 2, "functionalityIdentifier1", properties},
                {"user2", 2, "functionalityIdentifier2", properties},
                {"user3", 3, "functionalityIdentifier3", properties}
        };
    }

    @Test(dataProvider = "TestGetFunctionalityLockPropertiesData")
    public void testGetFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                   Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");

            try {
                try (Connection connection = TestUtils.getConnection()) {
                    Connection spyConnection = TestUtils.spyConnection(connection);
                    when(dataSource.getConnection()).thenReturn(spyConnection);
                    try {
                        mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                        userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier,
                                properties);
                        assertEquals(userFunctionalityManager.getProperties(userId, tenantId, functionalityIdentifier),
                                properties);
                    } catch (UserFunctionalityManagementException e) {
                        log.error(String.format("Error while selecting functionality: %s", functionalityIdentifier), e);
                    } catch (UserStoreException e) {
                        log.error("Error while checking userid in userstore", e);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
        }
    }

    @DataProvider(name = "TestSetFunctionalityLockPropertiesData")
    public Object[][] testSetFunctionalityLockPropertiesData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Map<String, String> propertiesToUpdate1 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        Map<String, String> propertiesToUpdate2 = new HashMap<String, String>() {{
            put("k4", "v4");
            put("k5", "v5");
            put("k6", "v6");
        }};

        Map<String, String> propertiesToUpdate3 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k4", "v4");
        }};

        Map<String, String> expectedProperties1 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        Map<String, String> expectedProperties2 = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
            put("k4", "v4");
            put("k5", "v5");
            put("k6", "v6");
        }};

        Map<String, String> expectedProperties3 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v3");
            put("k4", "v4");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // functionalityIdentifier
                // properties
                // propertiesToUpdate
                {"user1", 1, "functionalityIdentifier1", properties, propertiesToUpdate1, expectedProperties1},
                {"user1", 2, "functionalityIdentifier1", properties, propertiesToUpdate2, expectedProperties2},
                {"user2", 2, "functionalityIdentifier2", properties, propertiesToUpdate3, expectedProperties3},
                {"user2", -1234, "functionalityIdentifier1", properties, new HashMap<String, String>(), properties}
        };
    }

    @Test(dataProvider = "TestSetFunctionalityLockPropertiesData")
    public void testSetFunctionalityLockProperties(String userId, int tenantId, String functionalityIdentifier,
                                                   Map<String, String> properties,
                                                   Map<String, String> propertiesToUpdate,
                                                   Map<String, String> expectedProperties) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, properties);
                userFunctionalityManager.setProperties(userId, tenantId, functionalityIdentifier, propertiesToUpdate);
                Map<String, String> functionalityLockProperties =
                        userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);

                assertEquals(functionalityLockProperties, expectedProperties);
            } catch (SQLException | UserFunctionalityManagementException | UserStoreException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @DataProvider(name = "FunctionalityLockedReasonData")
    public Object[][] functionalityLockedReasonData() {

        return new Object[][]{
                // functionalityType
                // userId
                // tenantId
                //expected
                {"FunctionalityType1", "user1", 1, null},
                {"FunctionalityType2", "user2", 1, null},
                {"FunctionalityType3", "user3", 1, "Lock Reason 1"},
                {"FunctionalityType3", "user3", 2, "Lock Reason 2"},
                {"FunctionalityType5", "user5", 3, "Lock Reason 2"}
        };
    }

    @Test(dataProvider = "FunctionalityLockedReasonData")
    public void testGetFunctionalityLockReasonForUser(String functionalityIdentifier, String userId, int tenantId,
                                                      String expected) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");
            try {
                try (Connection connection = TestUtils.getConnection()) {
                    Connection spyConnection = TestUtils.spyConnection(connection);
                    when(dataSource.getConnection()).thenReturn(spyConnection);
                    try {
                        mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                        assertEquals(
                                userFunctionalityManager
                                        .getLockStatus(userId, tenantId, functionalityIdentifier)
                                        .getLockReason(),
                                expected);
                    } catch (UserFunctionalityManagementException e) {
                        log.error(String.format("Error while selecting functionality: %s", functionalityIdentifier), e);
                    } catch (UserStoreException e) {
                        log.error("Error while checking userid in userstore", e);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
        }
    }

    @DataProvider(name = "LockFunctionalityForUserData")
    public Object[][] lockFunctionalityForUserData() {

        return new Object[][]{
                // functionalityType
                // userId
                // tenantId
                //expected
                {"FunctionalityType1", "user1", 1, true},
                {"FunctionalityType2", "user2", 1, true},
                {"FunctionalityType3", "user3", 1, true},
                {"FunctionalityType3", "user3", 2, true},
                {"FunctionalityType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "LockFunctionalityForUserData")
    public void testLockFunctionalityForUser(String functionalityIdentifier, String userId, int tenantId,
                                             boolean expected) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");
            long functionalityUnlockTime = System.currentTimeMillis() + 300000;
            String functionalityLockReasonCode = "Lock code";
            String functionalityLockReason = "Lock Reason 2";
            try {
                try (Connection connection = TestUtils.getConnection()) {
                    Connection spyConnection = TestUtils.spyConnection(connection);
                    when(dataSource.getConnection()).thenReturn(spyConnection);
                    try {
                        mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                        userFunctionalityManager.lock(userId, tenantId, functionalityIdentifier,
                                functionalityUnlockTime,
                                functionalityLockReasonCode, functionalityLockReason);

                        assertEquals(userFunctionalityManager
                                        .getLockStatus(userId, tenantId, functionalityIdentifier).getLockStatus(),
                                expected);
                        assertEquals(userFunctionalityManager.getLockStatus(userId, tenantId, functionalityIdentifier)
                                .getLockReason(), functionalityLockReason);
                    } catch (UserFunctionalityManagementException e) {
                        log.error(String.format("Error while selecting functionality: %s", functionalityIdentifier), e);
                    } catch (UserStoreException e) {
                        log.error("Error while checking userid in userstore", e);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
        }
    }

    @DataProvider(name = "UnlockFunctionalityForUserData")
    public Object[][] unlockFunctionalityForUserData() {

        return new Object[][]{
                // functionalityType
                // userId
                // tenantId
                //isFunctionalityLocked
                {"FunctionalityType1", "user1", 1, false},
                {"FunctionalityType2", "user2", 1, false},
                {"FunctionalityType3", "user3", 1, true},
                {"FunctionalityType3", "user3", 2, true},
                {"FunctionalityType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "UnlockFunctionalityForUserData")
    public void testUnlockFunctionalityForUser(String functionalityIdentifier, String userId, int tenantId,
                                               boolean isFunctionalityLocked) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder =
                     mockStatic(UserFunctionalityManagerComponentDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityUtil.when(() -> IdentityUtil
                            .getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING))
                    .thenReturn("true");
            try {
                try (Connection connection = TestUtils.getConnection()) {
                    Connection spyConnection = TestUtils.spyConnection(connection);
                    when(dataSource.getConnection()).thenReturn(spyConnection);
                    try {
                        mockIsUserStoreManager(userId, dataHolder, identityTenantUtil);
                        assertEquals(userFunctionalityManager
                                        .getLockStatus(userId, tenantId, functionalityIdentifier).getLockStatus(),
                                isFunctionalityLocked);
                        userFunctionalityManager.unlock(userId, tenantId, functionalityIdentifier);
                        assertEquals(userFunctionalityManager
                                        .getLockStatus(userId, tenantId, functionalityIdentifier).getLockStatus(),
                                false);
                    } catch (UserFunctionalityManagementException e) {
                        log.error(String.format("Error while selecting functionality: %s", functionalityIdentifier), e);
                    } catch (UserStoreException e) {
                        log.error("Error while checking userid in userstore", e);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
        }

    }

    private void mockIsUserStoreManager(String userId,
                                        MockedStatic<UserFunctionalityManagerComponentDataHolder> dataHolder,
                                        MockedStatic<IdentityTenantUtil> identityTenantUtil) throws UserStoreException {

        dataHolder.when(UserFunctionalityManagerComponentDataHolder::getInstance)
                .thenReturn(mockUserFunctionalityManagerComponentDataHolder);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("carbon.super");
        TestUtils.mockUserStoreManager(mockUserFunctionalityManagerComponentDataHolder, realmService, userRealm,
                userStoreManager);
        when(userStoreManager.isExistingUserWithID(userId)).thenReturn(true);
    }
}
