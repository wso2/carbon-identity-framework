package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedUserSession;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.*;

@PrepareForTest({IdentityDatabaseUtil.class, DataSource.class})
@PowerMockIgnore("jdk.internal.reflect.*")
@WithH2Database(files = {"dbscripts/h2.sql"})
public class UserSessionDAOImplTest {

    UserSessionDAOImpl userSessionDAO;

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "testOIDCSLO";
    private static String SESSION_CONTEXT_KEY = "02278824dfe9862d265e389365c0a71c365401672491b78c6ee7dd6fc44d8af4";
    private static String IDP_SESSION_INDEX = "15043ffc-877d-4205-af41-9b107f7da38c";

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
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
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

    private void prepareConnection(Connection connection1, boolean b) {

        mockStatic(IdentityDatabaseUtil.class);
        PowerMockito.when(IdentityDatabaseUtil.getDBConnection(b)).thenReturn(connection1);
    }

    private void setupSessionStore() throws Exception {

        initiateH2Base(DB_NAME, getFilePath("h2.sql"));

        String IDP_NAME = "Federated-IdP";
        String AUTHENTICATOR_ID = "OpenIDConnectAuthenticator";
        String PROTOCOL_TYPE = "oidc";

        try (Connection connection1 = getConnection(DB_NAME)) {
            prepareConnection(connection1, false);

            String sql = "INSERT INTO IDN_FED_AUTH_SESSION_MAPPING " +
                    "(IDP_SESSION_ID, SESSION_ID, IDP_NAME,  AUTHENTICATOR_ID, PROTOCOL_TYPE) VALUES ( '" +
                    IDP_SESSION_INDEX + "' , '" + SESSION_CONTEXT_KEY + "' , '" + IDP_NAME + "' , '" +
                    AUTHENTICATOR_ID +
                    "', '" + PROTOCOL_TYPE + "');";

            PreparedStatement statement = connection1.prepareStatement(sql);
            statement.execute();
        }

        try (Connection connection1 = getConnection(DB_NAME)) {
            prepareConnection(connection1, false);
            String query = "SELECT * FROM IDN_FED_AUTH_SESSION_MAPPING WHERE IDP_SESSION_ID=?";
            PreparedStatement statement2 = connection1.prepareStatement(query);
            statement2.setString(1, "15043ffc-877d-4205-af41-9b107f7da38c");
            ResultSet resultSet = statement2.executeQuery();
            String result = null;
            if (resultSet.next()) {
                result = resultSet.getString("SESSION_ID");
            }
            assertEquals(SESSION_CONTEXT_KEY, result, "Failed to handle for valid input");
        }

    }

    @Test
    public void testGetFederatedAuthSessionDetails() throws Exception {

        setupSessionStore();
        DataSource dataSource = mock(DataSource.class);
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(getConnection(DB_NAME));
        FederatedUserSession federatedUserSession = userSessionDAO.getFederatedAuthSessionDetails(IDP_SESSION_INDEX);
        assertEquals(federatedUserSession.getSessionId(), SESSION_CONTEXT_KEY);
    }
}