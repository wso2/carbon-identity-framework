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

package org.wso2.carbon.identity.mgt.listener;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.user.core.UserStoreManager;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests that {@link IdentityClaimValueEncryptionListener} never encrypts a claim value twice.
 */
public class IdentityClaimValueEncryptionListenerTest {

    private static final String SECRET_KEY_CLAIM = "http://wso2.org/claims/identity/secretkey";
    private static final String ENCRYPTED_VALUE = "already-encrypted-value";
    private static final String PLAIN_VALUE = "PLAINTEXTSECRET";
    private static final String TENANT_DOMAIN = "carbon.super";

    private IdentityClaimValueEncryptionListener listener;
    private UserStoreManager userStoreManager;
    private CryptoUtil cryptoUtil;
    private MockedStatic<CryptoUtil> cryptoUtilStatic;
    private MockedStatic<IdentityTenantUtil> tenantUtilStatic;

    @BeforeMethod
    public void setUp() throws Exception {

        cryptoUtil = mock(CryptoUtil.class);
        when(cryptoUtil.base64DecodeAndIsSelfContainedCipherText(anyString()))
                .thenAnswer(invocation -> ENCRYPTED_VALUE.equals(invocation.getArgument(0)));
        when(cryptoUtil.encryptAndBase64Encode(any(byte[].class), any()))
                .thenAnswer(invocation -> ENCRYPTED_VALUE + "(" +
                        new String((byte[]) invocation.getArgument(0), StandardCharsets.UTF_8) + ")");
        cryptoUtilStatic = mockStatic(CryptoUtil.class);
        cryptoUtilStatic.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(cryptoUtil);

        tenantUtilStatic = mockStatic(IdentityTenantUtil.class);
        tenantUtilStatic.when(() -> IdentityTenantUtil.getTenantDomain(1)).thenReturn(TENANT_DOMAIN);

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimURI()).thenReturn(SECRET_KEY_CLAIM);
        when(localClaim.getClaimProperties()).thenReturn(
                Collections.singletonMap(IdentityMgtConstants.ENABLE_ENCRYPTION, "true"));
        ClaimMetadataManagementService claimService = mock(ClaimMetadataManagementService.class);
        when(claimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(localClaim));
        IdentityMgtServiceDataHolder.setClaimManagementService(claimService);

        userStoreManager = mock(UserStoreManager.class);
        when(userStoreManager.getTenantId()).thenReturn(1);

        listener = spy(new IdentityClaimValueEncryptionListener());
        doReturn(true).when(listener).isEnable();
    }

    @AfterMethod
    public void tearDown() {

        cryptoUtilStatic.close();
        tenantUtilStatic.close();
        IdentityMgtServiceDataHolder.setClaimManagementService(null);
    }

    @Test
    public void testPlainValueIsEncryptedOnce() throws Exception {

        Map<String, String> claims = new HashMap<>();
        claims.put(SECRET_KEY_CLAIM, PLAIN_VALUE);

        listener.doPreSetUserClaimValues("user", claims, null, userStoreManager);

        assertEquals(claims.get(SECRET_KEY_CLAIM), ENCRYPTED_VALUE + "(" + PLAIN_VALUE + ")");
    }

    @Test
    public void testAlreadyEncryptedValueIsNotEncryptedAgain() throws Exception {

        Map<String, String> claims = new HashMap<>();
        claims.put(SECRET_KEY_CLAIM, ENCRYPTED_VALUE);

        listener.doPreSetUserClaimValues("user", claims, null, userStoreManager);

        assertEquals(claims.get(SECRET_KEY_CLAIM), ENCRYPTED_VALUE);
        verify(cryptoUtil, never()).encryptAndBase64Encode(any(byte[].class), any());
    }

    @Test
    public void testRepeatedInvocationOnSameMapEncryptsOnce() throws Exception {

        Map<String, String> claims = new HashMap<>();
        claims.put(SECRET_KEY_CLAIM, PLAIN_VALUE);

        listener.doPreSetUserClaimValues("user", claims, null, userStoreManager);
        String afterFirstPass = claims.get(SECRET_KEY_CLAIM);
        // The listener rewrites the caller's map in place, so a second write of the same map sees ciphertext.
        when(cryptoUtil.base64DecodeAndIsSelfContainedCipherText(afterFirstPass)).thenReturn(true);
        listener.doPreSetUserClaimValues("user", claims, null, userStoreManager);

        assertEquals(claims.get(SECRET_KEY_CLAIM), afterFirstPass);
    }
}
