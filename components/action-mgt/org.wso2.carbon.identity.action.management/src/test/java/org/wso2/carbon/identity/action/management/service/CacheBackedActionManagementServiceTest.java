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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.internal.service.impl.ActionManagementServiceImpl;
import org.wso2.carbon.identity.action.management.internal.service.impl.CacheBackedActionManagementService;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_PATH;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_USERNAME;

/**
 * Test class for CacheBackedActionManagement.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedActionManagementServiceTest {

    private ActionManagementServiceImpl actionManagementServiceImpl;
    private CacheBackedActionManagementService cacheBackedActionManagementService;

    private final List<Action> mockedActionsList = new ArrayList<>();
    private Action mockedAction;
    
    @BeforeClass
    public void setUpClass() {

        cacheBackedActionManagementService = CacheBackedActionManagementService.getInstance();
        Timestamp currentTime = new Timestamp(new Date().getTime());
        mockedAction = new Action.ActionResponseBuilder()
                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .status(Action.Status.ACTIVE)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .endpoint(TestUtil.buildMockEndpointConfig(TEST_ACTION_URI,
                        TestUtil.buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD)))
                .build();
        mockedActionsList.add(mockedAction);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(TestUtil.TEST_SECRET_TYPE_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);

        actionManagementServiceImpl = mock(ActionManagementServiceImpl.class);
        // Set ACTION_MGT_SERVICE field using reflection
        setFinalField(cacheBackedActionManagementService, "ACTION_MGT_SERVICE", actionManagementServiceImpl);
    }

    @Test(priority = 1)
    public void testGetActionsByActionTypeFromDB() throws ActionMgtException {

        doReturn(mockedActionsList).when(actionManagementServiceImpl).getActionsByActionType(any(), any());

        List<Action> actions = cacheBackedActionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH,
                TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).getActionsByActionType(any(), any());
        Assert.assertEquals(actions.size(), mockedActionsList.size());
        Action result = actions.get(0);
        assertAction(result);
    }

    @Test(priority = 2, dependsOnMethods = "testGetActionsByActionTypeFromDB")
    public void testGetActionsByActionTypeFromCache() throws ActionMgtException {

        doReturn(null).when(actionManagementServiceImpl).getActionsByActionType(any(), any());

        List<Action> actions = cacheBackedActionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH,
                TENANT_DOMAIN);
        verify(actionManagementServiceImpl, never()).getActionsByActionType(any(), any());
        Assert.assertNotNull(actions);
        Assert.assertEquals(actions.size(), mockedActionsList.size());
        Action result = actions.get(0);
        assertAction(result);
    }

    @Test(priority = 3, dependsOnMethods = "testGetActionsByActionTypeFromDB")
    public void testGetActionsByActionIdFromCache() throws ActionMgtException {

        doReturn(null).when(actionManagementServiceImpl).getActionByActionId(any(), any(), any());

        Action action = cacheBackedActionManagementService.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_PATH,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, never()).getActionByActionId(any(), any(), any());
        Assert.assertNotNull(action);
        assertAction(action);
    }

    @Test(priority = 4)
    public void testAddAction() throws ActionMgtException {

        doReturn(mockedAction).when(actionManagementServiceImpl).addAction(any(), any(), any());

        Action action = cacheBackedActionManagementService.addAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                mockedAction, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).addAction(any(), any(), any());
        Assert.assertNotNull(action);
        checkCacheInvalidation();
    }

    @Test(priority = 5)
    public void testGetActionsByActionIdFromDB() throws ActionMgtException {

        doReturn(mockedAction).when(actionManagementServiceImpl).getActionByActionId(any(), any(), any());

        Action action = cacheBackedActionManagementService.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_PATH,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).getActionByActionId(any(), any(), any());
        Assert.assertNotNull(action);
        assertAction(action);
    }

    @Test(priority = 6)
    public void testUpdateAction() throws ActionMgtException {

        doReturn(mockedAction).when(actionManagementServiceImpl).updateAction(any(), any(), any(), any());

        Action action = cacheBackedActionManagementService.updateAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, mockedAction, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).updateAction(any(), any(), any(), any());
        Assert.assertNotNull(action);
        checkCacheInvalidation();
    }

    @Test(priority = 7)
    public void testDeactivateAction() throws ActionMgtException {

        // Update cache.
        doReturn(mockedActionsList).when(actionManagementServiceImpl).getActionsByActionType(any(), any());
        cacheBackedActionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH, TENANT_DOMAIN);

        cacheBackedActionManagementService.deactivateAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).deactivateAction(any(), any(), any());
        checkCacheInvalidation();
    }

    @Test(priority = 8)
    public void testActivateAction() throws ActionMgtException {

        // Update cache.
        doReturn(mockedActionsList).when(actionManagementServiceImpl).getActionsByActionType(any(), any());
        cacheBackedActionManagementService.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_PATH, TENANT_DOMAIN);

        cacheBackedActionManagementService.activateAction(PRE_ISSUE_ACCESS_TOKEN_PATH,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).activateAction(any(), any(), any());
        checkCacheInvalidation();
    }

    @Test(priority = 9)
    public void testUpdateActionEndpointAuthentication() throws ActionMgtException {

        // Update cache.
        doReturn(mockedAction).when(actionManagementServiceImpl).updateActionEndpointAuthentication(any(), any(),
                any(), any());

        Action action = cacheBackedActionManagementService.updateActionEndpointAuthentication(
                PRE_ISSUE_ACCESS_TOKEN_PATH, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                mockedAction.getEndpoint().getAuthentication(), TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).updateActionEndpointAuthentication(any(),
                any(), any(), any());
        Assert.assertNotNull(action);
        checkCacheInvalidation();
    }

    @Test(priority = 14)
    public void testDeleteAction() throws ActionMgtException {

        // Update cache.
        doNothing().when(actionManagementServiceImpl).deleteAction(any(), any(), any());

        cacheBackedActionManagementService.deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH, mockedAction.getId(),
                TENANT_DOMAIN);
        verify(actionManagementServiceImpl, times(1)).deleteAction(any(), any(), any());
        checkCacheInvalidation();
    }

    @Test(priority = 10)
    public void testGetActionsCountPerType() throws ActionMgtException {

        Map<String, Integer> mockedActionMap = new HashMap<>();
        mockedActionMap.put(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType(), 1);
        mockedActionMap.put(Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType(), 2);
        doReturn(mockedActionMap).when(actionManagementServiceImpl).getActionsCountPerType(any());

        Map<String, Integer> actionMap = cacheBackedActionManagementService.getActionsCountPerType(TENANT_DOMAIN);
        Assert.assertNotNull(actionMap.get(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType()));
        Assert.assertNotNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_UPDATE_PROFILE.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.PRE_REGISTRATION.getActionType()));
        Assert.assertNull(actionMap.get(Action.ActionTypes.AUTHENTICATION.getActionType()));

        Assert.assertEquals(actionMap.get(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType()).intValue(), 1);
        Assert.assertEquals(actionMap.get(Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType()).intValue(), 2);
    }

    private void checkCacheInvalidation() throws ActionMgtException {

        reset(actionManagementServiceImpl);
        doReturn(null).when(actionManagementServiceImpl).getActionsByActionType(any(), any());
        doReturn(null).when(actionManagementServiceImpl).getActionByActionId(any(), any(), any());

        List<Action> actions = cacheBackedActionManagementService.getActionsByActionType(
                mockedAction.getType().getPathParam(), TENANT_DOMAIN);
        Assert.assertNull(actions);
        verify(actionManagementServiceImpl, times(1)).getActionsByActionType(any(), any());

        Action action = cacheBackedActionManagementService.getActionByActionId(mockedAction.getType().getPathParam(),
                mockedAction.getId(), TENANT_DOMAIN);
        Assert.assertNull(action);
        verify(actionManagementServiceImpl, times(1)).getActionByActionId(any(), any(), any());
    }

    private void assertAction(Action action) {

        Assert.assertEquals(action.getId(), mockedAction.getId());
        Assert.assertEquals(action.getName(), mockedAction.getName());
        Assert.assertEquals(action.getDescription(), mockedAction.getDescription());
        Assert.assertEquals(action.getType(), mockedAction.getType());
        Assert.assertEquals(action.getStatus(), mockedAction.getStatus());
        Assert.assertEquals(action.getCreatedAt(), mockedAction.getCreatedAt());
        Assert.assertEquals(action.getUpdatedAt(), mockedAction.getUpdatedAt());
        Assert.assertEquals(action.getEndpoint().getUri(), mockedAction.getEndpoint().getUri());

        Authentication actionAuth = action.getEndpoint().getAuthentication();
        Authentication mockedActionAuth = mockedAction.getEndpoint().getAuthentication();

        Assert.assertEquals(actionAuth.getType(), mockedActionAuth.getType());
        Assert.assertEquals(actionAuth.getProperty(Authentication.Property.USERNAME).getValue(),
                mockedActionAuth.getProperty(Authentication.Property.USERNAME).getValue());
        Assert.assertEquals(actionAuth.getProperty(Authentication.Property.PASSWORD).getValue(),
                mockedActionAuth.getProperty(Authentication.Property.PASSWORD).getValue());
    }

    /**
     * Set a final field using reflection (compatible with Java 12+).
     * Uses Unsafe API to modify static final fields.
     *
     * @param target    The target object.
     * @param fieldName The field name.
     * @param value     The value to set.
     * @throws Exception If an error occurs.
     */
    private void setFinalField(Object target, String fieldName, Object value) throws Exception {

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field.setAccessible(true);

        // Use Unsafe to modify static final fields in Java 12+
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object fieldBase = unsafe.staticFieldBase(field);
        long fieldOffset = unsafe.staticFieldOffset(field);
        unsafe.putObject(fieldBase, fieldOffset, value);
    }
}
