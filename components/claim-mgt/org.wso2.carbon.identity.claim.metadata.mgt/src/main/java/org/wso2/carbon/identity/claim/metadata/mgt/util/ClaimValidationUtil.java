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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ClaimUniquenessScope;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;

/**
 * Utility class for claim validation operations including uniqueness checks and password validation.
 */
public class ClaimValidationUtil {

    private static final Log LOG = LogFactory.getLog(ClaimValidationUtil.class);

    private ClaimValidationUtil() {

    }

    /**
     * Determine the uniqueness scope for a claim from its properties.
     *
     * @param claimProperties Claim properties map.
     * @return ClaimUniquenessScope.
     */
    public static ClaimUniquenessScope getClaimUniquenessScope(Map<String, String> claimProperties) {

        if (claimProperties == null) {
            return ClaimUniquenessScope.NONE;
        }

        String uniquenessScope = claimProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
        if (StringUtils.isNotBlank(uniquenessScope)) {
            try {
                return ClaimUniquenessScope.valueOf(uniquenessScope);
            } catch (IllegalArgumentException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid uniqueness scope: " + uniquenessScope + ". Defaulting to NONE.");
                }
                return ClaimUniquenessScope.NONE;
            }
        }

        boolean isUnique = Boolean.parseBoolean(claimProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY));
        if (isUnique) {
            return ClaimUniquenessScope.ACROSS_USERSTORES;
        }
        return ClaimUniquenessScope.NONE;
    }

    /**
     * Determines whether uniqueness validation should be performed for a given uniqueness scope.
     * Returns true for any scope other than NONE.
     *
     * @param uniquenessScope The ClaimUniquenessScope to check
     * @return true if uniqueness validation should be performed, false otherwise
     */
    public static boolean shouldValidateUniqueness(ClaimConstants.ClaimUniquenessScope uniquenessScope) {

        return !ClaimConstants.ClaimUniquenessScope.NONE.equals(uniquenessScope);
    }

    /**
     * Check if a claim value is duplicated across all users.
     *
     * @param claimURI   Claim URI to check.
     * @param claimValue Claim value to check.
     * @return true if the claim value is duplicated, false otherwise.
     * @throws UserStoreException If an error occurs while accessing the user store.
     */
    public static boolean isClaimDuplicated(String claimURI, String claimValue)
            throws UserStoreException {

        org.wso2.carbon.user.core.UserStoreManager userStoreManager = getUserstoreManager();
        Claim claim = getClaimObject(userStoreManager, claimURI);

        if (claim != null && claim.isMultiValued()) {
            return false;
        }

        String[] userList = userStoreManager.getUserList(claimURI, claimValue, null);
        if (userList == null || userList.length == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No users found for claim " + claimURI);
            }
            return false;
        }
        return true;
    }

    /**
     * Retrieves a claim object for a given claim URI.
     *
     * @param userStoreManager The user store manager handling claims.
     * @param claimUri         The claim URI to retrieve.
     * @return The corresponding claim object, or null if retrieval fails.
     */
    private static Claim getClaimObject(org.wso2.carbon.user.core.UserStoreManager userStoreManager, String claimUri) {

        try {
            return userStoreManager.getClaimManager().getClaim(claimUri);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while retrieving claim from claimUri: " + claimUri + ".", e);
            }
        }
        return null;
    }

    /**
     * Get the UserStoreManager for the current tenant.
     *
     * @return UserStoreManager instance.
     * @throws org.wso2.carbon.user.core.UserStoreException If an error occurs while retrieving the user store manager.
     */
    private static org.wso2.carbon.user.core.UserStoreManager getUserstoreManager() throws UserStoreException {

        try {
            RealmService userRealm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService();
            if (userRealm != null) {
                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                return (AbstractUserStoreManager) userRealm
                        .getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
            }
        } catch (UserStoreException e) {
            throw new org.wso2.carbon.user.core.UserStoreException(e);
        }
        throw new org.wso2.carbon.user.core.UserStoreException("User realm is null for the tenant.");
    }
}
