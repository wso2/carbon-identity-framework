/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model.test.dao;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.cache.*;
import org.wso2.carbon.identity.application.common.dao.impl.AuthenticatorManagementDAOImpl;
import org.wso2.carbon.identity.application.common.dao.impl.AuthenticatorManagementFacade;
import org.wso2.carbon.identity.application.common.dao.impl.CacheBackedAuthenticatorMgtDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.test.util.SystemDefinedLocalAuthenticatorDataUtil;
import org.wso2.carbon.identity.application.common.model.test.util.UserDefinedLocalAuthenticatorDataUtil;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WithCarbonHome
public class CacheBackedAuthenticatorMgtDAOTest {

    private static final String AUTHENTICATOR1_NAME = "custom-auth1";
    private static final String AUTHENTICATOR2_NAME = "custom-auth2";
    private static final int TEST_TENANT_ID = 1;
    private static final String TEST_TENANT_DOMAIN = "wso2.com";
    private static final String SYSTEM_AUTHENTICATOR_NAME = "system-auth1";

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private AuthenticatorManagementFacade mockAuthenticatorManagementFacade;
    private CacheBackedAuthenticatorMgtDAO cacheBackedAuthenticatorMgtDAO;
    private AuthenticatorCache authenticatorCache;
    private SystemDefinedAuthenticatorCache systemDefinedAuthenticatorCache;
    private UserDefinedLocalAuthenticatorsCache userDefinedLocalAuthenticatorsCache;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig1;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig2;
    private UserDefinedLocalAuthenticatorConfig authenticatorForUpdate;
    private LocalAuthenticatorConfig localAuthenticatorConfig1;
    private LocalAuthenticatorConfig localAuthenticatorForUpdate;

    @BeforeClass
    public void setUp() throws Exception {

        cacheBackedAuthenticatorMgtDAO = new CacheBackedAuthenticatorMgtDAO(new AuthenticatorManagementDAOImpl());
        authenticatorCache = AuthenticatorCache.getInstance();
        systemDefinedAuthenticatorCache = SystemDefinedAuthenticatorCache.getInstance();
        userDefinedLocalAuthenticatorsCache = UserDefinedLocalAuthenticatorsCache.getInstance();

        mockAuthenticatorManagementFacade = mock(AuthenticatorManagementFacade.class);
        setField(cacheBackedAuthenticatorMgtDAO, "authenticatorMgtFacade", mockAuthenticatorManagementFacade);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TEST_TENANT_ID)).
                thenReturn(TEST_TENANT_DOMAIN);

        authenticatorConfig1 = UserDefinedLocalAuthenticatorDataUtil.createUserDefinedAuthenticatorConfig
                (AUTHENTICATOR1_NAME, AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        authenticatorConfig2 = UserDefinedLocalAuthenticatorDataUtil.createUserDefinedAuthenticatorConfig
                (AUTHENTICATOR2_NAME, AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        authenticatorForUpdate = UserDefinedLocalAuthenticatorDataUtil.updateUserDefinedAuthenticatorConfig(
                authenticatorConfig1);
        localAuthenticatorConfig1 = SystemDefinedLocalAuthenticatorDataUtil.createSystemDefinedAuthenticatorConfig(
                SYSTEM_AUTHENTICATOR_NAME);
        localAuthenticatorForUpdate = SystemDefinedLocalAuthenticatorDataUtil.updateSystemDefinedAuthenticatorConfig(
                localAuthenticatorConfig1);
    }

    @BeforeMethod
    public void tearDown() {

        reset(mockAuthenticatorManagementFacade);
    }

    @AfterClass
    public void end() {

        identityTenantUtil.close();
    }


    @Test
    public void testAddUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig mockAuthenticatorConfig = mock(UserDefinedLocalAuthenticatorConfig.class);
        doReturn(authenticatorConfig1.getName()).when(mockAuthenticatorConfig).getName();
        doReturn(authenticatorConfig1).when(mockAuthenticatorManagementFacade).addUserDefinedLocalAuthenticator(
                mockAuthenticatorConfig, TEST_TENANT_ID);

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(authenticatorConfig1.getName());
        Assert.assertNull(authenticatorCache.getValueFromCache(authenticatorCacheKey, TEST_TENANT_ID));

        UserDefinedLocalAuthenticatorConfig createdAuthenticator = cacheBackedAuthenticatorMgtDAO
                .addUserDefinedLocalAuthenticator(mockAuthenticatorConfig, TEST_TENANT_ID);

        // Verify the result
        Assert.assertNotNull(createdAuthenticator);
        Assert.assertEquals(createdAuthenticator.getName(), authenticatorConfig1.getName());
        Assert.assertEquals(createdAuthenticator.getImageUrl(), authenticatorConfig1.getImageUrl());
        Assert.assertEquals(createdAuthenticator.getDescription(), authenticatorConfig1.getDescription());
        Assert.assertEquals(createdAuthenticator.getDisplayName(), authenticatorConfig1.getDisplayName());
        Assert.assertEquals(createdAuthenticator.isEnabled(), authenticatorConfig1.isEnabled());
        Assert.assertEquals(createdAuthenticator.getDefinedByType(), authenticatorConfig1.getDefinedByType());

        // Verify cache operations
        verify(mockAuthenticatorManagementFacade, times(1)).addUserDefinedLocalAuthenticator(
                mockAuthenticatorConfig, TEST_TENANT_ID);

        UserDefinedLocalAuthenticatorConfig cachedAuthenticator = authenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);
        Assert.assertEquals(cachedAuthenticator, authenticatorConfig1);

        UserDefinedLocalAuthenticatorsCacheKey localAuthenticatorsCacheKey =
                new UserDefinedLocalAuthenticatorsCacheKey(TEST_TENANT_ID);
        Assert.assertNull(userDefinedLocalAuthenticatorsCache.getValueFromCache(localAuthenticatorsCacheKey,
                TEST_TENANT_ID));
    }

    @Test(dependsOnMethods = "testAddUserDefinedLocalAuthenticator")
    public void testIsExistingAuthenticatorName() throws AuthenticatorMgtException {

        doReturn(true).when(mockAuthenticatorManagementFacade).isExistingAuthenticatorName(
                authenticatorConfig1.getName(), TEST_TENANT_ID);

        Assert.assertTrue(cacheBackedAuthenticatorMgtDAO.isExistingAuthenticatorName(authenticatorConfig1.getName(),
                TEST_TENANT_ID));
        verify(mockAuthenticatorManagementFacade, times(1)).isExistingAuthenticatorName(
                authenticatorConfig1.getName(), TEST_TENANT_ID);
    }

    @Test(dependsOnMethods = "testIsExistingAuthenticatorName")
    public void testGetUserDefinedLocalAuthenticatorFromCache() throws AuthenticatorMgtException {

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(authenticatorConfig1.getName());
        UserDefinedLocalAuthenticatorConfig cachedAuthenticator = authenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);

        UserDefinedLocalAuthenticatorConfig fetchedAuthenticator = cacheBackedAuthenticatorMgtDAO
                .getUserDefinedLocalAuthenticator(authenticatorConfig1.getName(), TEST_TENANT_ID);

        // Verify the result
        Assert.assertNotNull(fetchedAuthenticator);
        Assert.assertEquals(fetchedAuthenticator, cachedAuthenticator);
        verify(mockAuthenticatorManagementFacade, never())
                .getUserDefinedLocalAuthenticator(authenticatorConfig1.getName(), TEST_TENANT_ID);
    }

    @Test(dependsOnMethods = "testGetUserDefinedLocalAuthenticatorFromCache")
    public void testGetAllUserDefinedLocalAuthenticatorsFromDB() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorsCacheKey localAuthenticatorsCacheKey =
                new UserDefinedLocalAuthenticatorsCacheKey(TEST_TENANT_ID);
        Assert.assertNull(userDefinedLocalAuthenticatorsCache.getValueFromCache(localAuthenticatorsCacheKey,
                TEST_TENANT_ID));

        List<UserDefinedLocalAuthenticatorConfig> authenticatorConfigs = new ArrayList<>();
        authenticatorConfigs.add(authenticatorConfig1);
        authenticatorConfigs.add(authenticatorConfig2);
        doReturn(authenticatorConfigs).when(mockAuthenticatorManagementFacade)
                .getAllUserDefinedLocalAuthenticators(TEST_TENANT_ID);

        List<UserDefinedLocalAuthenticatorConfig> fetchedAuthenticators = cacheBackedAuthenticatorMgtDAO
                .getAllUserDefinedLocalAuthenticators(TEST_TENANT_ID);

        // Verify the result
        Assert.assertNotNull(fetchedAuthenticators);
        Assert.assertEquals(fetchedAuthenticators.get(0), authenticatorConfig1);
        Assert.assertEquals(fetchedAuthenticators.get(1), authenticatorConfig2);

        verify(mockAuthenticatorManagementFacade, times(1)).getAllUserDefinedLocalAuthenticators(TEST_TENANT_ID);

        List<UserDefinedLocalAuthenticatorConfig> cachedAuthenticators = userDefinedLocalAuthenticatorsCache
                .getValueFromCache(localAuthenticatorsCacheKey, TEST_TENANT_ID)
                .getUserDefinedLocalAuthenticators();
        Assert.assertNotNull(cachedAuthenticators);
        Assert.assertEquals(cachedAuthenticators.get(0), authenticatorConfig1);
        Assert.assertEquals(cachedAuthenticators.get(1), authenticatorConfig2);
    }

    @Test(dependsOnMethods = "testGetAllUserDefinedLocalAuthenticatorsFromDB")
    public void testGetAllUserDefinedLocalAuthenticatorsFromCache() throws AuthenticatorMgtException {

        reset(mockAuthenticatorManagementFacade);
        List<UserDefinedLocalAuthenticatorConfig> fetchedAuthenticators = cacheBackedAuthenticatorMgtDAO
                .getAllUserDefinedLocalAuthenticators(TEST_TENANT_ID);

        // Verify the result
        Assert.assertNotNull(fetchedAuthenticators);
        Assert.assertEquals(fetchedAuthenticators.get(0), authenticatorConfig1);
        Assert.assertEquals(fetchedAuthenticators.get(1), authenticatorConfig2);

        verify(mockAuthenticatorManagementFacade, never()).getAllUserDefinedLocalAuthenticators(TEST_TENANT_ID);

        UserDefinedLocalAuthenticatorsCacheKey localAuthenticatorsCacheKey =
                new UserDefinedLocalAuthenticatorsCacheKey(TEST_TENANT_ID);
        List<UserDefinedLocalAuthenticatorConfig> cachedAuthenticators = userDefinedLocalAuthenticatorsCache
                .getValueFromCache(localAuthenticatorsCacheKey, TEST_TENANT_ID)
                .getUserDefinedLocalAuthenticators();
        Assert.assertNotNull(cachedAuthenticators);
        Assert.assertEquals(cachedAuthenticators.get(0), authenticatorConfig1);
        Assert.assertEquals(cachedAuthenticators.get(1), authenticatorConfig2);
    }

    @Test(dependsOnMethods = "testGetAllUserDefinedLocalAuthenticatorsFromCache")
    public void testUpdateUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig mockUpdatingAuthenticatorConfig =
                mock(UserDefinedLocalAuthenticatorConfig.class);
        doReturn(authenticatorForUpdate).when(mockAuthenticatorManagementFacade).updateUserDefinedLocalAuthenticator(
                authenticatorConfig1, mockUpdatingAuthenticatorConfig, TEST_TENANT_ID);

        UserDefinedLocalAuthenticatorConfig updatedConfig = cacheBackedAuthenticatorMgtDAO
                .updateUserDefinedLocalAuthenticator(authenticatorConfig1, mockUpdatingAuthenticatorConfig,
                        TEST_TENANT_ID);

        Assert.assertNotNull(updatedConfig);
        Assert.assertEquals(updatedConfig, authenticatorForUpdate);

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(authenticatorConfig1.getName());
        Assert.assertNull(authenticatorCache.getValueFromCache(authenticatorCacheKey, TEST_TENANT_ID));

        UserDefinedLocalAuthenticatorsCacheKey localAuthenticatorsCacheKey =
                new UserDefinedLocalAuthenticatorsCacheKey(TEST_TENANT_ID);
        Assert.assertNull(userDefinedLocalAuthenticatorsCache.getValueFromCache(localAuthenticatorsCacheKey,
                TEST_TENANT_ID));
    }


    @Test(dependsOnMethods = "testUpdateUserDefinedLocalAuthenticator")
    public void testGetUserDefinedLocalAuthenticatorFromDB() throws AuthenticatorMgtException {

        doReturn(authenticatorForUpdate).when(mockAuthenticatorManagementFacade).getUserDefinedLocalAuthenticator(
                anyString(), anyInt());

        UserDefinedLocalAuthenticatorConfig fetchedAuthenticator = cacheBackedAuthenticatorMgtDAO
                .getUserDefinedLocalAuthenticator(authenticatorForUpdate.getName(), TEST_TENANT_ID);

        Assert.assertNotNull(fetchedAuthenticator);
        Assert.assertEquals(fetchedAuthenticator, authenticatorForUpdate);
        verify(mockAuthenticatorManagementFacade, times(1))
                .getUserDefinedLocalAuthenticator(authenticatorForUpdate.getName(), TEST_TENANT_ID);

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(authenticatorForUpdate.getName());
        UserDefinedLocalAuthenticatorConfig cachedAuthenticator = authenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);
        Assert.assertEquals(cachedAuthenticator, authenticatorForUpdate);
    }

    @Test(dependsOnMethods = "testGetUserDefinedLocalAuthenticatorFromDB")
    public void testDeleteUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        doNothing().when(mockAuthenticatorManagementFacade).deleteUserDefinedLocalAuthenticator(
                anyString(), any(UserDefinedLocalAuthenticatorConfig.class), anyInt());

        cacheBackedAuthenticatorMgtDAO.deleteUserDefinedLocalAuthenticator(
                authenticatorForUpdate.getName(), authenticatorForUpdate, TEST_TENANT_ID);

        verify(mockAuthenticatorManagementFacade, times(1)).deleteUserDefinedLocalAuthenticator(
                authenticatorForUpdate.getName(), authenticatorForUpdate, TEST_TENANT_ID);

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(authenticatorForUpdate.getName());
        Assert.assertNull(authenticatorCache.getValueFromCache(authenticatorCacheKey, TEST_TENANT_ID));

        UserDefinedLocalAuthenticatorsCacheKey localAuthenticatorsCacheKey =
                new UserDefinedLocalAuthenticatorsCacheKey(TEST_TENANT_ID);
        Assert.assertNull(userDefinedLocalAuthenticatorsCache.getValueFromCache(localAuthenticatorsCacheKey,
                TEST_TENANT_ID));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testAddSystemLocalAuthenticator() throws AuthenticatorMgtException {

        LocalAuthenticatorConfig mockAuthenticatorConfig = mock(LocalAuthenticatorConfig.class);
        doReturn(localAuthenticatorConfig1.getName()).when(mockAuthenticatorConfig).getName();
        doReturn(localAuthenticatorConfig1).when(mockAuthenticatorManagementFacade).addSystemLocalAuthenticator(
                mockAuthenticatorConfig, TEST_TENANT_ID);

        SystemDefinedAuthenticatorCacheKey authenticatorCacheKey =
                new SystemDefinedAuthenticatorCacheKey(localAuthenticatorConfig1.getName());
        Assert.assertNull(systemDefinedAuthenticatorCache.getValueFromCache(authenticatorCacheKey, TEST_TENANT_ID));

        LocalAuthenticatorConfig createdAuthenticator = cacheBackedAuthenticatorMgtDAO
                .addSystemLocalAuthenticator(mockAuthenticatorConfig, TEST_TENANT_ID);

        Assert.assertNotNull(createdAuthenticator);
        Assert.assertEquals(createdAuthenticator.getName(), localAuthenticatorConfig1.getName());
        Assert.assertEquals(createdAuthenticator.getDisplayName(), localAuthenticatorConfig1.getDisplayName());
        Assert.assertEquals(createdAuthenticator.isEnabled(), localAuthenticatorConfig1.isEnabled());
        Assert.assertEquals(createdAuthenticator.getDefinedByType(), localAuthenticatorConfig1.getDefinedByType());

        verify(mockAuthenticatorManagementFacade, times(1)).addSystemLocalAuthenticator(
                mockAuthenticatorConfig, TEST_TENANT_ID);

        LocalAuthenticatorConfig cachedAuthenticator = systemDefinedAuthenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);
        Assert.assertEquals(cachedAuthenticator, localAuthenticatorConfig1);

        SystemDefinedAuthenticatorCacheKey systemAuthenticatorCacheKey =
                new SystemDefinedAuthenticatorCacheKey(localAuthenticatorConfig1.getName());
        Assert.assertNull(systemDefinedAuthenticatorCache.getValueFromCache(
                systemAuthenticatorCacheKey, TEST_TENANT_ID));
    }

    @Test(dependsOnMethods = "testAddSystemLocalAuthenticator")
    public void testGetSystemLocalAuthenticatorFromCache() throws AuthenticatorMgtException{

        SystemDefinedAuthenticatorCacheKey authenticatorCacheKey =
                new SystemDefinedAuthenticatorCacheKey(localAuthenticatorConfig1.getName());
        LocalAuthenticatorConfig cachedAuthenticator = systemDefinedAuthenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);

        LocalAuthenticatorConfig fetchedAuthenticator = cacheBackedAuthenticatorMgtDAO
                .getSystemLocalAuthenticator(localAuthenticatorConfig1.getName(), TEST_TENANT_ID);

        Assert.assertNotNull(fetchedAuthenticator);
        Assert.assertEquals(fetchedAuthenticator, cachedAuthenticator);
        verify(mockAuthenticatorManagementFacade, never()).getSystemLocalAuthenticator(
                localAuthenticatorConfig1.getName(), TEST_TENANT_ID);
    }

    @Test(dependsOnMethods = "testGetSystemLocalAuthenticatorFromCache")
    public void testUpdateSystemLocalAuthenticatorAmrValue() throws AuthenticatorMgtException {

        LocalAuthenticatorConfig existingAuthenticatorConfig = mock(LocalAuthenticatorConfig.class);
        doReturn(localAuthenticatorForUpdate).when(mockAuthenticatorManagementFacade).
                updateSystemLocalAuthenticatorAmrValue(
                        localAuthenticatorConfig1, existingAuthenticatorConfig, TEST_TENANT_ID);

        LocalAuthenticatorConfig updatedConfig = cacheBackedAuthenticatorMgtDAO
                .updateSystemLocalAuthenticatorAmrValue(
                        existingAuthenticatorConfig, existingAuthenticatorConfig, TEST_TENANT_ID);

        Assert.assertNotNull(updatedConfig);
        Assert.assertEquals(updatedConfig, localAuthenticatorForUpdate);

        AuthenticatorCacheKey authenticatorCacheKey = new AuthenticatorCacheKey(localAuthenticatorConfig1.getName());
        Assert.assertNull(authenticatorCache.getValueFromCache(authenticatorCacheKey, TEST_TENANT_ID));

        SystemDefinedAuthenticatorCacheKey systemAuthenticatorCacheKey =
                new SystemDefinedAuthenticatorCacheKey(localAuthenticatorConfig1.getName());
        Assert.assertNull(systemDefinedAuthenticatorCache.getValueFromCache(
                systemAuthenticatorCacheKey, TEST_TENANT_ID));
    }

    @Test(dependsOnMethods = "testUpdateSystemLocalAuthenticatorAmrValue")
    public void testGetSystemLocalAuthenticatorFromDB() throws AuthenticatorMgtException {

        doReturn(localAuthenticatorForUpdate).when(mockAuthenticatorManagementFacade).getSystemLocalAuthenticator(
                anyString(), anyInt());

        LocalAuthenticatorConfig fetchedAuthenticator = cacheBackedAuthenticatorMgtDAO
                .getSystemLocalAuthenticator(localAuthenticatorForUpdate.getName(), TEST_TENANT_ID);

        Assert.assertNotNull(fetchedAuthenticator);
        Assert.assertEquals(fetchedAuthenticator, localAuthenticatorForUpdate);

        verify(mockAuthenticatorManagementFacade, times(1)).getSystemLocalAuthenticator(
                localAuthenticatorForUpdate.getName(), TEST_TENANT_ID);

        SystemDefinedAuthenticatorCacheKey authenticatorCacheKey = new SystemDefinedAuthenticatorCacheKey(
                localAuthenticatorForUpdate.getName());
        LocalAuthenticatorConfig cachedAuthenticator =  systemDefinedAuthenticatorCache.getValueFromCache(
                authenticatorCacheKey, TEST_TENANT_ID).getAuthenticatorConfig();
        Assert.assertNotNull(cachedAuthenticator);
        Assert.assertEquals(cachedAuthenticator, localAuthenticatorForUpdate);
    }
}
