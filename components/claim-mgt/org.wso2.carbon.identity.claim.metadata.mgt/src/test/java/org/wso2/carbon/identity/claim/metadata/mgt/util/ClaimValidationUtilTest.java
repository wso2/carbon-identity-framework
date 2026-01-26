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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ClaimUniquenessScope;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for ClaimValidationUtil.
 */
public class ClaimValidationUtilTest {

    @Mock
    private IdentityClaimManagementServiceDataHolder dataHolder;

    @Mock
    private RealmService realmService;

    @Mock
    private UserRealm userRealm;

    @Mock
    private AbstractUserStoreManager userStoreManager;

    @Mock
    private org.wso2.carbon.user.api.ClaimManager claimManager;

    @Mock
    private Claim claim;

    @Mock
    private CarbonContext carbonContext;

    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderMockedStatic;
    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;

    private static final String TEST_CLAIM_URI = "http://wso2.org/claims/email";
    private static final String TEST_CLAIM_VALUE = "test@example.com";
    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final int TEST_TENANT_ID = -1234;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        dataHolderMockedStatic = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);

        dataHolderMockedStatic.when(IdentityClaimManagementServiceDataHolder::getInstance)
                .thenReturn(dataHolder);
        when(dataHolder.getRealmService()).thenReturn(realmService);

        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext)
                .thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);

        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                .thenReturn(TEST_TENANT_ID);
        when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        doReturn(claimManager).when(userStoreManager).getClaimManager();
    }

    @AfterMethod
    public void tearDown() {

        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
        if (carbonContextMockedStatic != null) {
            carbonContextMockedStatic.close();
        }
        if (identityTenantUtilMockedStatic != null) {
            identityTenantUtilMockedStatic.close();
        }
    }

    @DataProvider(name = "claimUniquenessScopeProvider")
    public Object[][] provideClaimUniquenessScopeData() {

        return new Object[][] {
                {null, ClaimUniquenessScope.NONE},
                {new HashMap<>(), ClaimUniquenessScope.NONE},
                {createPropertiesMap(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, "NONE"),
                        ClaimUniquenessScope.NONE},
                {createPropertiesMap(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, "WITHIN_USERSTORE"),
                        ClaimUniquenessScope.WITHIN_USERSTORE},
                {createPropertiesMap(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, "ACROSS_USERSTORES"),
                        ClaimUniquenessScope.ACROSS_USERSTORES},
                {createPropertiesMap(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, "INVALID_SCOPE"),
                        ClaimUniquenessScope.NONE},
                {createPropertiesMap(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true"),
                        ClaimUniquenessScope.ACROSS_USERSTORES},
                {createPropertiesMap(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "false"),
                        ClaimUniquenessScope.NONE},
        };
    }

    @Test(dataProvider = "claimUniquenessScopeProvider")
    public void testGetClaimUniquenessScope(Map<String, String> claimProperties,
                                            ClaimUniquenessScope expectedScope) {

        ClaimUniquenessScope result = ClaimValidationUtil.getClaimUniquenessScope(claimProperties);
        assertEquals(result, expectedScope);
    }

    @Test
    public void testGetClaimUniquenessScopeWithBothPropertiesSet() {

        Map<String, String> properties = new HashMap<>();
        properties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, "WITHIN_USERSTORE");
        properties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");

        ClaimUniquenessScope result = ClaimValidationUtil.getClaimUniquenessScope(properties);
        assertEquals(result, ClaimUniquenessScope.WITHIN_USERSTORE);
    }

    @DataProvider(name = "shouldValidateUniquenessProvider")
    public Object[][] provideShouldValidateUniquenessData() {

        return new Object[][] {
                {ClaimUniquenessScope.NONE, false},
                {ClaimUniquenessScope.WITHIN_USERSTORE, true},
                {ClaimUniquenessScope.ACROSS_USERSTORES, true},
        };
    }

    @Test(dataProvider = "shouldValidateUniquenessProvider")
    public void testShouldValidateUniqueness(ClaimUniquenessScope scope, boolean expectedResult) {

        boolean result = ClaimValidationUtil.shouldValidateUniqueness(scope);
        assertEquals(result, expectedResult);
    }

    @Test
    public void testIsClaimDuplicatedWhenUsersExist() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI)).thenReturn(claim);
        when(claim.isMultiValued()).thenReturn(false);
        when(userStoreManager.getUserList(eq(TEST_CLAIM_URI), eq(TEST_CLAIM_VALUE), isNull()))
                .thenReturn(new String[]{"user1", "user2"});

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertTrue(result);
    }

    @Test
    public void testIsClaimDuplicatedWhenNoUsersExist() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI)).thenReturn(claim);
        when(claim.isMultiValued()).thenReturn(false);
        when(userStoreManager.getUserList(eq(TEST_CLAIM_URI), eq(TEST_CLAIM_VALUE), isNull()))
                .thenReturn(new String[]{});

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertFalse(result);
    }

    @Test
    public void testIsClaimDuplicatedWhenUserListIsNull() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI)).thenReturn(claim);
        when(claim.isMultiValued()).thenReturn(false);
        when(userStoreManager.getUserList(eq(TEST_CLAIM_URI), eq(TEST_CLAIM_VALUE), isNull()))
                .thenReturn(null);

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertFalse(result);
    }

    @Test
    public void testIsClaimDuplicatedForMultiValuedClaim() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI)).thenReturn(claim);
        when(claim.isMultiValued()).thenReturn(true);

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertFalse(result);
    }

    @Test
    public void testIsClaimDuplicatedWhenClaimIsNull() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI)).thenReturn(null);
        when(userStoreManager.getUserList(eq(TEST_CLAIM_URI), eq(TEST_CLAIM_VALUE), isNull()))
                .thenReturn(new String[]{"user1"});

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertTrue(result);
    }

    @Test
    public void testIsClaimDuplicatedWhenClaimManagerThrowsException() throws Exception {

        when(claimManager.getClaim(TEST_CLAIM_URI))
                .thenThrow(new UserStoreException("Error retrieving claim"));
        when(userStoreManager.getUserList(eq(TEST_CLAIM_URI), eq(TEST_CLAIM_VALUE), isNull()))
                .thenReturn(new String[]{"user1"});

        boolean result = ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
        assertTrue(result);
    }

    @Test(expectedExceptions = org.wso2.carbon.user.core.UserStoreException.class)
    public void testIsClaimDuplicatedWhenRealmServiceIsNull() throws Exception {

        when(dataHolder.getRealmService()).thenReturn(null);
        ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
    }

    @Test(expectedExceptions = org.wso2.carbon.user.core.UserStoreException.class)
    public void testIsClaimDuplicatedWhenUserStoreExceptionOccurs() throws Exception {

        when(realmService.getTenantUserRealm(anyInt()))
                .thenThrow(new UserStoreException("Error accessing user realm"));
        ClaimValidationUtil.isClaimDuplicated(TEST_CLAIM_URI, TEST_CLAIM_VALUE);
    }

    /**
     * Helper method to create a properties map with a single entry.
     */
    private Map<String, String> createPropertiesMap(String key, String value) {
        Map<String, String> properties = new HashMap<>();
        properties.put(key, value);
        return properties;
    }
}