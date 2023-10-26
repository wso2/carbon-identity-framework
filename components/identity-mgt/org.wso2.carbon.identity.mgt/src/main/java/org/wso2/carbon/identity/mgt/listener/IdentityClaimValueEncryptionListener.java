/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityClaimValueEncryptionListener extends AbstractIdentityUserOperationEventListener {

    private static final Log LOG = LogFactory.getLog(IdentityClaimValueEncryptionListener.class);

    private static final String CLAIM_VALUE = "ClaimValue";
    private static final String CLAIM_URI = "claimURI";
    private static final String TOTP_KEY = "CryptoService.TotpSecret";
    // List of claims related to TOTP.
    private static final List<String> CLAIMS_FOR_TOTP =
            new ArrayList<>(Arrays.asList("http://wso2.org/claims/identity/verifySecretkey",
                    "http://wso2.org/claims/identity/secretkey"));

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 13;
    }


    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        try {
            if (!isEnable() || userStoreManager == null) {
                return true;
            }
            updateClaimValues(claims, userStoreManager);
            return true;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            LOG.error("Error while retrieving tenant ID", e);
            return false;
        } catch (CryptoException e) {
            return false;
        }
    }

    @Override
    public boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
                                      String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return doPreAddUser(null, credential, roleList, claims, profile, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String
            profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        try {
            updateClaimValues(claims, userStoreManager);
        } catch (CryptoException e) {
            LOG.error("Error occurred while encrypting claim value", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return doPreSetUserClaimValues(null, claims, profileName, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        if (checkEnableEncryption(claimURI, userStoreManager)) {
            IdentityUtil.threadLocalProperties.get().remove(CLAIM_URI);
            IdentityUtil.threadLocalProperties.get().remove(CLAIM_VALUE);

            IdentityUtil.threadLocalProperties.get().put(CLAIM_URI, claimURI);
            IdentityUtil.threadLocalProperties.get().put(CLAIM_VALUE, claimValue);
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        return doPreSetUserClaimValue(null, claimURI, claimValue, profileName, userStoreManager);
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        try {
            String claimURI = (String) IdentityUtil.threadLocalProperties.get().get(CLAIM_URI);
            String claimValue = (String) IdentityUtil.threadLocalProperties.get().get(CLAIM_VALUE);
            if (StringUtils.isNotBlank(claimURI) && StringUtils.isNotBlank(claimValue)) {
                Map<String, String> claims = new HashMap<>();
                claims.put(claimURI, claimValue);
                userStoreManager.setUserClaimValues(userName, claims, null);
            }
            return true;
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(CLAIM_URI);
            IdentityUtil.threadLocalProperties.get().remove(CLAIM_VALUE);
        }
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        return doPostSetUserClaimValue(null, userStoreManager);
    }

    /**
     * Encrypt mapped claim values.
     *
     * @param claims           Claim values to be encrypted.
     * @param userStoreManager User store manager.
     * @throws UserStoreException
     */
    private void updateClaimValues(Map<String, String> claims, UserStoreManager userStoreManager)
            throws UserStoreException, CryptoException {

        for (Map.Entry<String, String> entry : claims.entrySet()) {
            String claimURI = entry.getKey();
            if (checkEnableEncryption(claimURI, userStoreManager)) {
                String claimValue = entry.getValue();
                try {
                    boolean isCustomKeyEnabled = CLAIMS_FOR_TOTP.contains(claimURI) &&
                            userStoreManager.getTenantId() == -1234;
                    claimValue = encryptClaimValue(claimValue, isCustomKeyEnabled);
                } catch (CryptoException e) {
                    LOG.error("Error occurred while encrypting claim value of claim " + claimURI, e);
                    throw new CryptoException("Error occurred while encrypting claim value of claim " + claimURI, e);
                }
                claims.put(claimURI, claimValue);
            }
        }
    }

    /**
     * Encrypt claim value.
     *
     * @param plainText text to be encrypted.
     * @return encrypted claim value.
     */
    private String encryptClaimValue(String plainText, boolean isCustomKeyEnabled) throws CryptoException {

        if (plainText.isEmpty()) {
            return plainText;
        }
        // Get custom key from server configuration.
        String customKey = null;
        if (isCustomKeyEnabled) {
            customKey = ServerConfiguration.getInstance().getFirstProperty(TOTP_KEY);
        }
        return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                plainText.getBytes(StandardCharsets.UTF_8), customKey);
    }

    /**
     * Check whether encryption is enabled for a given claim.
     *
     * @param claimURI         Claim URI.
     * @param userStoreManager User store manager.
     * @return true if encryption is enabled for the claim.
     * @throws UserStoreException
     */
    private boolean checkEnableEncryption(String claimURI, UserStoreManager userStoreManager)
            throws UserStoreException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());
        Map<String, String> claimProperties = new HashMap<>();

        if (claimURI.contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI_PREFIX)) {
            claimProperties = getClaimProperties(tenantDomain, claimURI);
        }
        return claimProperties.containsKey(IdentityMgtConstants.ENABLE_ENCRYPTION);
    }

    /**
     * Get claim properties of a claim in a given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param claimURI     Claim URI.
     * @return Properties of the claim.
     */
    private Map<String, String> getClaimProperties(String tenantDomain, String claimURI) {

        try {
            if (IdentityMgtServiceDataHolder.getClaimManagementService() == null) {
                LOG.error("ClaimManagementService is null");
                throw new ClaimMetadataException("ClaimManagementService is null");
            }
            List<LocalClaim> localClaims =
                    IdentityMgtServiceDataHolder.getClaimManagementService().getLocalClaims(tenantDomain);
            if (localClaims == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returned claim list from ClaimManagementService is null");
                }
                return Collections.emptyMap();
            }
            for (LocalClaim localClaim : localClaims) {
                if (StringUtils.equalsIgnoreCase(claimURI, localClaim.getClaimURI())) {
                    return localClaim.getClaimProperties();
                }
            }
        } catch (ClaimMetadataException e) {
            LOG.error("Error while retrieving local claim meta data.", e);
        }
        return Collections.emptyMap();
    }
}
