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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_PATH;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_DOMAIN;

/**
 * This class is a test suite for the ActionManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ActionManagementServiceImpl class.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ActionManagementServiceImplTest {

    private ActionManagementService actionManagementService;

    private Action action;
    private Map<String, String> secretProperties;

    @BeforeClass
    public void setUpClass() {

        actionManagementService = ActionManagementServiceImpl.getInstance();
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

        Action creatingAction = TestUtil.buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"));
        action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);
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
        Action creatingAction = TestUtil.buildMockAction(
                "PreIssueAccessToken_#1",
                "To configure PreIssueAccessToken",
                "https://example.com",
                TestUtil.buildMockAPIKeyAuthentication("-test-header", "thisisapikey"));
        Action action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);
        Assert.assertNull(action);
    }

    @Test(priority = 3, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddActionWithEmptyData() throws ActionMgtException {
        Action creatingAction = TestUtil.buildMockAction(
                "",
                "To configure PreIssueAccessToken",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication(null, "admin"));
        Action action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);
        Assert.assertNull(action);
    }

    @Test(priority = 4, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddMaximumActionsPerType() throws ActionMgtException {

        Action creatingAction = TestUtil.buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"));
        action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction,
                TENANT_DOMAIN);
    }

    @Test(priority = 5)
    public void testGetActionsByActionType() throws ActionMgtException, SecretManagementException {

        List<Action> actions = actionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH,
                TENANT_DOMAIN);
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

        Action result = actionManagementService.getActionByActionId(action.getType().getPathParam(), action.getId(), 
                TENANT_DOMAIN);
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
        List<Action> actions = actionManagementService.getActionsByActionType(
                PRE_ISSUE_ACCESS_TOKEN_PATH, TENANT_DOMAIN);
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

        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To update configuration pre issue access token",
                "https://sample.com",
                TestUtil.buildMockAPIKeyAuthentication("header", "value"));
        Action result = actionManagementService.updateAction(PRE_ISSUE_ACCESS_TOKEN_PATH, action.getId(),
                updatingAction, TENANT_DOMAIN);
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
        Action deactivatedAction = actionManagementService.deactivateAction(
                PRE_ISSUE_ACCESS_TOKEN_PATH, action.getId(), TENANT_DOMAIN);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 10)
    public void testActivateAction() throws ActionMgtException {

        Action result = actionManagementService.activateAction(
                PRE_ISSUE_ACCESS_TOKEN_PATH, action.getId(), TENANT_DOMAIN);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 11)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = actionManagementService.getActionsCountPerType(TENANT_DOMAIN);
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
    public void testDeleteAction() throws ActionMgtException {

        actionManagementService.deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH, action.getId(), TENANT_DOMAIN);
        Assert.assertNull(actionManagementService.getActionByActionId(action.getType().getPathParam(), action.getId(),
                TENANT_DOMAIN));
        Map<String, Integer> actions = actionManagementService.getActionsCountPerType(TENANT_DOMAIN);
        Assert.assertNull(actions.get(PRE_ISSUE_ACCESS_TOKEN_PATH));
    }

    private Map<String, String> mapActionAuthPropertiesWithSecrets(Action action) throws SecretManagementException {

        return action.getEndpoint().getAuthentication()
                .getPropertiesWithSecretReferences(action.getId())
                .stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
    }
}
