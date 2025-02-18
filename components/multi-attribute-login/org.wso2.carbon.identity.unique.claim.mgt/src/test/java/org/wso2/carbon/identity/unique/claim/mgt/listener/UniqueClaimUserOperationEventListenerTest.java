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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes.ERROR_CODE_DUPLICATE_CLAIM_VALUE;

public class UniqueClaimUserOperationEventListenerTest {

    private UniqueClaimUserOperationEventListener uniqueClaimUserOperationEventListener;

    @Mock
    private UserStoreManager userStoreManager;

    @Mock
    private ClaimManager claimManager;

    @Mock
    private Claim claim;

    @BeforeMethod
    public void setUp() throws UserStoreException {

        MockitoAnnotations.initMocks(this);
        uniqueClaimUserOperationEventListener = spy(new UniqueClaimUserOperationEventListener());
        userStoreManager = mock(UserStoreManager.class);
        claimManager = mock(ClaimManager.class);
        claim = mock(Claim.class);

        when(userStoreManager.getClaimManager()).thenReturn(claimManager);
    }

    @DataProvider(name = "duplicateClaimDataProvider")
    public Object[][] duplicateClaimDataProvider() {

        Map<String, String> claimsOne = new HashMap<>();
        claimsOne.put("http://wso2.org/claims/mobile", "0711234567");
        claimsOne.put("http://wso2.org/claims/emailAddress", "sample@wso2.com");
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
        LocalClaim localClaimEmail = new LocalClaim("http://wso2.org/claims/emailAddress");
        localClaimEmail.setClaimProperties(new HashMap<String, String>() {{
            put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        }});
        localClaimMobile.setClaimProperties(new HashMap<String, String>() {{
            put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        }});
        localClaims.add(localClaimMobile);
        localClaims.add(localClaimEmail);
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(localClaims);

        Claim claimMobile = new Claim();
        claimMobile.setClaimUri("http://wso2.org/claims/mobile");
        claimMobile.setDisplayTag("Mobile");
        Claim claimEmail = new Claim();
        claimEmail.setClaimUri("http://wso2.org/claims/emailAddress");
        claimEmail.setDisplayTag("Email");
        when(userStoreManager.getClaimManager().getClaim("http://wso2.org/claims/mobile")).thenReturn(claimMobile);
        when(userStoreManager.getClaimManager().getClaim("http://wso2.org/claims/emailAddress")).thenReturn(claimEmail);
        doReturn(true).when(uniqueClaimUserOperationEventListener).isEnable();

        try {
            uniqueClaimUserOperationEventListener.doPreSetUserClaimValues(userName, claims, profile, userStoreManager);
        } catch (UserStoreClientException e) {
            // Assert error code from PolicyViolationException
            Assert.assertNotNull(e.getCause());
            PolicyViolationException policyViolationException = (PolicyViolationException) e.getCause();
            Assert.assertEquals(policyViolationException.getErrorCode(), ERROR_CODE_DUPLICATE_CLAIM_VALUE);
            throw e;
        }
    }
}
