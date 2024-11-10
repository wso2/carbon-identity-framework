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

import org.junit.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_ID;

/**
 * This class is a test suite for the ActionManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods in the ActionManagementDAOImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class ActionManagementDAOImplTest {

    private ActionManagementDAOImpl daoImpl;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private Action createdAction;

    @BeforeClass
    public void setUpClass() {

        daoImpl = new ActionManagementDAOImpl();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
        when(secretType.getId()).thenReturn("secretId");
        when(secretManager.getSecretType(any())).thenReturn(secretType);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException {

        Action creatingAction = TestUtil.buildMockAction(
                "PreIssueAccessToken",
                "To configure PreIssueAccessToken",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"));

        createdAction = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, creatingAction,
                TENANT_ID);
        Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, createdAction.getId());
        Assert.assertEquals(creatingAction.getName(), createdAction.getName());
        Assert.assertEquals(creatingAction.getDescription(), createdAction.getDescription());
        Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getType().getActionType());
        Assert.assertEquals(Action.Status.ACTIVE, createdAction.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), createdAction.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                createdAction.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 2, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Action.")
    public void testAddActionWithoutName() throws ActionMgtException {

        Action action = TestUtil.buildMockAction(
                null,
                "To configure PreIssueAccessToken",
                "https://example.com",
                 TestUtil.buildMockBasicAuthentication("admin", "admin"));

        daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, String.valueOf(UUID.randomUUID()), action, TENANT_ID);
    }

    @Test(priority = 3, dependsOnMethods = "testAddAction")
    public void testGetActionsByActionType() throws ActionMgtException {

        List<Action> actionList = daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_TYPE, TENANT_ID);
        Assert.assertEquals(1, actionList.size());
        Action result = actionList.get(0);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(createdAction.getName(), result.getName());
        Assert.assertEquals(createdAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(createdAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(createdAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 4)
    public void testGetActionByActionId() throws ActionMgtException {

        Action result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(createdAction.getName(), result.getName());
        Assert.assertEquals(createdAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(createdAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(createdAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 5)
    public void testDeleteAction() throws ActionMgtException {

        daoImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, createdAction, TENANT_ID);
        Assert.assertNull(daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID));
    }

    @Test(priority = 6)
    public void testAddActionWithoutDescription() throws ActionMgtException {

        String id = String.valueOf(UUID.randomUUID());
        Action creatingAction = TestUtil.buildMockAction(
                "PreIssueAccessToken",
                null,
                "https://example.com",
                 TestUtil.buildMockBasicAuthentication("admin", "admin"));
        createdAction = daoImpl.addAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, id, creatingAction, TENANT_ID);
        Assert.assertEquals(id, createdAction.getId());
        Assert.assertEquals(creatingAction.getName(), createdAction.getName());
        Assert.assertNull(null, createdAction.getDescription());
        Assert.assertEquals(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getType().getActionType());
        Assert.assertEquals(Action.Status.ACTIVE, createdAction.getStatus());
        Assert.assertEquals(creatingAction.getEndpoint().getUri(), createdAction.getEndpoint().getUri());
        Assert.assertEquals(creatingAction.getEndpoint().getAuthentication().getType(),
                createdAction.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 7, dependsOnMethods = "testAddActionWithoutDescription")
    public void testUpdateAction() throws ActionMgtException {

        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                "https://sample.com",
                 TestUtil.buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(
                updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType()
        );
        createdAction = result;
    }

    @Test(priority = 8)
    public void testUpdateActionWithoutNameAndDescription() throws ActionMgtException {

        // TODO: 'Name' is a required attribute. Thus, DAO layer should throw an exception if name is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = TestUtil.buildMockAction(
                null,
                null,
                "https://sample.com",
                 TestUtil.buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(createdAction.getName(), result.getName());
        Assert.assertEquals(createdAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 9)
    public void testUpdateActionWithNameAndDescription() throws ActionMgtException {

        // TODO: 'Uri','AuthenticationType','AuthProperties' are required attributes. Thus, DAO layer should throw an
        //  exception if those attributes are null. This should be fixed in DAO layer and test case needs to be updated
        //  accordingly.
        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                null);
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(createdAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(createdAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 10)
    public void testUpdateActionWithoutEndpointUri() throws ActionMgtException {

        // TODO: 'Uri' is a required attribute. Thus, DAO layer should throw an exception if Uri is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                 TestUtil.buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(createdAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 11)
    public void testUpdateActionWithAuthType() throws ActionMgtException {

        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                "https://sample.com",
                TestUtil.buildMockBearerAuthentication("57c7df90-cacc-4f56-9b0a-f14bfbff3076"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(createdAction.getName(), result.getName());
        Assert.assertEquals(createdAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        createdAction = result;
    }

    @Test(priority = 12)
    public void testUpdateActionWithUri() throws ActionMgtException {

        // TODO: 'Name','AuthenticationType' and 'AuthProperties' are required attributes. Thus, DAO layer should throw
        //  an exception if those attributes are null. This should be fixed in DAO layer and test case needs to be
        //  updated accordingly.
        Action updatingAction = TestUtil.buildMockAction(
                null,
                null,
                "https://sample.com",
                null);
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(createdAction.getName(), result.getName());
        Assert.assertEquals(createdAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(updatingAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(createdAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
        createdAction = result;
    }

    @Test(priority = 13)
    public void testUpdateActionWithAuthTypeWithoutUri() throws ActionMgtException {

        // TODO: 'Uri' is a required attribute. Thus, DAO layer should throw an exception if uri is null.
        //  This should be fixed in DAO layer and test case needs to be updated accordingly.
        Action updatingAction = TestUtil.buildMockAction(
                "Pre Issue Access Token",
                "To configure pre issue access token",
                null,
                TestUtil.buildMockBasicAuthentication("updatingadmin", "updatingadmin"));
        Action result = daoImpl.updateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), updatingAction,
                createdAction, TENANT_ID);
        Assert.assertEquals(createdAction.getId(), result.getId());
        Assert.assertEquals(updatingAction.getName(), result.getName());
        Assert.assertEquals(updatingAction.getDescription(), result.getDescription());
        Assert.assertEquals(createdAction.getType(), result.getType());
        Assert.assertEquals(createdAction.getStatus(), result.getStatus());
        Assert.assertEquals(createdAction.getEndpoint().getUri(), result.getEndpoint().getUri());
        Assert.assertEquals(updatingAction.getEndpoint().getAuthentication().getType(),
                result.getEndpoint().getAuthentication().getType());
    }

    @Test(priority = 14)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(Action.Status.ACTIVE, createdAction.getStatus());
        Action deactivatedAction = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(),
                TENANT_ID);
        Assert.assertEquals(Action.Status.INACTIVE, deactivatedAction.getStatus());
    }

    @Test(priority = 15)
    public void testActivateAction() throws ActionMgtException {

        Action result = daoImpl.activateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdAction.getId(), TENANT_ID);
        Assert.assertEquals(Action.Status.ACTIVE, result.getStatus());
    }

    @Test(priority = 16)
    public void testGetActionsCountPerType() throws ActionMgtException {

        PreUpdatePasswordAction actionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "PreUpdatePassword",
                "To configure PreUpdatePassword",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"),
                PreUpdatePasswordAction.PasswordFormat.PLAIN_TEXT,
                null);

        Action preUpdatePasswordAction = daoImpl.addAction(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID, 
                actionModel, TENANT_ID);
       
        Map<String, Integer> actionMap = daoImpl.getActionsCountPerType(TENANT_ID);
        Assert.assertTrue(actionMap.containsKey(PRE_ISSUE_ACCESS_TOKEN_TYPE));
        Assert.assertEquals(1, actionMap.get(PRE_ISSUE_ACCESS_TOKEN_TYPE).intValue());
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PASSWORD_TYPE));
        Assert.assertEquals(1, actionMap.get(PRE_UPDATE_PASSWORD_TYPE).intValue());
        
        daoImpl.deleteAction(PRE_UPDATE_PASSWORD_TYPE, PRE_UPDATE_PASSWORD_ACTION_ID, preUpdatePasswordAction, 
                TENANT_ID);
        daoImpl.deleteAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, createdAction, TENANT_ID);
    }
}
