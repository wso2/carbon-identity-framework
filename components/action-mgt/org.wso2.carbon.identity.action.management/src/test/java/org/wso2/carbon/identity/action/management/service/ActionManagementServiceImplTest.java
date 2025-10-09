/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.service;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.internal.service.impl.ActionManagementServiceImpl;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.management.api.util.AuditLogBuilderForRule;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_PATH;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_HEADER;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_HEADER_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_VALUE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_VALUE_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_INVALID_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_INVALID_API_KEY_HEADER;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_USERNAME;

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
    private Action sampleAction;
    private Rule sampleRule;
    private MockedStatic<LoggerUtils> loggerUtilsMockedStatic;
    private MockedStatic<AuditLogBuilderForRule> auditLogBuilderForRuleMockedStatic;

    @BeforeClass
    public void setUpClass() {

        actionManagementService = new ActionManagementServiceImpl();
        sampleRule = TestUtil.buildMockRule("ruleId", true);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(TestUtil.TEST_SECRET_TYPE_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);

        RuleManagementService ruleManagementService = mock(RuleManagementService.class);
        ActionMgtServiceComponentHolder.getInstance().setRuleManagementService(ruleManagementService);
        when(ruleManagementService.getRuleByRuleId(any(), any())).thenReturn(sampleRule);
        when(ruleManagementService.addRule(any(), any())).thenReturn(sampleRule);
        when(ruleManagementService.updateRule(any(), any())).thenReturn(sampleRule);

        loggerUtilsMockedStatic = mockStatic(LoggerUtils.class);
        loggerUtilsMockedStatic.when(() -> LoggerUtils.triggerAuditLogEvent(any())).thenAnswer(inv -> null);
        auditLogBuilderForRuleMockedStatic = mockStatic(AuditLogBuilderForRule.class);
        auditLogBuilderForRuleMockedStatic.when(() -> AuditLogBuilderForRule.buildRuleValue(any(Rule.class)))
                .thenReturn("");
    }

    @AfterMethod
    public void tearDown() {

        loggerUtilsMockedStatic.close();
        auditLogBuilderForRuleMockedStatic.close();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException, SecretManagementException {

        Action creatingAction = TestUtil.buildMockAction(
                TEST_ACTION_NAME,
                TEST_ACTION_DESCRIPTION,
                TEST_ACTION_URI,
                TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD));
        sampleAction = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);

        Assert.assertNotNull(sampleAction.getId());
        Assert.assertEquals(sampleAction.getName(), creatingAction.getName());
        Assert.assertEquals(sampleAction.getDescription(), creatingAction.getDescription());
        Assert.assertEquals(sampleAction.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(sampleAction.getCreatedAt());
        Assert.assertNotNull(sampleAction.getUpdatedAt());
        Assert.assertEquals(sampleAction.getCreatedAt().getTime(), sampleAction.getUpdatedAt().getTime());
        Assert.assertEquals(sampleAction.getType(), Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        Assert.assertEquals(sampleAction.getEndpoint().getUri(), creatingAction.getEndpoint().getUri());

        Authentication sampleActionAuth = sampleAction.getEndpoint().getAuthentication();
        Authentication creatingActionAuth = creatingAction.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(creatingActionAuth, sampleAction.getId());

        Assert.assertEquals(sampleActionAuth.getType(), creatingActionAuth.getType());
        Assert.assertEquals(sampleActionAuth.getProperties().size(), creatingActionAuth.getProperties().size());
        Assert.assertEquals(sampleActionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                secretProperties.get(Authentication.Property.USERNAME.getName()));
        Assert.assertEquals(sampleActionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                secretProperties.get(Authentication.Property.PASSWORD.getName()));
    }

    @Test(priority = 2, expectedExceptions = ActionMgtClientException.class,
            expectedExceptionsMessageRegExp = "Invalid request.")
    public void testAddActionWithInvalidData() throws ActionMgtException {

        Action creatingAction = TestUtil.buildMockAction(
                TEST_INVALID_ACTION_NAME,
                TEST_ACTION_DESCRIPTION,
                TEST_ACTION_URI,
                TestUtil.buildMockAPIKeyAuthentication(TEST_INVALID_API_KEY_HEADER, TEST_API_KEY_VALUE));
        Action action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);
        Assert.assertNull(action);
    }

    @Test(priority = 3, expectedExceptions = ActionMgtClientException.class,
            expectedExceptionsMessageRegExp = "Invalid request.")
    public void testAddActionWithEmptyData() throws ActionMgtException {

        Action creatingAction = TestUtil.buildMockAction(
                StringUtils.EMPTY,
                TEST_ACTION_DESCRIPTION,
                TEST_ACTION_URI,
                TestUtil.buildMockBasicAuthentication(null, TEST_PASSWORD));
        Action action = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);
        Assert.assertNull(action);
    }

    @Test(priority = 4, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to create an Action.")
    public void testAddMaximumActionsPerType() throws ActionMgtException {

        Action creatingAction = TestUtil.buildMockAction(
                TEST_ACTION_NAME,
                TEST_ACTION_DESCRIPTION,
                TEST_ACTION_URI,
                TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD));
        sampleAction = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction,
                TENANT_DOMAIN);
    }

    @Test(priority = 5)
    public void testGetActionsByActionType() throws ActionMgtException {

        List<Action> actions = actionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH,
                TENANT_DOMAIN);
        Assert.assertEquals(actions.size(), 1);
        Action result = actions.get(0);
        Assert.assertEquals(result.getId(), sampleAction.getId());
        Assert.assertEquals(result.getName(), sampleAction.getName());
        Assert.assertEquals(result.getDescription(), sampleAction.getDescription());
        Assert.assertEquals(result.getType().getActionType(), sampleAction.getType().getActionType());
        Assert.assertEquals(result.getStatus(), sampleAction.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertEquals(result.getUpdatedAt(), sampleAction.getUpdatedAt());
        Assert.assertEquals(result.getEndpoint().getUri(), sampleAction.getEndpoint().getUri());

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Authentication sampleActionAuth = sampleAction.getEndpoint().getAuthentication();

        Assert.assertEquals(resultActionAuth.getType(), sampleActionAuth.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.USERNAME).getValue());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.PASSWORD).getValue());
    }

    @Test(priority = 6)
    public void testGetActionByActionId() throws ActionMgtException {

        Action result = actionManagementService.getActionByActionId(sampleAction.getType().getPathParam(),
                sampleAction.getId(), TENANT_DOMAIN);
        Assert.assertEquals(result.getId(), sampleAction.getId());
        Assert.assertEquals(result.getName(), sampleAction.getName());
        Assert.assertEquals(result.getDescription(), sampleAction.getDescription());
        Assert.assertEquals(result.getType(), sampleAction.getType());
        Assert.assertEquals(result.getStatus(), sampleAction.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertEquals(result.getUpdatedAt(), sampleAction.getUpdatedAt());
        Assert.assertEquals(result.getEndpoint().getUri(), sampleAction.getEndpoint().getUri());

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Authentication sampleActionAuth = sampleAction.getEndpoint().getAuthentication();

        Assert.assertEquals(resultActionAuth.getType(), sampleActionAuth.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.USERNAME).getValue());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.PASSWORD).getValue());
    }

    @Test(priority = 6)
    public void testUpdateActionFailureWithNotAllowedActionVersion() throws Exception {

        Action updatingAction = TestUtil.buildMockActionWithVersion(
                TEST_ACTION_NAME_UPDATED,
                TEST_ACTION_DESCRIPTION_UPDATED,
                "v2",
                TEST_ACTION_URI,
                TestUtil.buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER, TEST_API_KEY_VALUE));
        Assert.assertThrows(ActionMgtException.class, () ->actionManagementService.updateAction(
                PRE_ISSUE_ACCESS_TOKEN_PATH, sampleAction.getId(), updatingAction, TENANT_DOMAIN));
    }

    @Test(priority = 7)
    public void testUpdateAction() throws ActionMgtException, SecretManagementException {

        Action updatingAction = TestUtil.buildMockAction(
                TEST_ACTION_NAME_UPDATED,
                TEST_ACTION_DESCRIPTION_UPDATED,
                TEST_ACTION_URI,
                TestUtil.buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER, TEST_API_KEY_VALUE));
        Action result = actionManagementService.updateAction(PRE_ISSUE_ACCESS_TOKEN_PATH, sampleAction.getId(),
                updatingAction, TENANT_DOMAIN);

        Assert.assertEquals(result.getId(), sampleAction.getId());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getType(), sampleAction.getType());
        Assert.assertEquals(result.getStatus(), sampleAction.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(sampleAction.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Authentication updatingActionAuth = updatingAction.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(updatingActionAuth, sampleAction.getId());

        Assert.assertEquals(resultActionAuth.getType(), updatingActionAuth.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.HEADER).getValue(),
                secretProperties.get(Authentication.Property.HEADER.getName()));
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.VALUE).getValue(),
                secretProperties.get(Authentication.Property.VALUE.getName()));
        sampleAction = result;
    }

    @Test(priority = 8)
    public void testActivateAction() throws ActionMgtException {

        Assert.assertEquals(sampleAction.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Action activatedAction = actionManagementService.activateAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                sampleAction.getId(), TENANT_DOMAIN);
        Assert.assertEquals(activatedAction.getStatus(), Action.Status.ACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(activatedAction.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(activatedAction.getUpdatedAt().after(sampleAction.getUpdatedAt()));
        sampleAction = activatedAction;
    }

    @Test(priority = 10)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = actionManagementService.getActionsCountPerType(TENANT_DOMAIN);
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PROFILE.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_REGISTRATION.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.AUTHENTICATION.getActionType()));
        for (Map.Entry<String, Integer> entry : actionMap.entrySet()) {
            Assert.assertEquals(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType(), entry.getKey());
            Assert.assertEquals(entry.getValue().intValue(), 1);
        }
    }

    @Test(priority = 11)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(sampleAction.getStatus(), Action.Status.ACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Action deactivatedAction = actionManagementService.deactivateAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                sampleAction.getId(), TENANT_DOMAIN);
        Assert.assertEquals(deactivatedAction.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(deactivatedAction.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(deactivatedAction.getUpdatedAt().after(sampleAction.getUpdatedAt()));
    }

    @Test(priority = 11)
    public void testUpdateEndpointConfigWithSameAuthenticationType() throws ActionMgtException,
            SecretManagementException {

        Authentication updatingAuthentication = TestUtil.buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER_UPDATED,
                TEST_API_KEY_VALUE_UPDATED);
        Action result = actionManagementService.updateActionEndpointAuthentication(PRE_ISSUE_ACCESS_TOKEN_PATH,
                sampleAction.getId(), updatingAuthentication, TENANT_DOMAIN);

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(updatingAuthentication, sampleAction.getId());

        Assert.assertEquals(resultActionAuth.getType(), updatingAuthentication.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.HEADER).getValue(),
                secretProperties.get(Authentication.Property.HEADER.getName()));
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.VALUE).getValue(),
                secretProperties.get(Authentication.Property.VALUE.getName()));
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(sampleAction.getUpdatedAt()));
    }

    @Test(priority = 12)
    public void testUpdateEndpointConfigWithDifferentAuthenticationType()
            throws ActionMgtException, SecretManagementException {

        Authentication updatingAuthentication = TestUtil.buildMockBearerAuthentication(TEST_ACCESS_TOKEN);
        Action result = actionManagementService.updateActionEndpointAuthentication(PRE_ISSUE_ACCESS_TOKEN_PATH,
                sampleAction.getId(), updatingAuthentication, TENANT_DOMAIN);

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(updatingAuthentication, sampleAction.getId());

        Assert.assertEquals(resultActionAuth.getType(), updatingAuthentication.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.ACCESS_TOKEN).getValue(),
                secretProperties.get(Authentication.Property.ACCESS_TOKEN.getName()));
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(sampleAction.getUpdatedAt()));
    }

    @Test(priority = 13)
    public void testDeleteAction() throws ActionMgtException {

        actionManagementService.deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH, sampleAction.getId(), TENANT_DOMAIN);
        Assert.assertNull(actionManagementService.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_PATH, sampleAction.getId(),
                TENANT_DOMAIN));
        Map<String, Integer> actions = actionManagementService.getActionsCountPerType(TENANT_DOMAIN);
        Assert.assertNull(actions.get(PRE_ISSUE_ACCESS_TOKEN_PATH));
    }

    @Test(priority = 14)
    public void testDeleteNonExistingAction() {

        try {
            actionManagementService.deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH, "invalid_id", TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test(priority = 15)
    public void testAddActionWithRule() throws ActionMgtException, SecretManagementException {

        Action creatingAction = TestUtil.buildMockActionWithRule(
                TEST_ACTION_NAME,
                TEST_ACTION_DESCRIPTION,
                TEST_ACTION_URI,
                TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD),
                sampleRule);
        sampleAction = actionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH, creatingAction, TENANT_DOMAIN);

        Assert.assertNotNull(sampleAction.getId());
        Assert.assertEquals(sampleAction.getName(), creatingAction.getName());
        Assert.assertEquals(sampleAction.getDescription(), creatingAction.getDescription());
        Assert.assertEquals(sampleAction.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(sampleAction.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(sampleAction.getCreatedAt());
        Assert.assertNotNull(sampleAction.getUpdatedAt());
        Assert.assertEquals(sampleAction.getType(), Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        Assert.assertEquals(sampleAction.getEndpoint().getUri(), creatingAction.getEndpoint().getUri());

        Authentication sampleActionAuth = sampleAction.getEndpoint().getAuthentication();
        Authentication creatingActionAuth = creatingAction.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(creatingActionAuth, sampleAction.getId());

        Assert.assertEquals(sampleActionAuth.getType(), creatingActionAuth.getType());
        Assert.assertEquals(sampleActionAuth.getProperties().size(), creatingActionAuth.getProperties().size());
        Assert.assertEquals(sampleActionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                secretProperties.get(Authentication.Property.USERNAME.getName()));
        Assert.assertEquals(sampleActionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                secretProperties.get(Authentication.Property.PASSWORD.getName()));

        Assert.assertNotNull(sampleAction.getActionRule());
        Assert.assertEquals(sampleAction.getActionRule().getId(), creatingAction.getActionRule().getId());
        Assert.assertEquals(sampleAction.getActionRule().getRule(), creatingAction.getActionRule().getRule());
    }

    @Test(priority = 16)
    public void testGetActionsWithRulesByActionType() throws ActionMgtException {

        List<Action> actions = actionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH,
                TENANT_DOMAIN);
        Assert.assertEquals(actions.size(), 1);
        Action result = actions.get(0);
        Assert.assertEquals(result.getId(), sampleAction.getId());
        Assert.assertEquals(result.getName(), sampleAction.getName());
        Assert.assertEquals(result.getDescription(), sampleAction.getDescription());
        Assert.assertEquals(result.getType().getActionType(), sampleAction.getType().getActionType());
        Assert.assertEquals(result.getStatus(), sampleAction.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertEquals(result.getUpdatedAt(), sampleAction.getUpdatedAt());
        Assert.assertEquals(result.getEndpoint().getUri(), sampleAction.getEndpoint().getUri());

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Authentication sampleActionAuth = sampleAction.getEndpoint().getAuthentication();

        Assert.assertEquals(resultActionAuth.getType(), sampleActionAuth.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.USERNAME).getValue());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                sampleActionAuth.getProperty(Authentication.Property.PASSWORD).getValue());

        Assert.assertNotNull(result.getActionRule());
        Assert.assertEquals(result.getActionRule().getId(), sampleAction.getActionRule().getId());
        Assert.assertEquals(result.getActionRule().getRule(), sampleAction.getActionRule().getRule());
    }

    @Test(priority = 17)
    public void testUpdateActionUpdatingRule() throws ActionMgtException, SecretManagementException {

        Action updatingAction = TestUtil.buildMockActionWithRule(
                TEST_ACTION_NAME_UPDATED,
                TEST_ACTION_DESCRIPTION_UPDATED,
                TEST_ACTION_URI,
                TestUtil.buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER, TEST_API_KEY_VALUE), sampleRule);
        Action result = actionManagementService.updateAction(PRE_ISSUE_ACCESS_TOKEN_PATH, sampleAction.getId(),
                updatingAction, TENANT_DOMAIN);

        Assert.assertEquals(result.getId(), sampleAction.getId());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getType(), sampleAction.getType());
        Assert.assertEquals(result.getStatus(), sampleAction.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertEquals(result.getCreatedAt(), sampleAction.getCreatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(sampleAction.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());

        Authentication resultActionAuth = result.getEndpoint().getAuthentication();
        Authentication updatingActionAuth = updatingAction.getEndpoint().getAuthentication();
        Map<String, String> secretProperties = resolveAuthPropertiesMap(updatingActionAuth, sampleAction.getId());

        Assert.assertEquals(resultActionAuth.getType(), updatingActionAuth.getType());
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.HEADER).getValue(),
                secretProperties.get(Authentication.Property.HEADER.getName()));
        Assert.assertEquals(resultActionAuth.getProperty(Authentication.Property.VALUE).getValue(),
                secretProperties.get(Authentication.Property.VALUE.getName()));

        Assert.assertNotNull(result.getActionRule());
        Assert.assertEquals(result.getActionRule().getId(), sampleAction.getActionRule().getId());
        Assert.assertEquals(result.getActionRule().getRule(), sampleAction.getActionRule().getRule());
    }

    private Map<String, String> resolveAuthPropertiesMap(Authentication authentication, String actionId)
            throws SecretManagementException {

        return authentication.getPropertiesWithSecretReferences(actionId)
                .stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
    }
}
