/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
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

/**
 * This class is a test suite for the ActionManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ActionManagementDAOImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
public class ActionManagementDAOImplTest {

    private ActionManagementDAOImpl daoImpl;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private static final String DB_NAME = "action_mgt";
    private static final String PRE_ISSUE_ACCESS_TOKEN = "PRE_ISSUE_ACCESS_TOKEN";
    private static final int TENANT_ID = 2;
    private Action action;
    private final AuthType authType = new AuthType.AuthTypeBuilder()
            .type(AuthType.AuthenticationType.BASIC)
            .properties(Arrays.asList(
                    new AuthProperty.AuthPropertyBuilder()
                            .name(AuthType.AuthenticationType
                                    .AuthenticationProperty.USERNAME.getName())
                            .value("newadmin")
                            .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                    .USERNAME.getIsConfidential())
                            .build(),
                    new AuthProperty.AuthPropertyBuilder()
                            .name(AuthType.AuthenticationType
                                    .AuthenticationProperty.PASSWORD.getName())
                            .value("newadmin")
                            .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                    .PASSWORD.getIsConfidential())
                            .build()))
            .build();

    @BeforeClass
    public void setUpClass() throws Exception {

        daoImpl = new ActionManagementDAOImpl();
        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() throws SQLException, SecretManagementException {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        mockDBConnection();
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
        when(secretType.getId()).thenReturn("secretId");
        when(secretManager.getSecretType(any())).thenReturn(secretType);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
        identityDatabaseUtil.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException {

        Action creatingAction = buildMockAction(false);
        action = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction.getId(), creatingAction, TENANT_ID);
        Assert.assertEquals(creatingAction.getId(), action.getId());
        Assert.assertEquals(creatingAction.getName(), action.getName());
        Assert.assertEquals(creatingAction.getDescription(), action.getDescription());
        Assert.assertEquals(creatingAction.getType(), action.getType());
        Assert.assertEquals(creatingAction.getStatus(), action.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), action.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 2)
    public void testGetActionsByActionType() throws ActionMgtException, SQLException {

        Assert.assertEquals(1, daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).size());
        mockDBConnection();
        Action result = daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, TENANT_ID).get(0);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 3)
    public void testGetActionByActionId() throws ActionMgtException {

        Action result = daoImpl.getActionByActionId(action.getId(), TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 4)
    public void testDeleteAction() throws ActionMgtException, SQLException {

        daoImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        mockDBConnection();
        Assert.assertNull(daoImpl.getActionByActionId(action.getId(), TENANT_ID));
    }

    @Test(priority = 5)
    public void testAddActionWithoutOptionalAttributes() throws ActionMgtException {

        Action creatingAction = buildMockAction(true);
        action = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction.getId(), creatingAction, TENANT_ID);
        Assert.assertEquals(creatingAction.getId(), action.getId());
        Assert.assertEquals(creatingAction.getName(), action.getName());
        Assert.assertNull(null, action.getDescription());
        Assert.assertEquals(creatingAction.getType(), action.getType());
        Assert.assertEquals(creatingAction.getStatus(), action.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), action.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 6)
    public void testUpdateAction() throws ActionMgtException {

        Action updatingAction = new Action.ActionRequestBuilder()
                .name("Pre Issue Access Token")
                .description("To configure pre issue access token")
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri("https://sample.com")
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(AuthType.AuthenticationType.BASIC)
                                .properties(Arrays.asList(
                                        new AuthProperty.AuthPropertyBuilder()
                                                .name("username")
                                                .value("updatingadmin")
                                                .isConfidential(true)
                                                .build(),
                                        new AuthProperty.AuthPropertyBuilder()
                                                .name("password")
                                                .value("updatingadmin")
                                                .isConfidential(true)
                                                .build()))
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
                updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType()
        );
    }

    @Test(priority = 7)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Action deactivatedAction = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 8)
    public void testActivateAction() throws ActionMgtException {

        Action result = daoImpl.activateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 9)
    public void testUpdateActionEndpointAuthProperties() {

        try {
            Action result = daoImpl.updateActionEndpointAuthProperties(action.getId(), authType, TENANT_ID);
            Assert.assertEquals(AuthType.AuthenticationType.BASIC, result.getEndpoint().getAuthentication().getType());
            Assert.assertEquals(2, result.getEndpoint().getAuthentication().getProperties().size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test(priority = 10)
    public void testUpdateActionEndpoint() throws ActionMgtException {

        EndpointConfig endpointConfig = new EndpointConfig.EndpointConfigBuilder()
                .uri("https://template.com")
                .authentication(new AuthType.AuthTypeBuilder()
                        .type(AuthType.AuthenticationType.BEARER)
                        .properties(Arrays.asList(
                                new AuthProperty.AuthPropertyBuilder()
                                        .name(AuthType.AuthenticationType.AuthenticationProperty.ACCESS_TOKEN.getName())
                                        .value("c7fce95f-3f5b-4cda-8bb1-4cb7b3990f83")
                                        .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                                .ACCESS_TOKEN.getIsConfidential())
                                        .build()))
                        .build())
                .build();
        Action result = daoImpl.updateActionEndpoint(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), endpointConfig, action.getEndpoint()
                .getAuthentication(), TENANT_ID);
        Assert.assertNotEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(AuthType.AuthenticationType.BEARER, result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 11)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = daoImpl.getActionsCountPerType(TENANT_ID);
        for (Map.Entry<String, Integer> map: actionMap.entrySet()) {
            Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN, map.getKey());
            Assert.assertEquals(1, map.getValue().intValue());
        }
    }

    private Action buildMockAction(boolean isOptional) {

        String id = String.valueOf(UUID.randomUUID());
        return new Action.ActionResponseBuilder()
                .id(id)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name("Pre Issue Access Token")
                .description(!isOptional ? "To configure pre issue access token" : null)
                .status(Action.Status.ACTIVE)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri("https://example.com")
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(AuthType.AuthenticationType.BASIC)
                                .properties(Arrays.asList(
                                        new AuthProperty.AuthPropertyBuilder()
                                                .name(AuthType.AuthenticationType
                                                        .AuthenticationProperty.USERNAME.getName())
                                                .value("admin")
                                                .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                                        .USERNAME.getIsConfidential())
                                                .build(),
                                        new AuthProperty.AuthPropertyBuilder()
                                                .name(AuthType.AuthenticationType
                                                        .AuthenticationProperty.PASSWORD.getName())
                                                .value("admin")
                                                .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                                        .PASSWORD.getIsConfidential())
                                                .build()))
                                .build())
                        .build())
                .build();
    }

    private void mockDBConnection() throws SQLException {

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
