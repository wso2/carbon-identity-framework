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

package org.wso2.carbon.identity.action.management;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * This class is a test suite for the ActionManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ActionManagementServiceImpl class.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRegistry
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ActionManagementServiceImplTest {

    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private static final String DB_NAME = "action_mgt";
    private static final String ACCESS_TOKEN = "6e47f1f7-bd29-41e9-b5dc-e9dd70ac22b7";
    private Action action;
    private String tenantDomain;
    private ActionManagementService serviceImpl;

    @BeforeClass
    public void setUpClass() throws Exception {

        serviceImpl = ActionManagementServiceImpl.getInstance();
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        mockDBConnection();
    }

    @AfterMethod
    public void tearDown() {

        identityDatabaseUtil.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException {

        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                AuthType.AuthenticationType.BASIC,
                buildMockBasicAuthProperties("admin", "admin"));
        action = serviceImpl.addAction(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), creatingAction,
                tenantDomain);
        Assert.assertNotNull(action.getId());
        Assert.assertEquals(creatingAction.getName(), action.getName());
        Assert.assertEquals(creatingAction.getDescription(), action.getDescription());
        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Assert.assertEquals(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType(),
                action.getType().getActionType());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), action.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getProperties().size(),
                action.getEndpoint().getAuthentication().getProperties().size());
    }

    @Test(priority = 2, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddMaximumActionsPerType() throws ActionMgtException {

        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                AuthType.AuthenticationType.BASIC,
                buildMockBasicAuthProperties("admin", "admin"));
        action = serviceImpl.addAction(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), creatingAction,
                tenantDomain);
    }

    @Test(priority = 3)
    public void testGetActionsByActionType() throws ActionMgtException {

        List<Action> actions = serviceImpl.getActionsByActionType(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), tenantDomain);
        Assert.assertEquals(1, actions.size());
    }

    @Test(priority = 4)
    public void testGetActionsByActionTypeFromCache() throws ActionMgtException {

        List<Action> actions = serviceImpl.getActionsByActionType(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), tenantDomain);
        Assert.assertEquals(1, actions.size());
    }

    @Test(priority = 5)
    public void testUpdateAction() throws ActionMgtException {

        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To update configuration pre issue access token",
                "https://sample.com",
                AuthType.AuthenticationType.BASIC,
                buildMockBasicAuthProperties("updatingadmin", "updatingadmin"));
        Action result = serviceImpl.updateAction(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(),
                action.getId(), updatingAction, tenantDomain);
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
        action = result;
    }

    @Test(priority = 6)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Action deactivatedAction = serviceImpl.deactivateAction(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), action.getId(), tenantDomain);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 7)
    public void testActivateAction() throws ActionMgtException {

        Action result = serviceImpl.activateAction(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), action.getId(), tenantDomain);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 8)
    public void testGetActionByActionId() throws ActionMgtException {

        Action result = serviceImpl.getActionByActionId(action.getId(), tenantDomain);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 9)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = serviceImpl.getActionsCountPerType(tenantDomain);
        for (Map.Entry<String, Integer> entry: actionMap.entrySet()) {
            Assert.assertEquals(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType(), entry.getKey());
            Assert.assertEquals(1, entry.getValue().intValue());
        }
    }

    @Test(priority = 10)
    public void testUpdateEndpointConfigWithSameAuth() throws ActionMgtException {

        AuthType authType = buildMockAuthType(AuthType.AuthenticationType.BASIC,
                buildMockBasicAuthProperties("newadmin", "newadmin"));
        Action result = serviceImpl.updateActionEndpointAuthentication(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), action.getId(), authType, tenantDomain);
        Assert.assertEquals(AuthType.AuthenticationType.BASIC, result.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getProperties().get(0).getValue(),
                result.getEndpoint().getAuthentication().getProperties().get(0).getValue());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getProperties().get(1).getValue(),
                result.getEndpoint().getAuthentication().getProperties().get(1).getValue());
    }

    @Test(priority = 11)
    public void testUpdateEndpointConfigWithDifferentAuth() throws ActionMgtException {

        AuthType authType = buildMockAuthType(AuthType.AuthenticationType.BEARER,
                buildMockBearerAuthProperties(ACCESS_TOKEN));
        Action result = serviceImpl.updateActionEndpointAuthentication(
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(), action.getId(), authType, tenantDomain);
        Assert.assertEquals(AuthType.AuthenticationType.BEARER, result.getEndpoint().getAuthentication().getType());
        Assert.assertNotEquals(authType.getProperties().get(0).getValue(),
                result.getEndpoint().getAuthentication().getProperties().get(0).getValue());
    }

    @Test(priority = 12)
    public void testDeleteAction() throws ActionMgtException {

        serviceImpl.deleteAction(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam(),
                action.getId(), tenantDomain);
        Assert.assertNull(serviceImpl.getActionByActionId(action.getId(), tenantDomain));
    }

    private AuthProperty buildMockAuthProperty(
            AuthType.AuthenticationType.AuthenticationProperty authenticationProperty, String value) {

        return new AuthProperty.AuthPropertyBuilder()
                .name(authenticationProperty.getName())
                .value(value)
                .isConfidential(authenticationProperty.getIsConfidential())
                .build();
    }

    private List<AuthProperty> buildMockBasicAuthProperties(String username, String password) {

        return Arrays.asList(
                buildMockAuthProperty(AuthType.AuthenticationType.AuthenticationProperty.USERNAME, username),
                buildMockAuthProperty(AuthType.AuthenticationType.AuthenticationProperty.PASSWORD, password));
    }

    private List<AuthProperty> buildMockBearerAuthProperties(String accessToken) {

        return Arrays.asList(
                buildMockAuthProperty(AuthType.AuthenticationType.AuthenticationProperty.ACCESS_TOKEN, accessToken));
    }

    private EndpointConfig buildMockEndpointConfig(String uri, AuthType.AuthenticationType authenticationType,
                                                   List<AuthProperty> authProperties) {

        if (uri == null && authProperties == null) {
            return null;
        }
        return new EndpointConfig.EndpointConfigBuilder()
                .uri(uri)
                .authentication(buildMockAuthType(authenticationType, authProperties))
                .build();
    }

    private AuthType buildMockAuthType(AuthType.AuthenticationType authenticationType,
                                       List<AuthProperty> authProperties) {

        if (authenticationType == null || authProperties == null) {
            return null;
        }
        return new AuthType.AuthTypeBuilder()
                .type(authenticationType)
                .properties(authProperties)
                .build();
    }

    private Action buildMockAction(String name,
                                   String description,
                                   String uri,
                                   AuthType.AuthenticationType authType,
                                   List<AuthProperty> authProperties) {

        return new Action.ActionRequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authType, authProperties))
                .build();
    }

    private void mockDBConnection() {

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
    }

    private Connection getConnection() throws Exception {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("Invalid datasource.");
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
