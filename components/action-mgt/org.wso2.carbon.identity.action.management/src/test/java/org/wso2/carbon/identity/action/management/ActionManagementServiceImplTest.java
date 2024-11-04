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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class is a test suite for the ActionManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ActionManagementServiceImpl class.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ActionManagementServiceImplTest {

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private Action action;
    private String tenantDomain;
    private ActionManagementService serviceImpl;
    private Map<String, String> secretProperties;
    private static final String ACCESS_TOKEN = "6e47f1f7-bd29-41e9-b5dc-e9dd70ac22b7";
    private static final String PRE_ISSUE_ACCESS_TOKEN = Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam();

    @BeforeClass
    public void setUpClass() {

        serviceImpl = ActionManagementServiceImpl.getInstance();
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        when(secretManager.getSecretType(any())).thenReturn(secretType);
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException, SecretManagementException {

        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                buildMockBasicAuthentication("admin", "admin"));
        action = serviceImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction,
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
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getProperties().size(),
                action.getEndpoint().getAuthentication().getPropertiesWithSecretReferences(action.getId()).size());
        secretProperties = mapActionAuthPropertiesWithSecrets(action);
        Assert.assertEquals(
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue(),
                secretProperties.get(Authentication.Property.USERNAME.getName()));
        Assert.assertEquals(
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue(),
                secretProperties.get(Authentication.Property.PASSWORD.getName()));
    }

    @Test(priority = 2, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddActionWithInvalidData() throws ActionMgtException {
        Action creatingAction = buildMockAction(
                "PreIssueAccessToken_#1",
                "To configure PreIssueAccessToken",
                "https://example.com",
                buildMockAPIKeyAuthentication("-test-header", "thisisapikey"));
        Action action = serviceImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction, tenantDomain);
        Assert.assertNull(action);
    }

    @Test(priority = 3, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddActionWithEmptyData() throws ActionMgtException {
        Action creatingAction = buildMockAction(
                "",
                "To configure PreIssueAccessToken",
                "https://example.com",
                buildMockBasicAuthentication(null, "admin"));
        Action action = serviceImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction, tenantDomain);
        Assert.assertNull(action);
    }

    @Test(priority = 4, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddMaximumActionsPerType() throws ActionMgtException {

        Action creatingAction = buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                buildMockBasicAuthentication("admin", "admin"));
        action = serviceImpl.addAction(PRE_ISSUE_ACCESS_TOKEN, creatingAction,
                tenantDomain);
    }

    @Test(priority = 5)
    public void testGetActionsByActionType() throws ActionMgtException, SecretManagementException {

        List<Action> actions = serviceImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN, tenantDomain);
        Assert.assertEquals(1, actions.size());
        for (Action result: actions) {
            Assert.assertEquals(action.getId(), result.getId());
            Assert.assertEquals(action.getName(), result.getName());
            Assert.assertEquals(action.getDescription(), result.getDescription());
            Assert.assertEquals(action.getType().getActionType(), result.getType().getActionType());
            Assert.assertEquals(action.getStatus(), result.getStatus());
            Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
            Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                    result.getEndpoint().getAuthentication().getType());
            secretProperties = mapActionAuthPropertiesWithSecrets(result);
            Assert.assertEquals(
                    result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue(),
                    secretProperties.get(Authentication.Property.USERNAME.getName()));
            Assert.assertEquals(
                    result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue(),
                    secretProperties.get(Authentication.Property.PASSWORD.getName()));
        }
    }

    @Test(priority = 6)
    public void testGetActionByActionId() throws ActionMgtException, SecretManagementException {

        Action result = serviceImpl.getActionByActionId(action.getType().getPathParam(), action.getId(), tenantDomain);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        secretProperties = mapActionAuthPropertiesWithSecrets(result);
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue(),
                secretProperties.get(Authentication.Property.USERNAME.getName()));
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue(),
                secretProperties.get(Authentication.Property.PASSWORD.getName()));
    }

    @Test(priority = 7)
    public void testGetActionsByActionTypeFromCache() throws ActionMgtException, SecretManagementException {

        // Verify that the action is retrieved from the cache based on action type.
        List<Action> actions = serviceImpl.getActionsByActionType(
                PRE_ISSUE_ACCESS_TOKEN, tenantDomain);
        Assert.assertEquals(1, actions.size());
        Action result = actions.get(0);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(action.getName(), result.getName());
        Assert.assertEquals(action.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(action.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(action.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        secretProperties = mapActionAuthPropertiesWithSecrets(result);
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME).getValue(),
                secretProperties.get(Authentication.Property.USERNAME.getName()));
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD).getValue(),
                secretProperties.get(Authentication.Property.PASSWORD.getName()));
    }

    @Test(priority = 8)
    public void testUpdateAction() throws ActionMgtException, SecretManagementException {

        Action updatingAction = buildMockAction(
                "Pre Issue Access Token",
                "To update configuration pre issue access token",
                "https://sample.com",
                buildMockAPIKeyAuthentication("header", "value"));
        Action result = serviceImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), updatingAction, tenantDomain);
        Assert.assertEquals(action.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(action.getType(), result.getType());
        Assert.assertEquals(action.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(
                updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(
                updatingAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.HEADER).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.HEADER).getValue());
        secretProperties = mapActionAuthPropertiesWithSecrets(result);
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.VALUE).getValue(),
                secretProperties.get(Authentication.Property.VALUE.getName()));
        action = result;
    }

    @Test(priority = 9)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(Action.Status.ACTIVE, action.getStatus());
        Action deactivatedAction = serviceImpl.deactivateAction(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), tenantDomain);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 10)
    public void testActivateAction() throws ActionMgtException {

        Action result = serviceImpl.activateAction(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), tenantDomain);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 11)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = serviceImpl.getActionsCountPerType(tenantDomain);
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PROFILE.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_REGISTRATION.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.AUTHENTICATION.getActionType()));
        for (Map.Entry<String, Integer> entry: actionMap.entrySet()) {
            Assert.assertEquals(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType(), entry.getKey());
            Assert.assertEquals(1, entry.getValue().intValue());
        }
    }

    @Test(priority = 12)
    public void testUpdateEndpointConfigWithSameAuthenticationType()
            throws ActionMgtException, SecretManagementException {

        Authentication authentication = buildMockAPIKeyAuthentication("newheader", "newvalue");
        Action result = serviceImpl.updateActionEndpointAuthentication(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), authentication, tenantDomain);
        Assert.assertEquals(Authentication.Type.API_KEY, result.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(authentication.getProperty(Authentication.Property.HEADER).getValue(),
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.HEADER).getValue());
        secretProperties = mapActionAuthPropertiesWithSecrets(result);
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.VALUE).getValue(),
                secretProperties.get(Authentication.Property.VALUE.getName()));
    }

    @Test(priority = 13)
    public void testUpdateEndpointConfigWithDifferentAuthenticationType()
            throws ActionMgtException, SecretManagementException {

        Authentication authentication = buildMockBearerAuthentication(ACCESS_TOKEN);
        Action result = serviceImpl.updateActionEndpointAuthentication(
                PRE_ISSUE_ACCESS_TOKEN, action.getId(), authentication, tenantDomain);
        Assert.assertEquals(Authentication.Type.BEARER, result.getEndpoint().getAuthentication().getType());
        secretProperties = mapActionAuthPropertiesWithSecrets(result);
        Assert.assertEquals(
                result.getEndpoint().getAuthentication().getProperty(Authentication.Property.ACCESS_TOKEN).getValue(),
                secretProperties.get(Authentication.Property.ACCESS_TOKEN.getName()));
    }

    @Test(priority = 14)
    public void testDeleteAction() throws ActionMgtException {

        serviceImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN, action.getId(), tenantDomain);
        Assert.assertNull(serviceImpl.getActionByActionId(action.getType().getPathParam(), action.getId(),
                tenantDomain));
        Map<String, Integer> actions = serviceImpl.getActionsCountPerType(tenantDomain);
        Assert.assertNull(actions.get(PRE_ISSUE_ACCESS_TOKEN));
    }

    private Map<String, String> mapActionAuthPropertiesWithSecrets(Action action) throws SecretManagementException {

        return action.getEndpoint().getAuthentication()
                .getPropertiesWithSecretReferences(action.getId())
                .stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
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
}
