/*
 * Copyright (c) 2021-2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR;
import static org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes.ERROR_CODE_DUPLICATE_CLAIM_VALUE;
import static org.wso2.carbon.identity.unique.claim.mgt.constants.UniqueClaimConstants.ErrorMessages.ERROR_CODE_DUPLICATE_MULTIPLE_CLAIMS;
import static org.wso2.carbon.identity.unique.claim.mgt.constants.UniqueClaimConstants.ErrorMessages.ERROR_CODE_DUPLICATE_SINGLE_CLAIM;

/**
 * A userstore operation event listener to keep the uniqueness of a given set of claims.
 */
public class UniqueClaimUserOperationEventListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UniqueClaimUserOperationEventListener.class);

    private static final String IS_UNIQUE_CLAIM = "isUnique";
    private static final String SCOPE_WITHIN_USERSTORE = "ScopeWithinUserstore";
    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    private static final String CREATED_TIME_CLAIM = "http://wso2.org/claims/created";

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
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                 String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        try {
            checkClaimUniqueness(userName, claims, profile, userStoreManager, null);
        } catch (UserStoreClientException e) {
            log.warn("Rolling back user creation: " + e.getMessage());
            userStoreManager.deleteUser(userName);
            throw e;
        }
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
            ClaimConstants.ClaimUniquenessScope uniquenessScope = getClaimUniquenessScope(claimURI, tenantDomain);
            if (shouldValidateUniqueness(uniquenessScope)) {
                return !isClaimDuplicated(userName, claimURI, claimValue, profile, userStoreManager, uniquenessScope);
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

    /**
     * Validates that user claims are unique and do not conflict with existing users' attributes.
     * Also ensures that the password is not used as an attribute value.
     *
     * @param username         The username of the user whose claims are being validated.
     * @param claims           A map of claim URIs and their respective values to be validated.
     * @param profile          The profile name associated with the claims.
     * @param userStoreManager The user store manager responsible for handling user attributes.
     * @param credential       The user's password or authentication credential.
     * @throws UserStoreException If a claim value is not unique or if the password matches a claim value.
     */
    private void checkClaimUniqueness(String username, Map<String, String> claims, String profile,
                                      UserStoreManager userStoreManager, Object credential) throws UserStoreException {

        List<String> duplicateClaims = new ArrayList<>();
        processClaims(username, claims, profile, userStoreManager, credential, true, duplicateClaims);

        if (!duplicateClaims.isEmpty()) {
            throwDuplicateClaimException(duplicateClaims);
        }
    }

    /**
     * Ensures that the provided password is not equal to any of the claim values.
     *
     * @param claims           A map of claim URIs and their respective values to be processed.
     * @param userStoreManager The user store manager responsible for user attribute management.
     * @param credential       The user's authentication credential.
     * @throws UserStoreException If the password is found to be equal to a claim value.
     */
    private void validatePasswordNotEqualToClaims(Map<String, String> claims,
                                                  UserStoreManager userStoreManager, Object credential)
            throws UserStoreException {

        processClaims(null, claims, null, userStoreManager, credential, false, null);
    }

    /**
     * Processes claims to validate uniqueness and check the password policy.
     *
     * @param username         The username of the user (nullable if not checking duplicates).
     * @param claims           A map of claim URIs and their values.
     * @param profile          The user profile (nullable if not checking duplicates).
     * @param userStoreManager The user store manager handling claims.
     * @param credential       The user's password.
     * @param duplicateClaims  A list to collect duplicate claims (used only if checking duplicates).
     * @throws UserStoreException If a policy violation occurs.
     */
    private void processClaims(String username, Map<String, String> claims, String profile,
                               UserStoreManager userStoreManager, Object credential, boolean checkForDuplicates,
                               List<String> duplicateClaims) throws UserStoreException {

        String tenantDomain = getTenantDomain(userStoreManager);

        for (Map.Entry<String, String> claim : claims.entrySet()) {
            try {
                String claimKey = claim.getKey();
                String claimValue = claim.getValue();
                ClaimConstants.ClaimUniquenessScope uniquenessScope = getClaimUniquenessScope(claimKey, tenantDomain);

                if (StringUtils.isNotEmpty(claimValue) && shouldValidateUniqueness(uniquenessScope)) {
                    Claim claimObject = getClaimObject(userStoreManager, claimKey);
                    if (claimObject == null) {
                        continue;
                    }

                    // Checks whether allowed login identifiers are equal to the password
                    validatePasswordNotEqualToClaim(credential, claimObject, claimValue);

                    // Check for duplicate claims if required
                    if (checkForDuplicates && isClaimDuplicated(username, claimKey, claimValue, profile,
                            userStoreManager, uniquenessScope)) {
                        duplicateClaims.add(getClaimDisplayTag(claimObject, claimKey));
                    }
                }
            } catch (ClaimMetadataException e) {
                log.error("Error while getting claim metadata for claimUri: " + claim.getKey() + ".", e);
            }
        }
    }

    /**
     * Retrieves a claim object for a given claim URI.
     *
     * @param userStoreManager The user store manager handling claims.
     * @param claimUri         The claim URI to retrieve.
     * @return The corresponding claim object, or null if retrieval fails.
     */
    private Claim getClaimObject(UserStoreManager userStoreManager, String claimUri) {

        try {
            return userStoreManager.getClaimManager().getClaim(claimUri);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while retrieving claim from claimUri: " + claimUri + ".", e);
        }
        return null;
    }

    /**
     * Ensures that the password is not equal to any claim value.
     *
     * @param credential  The user's password.
     * @param claimObject The claim object being validated.
     * @param claimValue  The claim value to check against.
     * @throws UserStoreException If the password matches the claim value.
     */
    private void validatePasswordNotEqualToClaim(Object credential, Claim claimObject, String claimValue)
            throws UserStoreException {

        if (credential != null && credential.toString().equals(claimValue)) {
            String errorMessage = "Password cannot be equal to the value defined for " +
                    claimObject.getDisplayTag() + "!";
            throw new UserStoreException(errorMessage, new PolicyViolationException(errorMessage));
        }
    }

    /**
     * Gets the display tag of a claim, falling back to the claim URI if no display tag is available.
     *
     * @param claimObject The claim object.
     * @param claimKey    The claim key/URI.
     * @return The display name of the claim or the claim key if the display name is not set.
     */
    private String getClaimDisplayTag(Claim claimObject, String claimKey) {

        return StringUtils.defaultIfBlank(claimObject.getDisplayTag(), claimKey);
    }

    /**
     * Throws an exception if duplicate claims are found.
     *
     * @param duplicateClaims List of duplicate claim display names.
     * @throws UserStoreClientException If duplicate claims are detected.
     */
    private void throwDuplicateClaimException(List<String> duplicateClaims) throws UserStoreClientException {

        String errorMessage;
        String errorCode;
        if (duplicateClaims.size() == 1) {
            errorMessage = ERROR_CODE_DUPLICATE_SINGLE_CLAIM.getMessage(duplicateClaims.get(0));
            errorCode = ERROR_CODE_DUPLICATE_SINGLE_CLAIM.getCode();
        } else {
            String claimList = String.join(", ", duplicateClaims);
            errorMessage = ERROR_CODE_DUPLICATE_MULTIPLE_CLAIMS.getMessage(claimList);
            errorCode = ERROR_CODE_DUPLICATE_MULTIPLE_CLAIMS.getCode();
        }
        throw new UserStoreClientException(errorMessage, errorCode,
                new PolicyViolationException(ERROR_CODE_DUPLICATE_CLAIM_VALUE, errorMessage));
    }

    private boolean isClaimDuplicated(String username, String claimUri, String claimValue, String profile,
                                      UserStoreManager userStoreManager,
                                      ClaimConstants.ClaimUniquenessScope uniquenessScope) throws UserStoreException {

        String domainName = userStoreManager.getRealmConfiguration().getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        Claim claim = getClaimObject(userStoreManager, claimUri);
        String[] userList;
        // Get UserStoreManager from realm since the received one might be for a secondary user store
        UserStoreManager userStoreMgrFromRealm = getUserstoreManager(userStoreManager.getTenantId());

        if (claim != null && claim.isMultiValued()) {
            return isMultiValuedClaimDuplicated(username, claimUri, claimValue, profile, userStoreMgrFromRealm,
                    domainName, uniquenessScope);
        }

        if (ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.equals(uniquenessScope)) {
            String claimValueWithDomain = domainName + UserCoreConstants.DOMAIN_SEPARATOR + claimValue;
            userList = userStoreMgrFromRealm.getUserList(claimUri, claimValueWithDomain, profile);
        } else {
            userList = userStoreMgrFromRealm.getUserList(claimUri, claimValue, profile);
        }

        String usernameWithUserStoreDomain = UserCoreUtil.addDomainToName(username, domainName);
        if (userList.length == 0) {
            return false;
        } else if (userList.length == 1) {
            if (usernameWithUserStoreDomain.equalsIgnoreCase(userList[0])) {
                if (log.isDebugEnabled()) {
                    log.debug("Single user found for claim URI: " + claimUri + ". The user is the same as the " +
                            "current user; skipping uniqueness check for this claim value.");
                }
                return false;
            } else {
                // Used in pre-add user and pre-update scenarios where the current user is not yet created.
                if (log.isDebugEnabled()) {
                    log.debug("Single user found for claim URI: " + claimUri + ". The user is different from " +
                            "the current user.");
                }
                return true;
            }
        } else {
            String earliestCreatedUser = getEarliestCreatedUser(userList, userStoreMgrFromRealm);
            if (usernameWithUserStoreDomain.equalsIgnoreCase(earliestCreatedUser)) {
                if (log.isDebugEnabled()) {
                    log.debug("Multiple users found for claim URI: " + claimUri + ". The earliest created user is " +
                            "the same as the current user; skipping uniqueness check for this claim value.");
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Multiple users found for claim URI: " + claimUri + ". The earliest created user is " +
                            "different from the current user.");
                }
                return true;
            }
        }
    }

    /**
     * Determines the uniqueness validation scope for a given claim URI.
     * This method checks the claim properties to determine how uniqueness should be enforced:
     * 1. First checks for explicit uniquenessScope property
     * 2. If not found, checks for legacy isUnique property
     * 3. If claim is unique, scope is determined by isScopeWithinUserstore server-level configuration
     * 4. Defaults to NONE if no uniqueness requirements are found
     *
     * @param claimUri The URI of the claim to check
     * @param tenantDomain The tenant domain where the claim exists
     * @return The ClaimUniquenessScope (NONE, WITHIN_USERSTORE, or ACROSS_USERSTORES)
     * @throws ClaimMetadataException If there is an error accessing claim metadata
     */
    private ClaimConstants.ClaimUniquenessScope getClaimUniquenessScope(String claimUri, String tenantDomain)
            throws ClaimMetadataException {

        List<LocalClaim> localClaims = UniqueClaimUserOperationDataHolder.getInstance()
                .getClaimMetadataManagementService().getLocalClaims(tenantDomain);

        LocalClaim targetLocalClaim = localClaims.stream()
                .filter(claim -> claim.getClaimURI().equals(claimUri))
                .findFirst()
                .orElse(null);

        if (targetLocalClaim != null) {
            String uniquenessScope = targetLocalClaim.getClaimProperty(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
            if (StringUtils.isNotBlank(uniquenessScope)) {
                try {
                    return ClaimConstants.ClaimUniquenessScope.valueOf(uniquenessScope);
                } catch (IllegalArgumentException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Invalid uniqueness validation scope '" + uniquenessScope + "' provided for " +
                                "claim URI: " + claimUri + ". Defaulting to NONE, where no uniqueness validation " +
                                "will be performed.");
                    }
                    return ClaimConstants.ClaimUniquenessScope.NONE;
                }
            }

            boolean isUniqueClaim = Boolean.parseBoolean(targetLocalClaim.getClaimProperty(IS_UNIQUE_CLAIM));
            if (isUniqueClaim) {
                return isScopeWithinUserstore()
                        ? ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE
                        : ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES;
            }
        }

        return ClaimConstants.ClaimUniquenessScope.NONE;
    }

    /**
     * Determines whether uniqueness validation should be performed for a given uniqueness scope.
     * Returns true for any scope other than NONE.
     *
     * @param uniquenessScope The ClaimUniquenessScope to check
     * @return true if uniqueness validation should be performed, false otherwise
     * @throws ClaimMetadataException If there is an error processing the metadata
     */
    private boolean shouldValidateUniqueness(ClaimConstants.ClaimUniquenessScope uniquenessScope) {

        return !ClaimConstants.ClaimUniquenessScope.NONE.equals(uniquenessScope);
    }

    private void checkUsernameUniqueness(String username, UserStoreManager userStoreManager) throws UserStoreException {

        String errorMessage;
        String tenantDomain = getTenantDomain(userStoreManager);

        try {
            ClaimConstants.ClaimUniquenessScope uniquenessScope = getClaimUniquenessScope(USERNAME_CLAIM, tenantDomain);
            if (shouldValidateUniqueness(uniquenessScope) &&
                    isClaimDuplicated(username, USERNAME_CLAIM, username, null, userStoreManager, uniquenessScope)) {

                errorMessage = "Username " + LoggerUtils.getMaskedContent(username) +
                        " is already in use by a different user!";
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
        validatePasswordNotEqualToClaims(claimMap, userStoreManager, newCredential);
        return true;
    }

    private boolean isMultiValuedClaimDuplicated(String username, String claimUri, String claimValue, String profile,
                                                 UserStoreManager userStoreManager, String domainName,
                                                 ClaimConstants.ClaimUniquenessScope uniquenessScope)
            throws UserStoreException {

        String multiAttributeSeparator = userStoreManager.getSecondaryUserStoreManager(domainName)
                .getRealmConfiguration().getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);

        String usernameWithUserStoreDomain = UserCoreUtil.addDomainToName(username, domainName);
        String existingUserClaimValue = userStoreManager.getUserClaimValues(usernameWithUserStoreDomain,
                new String[] {claimUri},
                UserCoreConstants.DEFAULT_PROFILE).get(claimUri);
        List<String> existingClaimValues = new ArrayList<>();
        if (StringUtils.isNotEmpty(existingUserClaimValue)) {
            existingClaimValues = Arrays.stream(existingUserClaimValue.split(multiAttributeSeparator))
                    .collect(Collectors.toList());
        }
        List<String> currentClaimValues = Arrays.stream(claimValue.split(multiAttributeSeparator))
                .collect(Collectors.toList());
        currentClaimValues.removeAll(existingClaimValues);

        for (String claimValuePart : currentClaimValues) {
            String[] userList;
            if (ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.equals(uniquenessScope)) {
                String claimValueWithDomain = domainName + UserCoreConstants.DOMAIN_SEPARATOR + claimValuePart;
                userList = userStoreManager.getUserList(claimUri, claimValueWithDomain, profile);
            } else {
                userList = userStoreManager.getUserList(claimUri, claimValuePart, profile);
            }
            if (userList.length == 1) {
                if (!usernameWithUserStoreDomain.equalsIgnoreCase(userList[0])) {
                    if (log.isDebugEnabled()) {
                        log.debug("Single user found for claim URI: " + claimUri + ". The user is different from " +
                                "the current user.");
                    }
                    return true;
                } else if (log.isDebugEnabled()) {
                    log.debug("Single user found for claim URI: " + claimUri + ". The user is the same as the " +
                            "current user; skipping uniqueness check for this claim value.");
                }
            }
            if (userList.length > 1) {
                String earliestCreatedUser = getEarliestCreatedUser(userList, userStoreManager);
                if (usernameWithUserStoreDomain.equalsIgnoreCase(earliestCreatedUser)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Multiple users found for claim URI: " + claimUri + ". The earliest created user " +
                                "is the same as the current user; skipping uniqueness check for this claim value.");
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Multiple users found for claim URI: " + claimUri + ". The earliest created user " +
                                "is different from the current user.");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the earliest created user from a list of users.
     * This is used to determine if the current user is the earliest created user when multiple users
     * have the same claim value.
     *
     * @param userList List of users to check
     * @param userStoreMgrFromRealm UserStoreManager to retrieve user claim values
     * @return The username of the earliest created user, or null if the list is empty
     */
    private String getEarliestCreatedUser(String[] userList, UserStoreManager userStoreMgrFromRealm) {

        Map<String, Long> userCreationTimeMap = new HashMap<>();
        for (String user : userList) {
            try {
                String createdTime = userStoreMgrFromRealm.getUserClaimValue(user,
                        UniqueClaimUserOperationEventListener.CREATED_TIME_CLAIM, null);
                if (StringUtils.isNotBlank(createdTime)) {
                    long createdTimeEpochMillis = Instant.parse(createdTime).toEpochMilli();
                    userCreationTimeMap.put(user, createdTimeEpochMillis);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Created time claim is blank for user: " + LoggerUtils.getMaskedContent(user) +
                                ". Using a high value for timestamp.");
                    }
                    userCreationTimeMap.put(user, Long.MAX_VALUE);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException | DateTimeParseException e) {
                log.warn("Error retrieving user creation time for user: " +
                        LoggerUtils.getMaskedContent(user) + ". Using a high value for timestamp.", e);
                // If there is an error retrieving the creation time, add the user with a high value to avoid
                // selecting it as the earliest created user.
                userCreationTimeMap.put(user, Long.MAX_VALUE);
            }
        }
        // Return the user with the minimum creation time.
        return userCreationTimeMap.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
