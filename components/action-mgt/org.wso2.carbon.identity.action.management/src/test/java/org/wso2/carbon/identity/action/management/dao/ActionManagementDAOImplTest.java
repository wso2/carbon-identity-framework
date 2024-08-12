package org.wso2.carbon.identity.action.management.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithH2Database(files = {"dbscripts/h2.sql"})
public class ActionManagementDAOImplTest {

    private ActionManagementDAOImpl daoImpl;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityUtil> identityUtil;
    private static final String DB_NAME = "action_mgt";
    public static final String PRE_ISSUE_ACCESS_TOKEN = "PRE_ISSUE_ACCESS_TOKEN";
    private static final int TENANT_ID = 2;
    private ActionSecretProcessor actionSecretProcessor;
    private SecretManagerImpl secretManager;

    private final AuthType authType = new AuthType.AuthTypeBuilder()
            .type(AuthType.AuthenticationType.BEARER)
            .properties(Arrays.asList(
                    new AuthProperty.AuthPropertyBuilder()
                            .name("accesstoken")
                            .value("1c4a0d92-a6f5-4eed-9465-51ab1174a90f")
                            .isConfidential(true)
                            .build()
            ))
            .build();

    @BeforeClass
    public void setUpClass() throws Exception{

        daoImpl = new ActionManagementDAOImpl();
        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        actionSecretProcessor = mock(ActionSecretProcessor.class);
        secretManager = mock(SecretManagerImpl.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        try{
            loadMockDatabaseUtil();
            identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
            SecretType secretType = mock(SecretType.class);
            when(secretType.getId()).thenReturn("secretId");
            when(secretManager.getSecretType(any())).thenReturn(secretType);
        }
        catch (Exception ignored){
        }
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
        identityDatabaseUtil.close();
        identityUtil.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException, SQLException {

        Action action = buildMockAction(false,false);
        Action result = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 2)
    public void testGetActionsByActionType() throws ActionMgtException, SQLException {

        Action action = buildMockAction(false,false);
        Assert.assertEquals(1, daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).size());
        loadMockDatabaseUtil();
        daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(2, daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).size());
    }

    @Test(priority = 3)
    public void testDeleteAction() throws ActionMgtException, SQLException {

        Action action = buildMockAction(false,true);
        Assert.assertEquals(3, daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).size());
        loadMockDatabaseUtil();
        daoImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(2, daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).size());
    }

    @Test(priority = 4)
    public void testAddActionWithoutOptional() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,false);
        Action result = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 5)
    public void testUpdateAction() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        Action updatingAction = new Action.ActionRequestBuilder()
                .name("Pre Issue Access Token")
                .description("To configure pre issue access token")
                .endpoint( new EndpointConfig.EndpointConfigBuilder()
                        .uri("https://sample.com")
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(AuthType.AuthenticationType.BASIC)
                                .properties(Arrays.asList(
                                        new AuthProperty.AuthPropertyBuilder().name("username").value("updatingadmin").isConfidential(true).build(),
                                        new AuthProperty.AuthPropertyBuilder().name("password").value("updatingadmin").isConfidential(true).build()))
                                .build())
                        .build())
                .build();
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(
                updatingAction.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 6)
    public void testDeactivateAction() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Action deactivatedAction = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 7)
    public void testActivateAction() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        Action deactivatedAction = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        loadMockDatabaseUtil();
        Action result = daoImpl.activateAction(PRE_ISSUE_ACCESS_TOKEN, deactivatedAction.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 8)
    public void testGetActionByActionId() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        Action result = daoImpl.getActionByActionId(action.getId(), TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 9)
    public void testUpdateActionEndpointAuthProperties() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        Action result = daoImpl.updateActionEndpointAuthProperties(action.getId(), authType, TENANT_ID);
        Assert.assertEquals(2, result.getEndpoint().getAuthentication().getProperties().size());
    }

    @Test(priority = 10)
    public void testUpdateActionEndpoint() throws ActionMgtException, SQLException {

        Action action = buildMockAction(true,true);
        EndpointConfig endpointConfig = new EndpointConfig.EndpointConfigBuilder()
                .uri("https://example123.com")
                .authentication(new AuthType.AuthTypeBuilder()
                        .type(AuthType.AuthenticationType.BEARER)
                        .properties(Arrays.asList(
                                new AuthProperty.AuthPropertyBuilder().name("accesstoken").value("123456").isConfidential(true).build()))
                        .build())
                .build();
        Action result = daoImpl.updateActionEndpoint(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), endpointConfig, action.getEndpoint()
                .getAuthentication(), TENANT_ID);
        Assert.assertNotEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(AuthType.AuthenticationType.BEARER, result.getEndpoint().getAuthentication().getType());
    }

    private Action buildMockAction(boolean isOptional, boolean persistAction) throws SQLException, ActionMgtException {

        String id = String.valueOf(UUID.randomUUID());
        Action action = new Action.ActionResponseBuilder()
                .id(id)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name("Pre Issue Access Token")
                .description(!isOptional ? "To configure pre issue access token" : null)
                .status(Action.Status.ACTIVE)
                .endpoint( new EndpointConfig.EndpointConfigBuilder()
                        .uri("https://example.com")
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(AuthType.AuthenticationType.BASIC)
                                .properties(Arrays.asList(
                                        new AuthProperty.AuthPropertyBuilder().name("username").value("admin").isConfidential(true).build(),
                                        new AuthProperty.AuthPropertyBuilder().name("password").value("admin").isConfidential(true).build()))
                                .build())
                        .build())
                .build();

        if (persistAction) {
            daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
            loadMockDatabaseUtil();
        }

        return action;

    }

    private void loadMockDatabaseUtil() throws SQLException {

        Connection connection = dataSourceMap.get(DB_NAME).getConnection();
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
    }

    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static void closeH2Database() throws SQLException {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
