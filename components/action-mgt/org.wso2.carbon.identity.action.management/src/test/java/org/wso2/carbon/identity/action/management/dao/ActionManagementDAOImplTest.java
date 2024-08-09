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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithH2Database(files = {"dbscripts/h2.sql"})
public class ActionManagementDAOImplTest {

    private ActionManagementDAOImpl daoImpl;
    private static final int TENANT_ID = 2;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityUtil> identityUtil;
    private static final String DB_NAME = "action_mgt";
    private ActionSecretProcessor actionSecretProcessor;
    private SecretManagerImpl secretManager;

    private final AuthType authType = new AuthType.AuthTypeBuilder()
            .type(AuthType.AuthenticationType.BEARER)
            .properties(Arrays.asList(
                    new AuthProperty.AuthPropertyBuilder().name("accesstoken").value("123456").isConfidential(true)
                            .build()))
            .build();

    @BeforeClass
    public void setUpClass() throws Exception{
        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() {

        daoImpl = new ActionManagementDAOImpl();
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        actionSecretProcessor = mock(ActionSecretProcessor.class);
        secretManager = mock(SecretManagerImpl.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        try{
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(getConnection());
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

    @Test
    public void testAddAction() throws ActionMgtException {

        Action action = getAction("120120");
        Action result = daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        Assert.assertEquals(action.getName(),result.getName());
        Assert.assertEquals(action.getDescription(),result.getDescription());
        Assert.assertEquals(action.getType(),result.getType());
        Assert.assertEquals(action.getStatus(),result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(),result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());

    }

    @Test
    public void testAddActionWithoutOptional() throws ActionMgtException {

        Action action = getActionWithoutOptionals("136136");
        Action result = daoImpl.addAction(action.getType().getActionType(), action.getId(), action,TENANT_ID);
        Assert.assertEquals(action.getName(),result.getName());
        Assert.assertEquals(action.getType(),result.getType());
        Assert.assertEquals(action.getStatus(),result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(),result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());

    }

    @Test
    public void testGetActionsByActionType() throws ActionMgtException{

        Action action = getAction("149149");
        Assert.assertEquals(5,daoImpl.getActionsByActionType(action.getType().getActionType(),TENANT_ID).size());
        loadMockDatabaseUtil();
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(6,daoImpl.getActionsByActionType(action.getType().getActionType(),TENANT_ID).size());

    }

    @Test
    public void testUpdateAction() throws ActionMgtException {

        Action action = getAction("139139");
        Action addedAction = daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Action updatedAction = new Action.ActionResponseBuilder()
                .id(action.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name("Pre Issue Access Token")
                .description("To configure pre issue access token")
                .status(Action.Status.ACTIVE)
                .endpoint( new EndpointConfig.EndpointConfigBuilder()
                        .uri("https://example123.com")
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(AuthType.AuthenticationType.BASIC)
                                .properties(Arrays.asList(
                                        new AuthProperty.AuthPropertyBuilder().name("username").value("admin").isConfidential(true).build(),
                                        new AuthProperty.AuthPropertyBuilder().name("password").value("admin").isConfidential(true).build()))
                                .build())
                        .build())
                .build();
        Action result = daoImpl.updateAction(updatedAction.getType().getActionType(),updatedAction.getId(),updatedAction,addedAction,TENANT_ID);

        Assert.assertEquals(updatedAction.getName(),result.getName());
        Assert.assertEquals(updatedAction.getDescription(),result.getDescription());
        Assert.assertEquals(updatedAction.getType(),result.getType());
        Assert.assertEquals(updatedAction.getStatus(),result.getStatus());
        Assert.assertEquals(updatedAction.getEndpoint().getUri(),result.getEndpoint().getUri());
        Assert.assertEquals(updatedAction.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());

    }

    @Test
    public void testDeleteAction() throws ActionMgtException {

        Action action = getAction("191191");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(5,daoImpl.getActionsByActionType(action.getType().getActionType(),TENANT_ID).size());
        loadMockDatabaseUtil();
        daoImpl.deleteAction(action.getType().getActionType(), action.getId(),action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(4,daoImpl.getActionsByActionType(action.getType().getActionType(),TENANT_ID).size());

    }

    @Test
    public void testDeactivateAction() throws ActionMgtException {

        Action action = getAction("184184");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Assert.assertEquals(Action.Status.ACTIVE,action.getStatus());
        Action deactivatedAction = daoImpl.deactivateAction(action.getType().getActionType(),action.getId(),TENANT_ID);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());

    }

    @Test
    public void testActivateAction() throws ActionMgtException {

        Action action = getAction("265265");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Action deactivatedAction = daoImpl.deactivateAction(action.getType().getActionType(), action.getId(),TENANT_ID);
        loadMockDatabaseUtil();
        Action result = daoImpl.activateAction(deactivatedAction.getType().getActionType(),deactivatedAction.getId(),TENANT_ID);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());

    }

    @Test
    public void testGetActionByActionId() throws ActionMgtException {
        Action action = getAction("274274");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Action result = daoImpl.getActionByActionId(action.getId(),TENANT_ID);
        Assert.assertEquals(action.getName(),result.getName());
        Assert.assertEquals(action.getDescription(),result.getDescription());
        Assert.assertEquals(action.getType(),result.getType());
        Assert.assertEquals(action.getStatus(),result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(),result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(), result.getEndpoint().getAuthentication().getType());

    }

    @Test
    public void testUpdateActionEndpointAuthProperties() throws ActionMgtException {
        Action action = getAction("239239");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        Action result = daoImpl.updateActionEndpointAuthProperties(action.getId(),authType,TENANT_ID);
        Assert.assertEquals(2,result.getEndpoint().getAuthentication().getProperties().size());

    }

    @Test
    public void testUpdateActionEndpoint() throws ActionMgtException{
        Action action = getAction("298298");
        daoImpl.addAction(action.getType().getActionType(), action.getId(), action, TENANT_ID);
        loadMockDatabaseUtil();
        EndpointConfig endpointConfig = new EndpointConfig.EndpointConfigBuilder()
                .uri("https://example123.com")
                .authentication(new AuthType.AuthTypeBuilder()
                        .type(AuthType.AuthenticationType.BEARER)
                        .properties(Arrays.asList(
                                new AuthProperty.AuthPropertyBuilder().name("accesstoken").value("123456").isConfidential(true).build()))
                        .build())
                .build();
        Action result = daoImpl.updateActionEndpoint(action.getType().getActionType(),action.getId(), endpointConfig,action.getEndpoint()
                .getAuthentication(), TENANT_ID);
        Assert.assertNotEquals(action.getEndpoint().getUri(),result.getEndpoint().getUri());
        Assert.assertEquals(AuthType.AuthenticationType.BEARER, result.getEndpoint().getAuthentication().getType());
    }

    private Action getAction(String id){
        return new Action.ActionResponseBuilder()
                .id(id)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name("Pre Issue Access Token")
                .description("To configure pre issue access token")
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
    }

    private Action getActionWithoutOptionals(String id){
        return new Action.ActionResponseBuilder()
                .id(id)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name("Pre Issue Access Token")
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
    }

    private void loadMockDatabaseUtil() {
        try {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + ActionManagementDAOImplTest.DB_NAME);
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

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

}