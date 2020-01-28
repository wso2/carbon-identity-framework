/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.count;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.user.store.count.dto.PairDTO;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.internal.UserStoreCountDSComponent;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalStoreCountConstants;
import org.wso2.carbon.identity.user.store.count.util.UserStoreCountUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.Set;

/**
 * Service class that expose count functionality for underline user stores on users, roles and claims.
 */
public class UserStoreCountService {

    private static final Log log = LogFactory.getLog(UserStoreCountService.class);
    int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

    /**
     * Get the count of users having a matching user name for the filter
     *
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter by each domain
     */
    public PairDTO[] countUsers(String filter) throws UserStoreCounterException {

        Set<String> userStoreDomains = UserStoreCountUtils.getCountEnabledUserStores();
        PairDTO[] userCounts = new PairDTO[userStoreDomains.size()];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            String filterWithDomain = getFilterWithDomain(userStoreDomain, filter);
            long count = -1L;
            try {
                count = getUserCountWithClaims(UserStoreCountUtils.USERNAME_CLAIM, filterWithDomain);
            } catch (UserStoreCounterException e) {
                String errorMsg = "Error while getting user count from user store domain : ";
                throw new UserStoreCounterException(errorMsg, e);
            }
            userCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }
        return userCounts;
    }

    /**
     * Get the count of roles having a matching role name for the filter
     *
     * @param filter the filter for the role name. Use '*' to have all.
     * @return the number of roles matching the filter by each domain
     */
    public PairDTO[] countRoles(String filter) throws UserStoreCounterException {

        Set<String> userStoreDomains = UserStoreCountUtils.getCountEnabledUserStores();
        //Add 2 more for the counts of Internal, Application domains.
        PairDTO[] roleCounts = new PairDTO[userStoreDomains.size() + 2];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            long count = -1L;
            String filterWithDomain = getFilterWithDomain(userStoreDomain, filter);

            try {
                count = getRoleCount(filterWithDomain);
            } catch (UserStoreCounterException e) {
                log.error("Error while getting role count from user store domain : " + userStoreDomain, e);
            }

            roleCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }
        String internalDomainFilter = UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + filter;
        String applicationDomainFilter = InternalStoreCountConstants.APPLICATION_DOMAIN +
                UserCoreConstants.DOMAIN_SEPARATOR + filter;
        roleCounts[i] = new PairDTO(UserCoreConstants.INTERNAL_DOMAIN, String.valueOf(
                getRoleCount(internalDomainFilter)));
        roleCounts[++i] = new PairDTO(InternalStoreCountConstants.APPLICATION_DOMAIN, String.valueOf(
                getRoleCount(applicationDomainFilter)));

        return roleCounts;
    }

    /**
     * Get the count of users having claim values matching the given filter for the given claim URI
     *
     * @param claimURI    the claim URI
     * @param valueFilter filter for the claim values
     * @return the number of users matching the given claim and filter by each domain
     */
    public PairDTO[] countClaim(String claimURI, String valueFilter) throws UserStoreCounterException {

        Set<String> userStoreDomains = UserStoreCountUtils.getCountEnabledUserStores();
        PairDTO[] claimCounts = new PairDTO[userStoreDomains.size()];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            long count = -1L;
            String filterWithDomain = getFilterWithDomain(userStoreDomain, valueFilter);

            try {
                count = getUserCountWithClaims(claimURI, filterWithDomain);
            } catch (UserStoreCounterException e) {
                log.error("Error while getting user count with claim : " + claimURI + ", from user store domain : "
                        + userStoreDomain, e);
            }
            claimCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }
        return claimCounts;
    }

    /**
     * Get the count of users having a matching user name for the filter
     *
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter only within this user store domain
     */
    public long countUsersInDomain(String filter, String domain) throws UserStoreCounterException {

        String filterWithDomain = getFilterWithDomain(domain, filter);
        return getUserCountWithClaims(UserStoreCountUtils.USERNAME_CLAIM, filterWithDomain);
    }

    /**
     * Get the count of roles having a matching role name for the filter
     *
     * @param filter the filter for the role name. Use '*' to have all.
     * @return the number of roles matching the filter within this user store domain
     */
    public long countRolesInDomain(String filter, String domain) throws UserStoreCounterException {

        String filterWithDomain = getFilterWithDomain(domain, filter);
        return getRoleCount(filterWithDomain);
    }

    /**
     * Get the count of users having claim values matching the given filter for the given claim URI
     *
     * @param claimURI    the claim URI
     * @param valueFilter filter for the claim values
     * @return the number of users matching the given claim and filter within this user store domain
     */
    public long countByClaimInDomain(String claimURI, String valueFilter, String domain)
            throws UserStoreCounterException {

        String filterWithDomain = getFilterWithDomain(domain, valueFilter);
        return getUserCountWithClaims(claimURI, filterWithDomain);
    }

    /**
     * Get count enabled user stores.
     *
     * @return CountEnabledUserStore Array.
     * @throws UserStoreCounterException
     */
    public String[] getCountEnabledUserStores() throws UserStoreCounterException {

        Set<String> domains = UserStoreCountUtils.getCountEnabledUserStores();
        return domains.toArray(new String[domains.size()]);

    }

    /**
     * Get User count.
     *
     * @param claimURI    claim uri.
     * @param valueFilter filter that filter the users.
     * @return user count.
     * @throws UserStoreCounterException UserStoreCounterException.
     */
    private long getUserCountWithClaims(String claimURI, String valueFilter) throws UserStoreCounterException {

        try {
            if (UserStoreCountDSComponent.getRealmService() == null) {
                String errorMsg = "Unable to retrieve realm service";
                throw new UserStoreCounterException(errorMsg);
            }
            UserStoreManager userStoreManager =
                    UserStoreCountDSComponent.getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            if (!(userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager)) {
                if (log.isDebugEnabled()) {
                    log.debug(userStoreManager.getClass() + " is not not an instance of " + userStoreManager);
                }
                throw new UserStoreCounterException("Error while retrieving User Store from core");
            }
            return ((org.wso2.carbon.user.core.UserStoreManager) userStoreManager).getUserCountWithClaims(claimURI,
                    valueFilter);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving role count from tenent Id " +
                        tenantId + " and the filter " + valueFilter);
            }
            String errorMsg = "error while retrieving user count from user store";
            throw new UserStoreCounterException(errorMsg, e);
        }
    }

    private long getRoleCount(String filter) throws UserStoreCounterException {

        try {
            if (UserStoreCountDSComponent.getRealmService() == null) {
                String errorMsg = "Unable to retrieve realm service";
                throw new UserStoreCounterException(errorMsg);
            }
            UserStoreManager userStoreManager =
                    UserStoreCountDSComponent.getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

            if (!(userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager)) {
                if (log.isDebugEnabled()) {
                    log.debug(userStoreManager.getClass() + " is not not an instance of " + userStoreManager);
                }
                throw new UserStoreCounterException("Error while retrieving role count from core");
            }
            return ((org.wso2.carbon.user.core.UserStoreManager) userStoreManager).countRoles(filter);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving role count from tenent Id " +
                        tenantId + " and the filter " + filter);
            }
            String errorMsg = "Error while retrieving role count from user store";
            throw new UserStoreCounterException(errorMsg, e);
        }
    }

    private String getFilterWithDomain(String domain, String filter) {

        return domain + UserCoreConstants.DOMAIN_SEPARATOR + filter;
    }
}
