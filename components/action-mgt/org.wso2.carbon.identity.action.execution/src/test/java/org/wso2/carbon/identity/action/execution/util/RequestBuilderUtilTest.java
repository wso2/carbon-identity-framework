/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.util;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.util.RequestBuilderUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link RequestBuilderUtil}.
 */
public class RequestBuilderUtilTest {

    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final int TEST_TENANT_ID = -1234;
    private static final String TEST_USER_ID = "test-user-id";
    private static final String CLAIM_URI_EMAIL = "http://wso2.org/claims/emailaddress";
    private static final String CLAIM_URI_FIRST_NAME = "http://wso2.org/claims/givenname";
    private static final String CLAIM_URI_LAST_NAME = "http://wso2.org/claims/lastname";

    @Mock
    private RealmService realmService;

    @Mock
    private TenantManager tenantManager;

    @Mock
    private UserRealm userRealm;

    @Mock
    private UniqueIDUserStoreManager uniqueIDUserStoreManager;

    private AutoCloseable closeable;

    @BeforeMethod
    public void setUp() throws Exception {

        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeable.close();
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Realm service is unavailable.")
    public void testGetUserStoreManagerWithNullRealmService() throws ActionExecutionRequestBuilderException {

        RequestBuilderUtil.getUserStoreManager(TEST_TENANT_DOMAIN, null);
    }

    @Test
    public void testGetUserStoreManagerSuccessful() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        UniqueIDUserStoreManager result = RequestBuilderUtil.getUserStoreManager(TEST_TENANT_DOMAIN, realmService);
        assertNotNull(result);
        assertEquals(result, uniqueIDUserStoreManager);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "User realm is not available for tenant: .*")
    public void testGetUserStoreManagerWithNullUserRealm() throws Exception {

        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);
        when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(null);

        RequestBuilderUtil.getUserStoreManager(TEST_TENANT_DOMAIN, realmService);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp =
                    "User store manager is not an instance of UniqueIDUserStoreManager for tenant: .*")
    public void testGetUserStoreManagerWithNonUniqueIDUserStoreManager() throws Exception {

        UserStoreManager nonUniqueManager = org.mockito.Mockito.mock(UserStoreManager.class);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);
        when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(nonUniqueManager);

        RequestBuilderUtil.getUserStoreManager(TEST_TENANT_DOMAIN, realmService);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while loading user store manager for tenant: .*")
    public void testGetUserStoreManagerWithUserStoreException() throws Exception {

        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenThrow(new UserStoreException("Test error"));

        RequestBuilderUtil.getUserStoreManager(TEST_TENANT_DOMAIN, realmService);
    }

    @DataProvider(name = "emptyClaimRequestsProvider")
    public Object[][] emptyClaimRequestsProvider() {

        return new Object[][]{
                {null},
                {Collections.emptyList()}
        };
    }

    @Test(dataProvider = "emptyClaimRequestsProvider")
    public void testGetClaimValuesWithEmptyOrNullRequestedClaims(List<String> requestedClaims) throws Exception {

        Map<String, String> result =
                RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetClaimValuesSuccessful() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        List<String> requestedClaims = Arrays.asList(CLAIM_URI_EMAIL, CLAIM_URI_FIRST_NAME);

        Map<String, String> mockClaimValues = new HashMap<>();
        mockClaimValues.put(CLAIM_URI_EMAIL, "test@wso2.com");
        mockClaimValues.put(CLAIM_URI_FIRST_NAME, "sampleFirstName");

        when(uniqueIDUserStoreManager.getUserClaimValuesWithID(
                eq(TEST_USER_ID), any(String[].class), eq(UserCoreConstants.DEFAULT_PROFILE)))
                .thenReturn(mockClaimValues);

        Map<String, String> result =
                RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);

        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertEquals(result.get(CLAIM_URI_EMAIL), "test@wso2.com");
        assertEquals(result.get(CLAIM_URI_FIRST_NAME), "sampleFirstName");
    }

    @Test
    public void testGetClaimValuesFiltersOnlyRequestedClaims() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        List<String> requestedClaims = Collections.singletonList(CLAIM_URI_EMAIL);

        Map<String, String> mockClaimValues = new HashMap<>();
        mockClaimValues.put(CLAIM_URI_EMAIL, "test@wso2.com");
        mockClaimValues.put(CLAIM_URI_FIRST_NAME, "sampleFirstName");
        mockClaimValues.put(CLAIM_URI_LAST_NAME, "sampleLastName");

        when(uniqueIDUserStoreManager.getUserClaimValuesWithID(
                eq(TEST_USER_ID), any(String[].class), eq(UserCoreConstants.DEFAULT_PROFILE)))
                .thenReturn(mockClaimValues);

        Map<String, String> result =
                RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(CLAIM_URI_EMAIL), "test@wso2.com");
    }

    @Test
    public void testGetClaimValuesWithNullClaimValuesFromStore() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        List<String> requestedClaims = Arrays.asList(CLAIM_URI_EMAIL, CLAIM_URI_FIRST_NAME);

        when(uniqueIDUserStoreManager.getUserClaimValuesWithID(
                eq(TEST_USER_ID), any(String[].class), eq(UserCoreConstants.DEFAULT_PROFILE)))
                .thenReturn(null);

        Map<String, String> result =
                RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetClaimValuesWithPartialMatch() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        List<String> requestedClaims = Arrays.asList(CLAIM_URI_EMAIL, CLAIM_URI_FIRST_NAME, CLAIM_URI_LAST_NAME);

        Map<String, String> mockClaimValues = new HashMap<>();
        mockClaimValues.put(CLAIM_URI_EMAIL, "test@wso2.com");

        when(uniqueIDUserStoreManager.getUserClaimValuesWithID(
                eq(TEST_USER_ID), any(String[].class), eq(UserCoreConstants.DEFAULT_PROFILE)))
                .thenReturn(mockClaimValues);

        Map<String, String> result =
                RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(CLAIM_URI_EMAIL), "test@wso2.com");
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Failed to retrieve user claims from user store.")
    public void testGetClaimValuesWithUserStoreException() throws Exception {

        mockRealmServiceToReturnUserStoreManager();

        List<String> requestedClaims = Arrays.asList(CLAIM_URI_EMAIL, CLAIM_URI_FIRST_NAME);

        when(uniqueIDUserStoreManager.getUserClaimValuesWithID(
                eq(TEST_USER_ID), any(String[].class), eq(UserCoreConstants.DEFAULT_PROFILE)))
                .thenThrow(new org.wso2.carbon.user.core.UserStoreException("Test error"));

        RequestBuilderUtil.getClaimValues(TEST_USER_ID, requestedClaims, TEST_TENANT_DOMAIN, realmService);
    }

    private void mockRealmServiceToReturnUserStoreManager() throws Exception {

        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);
        when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(uniqueIDUserStoreManager);
    }
}
