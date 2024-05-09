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
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedUserSession;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

@WithH2Database(files = {"dbScripts/h2.sql"})
public class UserSessionDAOImplTest extends IdentityBaseTest {

    UserSessionDAOImpl userSessionDAO;

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "testOIDCSLO";
    private static final String SESSION_CONTEXT_KEY
            = "02278824dfe9862d265e389365c0a71c365401672491b78c6ee7dd6fc44d8af4";
    private static final String IDP_SESSION_INDEX = "15043ffc-877d-4205-af41-9b107f7da38c";
    private static final String IDP_NAME = "Federated-IdP";
    private static final String AUTHENTICATOR_ID = "OpenIDConnectAuthenticator";
    private static final String PROTOCOL_TYPE = "oidc";

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
}
