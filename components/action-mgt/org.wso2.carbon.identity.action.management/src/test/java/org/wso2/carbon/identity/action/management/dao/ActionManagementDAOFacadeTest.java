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

package org.wso2.carbon.identity.action.management.dao;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionDTOModelResolverFactory;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOFacade;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PASSWORD_SHARING_TYPE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CERTIFICATE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD_SHARING_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD_SHARING_TYPE_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_USERNAME;

/**
 * This class is a test suite for the ActionManagementDAOFacade class.
 * It contains unit tests to verify the functionality of the methods in the ActionManagementDAOFacade class which is
 * responsible for handling external services.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class ActionManagementDAOFacadeTest {

    @Mock
    private ActionDTOModelResolver mockedActionDTOModelResolver;
    @Mock
    private RuleManagementService ruleManagementService;
    private TestActionDTOModelResolver testActionPropertyResolver;
    private MockedStatic<ActionDTOModelResolverFactory> actionPropertyResolverFactory;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    private ActionManagementDAOFacade daoFacade;
    private ActionDTO actionDTOToAddOrUpdate;
    private ActionDTO actionDTORetrieved;

    @BeforeClass
    public void setUpClass() {

        daoFacade = new ActionManagementDAOFacade(new ActionManagementDAOImpl());
        actionDTOToAddOrUpdate = createActionDTOForPasswordUpdateAction();
        testActionPropertyResolver = new TestActionDTOModelResolver();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(TestUtil.TEST_SECRET_TYPE_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

        MockitoAnnotations.openMocks(this);
        actionPropertyResolverFactory = mockStatic(ActionDTOModelResolverFactory.class);
        ActionMgtServiceComponentHolder.getInstance().setRuleManagementService(ruleManagementService);
    }

    @AfterMethod
    public void tearDown() {

        mockedActionDTOModelResolver = null;
        identityTenantUtil.close();
        actionPropertyResolverFactory.close();
    }

    @Test(priority = 1)
    public void testAddActionWithActionPropertyResolverClientException() throws ActionDTOModelResolverException {

        mockActionPropertyResolver(mockedActionDTOModelResolver);
        doThrow(new ActionDTOModelResolverClientException("Invalid Certificate.", "Invalid PEM format."))
                .when(mockedActionDTOModelResolver).resolveForAddOperation(any(), any());

        try {
            daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);
            Assert.fail("Successful addition of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getClass(), ActionMgtClientException.class);
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_ACTION_PROPERTIES.getCode());
            Assert.assertEquals(e.getMessage(), "Invalid Certificate.");
            Assert.assertEquals(e.getDescription(), "Invalid PEM format.");
        }
    }

    @Test(priority = 2)
    public void testAddActionWithActionPropertyResolverServerException() throws ActionDTOModelResolverException {

        mockActionPropertyResolver(mockedActionDTOModelResolver);
        doThrow(new ActionDTOModelResolverServerException("Error adding Certificate.", null, new Throwable()))
                .when(mockedActionDTOModelResolver).resolveForAddOperation(any(), any());

        try {
            daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);
            Assert.fail("Successful addition of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getClass(), ActionMgtServerException.class);
            Assert.assertEquals(e.getMessage(), ErrorMessage.ERROR_WHILE_ADDING_ACTION.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof ActionDTOModelResolverServerException) {
                    return;
                }
            }
            Assert.fail("Expected ActionPropertyResolverServerException was not found in the exception chain");
        }
    }

    @Test(priority = 3)
    public void testAddAction() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);
        try {
            daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, actionDTOToAddOrUpdate);
        Assert.assertEquals(actionDTORetrieved.getCreatedAt().getTime(), actionDTORetrieved.getUpdatedAt().getTime());
    }

    @Test(priority = 4)
    public void testGetActionsByType() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);
        List<ActionDTO> actionDTOs = daoFacade.getActionsByActionType(PRE_UPDATE_PASSWORD_TYPE, TENANT_ID);
        ActionDTO result = actionDTOs.get(0);

        verifyActionDTO(result, actionDTOToAddOrUpdate);
        Assert.assertEquals(actionDTORetrieved.getCreatedAt().getTime(), actionDTORetrieved.getUpdatedAt().getTime());
    }

    @Test(priority = 5)
    public void testUpdateActionPropertyResolverClientException() throws ActionDTOModelResolverException {

        mockActionPropertyResolver(mockedActionDTOModelResolver);
        doThrow(new ActionDTOModelResolverClientException("Invalid Certificate.", "Invalid PEM format."))
                .when(mockedActionDTOModelResolver).resolveForUpdateOperation(any(), any(), any());

        try {
            daoFacade.updateAction(actionDTOToAddOrUpdate, actionDTORetrieved, TENANT_ID);
            Assert.fail("Successful update of the actions without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getClass(), ActionMgtClientException.class);
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_ACTION_PROPERTIES.getCode());
            Assert.assertEquals(e.getMessage(), "Invalid Certificate.");
            Assert.assertEquals(e.getDescription(), "Invalid PEM format.");
        }
    }

    @Test(priority = 6)
    public void testUpdateActionWithActionPropertyResolverServerException() throws ActionDTOModelResolverException {

        mockActionPropertyResolver(mockedActionDTOModelResolver);
        doThrow(new ActionDTOModelResolverServerException("Error updating Certificate.", null)).when(
                        mockedActionDTOModelResolver)
                .resolveForUpdateOperation(any(), any(), any());

        try {
            daoFacade.updateAction(actionDTOToAddOrUpdate, actionDTORetrieved, TENANT_ID);
            Assert.fail("Successful update of the actions without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getClass(), ActionMgtServerException.class);
            Assert.assertEquals(e.getMessage(), ErrorMessage.ERROR_WHILE_UPDATING_ACTION.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof ActionDTOModelResolverServerException) {
                    return;
                }
            }
            Assert.fail("Expected ActionPropertyResolverServerException was not found in the exception chain");
        }
    }

    @Test(priority = 7, dependsOnMethods = "testAddAction")
    public void testUpdateCompleteAction() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);
        // Update action with certificate property deletion.
        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(actionDTORetrieved.getId())
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TEST_ACTION_NAME_UPDATED)
                .description(TEST_ACTION_DESCRIPTION_UPDATED)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI_UPDATED)
                        .authentication(TestUtil.buildMockBearerAuthentication(TEST_ACCESS_TOKEN))
                        .build())
                .property(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                        new ActionProperty.BuilderForService(TEST_PASSWORD_SHARING_TYPE_UPDATED).build())
                .property(CERTIFICATE_PROPERTY_NAME,
                        new ActionProperty.BuilderForService(
                                new Certificate.Builder().certificateContent(StringUtils.EMPTY).build()).build())
                .build();

        try {
            daoFacade.updateAction(updatingAction, actionDTORetrieved, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }

        ActionDTO result = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), actionDTORetrieved.getId());
        Assert.assertEquals(result.getType(), actionDTORetrieved.getType());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getStatus(), actionDTORetrieved.getStatus());
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(actionDTORetrieved.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());

        Authentication updatedAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(updatedAuthentication.getType(),
                updatingAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updatedAuthentication.getProperties().size(),
                updatingAction.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.ACCESS_TOKEN).getValue(),
                TestUtil.buildSecretName(PRE_UPDATE_PASSWORD_ACTION_ID, Authentication.Type.BEARER,
                        Authentication.Property.ACCESS_TOKEN));

        // Check whether the certificate is removed.
        Assert.assertEquals(result.getProperties().size(), updatingAction.getProperties().size() - 1);

        Assert.assertTrue(updatingAction.getProperties().containsKey(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertTrue(updatingAction.getProperties().containsKey(CERTIFICATE_PROPERTY_NAME));
        Assert.assertEquals(result.getPropertyValue(PASSWORD_SHARING_TYPE_PROPERTY_NAME),
                updatingAction.getPropertyValue(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertNull(result.getPropertyValue(CERTIFICATE_PROPERTY_NAME));
        actionDTORetrieved = result;
    }

    @Test(priority = 8)
    public void testActivateAction() throws ActionMgtException {

        Assert.assertEquals(actionDTORetrieved.getStatus(), Action.Status.INACTIVE);
        ActionDTO activatedActionDTO = daoFacade.activateAction(PRE_UPDATE_PASSWORD_TYPE, actionDTORetrieved.getId(),
                TENANT_ID);
        Assert.assertEquals(activatedActionDTO.getStatus(), Action.Status.ACTIVE);
        Assert.assertNotNull(activatedActionDTO.getCreatedAt());
        Assert.assertNotNull(activatedActionDTO.getUpdatedAt());
        Assert.assertTrue(activatedActionDTO.getUpdatedAt().after(actionDTORetrieved.getUpdatedAt()));
        actionDTORetrieved = activatedActionDTO;
    }

    @Test(priority = 9)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(actionDTORetrieved.getStatus(), Action.Status.ACTIVE);
        ActionDTO deactivatedActionDTO =
                daoFacade.deactivateAction(PRE_UPDATE_PASSWORD_TYPE, actionDTORetrieved.getId(),
                        TENANT_ID);
        Assert.assertEquals(deactivatedActionDTO.getStatus(), Action.Status.INACTIVE);
        Assert.assertNotNull(deactivatedActionDTO.getCreatedAt());
        Assert.assertNotNull(deactivatedActionDTO.getUpdatedAt());
        Assert.assertTrue(deactivatedActionDTO.getUpdatedAt().after(actionDTORetrieved.getUpdatedAt()));
    }

    @Test(priority = 10)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = daoFacade.getActionsCountPerType(TENANT_ID);
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PASSWORD_TYPE));
        Assert.assertEquals(actionMap.get(PRE_UPDATE_PASSWORD_TYPE).intValue(), 1);
    }

    @Test(priority = 11)
    public void testDeleteAction() throws ActionMgtException {

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, actionDTORetrieved.getId(),
                TENANT_ID);

        mockActionPropertyResolver(testActionPropertyResolver);
        try {
            daoFacade.deleteAction(actionDTORetrieved, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertNull(daoFacade.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID));
        Assert.assertEquals(daoFacade.getActionsCountPerType(TENANT_ID), Collections.emptyMap());
    }

    @Test(priority = 12)
    public void testAddActionWithRule() throws Exception {

        Rule rule = mockRule();
        mockRuleManagementService(rule);
        actionDTOToAddOrUpdate = createActionDTOForPasswordUpdateActionWithRule(rule);
        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, actionDTOToAddOrUpdate);
        verifyActionDTORule(actionDTORetrieved, actionDTOToAddOrUpdate);
    }

    @Test(priority = 13)
    public void testFailureInAddActionWithRuleAtRuleManagementException() throws Exception {

        Rule rule = mockRule();
        when(ruleManagementService.addRule(rule, TENANT_DOMAIN)).thenThrow(new RuleManagementException("Error adding " +
                "rule."));
        actionDTOToAddOrUpdate = createActionDTOForPasswordUpdateActionWithRule(rule);

        try {
            daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getMessage(), "Error while adding Action.");
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof ActionMgtServerException)) {
                cause = cause.getCause();
            }
            Assert.assertNotNull(cause, "Expected ActionMgtServerException was not found in the exception chain");
            Assert.assertEquals(cause.getMessage(), "Error while adding the Rule associated with the Action.");
        }
    }

    @Test(priority = 14)
    public void testFailureInRetrievingActionWithRuleAtRuleManagementException() throws Exception {

        Rule rule = mockRule();
        when(ruleManagementService.getRuleByRuleId(rule.getId(), TENANT_DOMAIN)).thenThrow(
                new RuleManagementException("Error retrieving rule."));

        try {
            daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                    TENANT_ID);
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getMessage(), "Error while retrieving Action by ID.");
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof ActionMgtServerException)) {
                cause = cause.getCause();
            }
            Assert.assertNotNull(cause, "Expected ActionMgtServerException was not found in the exception chain");
            Assert.assertEquals(cause.getMessage(), "Error while retrieving the Rule associated with the Action.");
        }
    }

    @Test(priority = 15)
    public void testGetActionsWithRulesByType() throws Exception {

        Rule rule = mockRule();
        mockRuleManagementService(rule);
        ActionDTO expectedActionDTO = createActionDTOForPasswordUpdateActionWithRule(rule);
        mockActionPropertyResolver(testActionPropertyResolver);

        List<ActionDTO> actionDTOs = daoFacade.getActionsByActionType(PRE_UPDATE_PASSWORD_TYPE, TENANT_ID);

        ActionDTO result = actionDTOs.get(0);
        verifyActionDTO(result, expectedActionDTO);
        verifyActionDTORule(result, expectedActionDTO);
    }

    @Test(priority = 16)
    public void testUpdateActionUpdatingRule() throws Exception {

        Rule rule = mockRule();
        mockRuleManagementService(rule);
        ActionDTO updatingActionDTO = createActionDTOForPasswordUpdateActionWithRule(rule);
        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.updateAction(updatingActionDTO, actionDTORetrieved, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, updatingActionDTO);
        verifyActionDTORule(actionDTORetrieved, updatingActionDTO);
        Assert.assertTrue(actionDTORetrieved.getUpdatedAt().after(actionDTORetrieved.getCreatedAt()));
    }

    @Test(priority = 17)
    public void testFailureInUpdateActionUpdatingRuleAtRuleManagementException() throws Exception {

        Rule rule = mockRule();
        when(ruleManagementService.updateRule(rule, TENANT_DOMAIN)).thenThrow(new RuleManagementException("Error " +
                "updating rule."));
        ActionDTO updatingActionDTO = createActionDTOForPasswordUpdateActionWithRule(rule);

        try {
            daoFacade.updateAction(updatingActionDTO, actionDTORetrieved, TENANT_ID);
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getMessage(), "Error while updating Action.");
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof ActionMgtServerException)) {
                cause = cause.getCause();
            }
            Assert.assertNotNull(cause, "Expected ActionMgtServerException was not found in the exception chain");
            Assert.assertEquals(cause.getMessage(), "Error while updating the Rule associated with the Action.");
        }
    }

    @Test(priority = 18)
    public void testUpdateActionRemovingRule() throws Exception {

        // In order to remove the rule from ActionRule create an ActionRule with a null Rule reference.
        ActionDTO updatingActionDTOWithoutRule = createActionDTOForPasswordUpdateActionWithRule(null);
        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.updateAction(updatingActionDTOWithoutRule, actionDTORetrieved, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, updatingActionDTOWithoutRule);
        Assert.assertNull(actionDTORetrieved.getActionRule());
    }

    @Test(priority = 19)
    public void testFailureInUpdateActionRemovingRuleAtRuleManagementException() throws Exception {

        doThrow(new RuleManagementException("Error updating rule.")).when(ruleManagementService)
                .deleteRule(any(), any());
        ActionDTO updatingActionDTOWithoutRule = createActionDTOForPasswordUpdateAction();
        mockActionPropertyResolver(testActionPropertyResolver);

        try {
            daoFacade.updateAction(updatingActionDTOWithoutRule, actionDTORetrieved, TENANT_ID);
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getMessage(), "Error while updating Action.");
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof ActionMgtServerException)) {
                cause = cause.getCause();
            }
            Assert.assertNotNull(cause, "Expected ActionMgtServerException was not found in the exception chain");
            Assert.assertEquals(cause.getMessage(), "Error while deleting the Rule associated with the Action.");
        }
    }

    @Test(priority = 20)
    public void testUpdateActionAddingRule() throws Exception {

        Rule rule = mockRule();
        mockRuleManagementService(rule);
        ActionDTO updatingActionDTO = createActionDTOForPasswordUpdateActionWithRule(rule);
        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.updateAction(updatingActionDTO, actionDTORetrieved, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, updatingActionDTO);
        verifyActionDTORule(actionDTORetrieved, updatingActionDTO);
        Assert.assertTrue(actionDTORetrieved.getUpdatedAt().after(actionDTORetrieved.getCreatedAt()));
    }

    @Test(priority = 21)
    public void testUpdateActionWithRuleWithoutUpdatingRule() throws Exception {

        Rule rule = mockRule();
        mockRuleManagementService(rule);
        ActionDTO updatingActionDTO = createActionDTOForPasswordUpdateAction();
        ActionDTO expectedActionDTO = createActionDTOForPasswordUpdateActionWithRule(rule);
        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.updateAction(updatingActionDTO, actionDTORetrieved, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, expectedActionDTO);
        verifyActionDTORule(actionDTORetrieved, expectedActionDTO);
        Assert.assertTrue(actionDTORetrieved.getUpdatedAt().after(actionDTORetrieved.getCreatedAt()));
    }

    @Test(priority = 22)
    public void testDeleteActionWithRule() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.deleteAction(actionDTORetrieved, TENANT_ID);

        Assert.assertNull(daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID));
        Assert.assertEquals(daoFacade.getActionsCountPerType(TENANT_ID), Collections.emptyMap());
    }

    @Test(priority = 23)
    public void testAddActionWithEmptyRule() throws Exception {

        actionDTOToAddOrUpdate = createActionDTOForPasswordUpdateActionWithRule(null);

        mockActionPropertyResolver(testActionPropertyResolver);

        daoFacade.addAction(actionDTOToAddOrUpdate, TENANT_ID);

        actionDTORetrieved = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);

        verifyActionDTO(actionDTORetrieved, actionDTOToAddOrUpdate);
        Assert.assertNull(actionDTORetrieved.getActionRule());

        daoFacade.deleteAction(actionDTORetrieved, TENANT_ID);
    }

    private void mockActionPropertyResolver(ActionDTOModelResolver actionDTOModelResolver) {

        actionPropertyResolverFactory.when(() -> ActionDTOModelResolverFactory.
                        getActionDTOModelResolver(Action.ActionTypes.PRE_UPDATE_PASSWORD))
                .thenReturn(actionDTOModelResolver);
    }

    private ActionDTO createActionDTOForPasswordUpdateAction() {

        return createActionDTOBuilderForPasswordUpdateAction().build();
    }

    private ActionDTO createActionDTOForPasswordUpdateActionWithRule(Rule rule) {

        return createActionDTOBuilderForPasswordUpdateAction().rule(ActionRule.create(rule)).build();
    }

    private ActionDTOBuilder createActionDTOBuilderForPasswordUpdateAction() {

        return new ActionDTOBuilder()
                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .status(Action.Status.INACTIVE)
                .actionVersion(TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD))
                        .build())
                .property(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                        new ActionProperty.BuilderForService(TEST_PASSWORD_SHARING_TYPE).build())
                .property(CERTIFICATE_PROPERTY_NAME,
                        new ActionProperty.BuilderForService(new Certificate.Builder()
                                .certificateContent(TEST_CERTIFICATE).build()).build());
    }

    private void verifyActionDTO(ActionDTO actualActionDTO, ActionDTO expectedActionDTO) {

        Assert.assertEquals(actualActionDTO.getId(), expectedActionDTO.getId());
        Assert.assertEquals(actualActionDTO.getType(), expectedActionDTO.getType());
        Assert.assertEquals(actualActionDTO.getName(), expectedActionDTO.getName());
        Assert.assertEquals(actualActionDTO.getDescription(), expectedActionDTO.getDescription());
        Assert.assertEquals(actualActionDTO.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(actualActionDTO.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(actualActionDTO.getCreatedAt());
        Assert.assertNotNull(actualActionDTO.getUpdatedAt());
        Assert.assertEquals(actualActionDTO.getEndpoint().getUri(), expectedActionDTO.getEndpoint().getUri());

        Authentication createdAuthentication = actualActionDTO.getEndpoint().getAuthentication();
        Assert.assertEquals(createdAuthentication.getType(),
                expectedActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(createdAuthentication.getProperties().size(),
                expectedActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                TestUtil.buildSecretName(PRE_UPDATE_PASSWORD_ACTION_ID, Authentication.Type.BASIC,
                        Authentication.Property.USERNAME));
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                TestUtil.buildSecretName(PRE_UPDATE_PASSWORD_ACTION_ID, Authentication.Type.BASIC,
                        Authentication.Property.PASSWORD));

        Assert.assertEquals(actualActionDTO.getProperties().size(), expectedActionDTO.getProperties().size());
        Assert.assertTrue(actualActionDTO.getProperties().containsKey(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertEquals(actualActionDTO.getPropertyValue(PASSWORD_SHARING_TYPE_PROPERTY_NAME),
                expectedActionDTO.getPropertyValue(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
    }

    private void verifyActionDTORule(ActionDTO actualActionDTO, ActionDTO expectedActionDTO)
            throws ActionMgtException {

        Assert.assertNotNull(actualActionDTO.getActionRule());
        Assert.assertEquals(actualActionDTO.getActionRule().getId(), expectedActionDTO.getActionRule().getId());
        Assert.assertEquals(actualActionDTO.getActionRule().getRule(), expectedActionDTO.getActionRule().getRule());
    }

    private Rule mockRule() {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn("ruleId");
        when(rule.isActive()).thenReturn(true);
        return rule;
    }

    private void mockRuleManagementService(Rule rule) throws RuleManagementException {

        when(ruleManagementService.addRule(rule, TENANT_DOMAIN)).thenReturn(rule);
        when(ruleManagementService.updateRule(rule, TENANT_DOMAIN)).thenReturn(rule);
        when(ruleManagementService.getRuleByRuleId("ruleId", TENANT_DOMAIN)).thenReturn(rule);
    }
}
