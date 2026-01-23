/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

/**
 * Test class that includes unit tests of UserSessionStore
 */
public class UserSessionStoreTest extends DataStoreBaseTest {

    private static final String DB_NAME = "USER_SESSION_STORE";
    private static final int IDLE_SESSION_TIMEOUT = 600;
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int TENANT_ID = 1;

    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<IdentityDatabaseUtil> mockedIdentityDatabaseUtil;
    private MockedStatic<JdbcUtils> mockedJdbcUtils;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private MockedStatic<IdPManagementUtil> mockedIdPManagementUtil;

    @BeforeClass
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        initH2DB(DB_NAME, getDatabaseScriptFilePath("user_session_store_h2.sql"));

        mockedIdentityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        mockedJdbcUtils = mockStatic(JdbcUtils.class);
        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdPManagementUtil = mockStatic(IdPManagementUtil.class);

        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        mockedIdPManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN)).
                thenReturn(IDLE_SESSION_TIMEOUT);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2DB(DB_NAME);
        mockedIdentityDatabaseUtil.close();
        mockedJdbcUtils.close();
        mockedIdentityTenantUtil.close();
        mockedIdPManagementUtil.close();
    }

    @DataProvider
    public Object[][] getValidUsers() {

        return new Object[][]{
                {"00000001", "testuser1", -1234, "PRIMARY", -1},
                {"00000002", "testuser2", -1234, "Abc.com", -1},
                {"00000003", "testuser3", -1234, "PRIMARY", 1}
        };
    }

    @DataProvider
    public Object[][] getFederatedUser() {

        return new Object[][]{
                {"00000003", "testuser3", "carbon.super", "PRIMARY", "FEDERATED"},
        };
    }

    @DataProvider
    public Object[][] getUsersWithDuplicatedId() {

        return new Object[][]{
                {"00000001", "testuser1", -1234, "PRIMARY", -1},
        };
    }

    @DataProvider
    public Object[][] getDuplicatedUsers() {

        return new Object[][]{
                {"00000003", "testuser1", -1234, "PRIMARY", -1},
        };
    }

    @DataProvider
    public Object[][] getValidSessionsForUsers() {

        return new Object[][]{
                {"00000001", "00000001"},
                {"00000001", "00000002"},
                {"00000002", "00000001"},
        };
    }

    @DataProvider
    public Object[][] getDuplicatedSessionsForUsers() {

        return new Object[][]{
                {"00000001", "00000001"},
        };
    }

    @DataProvider
    public Object[][] getInvalidSessionsForUsers() {

        return new Object[][]{
                {"00000001", "00000003"},
        };
    }

    @DataProvider
    public Object[][] getSessionAppsData() {

        return new Object[][]{
                {"00000001", "testuser1", 1, "authtype"},
        };
    }

    @DataProvider
    public Object[][] getSessionMetadata() {

        return new Object[][]{
                {"00000001", Stream.of(new String[][]{
                        {"IP", "localhost"},
                        {"Last Access Time", "someTime"},
                        {"Login Time", "someTime"},
                        {"User Agent", "someUserAgent"},
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]))},
        };
    }

    @Test(dataProvider = "getValidUsers")
    public void testStoreUserData(String userId, String username, int tenantId, String userDomain, int idpId) throws
            Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true, mockedIdentityDatabaseUtil);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getUsersWithDuplicatedId", dependsOnMethods = {"testStoreUserData"}, expectedExceptions =
            UserSessionException.class)
    public void testExceptionAtStoreUserDataForDuplicatedUserID(String userId, String username, int tenantId, String
            userDomain, int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true, mockedIdentityDatabaseUtil);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getDuplicatedUsers", dependsOnMethods = {"testStoreUserData"}, expectedExceptions =
            DuplicatedAuthUserException.class)
    public void testExceptionAtStoreUserDataForInvalidUser(String userId, String username, int tenantId, String
            userDomain, int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true, mockedIdentityDatabaseUtil);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdForAllUserParams(String expectedUserId, String username, int tenantId, String userDomain,
                                              int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            String actualUserId = UserSessionStore.getInstance().getUserId(username, tenantId, userDomain, idpId);
            Assert.assertEquals(actualUserId, expectedUserId, "Expected userId not received for user: " + username +
                    " of userstore domain: " + userDomain + ", tenant: " + tenantId + " and idp id: " + idpId);
        }
    }

    @Test(dataProvider = "getFederatedUser", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserFromUserId(String userId, String username, String tenantDomain, String userDomain,
                                      String idpName) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(-1234)).thenReturn("carbon.super");

            AuthenticatedUser user = UserSessionStore.getInstance().getUser(userId);
            Assert.assertEquals(user.getUserName(), username, "Expected username not received for user id: "
                    + userId);
            Assert.assertEquals(user.getUserStoreDomain(), userDomain.toUpperCase(), "Expected userDomain " +
                    "not received for user id: " + userId);
            Assert.assertEquals(user.getTenantDomain(), tenantDomain, "Expected tenantDomain " +
                    "not received for user id: " + userId);
            Assert.assertEquals(user.getFederatedIdPName(), idpName, "Expected idpName " +
                    "not received for user id: " + userId);
            AuthenticatedUser invalidUser = UserSessionStore.getInstance().getUser("invalidUserId");
            Assert.assertNull(invalidUser, "Expected null for invalid user id.");
        }
    }

    @Test(dependsOnMethods = {"testStoreUserData"}, expectedExceptions = UserSessionException.class)
    public void testGetUserForNullUserId() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(-1234)).thenReturn("carbon.super");
            UserSessionStore.getInstance().getUser(null);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdWithoutIdPParam(String expectedUserId, String username, int tenantId, String userDomain,
                                             int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            String actualUserId = UserSessionStore.getInstance().getUserId(username, tenantId, userDomain);
            Assert.assertEquals(actualUserId, expectedUserId, "Expected userId not received for user: " + username +
                    " of userstore domain: " + userDomain + ", tenant: " + tenantId);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdsOfUserStore(String expectedUserId, String username, int tenantId, String userDomain, int
            idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            List<String> userIdsOfUserStore
                    = UserSessionStore.getInstance().getUserIdsOfUserStore(userDomain, tenantId);
            Assert.assertTrue(userIdsOfUserStore.contains(expectedUserId), "Expected userId not found in the user " +
                    "list retrieved for userstore domain: " + userDomain + " of tenant: " + tenantId);
        }
    }

    @Test
    public void testGetIdPIdForLocalIdP() throws Exception {

        Assert.assertEquals(-1, UserSessionStore.getInstance().getIdPId("LOCAL"), "Expected -1 as the IdP " +
                "Id for LOCAL IdP.");
    }

    @Test(dataProvider = "getValidSessionsForUsers")
    public void testStoreUserSessionData(String userId, String sessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true, mockedIdentityDatabaseUtil);
            UserSessionStore.getInstance().storeUserSessionData(userId, sessionId);
        }
    }

    @Test(dataProvider = "getDuplicatedSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"},
            expectedExceptions = {UserSessionException.class, DuplicatedAuthUserException.class})
    public void testExceptionAtStoreUserSessionDataForDuplicatedSession(String userId, String sessionId) throws
            Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true, mockedIdentityDatabaseUtil);
            UserSessionStore.getInstance().storeUserSessionData(userId, sessionId);
        }
    }

    @Test(dataProvider = "getValidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testIsExistingMappingForValidSession(String userId, String sessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            Assert.assertTrue(UserSessionStore.getInstance().isExistingMapping(userId, sessionId), "Expected session:" +
                    " " + sessionId + " to be available for user id: " + userId);
        }
    }

    @Test(dataProvider = "getInvalidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testIsExistingMappingForInvalidSession(String userId, String sessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            Assert.assertFalse(UserSessionStore.getInstance().isExistingMapping(userId, sessionId),
                    "Expected session: " + sessionId + " to be unavailable for user id: " + userId);
        }
    }

    @Test(dataProvider = "getValidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testGetSessionId(String userId, String expectedSessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false, mockedIdentityDatabaseUtil);
            List<String> sessionIdsOfUser = UserSessionStore.getInstance().getSessionId(userId);
            Assert.assertTrue(sessionIdsOfUser.contains(expectedSessionId), "Expected session:" +
                    " " + expectedSessionId + " is unavailable for user id: " + userId);
        }
    }

    @Test(dataProvider = "getSessionAppsData", dependsOnMethods = {"testStoreUserSessionData"})
    public void testStoreAppSessionData(String sessionId, String subject, int appID, String inboundAuth)
            throws Exception {

        mockJdbcUtilsTemplate(getDatasource(DB_NAME), mockedJdbcUtils);
        UserSessionStore.getInstance().storeAppSessionData(sessionId, subject, appID, inboundAuth);
    }

    @Test(dataProvider = "getSessionMetadata", dependsOnMethods = {"testStoreUserSessionData"})
    public void testStoreSessionMetaData(String sessionId, Map<String, String> metaData) throws Exception {

        mockJdbcUtilsTemplate(getDatasource(DB_NAME), mockedJdbcUtils);
        UserSessionStore.getInstance().storeSessionMetaData(sessionId, metaData);
    }

    @Test()
    public void testGetActiveSessionCount() throws Exception {

        mockIdentityDataBaseUtilConnection(getConnection(DB_NAME), true, mockedIdentityDatabaseUtil);
        mockJdbcUtilsTemplate(getDatasource(DB_NAME), mockedJdbcUtils);
        populateTestDataForActiveSessions();

        Assert.assertEquals(UserSessionStore.getInstance().getActiveSessionCount(TENANT_DOMAIN), 2);
        cleanupTestData();
    }

    private void mockIdentityDataBaseUtilConnection(Connection connection, Boolean shouldApplyTransaction,
                                                    MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil)
            throws SQLException {

        Connection connection1 = spy(connection);
        doNothing().when(connection1).close();
        if (shouldApplyTransaction) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection1);
        } else {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(shouldApplyTransaction))
                    .thenReturn(connection1);
        }
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getSessionDBConnection(shouldApplyTransaction))
                .thenReturn(connection1);
    }

    private void mockJdbcUtilsTemplate(DataSource dataSource, MockedStatic<JdbcUtils> jdbcUtils)
            throws DataAccessException {

        DataSource dataSource1 = spy(dataSource);
        jdbcUtils.when(() -> JdbcUtils.isH2DB(JdbcUtils.Database.SESSION)).thenReturn(true);
        jdbcUtils.when(() -> JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION))
                .thenReturn(new JdbcTemplate(dataSource1));
        jdbcUtils.when(() -> JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION))
                .thenReturn(new JdbcTemplate(dataSource1));
    }

    private void populateTestDataForActiveSessions() throws SQLException {

        createUserSessionMapping("user1", "session1");
        createUserSessionMapping("user2", "session2");
        createUserSessionMapping("user3", "session3");
        createUserSessionMapping("user4", "session4");

        createSession("session1", "CREATE",
                System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(IDLE_SESSION_TIMEOUT) - 20000);
        createSession("session2", "CREATE", System.currentTimeMillis() - 10000);
        createSession("session3", "CREATE", System.currentTimeMillis() - 20000);
        createSession("session4", "CREATE", System.currentTimeMillis() - 30000);
        createSession("session2", "DELETE", System.currentTimeMillis() - 5000);

        addSessionMetadata("session1",
                String.valueOf(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(IDLE_SESSION_TIMEOUT) - 20000));
        addSessionMetadata("session2", String.valueOf(System.currentTimeMillis() - 10000));
        addSessionMetadata("session3", String.valueOf(System.currentTimeMillis() - 20000));
        addSessionMetadata("session4", String.valueOf(System.currentTimeMillis() - 30000));
    }

    private void createUserSessionMapping(String userId, String sessionId) throws SQLException {

        try (Connection connection = getConnection(DB_NAME)) {
            String sql = "INSERT INTO IDN_AUTH_USER_SESSION_MAPPING (USER_ID, SESSION_ID) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                stmt.setString(2, sessionId);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    private void createSession(String sessionId, String operation, long timeCreated) throws SQLException {

        try (Connection connection = getConnection(DB_NAME)) {
            String sql = "INSERT INTO IDN_AUTH_SESSION_STORE (SESSION_ID, SESSION_TYPE, OPERATION, TIME_CREATED, " +
                    "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, sessionId);
                stmt.setString(2, "AppAuthFrameworkSessionContextCache");
                stmt.setString(3, operation);
                stmt.setLong(4, timeCreated);
                stmt.setInt(5, operation.equals("DELETE") ? -1 : TENANT_ID);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    private void addSessionMetadata(String sessionId, String value) throws SQLException {

        try (Connection connection = getConnection(DB_NAME)) {
            String sql = "INSERT INTO IDN_AUTH_SESSION_META_DATA (SESSION_ID, PROPERTY_TYPE, `VALUE`) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, sessionId);
                stmt.setString(2, "Last Access Time");
                stmt.setString(3, value);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    private void cleanupTestData() throws SQLException {

        try (Connection connection = getConnection(DB_NAME)) {
            String[] cleanupStatements = {
                    "DELETE FROM IDN_AUTH_SESSION_STORE",
                    "DELETE FROM IDN_AUTH_USER_SESSION_MAPPING",
                    "DELETE FROM IDN_AUTH_SESSION_META_DATA",
                    "DELETE FROM IDN_AUTH_SESSION_APP_INFO"
            };

            for (String sql : cleanupStatements) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            connection.commit();
        }
    }
}
