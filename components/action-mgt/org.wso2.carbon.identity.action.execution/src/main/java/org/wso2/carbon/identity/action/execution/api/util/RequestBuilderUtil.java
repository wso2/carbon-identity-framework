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

package org.wso2.carbon.identity.action.execution.api.util;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class provides utility methods to build requests for action execution.
 */
public class RequestBuilderUtil {

    /**
     * Retrieves the claim values for the specified user and requested claims.
     *
     * @param userId          The user ID for which to retrieve claim values.
     * @param requestedClaims  The list of claim URIs to retrieve.
     * @param tenantDomain    The tenant domain of the user.
     * @return A map of claim URIs and their corresponding values for the user.
     * @throws ActionExecutionRequestBuilderException If there is an error retrieving claim values.
     */
    public static Map<String, String> getClaimValues(String userId, List<String> requestedClaims,
                                                     String tenantDomain)
            throws ActionExecutionRequestBuilderException {

        if (requestedClaims == null || requestedClaims.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            Map<String, String> claimValues = getUserStoreManager(tenantDomain).getUserClaimValuesWithID(userId,
                            requestedClaims.toArray(new String[0]), UserCoreConstants.DEFAULT_PROFILE);

            // Filter the claim values to include only the requested claims
            Map<String, String> result = new java.util.HashMap<>();
            if (claimValues != null) {
                for (String claim : requestedClaims) {
                    if (claimValues.containsKey(claim)) {
                        result.put(claim, claimValues.get(claim));
                    }
                }
            }
            return result;
        } catch (UserStoreException e) {
            throw new ActionExecutionRequestBuilderException("Failed to retrieve user claims from user store.", e);
        }
    }

    /**
     * Retrieves the UniqueIDUserStoreManager for the specified tenant domain.
     *
     * @param tenantDomain The tenant domain for which to retrieve the user store manager.
     * @return The UniqueIDUserStoreManager instance for the tenant domain.
     * @throws ActionExecutionRequestBuilderException If there is an error retrieving the user store manager.
     */
    public static UniqueIDUserStoreManager getUserStoreManager(String tenantDomain)
            throws ActionExecutionRequestBuilderException {

        RealmService realmService = ActionExecutionServiceComponentHolder.getInstance().getRealmService();
        if (realmService == null) {
            throw new ActionExecutionRequestBuilderException("Realm service is unavailable.");
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            if (userRealm == null) {
                throw new ActionExecutionRequestBuilderException(
                        "User realm is not available for tenant: " + tenantDomain);
            }

            org.wso2.carbon.user.api.UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
                throw new ActionExecutionRequestBuilderException(
                        "User store manager is not an instance of UniqueIDUserStoreManager for tenant: " +
                                tenantDomain);
            }

            return (UniqueIDUserStoreManager) userStoreManager;
        } catch (UserStoreException e) {
            throw new ActionExecutionRequestBuilderException(
                    "Error while loading user store manager for tenant: " + tenantDomain, e);
        }
    }
}
