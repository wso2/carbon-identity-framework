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

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class that includes unit tests of UserSessionStore
 */
@PrepareForTest({
        IdentityDatabaseUtil.class,
        org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils.class,
        org.wso2.carbon.identity.core.util.JdbcUtils.class
})
@PowerMockIgnore({"javax.xml.*"})
public class UserSessionStoreTest extends DataStoreBaseTest {

    private static final String DB_NAME = "USER_SESSION_STORE";

    @BeforeClass
    public void setUp() throws Exception {

        initMocks(this);
        initH2DB(DB_NAME, getDatabaseScriptFilePath("user_session_store_h2.sql"));
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2DB(DB_NAME);
    }

    @DataProvider
    public Object[][] getValidUsers() {

        return new Object[][]{
                {"00000001", "testuser1", -1234, "PRIMARY", -1},
                {"00000002", "testuser2", -1234, "Abc.com", -1},
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
            mockIdentityDataBaseUtilConnection(connection, true);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getUsersWithDuplicatedId", dependsOnMethods = {"testStoreUserData"}, expectedExceptions =
            UserSessionException.class)
    public void testExceptionAtStoreUserDataForDuplicatedUserID(String userId, String username, int tenantId, String
            userDomain, int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getDuplicatedUsers", dependsOnMethods = {"testStoreUserData"}, expectedExceptions =
            DuplicatedAuthUserException.class)
    public void testExceptionAtStoreUserDataForInvalidUser(String userId, String username, int tenantId, String
            userDomain, int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true);
            UserSessionStore.getInstance().storeUserData(userId, username, tenantId, userDomain, idpId);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdForAllUserParams(String expectedUserId, String username, int tenantId, String userDomain,
                                              int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
            String actualUserId = UserSessionStore.getInstance().getUserId(username, tenantId, userDomain, idpId);
            Assert.assertEquals(actualUserId, expectedUserId, "Expected userId not received for user: " + username +
                    " of userstore domain: " + userDomain + ", tenant: " + tenantId + " and idp id: " + idpId);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdWithoutIdPParam(String expectedUserId, String username, int tenantId, String userDomain,
                                             int idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
            String actualUserId = UserSessionStore.getInstance().getUserId(username, tenantId, userDomain);
            Assert.assertEquals(actualUserId, expectedUserId, "Expected userId not received for user: " + username +
                    " of userstore domain: " + userDomain + ", tenant: " + tenantId);
        }
    }

    @Test(dataProvider = "getValidUsers", dependsOnMethods = {"testStoreUserData"})
    public void testGetUserIdsOfUserStore(String expectedUserId, String username, int tenantId, String userDomain, int
            idpId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
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
            mockIdentityDataBaseUtilConnection(connection, true);
            UserSessionStore.getInstance().storeUserSessionData(userId, sessionId);
        }
    }

    @Test(dataProvider = "getDuplicatedSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"},
            expectedExceptions = UserSessionException.class)
    public void testExceptionAtStoreUserSessionDataForDuplicatedSession(String userId, String sessionId) throws
            Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, true);
            UserSessionStore.getInstance().storeUserSessionData(userId, sessionId);
        }
    }

    @Test(dataProvider = "getValidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testIsExistingMappingForValidSession(String userId, String sessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
            Assert.assertTrue(UserSessionStore.getInstance().isExistingMapping(userId, sessionId), "Expected session:" +
                    " " + sessionId + " to be available for user id: " + userId);
        }
    }

    @Test(dataProvider = "getInvalidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testIsExistingMappingForInvalidSession(String userId, String sessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
            Assert.assertFalse(UserSessionStore.getInstance().isExistingMapping(userId, sessionId),
                    "Expected session: " + sessionId + " to be unavailable for user id: " + userId);
        }
    }

    @Test(dataProvider = "getValidSessionsForUsers", dependsOnMethods = {"testStoreUserSessionData"})
    public void testGetSessionId(String userId, String expectedSessionId) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            mockIdentityDataBaseUtilConnection(connection, false);
            List<String> sessionIdsOfUser = UserSessionStore.getInstance().getSessionId(userId);
            Assert.assertTrue(sessionIdsOfUser.contains(expectedSessionId), "Expected session:" +
                    " " + expectedSessionId + " is unavailable for user id: " + userId);
        }
    }

    @Test(dataProvider = "getSessionAppsData", dependsOnMethods = {"testStoreUserSessionData"})
    public void testStoreAppSessionData(String sessionId, String subject, int appID, String inboundAuth)
            throws Exception {

        mockJdbcUtilsTemplate(getDatasource(DB_NAME));
        UserSessionStore.getInstance().storeAppSessionData(sessionId, subject, appID, inboundAuth);
    }

    @Test(dataProvider = "getSessionMetadata", dependsOnMethods = {"testStoreUserSessionData"})
    public void testStoreSessionMetaData(String sessionId, Map<String, String> metaData) throws Exception {

        mockJdbcUtilsTemplate(getDatasource(DB_NAME));
        UserSessionStore.getInstance().storeSessionMetaData(sessionId, metaData);
    }

    private void mockIdentityDataBaseUtilConnection(Connection connection, Boolean shouldApplyTransaction) throws
            SQLException {

        Connection connection1 = spy(connection);
        doNothing().when(connection1).close();
        mockStatic(IdentityDatabaseUtil.class);
        if (shouldApplyTransaction) {
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
        } else {
            when(IdentityDatabaseUtil.getDBConnection(shouldApplyTransaction)).thenReturn(connection1);
        }
        when(IdentityDatabaseUtil.getSessionDBConnection(shouldApplyTransaction)).thenReturn(connection1);
    }

    private void mockJdbcUtilsTemplate(DataSource dataSource) throws DataAccessException {

        DataSource dataSource1 = spy(dataSource);
        mockStatic(org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils.class);
        mockStatic(org.wso2.carbon.identity.core.util.JdbcUtils.class);
        when(org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB()).thenReturn(true);
        when(JdbcUtils.getNewTemplate()).thenReturn(new JdbcTemplate(dataSource1));
        when(JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION)).thenReturn(new JdbcTemplate(dataSource1));
    }
}
