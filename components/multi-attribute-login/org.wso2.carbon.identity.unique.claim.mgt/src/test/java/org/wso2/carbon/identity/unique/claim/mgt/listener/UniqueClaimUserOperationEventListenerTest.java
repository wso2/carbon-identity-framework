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

package org.wso2.carbon.identity.unique.claim.mgt.listener;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.policy.PolicyViolationException;
import org.wso2.carbon.identity.unique.claim.mgt.internal.UniqueClaimUserOperationDataHolder;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR;
import static org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes.ERROR_CODE_DUPLICATE_CLAIM_VALUE;

public class UniqueClaimUserOperationEventListenerTest {

    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String EMAIL_ADDRESSES_CLAIM_URI = "http://wso2.org/claims/emailaddresses";

    private UniqueClaimUserOperationEventListener uniqueClaimUserOperationEventListener;

    @Mock
    private UserStoreManager userStoreManager;

    @Mock
    private ClaimManager claimManager;

    @Mock
    private Claim claim;

    @InjectMocks
    private UniqueClaimUserOperationEventListener listener;

    @Mock
    private ClaimMetadataManagementService claimMetadataManagementService;

    @Mock
    private RealmService realmService;

    @Mock
    private TenantManager tenantManager;

    @Mock
    private IdentityEventListenerConfig mockIdentityEventListenerConfig;

    private MockedStatic<IdentityUtil> identityUtilMock;


    @BeforeMethod
    public void setUp() throws UserStoreException, ClaimMetadataException {

        MockitoAnnotations.initMocks(this);
        uniqueClaimUserOperationEventListener = spy(new UniqueClaimUserOperationEventListener());
        userStoreManager = mock(UserStoreManager.class);
        claimManager = mock(ClaimManager.class);
        claim = mock(Claim.class);

        when(userStoreManager.getClaimManager()).thenReturn(claimManager);

        identityUtilMock = mockStatic(IdentityUtil.class);
        claimManager = mock(ClaimManager.class);

        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getDomain(anyInt())).thenReturn("carbon.super");
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(realmService);
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(realmService);
        UniqueClaimUserOperationDataHolder.getInstance().setClaimMetadataManagementService(
                claimMetadataManagementService);
    }

    @DataProvider(name = "duplicateClaimDataProvider")
    public Object[][] duplicateClaimDataProvider() {

        Map<String, String> claimsOne = new HashMap<>();
        claimsOne.put("http://wso2.org/claims/mobile", "0711234567");
        claimsOne.put(EMAIL_CLAIM_URI, "sample@wso2.com");
        claimsOne.put(EMAIL_ADDRESSES_CLAIM_URI, "sample@wso2.com,sample1@wso2.com");
        return new Object[][]{
                {"testUser", claimsOne, "default"}
        };
    }

    @Test(dataProvider = "duplicateClaimDataProvider", expectedExceptions = UserStoreClientException.class)
    public void testDuplicateClaimThrowsException(String userName, Map<String, String> claims, String profile)
            throws UserStoreException, ClaimMetadataException {

        uniqueClaimUserOperationEventListener = spy(new UniqueClaimUserOperationEventListener());

        ClaimMetadataManagementService claimMetadataManagementService = mock(ClaimMetadataManagementService.class);

        UniqueClaimUserOperationDataHolder dataHolder = mock(UniqueClaimUserOperationDataHolder.class);
        when(dataHolder.getClaimMetadataManagementService()).thenReturn(claimMetadataManagementService);
        when(dataHolder.getRealmService()).thenReturn(mock(RealmService.class));

        mockStatic(UniqueClaimUserOperationDataHolder.class);
        when(UniqueClaimUserOperationDataHolder.getInstance()).thenReturn(dataHolder);

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);

        when(realmConfiguration.getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)).thenReturn("carbon.super");

        when(userStoreManager.getTenantId()).thenReturn(-1234);

        RealmService realmService = dataHolder.getRealmService();
        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        TenantManager tenantManager = mock(TenantManager.class);
        when(realmService.getTenantManager()).thenReturn(tenantManager);

        when(userStoreManager.getUserList(anyString(), anyString(), anyString())).thenReturn(new String[]{"testUser"});
        when(userStoreManager.getTenantId()).thenReturn(-1234);
        when(tenantManager.getDomain(-1234)).thenReturn("carbon.super");

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaimMobile = new LocalClaim("http://wso2.org/claims/mobile");
        LocalClaim localClaimEmail = new LocalClaim(EMAIL_CLAIM_URI);
        LocalClaim localClaimEmails = new LocalClaim(EMAIL_ADDRESSES_CLAIM_URI);

        localClaimEmail.setClaimProperties(new HashMap<String, String>() {{
            put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        }});
        localClaimMobile.setClaimProperties(new HashMap<String, String>() {{
            put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        }});
        localClaimEmails.setClaimProperties(new HashMap<String, String>() {{
            put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        }});
        localClaims.add(localClaimMobile);
        localClaims.add(localClaimEmail);
        localClaims.add(localClaimEmails);
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(localClaims);

        Claim claimMobile = new Claim();
        claimMobile.setClaimUri("http://wso2.org/claims/mobile");
        claimMobile.setDisplayTag("Mobile");
        Claim claimEmail = new Claim();
        claimEmail.setClaimUri(EMAIL_CLAIM_URI);
        claimEmail.setDisplayTag("Email");
        Claim claimEmails = new Claim();
        claimEmails.setClaimUri(EMAIL_ADDRESSES_CLAIM_URI);
        claimEmails.setDisplayTag("Email Addresses");
        claimEmails.setMultiValued(true);
        when(userStoreManager.getClaimManager().getClaim("http://wso2.org/claims/mobile")).thenReturn(claimMobile);
        when(userStoreManager.getClaimManager().getClaim(EMAIL_CLAIM_URI)).thenReturn(claimEmail);
        when(userStoreManager.getClaimManager().getClaim(EMAIL_ADDRESSES_CLAIM_URI))
                .thenReturn(claimEmails);
        when(userStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(userStoreManager);
        when(realmConfiguration.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR)).thenReturn(",");

        doReturn(true).when(uniqueClaimUserOperationEventListener).isEnable();

        try {
            uniqueClaimUserOperationEventListener.doPreSetUserClaimValues(userName, claims, profile, userStoreManager);
        } catch (UserStoreClientException e) {
            // Assert error code from PolicyViolationException
            Assert.assertNotNull(e.getCause());
            PolicyViolationException policyViolationException = (PolicyViolationException) e.getCause();
            Assert.assertEquals(policyViolationException.getErrorCode(), ERROR_CODE_DUPLICATE_CLAIM_VALUE);
            Assert.assertTrue(e.getMessage().contains(claimEmail.getDisplayTag()), e.getMessage());
            Assert.assertTrue(e.getMessage().contains(claimMobile.getDisplayTag()), e.getMessage());
            Assert.assertTrue(e.getMessage().contains(claimEmails.getDisplayTag()), e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCheckClaimUniquenessWithPasswordPolicyViolation() throws UserStoreException,
            NoSuchMethodException, IllegalAccessException, ClaimMetadataException {

        mockInitForCheckClaimUniqueness();
        String username = "testUser";
        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_CLAIM_URI, "test@example.com");
        String profile = "default";
        Object credential = "test@example.com";

        // Mock the necessary methods.
        when(userStoreManager.getTenantId()).thenReturn(1);
        when(userStoreManager.getClaimManager()).thenReturn(claimManager);
        Claim emailClaimMetaDate = new Claim();
        emailClaimMetaDate.setClaimUri(EMAIL_CLAIM_URI);
        emailClaimMetaDate.setDisplayTag("Email Address");
        when(claimManager.getClaim(anyString())).thenReturn(emailClaimMetaDate);

        java.lang.reflect.Method method = UniqueClaimUserOperationEventListener.class.getDeclaredMethod(
                "checkClaimUniqueness", String.class, Map.class, String.class, UserStoreManager.class, Object.class);
        method.setAccessible(true);
        try {
            method.invoke(listener, username, claims, profile, userStoreManager, credential);
        } catch (InvocationTargetException e) {
            assertEquals(e.getTargetException().getClass(), org.wso2.carbon.user.core.UserStoreException.class);
            assertTrue(e.getTargetException().getMessage().contains("Password cannot be equal to the value defined " +
                    "for Email Address!"));
        }
    }

    @Test
    public void testValidatePasswordNotEqualToClaims() throws Exception {

        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_CLAIM_URI, "test@example.com");
        Object newCredential = "Wso2@test";

        // Mock the necessary methods.
        when(userStoreManager.getTenantId()).thenReturn(1);
        when(userStoreManager.getClaimManager()).thenReturn(claimManager);
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        emailClaim.setDisplayTag("Email Address");
        when(claimManager.getClaim(anyString())).thenReturn(emailClaim);

        // Use reflection to invoke the private method.
        java.lang.reflect.Method method = UniqueClaimUserOperationEventListener.class.getDeclaredMethod(
                "validatePasswordNotEqualToClaims", Map.class, UserStoreManager.class, Object.class);
        method.setAccessible(true);

        try {
            // This shouldn't throw any exception. Shouldn't violate the policy.
            method.invoke(listener, claims, userStoreManager, newCredential);
        } catch (Exception e) {
            Assert.fail("Method threw an exception: " + e.getMessage());
        }
    }

    @DataProvider(name = "duplicateClaimedUserProvider")
    public Object[][] duplicateClaimedUserProvider() {

        return new Object[][]{
                {new String[]{"testUser", "testUser2"}, false},
                {new String[]{"testUser2", "testUser"}, true},
                {new String[]{"testUser"}, false},
                {new String[]{"testUser2"}, false},
                {new String[]{"testUser", "testUser2", "testUser3"}, false},
                {new String[]{"testUser3", "testUser2", "testUser"}, true},
                {new String[]{"testUser3", "testUser", "testUser2"}, true},
        };
    }

    @Test(dataProvider = "duplicateClaimedUserProvider")
    public void testDoPostAddUserWithDuplicateClaims(String[] userList, boolean isUserDeleted)
            throws UserStoreException, ClaimMetadataException {

        mockInitForCheckClaimUniqueness();
        String userName = "testUser";
        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_CLAIM_URI, "test@example.com");
        String profile = "default";
        String[] roleList = {"admin"};
        Object credential = "test@example.com";

        uniqueClaimUserOperationEventListener = spy(new UniqueClaimUserOperationEventListener());
        doReturn(true).when(uniqueClaimUserOperationEventListener).isEnable();

        Claim claimEmail = new Claim();
        claimEmail.setClaimUri(EMAIL_CLAIM_URI);
        claimEmail.setDisplayTag("Email");
        when(userStoreManager.getClaimManager().getClaim(EMAIL_CLAIM_URI)).thenReturn(claimEmail);

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("PRIMARY");
        when(userStoreManager.getTenantId()).thenReturn(-1234);

        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.getUserList(EMAIL_CLAIM_URI, "PRIMARY/test@example.com", profile))
                .thenReturn(userList);
        doNothing().when(userStoreManager).deleteUser(userName);

        try {
            uniqueClaimUserOperationEventListener
                    .doPostAddUser(userName, credential, roleList, claims, profile, userStoreManager);

            if (isUserDeleted) {
                verify(userStoreManager, times(1)).deleteUser(userName);
            }
        } catch (UserStoreException e) {
            if (isUserDeleted) {
                assertEquals("The value defined for Email is already in use by a different user!",
                        e.getMessage());
            }
        }
    }

    @Test(dataProvider = "duplicateClaimedUserProvider")
    public void testDoPostAddUserWithMultiValuedDuplicateClaims(String[] userList, boolean isUserDeleted)
            throws UserStoreException, ClaimMetadataException {

        mockInitForCheckClaimUniqueness();
        String userName = "testUser";
        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_ADDRESSES_CLAIM_URI, "test@example.com,test2@example.com");
        String profile = "default";
        String[] roleList = {"admin"};
        Object credential = "test@example.com";

        uniqueClaimUserOperationEventListener = spy(new UniqueClaimUserOperationEventListener());
        doReturn(true).when(uniqueClaimUserOperationEventListener).isEnable();

        Claim claimEmail = new Claim();
        claimEmail.setClaimUri(EMAIL_ADDRESSES_CLAIM_URI);
        claimEmail.setDisplayTag("Email");
        claimEmail.setMultiValued(true);
        when(userStoreManager.getClaimManager().getClaim(EMAIL_ADDRESSES_CLAIM_URI)).thenReturn(claimEmail);

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("PRIMARY");
        when(userStoreManager.getTenantId()).thenReturn(-1234);

        when(userStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(userStoreManager);
        when(realmConfiguration.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR)).thenReturn(",");

        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.getUserList(EMAIL_ADDRESSES_CLAIM_URI, "PRIMARY/test@example.com", profile))
                .thenReturn(new String[]{});
        when(userStoreManager.getUserList(EMAIL_ADDRESSES_CLAIM_URI, "PRIMARY/test2@example.com", profile))
                .thenReturn(userList);
        doNothing().when(userStoreManager).deleteUser(userName);

        try {
            uniqueClaimUserOperationEventListener
                    .doPostAddUser(userName, credential, roleList, claims, profile, userStoreManager);

            if (isUserDeleted) {
                verify(userStoreManager, times(1)).deleteUser(userName);
            }
        } catch (UserStoreException e) {
            if (isUserDeleted) {
                assertEquals("The value defined for Email is already in use by a different user!",
                        e.getMessage());
            }
        }
    }

    private void mockInitForCheckClaimUniqueness() throws ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim emailClaim = new LocalClaim(EMAIL_CLAIM_URI);
        emailClaim.setClaimProperty("isUnique", "true");
        localClaims.add(emailClaim);
        LocalClaim emailAddressesClaim = new LocalClaim(EMAIL_ADDRESSES_CLAIM_URI);
        emailAddressesClaim.setClaimProperty("isUnique", "true");
        localClaims.add(emailAddressesClaim);
        localClaims.add(new LocalClaim("http://wso2.org/claims/username"));
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(localClaims);
        identityUtilMock.when(() -> IdentityUtil.readEventListenerProperty(any(), any())).thenReturn(
                mockIdentityEventListenerConfig);
        Properties properties = new Properties();
        properties.put("ScopeWithinUserstore", "true");
        when(mockIdentityEventListenerConfig.getProperties()).thenReturn(properties);
    }

    @AfterMethod
    public void tearDown() {

        if (identityUtilMock != null) {
            identityUtilMock.close();
        }
    }
}
