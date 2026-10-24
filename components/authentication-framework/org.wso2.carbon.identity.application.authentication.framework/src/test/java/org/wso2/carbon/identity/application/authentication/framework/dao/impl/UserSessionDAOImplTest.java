/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.ExecuteCallable;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.QueryFilter;
import org.wso2.carbon.database.utils.jdbc.Template;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedUserSession;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@WithH2Database(files = {"dbScripts/h2.sql"})
public class UserSessionDAOImplTest extends IdentityBaseTest {

    UserSessionDAOImpl userSessionDAO;

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "testOIDCSLO";
    private static final int APP_ID = 4321;
    private static final int UNKNOWN_APP_ID = 9999;
    private static final String SESSION_CONTEXT_KEY
            = "02278824dfe9862d265e389365c0a71c365401672491b78c6ee7dd6fc44d8af4";
    private static final String IDP_SESSION_INDEX = "15043ffc-877d-4205-af41-9b107f7da38c";
    private static final String IDP_NAME = "Federated-IdP";
    private static final String AUTHENTICATOR_ID = "OpenIDConnectAuthenticator";
    private static final String PROTOCOL_TYPE = "oidc";
    private static final String SUBJECT = "user1";
    // A driver name that does not identify the database, so only the product name matches.
    private static final String WRAPPER_DRIVER = "Wrapper JDBC Driver";

    @BeforeMethod
    public void init() {

        userSessionDAO = new UserSessionDAOImpl();
    }

    private void initiateH2Base(String databaseName, String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbScripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    private void setupSessionStore(MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil) throws Exception {

        initiateH2Base(DB_NAME, getFilePath("h2.sql"));

        try (Connection connection1 = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection1);

            String sql = "INSERT INTO IDN_FED_AUTH_SESSION_MAPPING " +
                    "(IDP_SESSION_ID, SESSION_ID, IDP_NAME,  AUTHENTICATOR_ID, PROTOCOL_TYPE) VALUES ( '" +
                    IDP_SESSION_INDEX + "' , '" + SESSION_CONTEXT_KEY + "' , '" + IDP_NAME + "' , '" +
                    AUTHENTICATOR_ID +
                    "', '" + PROTOCOL_TYPE + "');";

            PreparedStatement statement = connection1.prepareStatement(sql);
            statement.execute();
        }

        try (Connection connection1 = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection1);

            String query = "SELECT * FROM IDN_FED_AUTH_SESSION_MAPPING WHERE IDP_SESSION_ID=?";
            PreparedStatement statement2 = connection1.prepareStatement(query);
            statement2.setString(1, IDP_SESSION_INDEX);
            ResultSet resultSet = statement2.executeQuery();
            String result = null;
            if (resultSet.next()) {
                result = resultSet.getString("SESSION_ID");
            }
            assertEquals(SESSION_CONTEXT_KEY, result, "Failed to retrieve session details for IDP_SESSION_ID");
        }
    }

    @Test
    public void testGetFederatedAuthSessionDetails() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            setupSessionStore(identityDatabaseUtil);
            DataSource dataSource = mock(DataSource.class);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getSessionDataSource).thenReturn(dataSource);
            identityDatabaseUtil.when(dataSource::getConnection).thenReturn(getConnection(DB_NAME));
            FederatedUserSession federatedUserSession =
                    userSessionDAO.getFederatedAuthSessionDetails(IDP_SESSION_INDEX);
            assertEquals(federatedUserSession.getSessionId(), SESSION_CONTEXT_KEY);
            assertEquals(federatedUserSession.getIdpName(), IDP_NAME);
            assertEquals(federatedUserSession.getAuthenticatorName(), AUTHENTICATOR_ID);
            assertEquals(federatedUserSession.getProtocolType(), PROTOCOL_TYPE);
        }
    }

    /**
     * Sessions are read in batches of a fixed size, so a list that spans more than one batch has to come back whole.
     */
    @Test
    public void testGetSessionsSpanningSeveralBatches() throws Exception {

        int sessionCount = 501;
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            String dbName = "testSessionBatches";
            initiateH2Base(dbName, getFilePath("h2.sql"));
            List<String> sessionIds = new ArrayList<>();
            try (Connection connection = getConnection(dbName)) {
                for (int i = 0; i < sessionCount; i++) {
                    String sessionId = "batched-session-" + i;
                    sessionIds.add(sessionId);
                    insertSessionAppInfo(connection, sessionId, APP_ID);
                    insertSessionMetadata(connection, sessionId, "IP", "10.0.0." + (i % 255));
                }
            }
            mockApplicationService();
            mockConnections(identityDatabaseUtil, dbName);

            Map<String, UserSession> sessions = userSessionDAO.getSessions(sessionIds);

            assertEquals(sessions.size(), sessionCount, "Every session of the list should be returned.");
            UserSession first = sessions.get("batched-session-0");
            assertNotNull(first, "A session of the first batch should be returned.");
            assertEquals(first.getApplications().get(0).getAppName(), "TestApp",
                    "The application name should be resolved.");
            assertNotNull(sessions.get("batched-session-500"), "A session of the last batch should be returned.");
        }
    }

    /**
     * A session with no application info is not returned, and neither is one whose only application is not in
     * SP_APP, since such an application is not considered part of a session.
     */
    @Test
    public void testGetSessionsSkipsSessionsWithoutResolvableApplications() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            String dbName = "testSessionSkips";
            initiateH2Base(dbName, getFilePath("h2.sql"));
            try (Connection connection = getConnection(dbName)) {
                // A session with an application that exists.
                insertSessionAppInfo(connection, "resolvable-session", APP_ID);
                insertSessionMetadata(connection, "resolvable-session", "IP", "10.0.0.1");
                // A session with metadata but no application info at all.
                insertSessionMetadata(connection, "no-app-info-session", "IP", "10.0.0.2");
                // A session whose only application is not in SP_APP.
                insertSessionAppInfo(connection, "unknown-app-session", UNKNOWN_APP_ID);
                insertSessionMetadata(connection, "unknown-app-session", "IP", "10.0.0.3");
            }
            mockApplicationService();
            mockConnections(identityDatabaseUtil, dbName);

            Map<String, UserSession> sessions = userSessionDAO.getSessions(Arrays.asList(
                    "resolvable-session", "no-app-info-session", "unknown-app-session"));

            assertEquals(sessions.size(), 1, "Only the session with a resolvable application should be returned.");
            assertNotNull(sessions.get("resolvable-session"));
            assertNull(sessions.get("no-app-info-session"), "A session without application info should be skipped.");
            assertNull(sessions.get("unknown-app-session"),
                    "A session whose application is not in SP_APP should be skipped.");
        }
    }

    /**
     * Blank session IDs must not reach the query, and an empty list must not query at all.
     */
    @Test
    public void testGetSessionsWithBlankAndEmptyInput() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            String dbName = "testSessionBlankInput";
            initiateH2Base(dbName, getFilePath("h2.sql"));
            mockApplicationService();
            mockConnections(identityDatabaseUtil, dbName);

            assertEquals(userSessionDAO.getSessions(Collections.emptyList()).size(), 0);
            assertEquals(userSessionDAO.getSessions(Arrays.asList(null, "", " ")).size(), 0);
        }
    }

    /**
     * The session cleanup task removes the federated session mappings of the expired sessions it removes, and keeps
     * those of active sessions.
     */
    @Test
    public void testRemoveExpiredSessionRecordsRemovesFederatedSessionMappings() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            String dbName = "testRemoveExpiredFedMappings";
            initiateH2Base(dbName, getFilePath("h2.sql"));
            long currentTime = FrameworkUtils.getCurrentStandardNano();
            try (Connection connection = getConnection(dbName)) {
                insertSessionContext(connection, "expired-session", currentTime - TimeUnit.MINUTES.toNanos(1));
                insertSessionContext(connection, "active-session", currentTime + TimeUnit.MINUTES.toNanos(10));
                insertFederatedSessionMapping(connection, "expired-session-index", "expired-session");
                insertFederatedSessionMapping(connection, "active-session-index", "active-session");
            }
            mockConnections(identityDatabaseUtil, dbName);

            userSessionDAO.removeExpiredSessionRecords();

            try (Connection connection = getConnection(dbName)) {
                assertFalse(isFederatedSessionMappingAvailable(connection, "expired-session"),
                        "Federated session mapping of an expired session should be removed.");
                assertTrue(isFederatedSessionMappingAvailable(connection, "active-session"),
                        "Federated session mapping of an active session should not be removed.");
            }
        }
    }

    /**
     * Removing the records of terminated sessions also removes their federated session mappings.
     */
    @Test
    public void testRemoveTerminatedSessionRecordsRemovesFederatedSessionMappings() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            String dbName = "testRemoveTerminatedFedMappings";
            initiateH2Base(dbName, getFilePath("h2.sql"));
            try (Connection connection = getConnection(dbName)) {
                insertFederatedSessionMapping(connection, "terminated-session-index", "terminated-session");
                insertFederatedSessionMapping(connection, "other-session-index", "other-session");
            }
            mockConnections(identityDatabaseUtil, dbName);

            userSessionDAO.removeTerminatedSessionRecords(Collections.singletonList("terminated-session"));

            try (Connection connection = getConnection(dbName)) {
                assertFalse(isFederatedSessionMappingAvailable(connection, "terminated-session"),
                        "Federated session mapping of a terminated session should be removed.");
                assertTrue(isFederatedSessionMappingAvailable(connection, "other-session"),
                        "Federated session mapping of another session should not be removed.");
            }
        }
    }

    @DataProvider
    public Object[][] storeAppSessionDataQueries() {

        return new Object[][]{
                {"Oracle JDBC driver", "Oracle", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE},
                {WRAPPER_DRIVER, "Oracle", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE},
                {"Microsoft JDBC Driver 12.4 for SQL Server", "Microsoft SQL Server",
                        SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MSSQL},
                {"jTDS Type 4 JDBC Driver for MS SQL Server and Sybase", "Microsoft SQL Server",
                        SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MSSQL},
                {"IBM DB2 JDBC Universal Driver Architecture", "DB2/LINUXX8664",
                        SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_DB2},
                {"IBM Data Server Driver for JDBC and SQLJ", "DB2/LINUXX8664",
                        SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_DB2},
                {"MySQL Connector/J", "MySQL", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB},
                {"MariaDB Connector/J", "MySQL", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB},
                {"MariaDB Connector/J", "MariaDB", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB},
                {WRAPPER_DRIVER, "MariaDB", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB},
                {"PostgreSQL JDBC Driver", "PostgreSQL", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_POSTGRES},
                {WRAPPER_DRIVER, "PostgreSQL", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_POSTGRES},
                {"H2 JDBC Driver", "H2", SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_H2}
        };
    }

    /**
     * The session DB type is read once, before the transaction, and selects the store query of the database.
     */
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "storeAppSessionDataQueries")
    public void testStoreAppSessionDataQueryPerDatabase(String driverName, String databaseProductName,
                                                        String expectedQuery) throws Exception {

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Template<Object> template = mock(Template.class);
        when(jdbcTemplate.getDriverName()).thenReturn(driverName);
        when(jdbcTemplate.getDatabaseProductName()).thenReturn(databaseProductName);
        when(jdbcTemplate.withTransaction(any(ExecuteCallable.class))).thenAnswer(
                invocation -> ((ExecuteCallable<Object>) invocation.getArgument(0)).get(template));

        try (MockedStatic<JdbcUtils> jdbcUtils = mockNewTemplate(jdbcTemplate)) {
            userSessionDAO.storeAppSessionData(SESSION_CONTEXT_KEY, SUBJECT, APP_ID, PROTOCOL_TYPE);
            // A DB type check that creates its own template would borrow a second connection.
            jdbcUtils.verify(() -> JdbcUtils.getNewTemplate(any(JdbcUtils.Database.class)), times(1));
        }
        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).getDriverName();
        inOrder.verify(jdbcTemplate).getDatabaseProductName();
        inOrder.verify(jdbcTemplate).withTransaction(any(ExecuteCallable.class));
        verify(template).executeUpdate(eq(expectedQuery), any(QueryFilter.class));
    }

    /**
     * A failure to read the session DB type is reported before any transaction starts.
     */
    @Test
    public void testStoreAppSessionDataWhenDBTypeCannotBeResolved() throws Exception {

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDriverName()).thenThrow(new DataAccessException("Database metadata unavailable."));

        try (MockedStatic<JdbcUtils> jdbcUtils = mockNewTemplate(jdbcTemplate)) {
            DataAccessException e = expectThrows(DataAccessException.class, () ->
                    userSessionDAO.storeAppSessionData(SESSION_CONTEXT_KEY, SUBJECT, APP_ID, PROTOCOL_TYPE));
            assertTrue(e.getMessage().startsWith("Error while resolving the session database type"));
        }
        verify(jdbcTemplate, never()).withTransaction(any(ExecuteCallable.class));
    }

    private static MockedStatic<JdbcUtils> mockNewTemplate(JdbcTemplate jdbcTemplate) {

        // Every new template is the given mock; the DB type checks run for real.
        return mockStatic(JdbcUtils.class, invocation -> "getNewTemplate".equals(invocation.getMethod().getName()) ?
                jdbcTemplate : invocation.callRealMethod());
    }

    private void mockConnections(MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil, String dbName)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getSessionDataSource).thenReturn(dataSource);
        when(dataSource.getConnection()).thenAnswer(invocation -> getConnection(dbName));
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenAnswer(invocation -> getConnection(dbName));
    }

    /**
     * Make only APP_ID resolvable, as the application management service does for applications that exist.
     */
    private void mockApplicationService() throws Exception {

        ApplicationBasicInfo info = new ApplicationBasicInfo();
        info.setApplicationId(APP_ID);
        info.setApplicationName("TestApp");
        info.setApplicationResourceId("uuid-" + APP_ID);
        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfosByIds(any())).thenAnswer(invocation -> {
            List<ApplicationBasicInfo> infos = new ArrayList<>();
            for (int appId : (int[]) invocation.getArgument(0)) {
                if (appId == APP_ID) {
                    infos.add(info);
                }
            }
            return infos;
        });
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
    }

    private void insertSessionAppInfo(Connection connection, String sessionId, int appId) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO IDN_AUTH_SESSION_APP_INFO (SESSION_ID, " +
                "SUBJECT, APP_ID, INBOUND_AUTH_TYPE) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, sessionId);
            ps.setString(2, "subject");
            ps.setInt(3, appId);
            ps.setString(4, "oauth2");
            ps.execute();
        }
    }

    private void insertSessionMetadata(Connection connection, String sessionId, String propertyType, String value)
            throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO IDN_AUTH_SESSION_META_DATA (SESSION_ID, " +
                "PROPERTY_TYPE, `VALUE`) VALUES (?, ?, ?)")) {
            ps.setString(1, sessionId);
            ps.setString(2, propertyType);
            ps.setString(3, value);
            ps.execute();
        }
    }

    private void insertSessionContext(Connection connection, String sessionId, long expiryTime) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO IDN_AUTH_SESSION_STORE (SESSION_ID, " +
                "SESSION_TYPE, OPERATION, TIME_CREATED, TENANT_ID, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, sessionId);
            ps.setString(2, "AppAuthFrameworkSessionContextCache");
            ps.setString(3, "STORE");
            ps.setLong(4, FrameworkUtils.getCurrentStandardNano());
            ps.setInt(5, -1234);
            ps.setLong(6, expiryTime);
            ps.execute();
        }
    }

    private void insertFederatedSessionMapping(Connection connection, String idpSessionId, String sessionId)
            throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO IDN_FED_AUTH_SESSION_MAPPING " +
                "(IDP_SESSION_ID, SESSION_ID, IDP_NAME, AUTHENTICATOR_ID, PROTOCOL_TYPE) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, idpSessionId);
            ps.setString(2, sessionId);
            ps.setString(3, IDP_NAME);
            ps.setString(4, AUTHENTICATOR_ID);
            ps.setString(5, PROTOCOL_TYPE);
            ps.execute();
        }
    }

    private boolean isFederatedSessionMappingAvailable(Connection connection, String sessionId) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM IDN_FED_AUTH_SESSION_MAPPING WHERE SESSION_ID = ?")) {
            ps.setString(1, sessionId);
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
