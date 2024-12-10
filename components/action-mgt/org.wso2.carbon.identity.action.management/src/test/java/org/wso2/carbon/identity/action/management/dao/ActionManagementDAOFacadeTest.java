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

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.constant.error.ErrorMessage;
import org.wso2.carbon.identity.action.management.dao.impl.ActionDTOModelResolverFactory;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOFacade;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
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
    private TestActionDTOModelResolver testActionPropertyResolver;
    private MockedStatic<ActionDTOModelResolverFactory> actionPropertyResolverFactory;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    private ActionManagementDAOFacade daoFacade;
    private ActionDTO creatingActionDTO;
    private ActionDTO createdActionDTO;

    @BeforeClass
    public void setUpClass() {

        daoFacade = new ActionManagementDAOFacade(new ActionManagementDAOImpl());
        creatingActionDTO = new ActionDTOBuilder()
                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD))
                        .build())
                .property(PASSWORD_SHARING_TYPE_PROPERTY_NAME, TEST_PASSWORD_SHARING_TYPE)
                .property(CERTIFICATE_PROPERTY_NAME,
                        new Certificate.Builder().certificateContent(TEST_CERTIFICATE).build())
                .build();
        testActionPropertyResolver = new TestActionDTOModelResolver();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(TestUtil.TEST_SECRET_TYPE_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

        MockitoAnnotations.openMocks(this);
        actionPropertyResolverFactory = mockStatic(ActionDTOModelResolverFactory.class);
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
            daoFacade.addAction(creatingActionDTO, TENANT_ID);
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
            daoFacade.addAction(creatingActionDTO, TENANT_ID);
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
            daoFacade.addAction(creatingActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }

        createdActionDTO = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID,
                TENANT_ID);
        Assert.assertEquals(createdActionDTO.getId(), creatingActionDTO.getId());
        Assert.assertEquals(createdActionDTO.getType(), creatingActionDTO.getType());
        Assert.assertEquals(createdActionDTO.getName(), creatingActionDTO.getName());
        Assert.assertEquals(createdActionDTO.getDescription(), creatingActionDTO.getDescription());
        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.ACTIVE);
        Assert.assertEquals(createdActionDTO.getEndpoint().getUri(), creatingActionDTO.getEndpoint().getUri());

        Authentication createdAuthentication = createdActionDTO.getEndpoint().getAuthentication();
        Assert.assertEquals(createdAuthentication.getType(),
                creatingActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(createdAuthentication.getProperties().size(),
                creatingActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                TestUtil.buildSecretName(PRE_UPDATE_PASSWORD_ACTION_ID, Authentication.Type.BASIC,
                        Authentication.Property.USERNAME));
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                TestUtil.buildSecretName(PRE_UPDATE_PASSWORD_ACTION_ID, Authentication.Type.BASIC,
                        Authentication.Property.PASSWORD));

        Assert.assertEquals(createdActionDTO.getProperties().size(), creatingActionDTO.getProperties().size());
        Assert.assertTrue(createdActionDTO.getProperties().containsKey(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertTrue(createdActionDTO.getProperties().containsKey(CERTIFICATE_PROPERTY_NAME));
        Assert.assertEquals(createdActionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME),
                creatingActionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertEquals(((Certificate) createdActionDTO.getProperty(CERTIFICATE_PROPERTY_NAME))
                        .getCertificateContent(), TEST_CERTIFICATE);
    }

    @Test(priority = 4)
    public void testGetActionsByType() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);
        List<ActionDTO> actionDTOs = daoFacade.getActionsByActionType(PRE_UPDATE_PASSWORD_TYPE, TENANT_ID);
        ActionDTO result = actionDTOs.get(0);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());

        Authentication resultAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(resultAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(resultAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                createdActionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME)
                        .getValue());
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                createdActionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD)
                        .getValue());

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertTrue(createdActionDTO.getProperties().containsKey(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertTrue(createdActionDTO.getProperties().containsKey(CERTIFICATE_PROPERTY_NAME));
        Assert.assertEquals(result.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME),
                createdActionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertEquals(((Certificate) result.getProperty(CERTIFICATE_PROPERTY_NAME)).getCertificateContent(),
                ((Certificate) createdActionDTO.getProperty(CERTIFICATE_PROPERTY_NAME)).getCertificateContent());
    }

    @Test(priority = 5)
    public void testUpdateActionPropertyResolverClientException() throws ActionDTOModelResolverException {

        mockActionPropertyResolver(mockedActionDTOModelResolver);
        doThrow(new ActionDTOModelResolverClientException("Invalid Certificate.", "Invalid PEM format."))
                .when(mockedActionDTOModelResolver).resolveForUpdateOperation(any(), any(), any());

        try {
            daoFacade.updateAction(creatingActionDTO, createdActionDTO, TENANT_ID);
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
            daoFacade.updateAction(creatingActionDTO, createdActionDTO, TENANT_ID);
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
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TEST_ACTION_NAME_UPDATED)
                .description(TEST_ACTION_DESCRIPTION_UPDATED)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI_UPDATED)
                        .authentication(TestUtil.buildMockBearerAuthentication(TEST_ACCESS_TOKEN))
                        .build())
                .property(PASSWORD_SHARING_TYPE_PROPERTY_NAME, TEST_PASSWORD_SHARING_TYPE_UPDATED)
                .property(CERTIFICATE_PROPERTY_NAME,
                        new Certificate.Builder().certificateContent(StringUtils.EMPTY).build())
                .build();

        try {
            daoFacade.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }

        ActionDTO result = daoFacade.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
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
        Assert.assertEquals(result.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME),
                updatingAction.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME));
        Assert.assertNull(result.getProperty(CERTIFICATE_PROPERTY_NAME));
        createdActionDTO = result;
    }

    @Test(priority = 8)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.ACTIVE);
        ActionDTO deactivatedActionDTO = daoFacade.deactivateAction(PRE_UPDATE_PASSWORD_TYPE, createdActionDTO.getId(),
                TENANT_ID);
        Assert.assertEquals(deactivatedActionDTO.getStatus(), Action.Status.INACTIVE);
    }

    @Test(priority = 9)
    public void testActivateAction() throws ActionMgtException {

        ActionDTO activatedActionDTO = daoFacade.activateAction(PRE_UPDATE_PASSWORD_TYPE, createdActionDTO.getId(),
                TENANT_ID);
        Assert.assertEquals(activatedActionDTO.getStatus(), Action.Status.ACTIVE);
    }

    @Test(priority = 10)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> actionMap = daoFacade.getActionsCountPerType(TENANT_ID);
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PASSWORD_TYPE));
        Assert.assertEquals(actionMap.get(PRE_UPDATE_PASSWORD_TYPE).intValue(), 1);
    }

    @Test(priority = 11)
    public void testDeleteAction() throws ActionMgtException {

        mockActionPropertyResolver(testActionPropertyResolver);
        try {
            daoFacade.deleteAction(createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertNull(daoFacade.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID));
        Assert.assertEquals(daoFacade.getActionsCountPerType(TENANT_ID), Collections.emptyMap());
    }

    private void mockActionPropertyResolver(ActionDTOModelResolver actionDTOModelResolver) {

        actionPropertyResolverFactory.when(
                () -> ActionDTOModelResolverFactory.getActionDTOModelResolver(Action.ActionTypes.PRE_UPDATE_PASSWORD))
                .thenReturn(actionDTOModelResolver);
    }
}
