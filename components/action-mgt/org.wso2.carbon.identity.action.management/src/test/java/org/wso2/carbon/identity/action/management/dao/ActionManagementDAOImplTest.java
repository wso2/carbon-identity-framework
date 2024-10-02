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
import org.wso2.carbon.identity.action.management.model.Authentication;
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
    private static final String DB_NAME = "action_mgt_dao";
    private static final String PRE_ISSUE_ACCESS_TOKEN = "PRE_ISSUE_ACCESS_TOKEN";
    private static final int TENANT_ID = 2;
    private Action action;

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

        String id = String.valueOf(UUID.randomUUID());
        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                 buildMockBasicAuthentication("admin", "admin"));
        action = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, id, creatingAction, TENANT_ID);
        Assert.assertEquals(id, action.getId());
        Assert.assertEquals(creatingAction.getName(), action.getName());
        Assert.assertEquals(creatingAction.getDescription(), action.getDescription());
        Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN, action.getType().getActionType());
        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), action.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 2, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Action.")
    public void testAddActionWithoutName() throws ActionMgtException {

        Action action = buildMockAction(
                null,
                "To configure PreIssueAccessToken",
                "https://example.com",
                 buildMockBasicAuthentication("admin", "admin"));
        this.action = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
    }

    @Test(priority = 3)
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

    @Test(priority = 4)
    public void testGetActionByActionId() throws ActionMgtException {

        Action result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 5)
    public void testDeleteAction() throws ActionMgtException, SQLException {

        daoImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), action, TENANT_ID);
        mockDBConnection();
        Assert.assertNull(daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID));
    }

    @Test(priority = 6)
    public void testAddActionWithoutDescription() throws ActionMgtException {

        String id = String.valueOf(UUID.randomUUID());
        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                null,
                "https://example.com",
                 buildMockBasicAuthentication("admin", "admin"));
        action = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, id, creatingAction, TENANT_ID);
        Assert.assertEquals(id, action.getId());
        Assert.assertEquals(creatingAction.getName(), action.getName());
        Assert.assertNull(null, action.getDescription());
        Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN, action.getType().getActionType());
        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), action.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 7)
    public void testUpdateAction() throws ActionMgtException {

        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                "https://sample.com",
                 buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
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
        action = result;
    }

    @Test(priority = 8)
    public void testUpdateActionWithoutNameAndDescription() throws ActionMgtException {

        // TODO: 'Name' is a required attribute. Thus, DAO layer should throw an exception if name is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = buildMockAction(
                null,
                null,
                "https://sample.com",
                 buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 9)
    public void testUpdateActionWithNameAndDescription() throws ActionMgtException {

        // TODO: 'Uri','AuthenticationType','AuthProperties' are required attributes. Thus, DAO layer should throw an
        //  exception if those attributes are null. This should be fixed in DAO layer and test case needs to be updated
        //  accordingly.
        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                null);
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 10)
    public void testUpdateActionEndpointAuthSecretProperties() throws ActionMgtException {

        Authentication authentication = buildMockBasicAuthentication("newadmin", "newadmin");
        Action result = daoImpl.updateActionEndpointAuthProperties(PRE_ISSUE_ACCESS_TOKEN, action.getId(),
                authentication, TENANT_ID);
        Assert.assertEquals(Authentication.Type.BASIC, result.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue());
        Assert.assertEquals(
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue());
    }

    @Test(priority = 11)
    public void testUpdateActionWithoutEndpointUri() throws ActionMgtException {

        // TODO: 'Uri' is a required attribute. Thus, DAO layer should throw an exception if Uri is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                 buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 12)
    public void testUpdateActionWithAuthType() throws ActionMgtException {

        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                "https://sample.com",
                buildMockBearerAuthentication("57c7df90-cacc-4f56-9b0a-f14bfbff3076"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        action = result;
    }

    @Test(priority = 13)
    public void testUpdateActionWithUri() throws ActionMgtException {

        // TODO: 'Name','AuthenticationType' and 'AuthProperties' are required attributes. Thus, DAO layer should throw
        //  an exception if those attributes are null. This should be fixed in DAO layer and test case needs to be
        //  updated accordingly.
        Action updatingAction = buildMockAction(
                null,
                null,
                "https://sample.com",
                null);
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        action = result;
    }

    @Test(priority = 14)
    public void testUpdateActionWithAuthTypeWithoutUri() throws ActionMgtException {

        // TODO: 'Uri' is a required attribute. Thus, DAO layer should throw an exception if uri is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, action, TENANT_ID);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 15)
    public void testUpdateActionEndpointAuthNonSecretProperties() throws ActionMgtException, SQLException {

        Action sampleAction = buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                "https://sample.com",
                 buildMockAPIKeyAuthentication("header", "value"));
        Action updatingAction = daoImpl.updateAction(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), sampleAction, action, TENANT_ID);
        mockDBConnection();
        Authentication authentication = buildMockAPIKeyAuthentication("updatingheader", "updatingvalue");
        Action result = daoImpl.updateActionEndpointAuthProperties(PRE_ISSUE_ACCESS_TOKEN, updatingAction.getId(),
                authentication, TENANT_ID);
        Assert.assertEquals(Authentication.Type.API_KEY, result.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(authentication.getProperty(Authentication.Property.HEADER).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.HEADER).getValue());
        Assert.assertEquals(
                updatingAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.VALUE).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.VALUE).getValue());
    }

    @Test(priority = 16)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Action deactivatedAction = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 17)
    public void testActivateAction() throws ActionMgtException {

        Action result = daoImpl.activateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 18)
    public void testUpdateActionEndpoint() throws ActionMgtException {

        EndpointConfig endpointConfig = buildMockEndpointConfig("https://template.com",
                buildMockBearerAuthentication("c7fce95f-3f5b-4cda-8bb1-4cb7b3990f83"));
        Action result = daoImpl.updateActionEndpoint(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), endpointConfig, action.getEndpoint()
                .getAuthentication(), TENANT_ID);
        Assert.assertNotEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(Authentication.Type.BEARER.getName(),
                result.getEndpoint().getAuthentication().getType().getName());
    }

    @Test(priority = 19)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = daoImpl.getActionsCountPerType(TENANT_ID);
        for (Map.Entry<String, Integer> entry: actionMap.entrySet()) {
            Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN, entry.getKey());
            Assert.assertEquals(1, entry.getValue().intValue());
        }
    }

    private Authentication buildMockBasicAuthentication(String username, String password) {

        return new Authentication.BasicAuthBuilder(username, password).build();
    }

    private Authentication buildMockBearerAuthentication(String accessToken) {

        return new Authentication.BearerAuthBuilder(accessToken).build();
    }

    private Authentication buildMockAPIKeyAuthentication(String header, String value) {

        return new Authentication.APIKeyAuthBuilder(header, value).build();
    }

    private EndpointConfig buildMockEndpointConfig(String uri, Authentication authentication) {

        if (uri == null && authentication == null) {
            return null;
        }

        return new EndpointConfig.EndpointConfigBuilder()
                .uri(uri)
                .authentication(authentication)
                .build();
    }

    private Action buildMockAction(String name,
                                   String description,
                                   String uri,
                                   Authentication authentication) {

        return new Action.ActionRequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authentication))
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
