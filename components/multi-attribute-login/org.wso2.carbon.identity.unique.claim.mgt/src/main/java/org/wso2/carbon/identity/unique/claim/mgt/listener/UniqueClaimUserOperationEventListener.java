/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.policy.PolicyViolationException;
import org.wso2.carbon.identity.unique.claim.mgt.internal.UniqueClaimUserOperationDataHolder;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A userstore operation event listener to keep the uniqueness of a given set of claims.
 */
public class UniqueClaimUserOperationEventListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UniqueClaimUserOperationEventListener.class);

    private static final String IS_UNIQUE_CLAIM = "isUnique";
    private static final String SCOPE_WITHIN_USERSTORE = "ScopeWithinUserstore";
    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 2;
    }

    @Override
    public boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserOperationEventListener.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return false;
        }

        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return false;
        }
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        checkUsernameUniqueness(userName, userStoreManager);
        checkClaimUniqueness(userName, claims, profile, userStoreManager, credential);
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profile,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        try {
            String tenantDomain = getTenantDomain(userStoreManager);
            if (isUniqueClaim(claimURI, tenantDomain)) {
                return !isClaimDuplicated(userName, claimURI, claimValue, profile, userStoreManager);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException | ClaimMetadataException e) {
            log.error("Error while retrieving details. " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profile,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        checkClaimUniqueness(userName, claims, profile, userStoreManager, null);
        return true;
    }

    private void checkClaimUniqueness(String username, Map<String, String> claims, String profile,
                                      UserStoreManager userStoreManager, Object credential) throws UserStoreException {

        String errorMessage = StringUtils.EMPTY;
        String tenantDomain = getTenantDomain(userStoreManager);
        List<String> duplicateClaim = new ArrayList<>();
        Claim claimObject = null;
        for (Map.Entry<String, String> claim : claims.entrySet()) {
            try {
                if (isUniqueClaim(claim.getKey(), tenantDomain)) {
                    try {
                        claimObject = userStoreManager.getClaimManager().getClaim(claim.getKey());
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        log.error("Error while getting claim from claimUri: " + claim.getKey() + ".", e);
                    }
                    if (claimObject == null) {
                        continue;
                    }
                    // checks whether allowed login identifiers are equal to the password
                    if (credential != null && (credential.toString()).equals(claim.getValue())) {
                        errorMessage = "Password can not be equal to the value defined for " +
                                claimObject.getDisplayTag() + "!";
                        throw new UserStoreException(errorMessage, new PolicyViolationException(errorMessage));
                    }
                    if (isClaimDuplicated(username, claim.getKey(), claim.getValue(), profile, userStoreManager)) {
                        String displayTag = claimObject.getDisplayTag();
                        if (StringUtils.isBlank(displayTag)) {
                            displayTag = claim.getKey();
                        }
                        duplicateClaim.add(displayTag);
                    }
                }
            } catch (ClaimMetadataException e) {
                log.error("Error while getting claim metadata for claimUri : " + claim.getKey() + ".", e);
            }
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            throw new UserStoreException(errorMessage,
                    new PolicyViolationException(errorMessage));
        }
        if (duplicateClaim.size() == 0) {
            return;
        } else if (duplicateClaim.size() == 1) {
            errorMessage = "The value defined for " + duplicateClaim.get(0) + " is already in use by different user!";
        } else {
            String claimList = String.join(", ", duplicateClaim);
            errorMessage = "The values defined for " + claimList + " are already in use by a different users!";
        }
        throw new UserStoreClientException(errorMessage, new PolicyViolationException(errorMessage));
    }

    private boolean isClaimDuplicated(String username, String claimUri, String claimValue, String profile,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        String domainName = userStoreManager.getRealmConfiguration().getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        String[] userList;
        // Get UserStoreManager from realm since the received one might be for a secondary user store
        UserStoreManager userStoreMgrFromRealm = getUserstoreManager(userStoreManager.getTenantId());
        if (isScopeWithinUserstore()) {
            String claimValueWithDomain = domainName + UserCoreConstants.DOMAIN_SEPARATOR + claimValue;
            userList = userStoreMgrFromRealm.getUserList(claimUri, claimValueWithDomain, profile);
        } else {
            userList = userStoreMgrFromRealm.getUserList(claimUri, claimValue, profile);
        }

        if (userList.length == 1) {
            String usernameWithUserStoreDomain = UserCoreUtil.addDomainToName(username, domainName);
            if (usernameWithUserStoreDomain.equalsIgnoreCase(userList[0])) {
                return false;
            }
        } else if (userList.length == 0) {
            return false;
        }
        return true;
    }

    public boolean isUniqueClaim(String claimUrI, String tenantDomain) throws ClaimMetadataException {

        List<LocalClaim> localClaims = UniqueClaimUserOperationDataHolder.getInstance().
                getClaimMetadataManagementService().getLocalClaims(tenantDomain);
        for (LocalClaim localClaim : localClaims) {
            if (localClaim.getClaimURI().equals(claimUrI) &&
                    Boolean.parseBoolean(localClaim.getClaimProperty(IS_UNIQUE_CLAIM))) {
                return true;
            }
        }
        return false;
    }

    private void checkUsernameUniqueness(String username, UserStoreManager userStoreManager) throws UserStoreException {

        String errorMessage;
        String tenantDomain = getTenantDomain(userStoreManager);

        try {
            if (isUniqueClaim(USERNAME_CLAIM, tenantDomain) &&
                    isClaimDuplicated(username, USERNAME_CLAIM, username, null, userStoreManager)) {

                errorMessage = "Username " + username + " is already in use by a different user!";
                throw new UserStoreException(errorMessage, new PolicyViolationException(errorMessage));
            }
        } catch (ClaimMetadataException e) {
            log.error("Error while getting claim metadata for claimUri : " + USERNAME_CLAIM + ".", e);
        }
    }

    private String getTenantDomain(UserStoreManager userStoreManager) throws UserStoreException {

        try {
            return UniqueClaimUserOperationDataHolder.getInstance().getRealmService().getTenantManager().
                    getDomain(userStoreManager.getTenantId());

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while retrieving tenant domain.", e);
        }
    }

    /**
     * Returns whether it should check the claim uniqueness within the userstore only.
     */
    private boolean isScopeWithinUserstore() {

        String scopeWithinUserstore =
                (String) IdentityUtil.readEventListenerProperty(UserOperationEventListener.class.getName(),
                        UniqueClaimUserOperationEventListener.class.getName()).getProperties().get(
                        SCOPE_WITHIN_USERSTORE);
        return StringUtils.isNotEmpty(scopeWithinUserstore) && Boolean.parseBoolean(scopeWithinUserstore);
    }

    private UserStoreManager getUserstoreManager(int tenantId) throws UserStoreException {

        UserRealm userRealm = null;
        try {
            userRealm = UniqueClaimUserOperationDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            if (userRealm != null) {
                return  (UserStoreManager) userRealm.getUserStoreManager();
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
        throw new UserStoreException("User realm is null for the tenant " + tenantId + ".");
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        Claim[] claims = userStoreManager.getUserClaimValues(userName, null);
        Map<String, String> claimMap = new HashMap<>();
        for (Claim claim : claims) {
            claimMap.put(claim.getClaimUri(), claim.getValue());
        }
        checkClaimUniqueness(userName, claimMap, null, userStoreManager, newCredential);
        return true;
    }
}
