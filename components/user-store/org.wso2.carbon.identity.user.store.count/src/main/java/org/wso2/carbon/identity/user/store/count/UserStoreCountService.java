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

import org.wso2.carbon.identity.user.store.count.dto.PairDTO;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalStoreCountConstants;
import org.wso2.carbon.identity.user.store.count.util.UserStoreCountUtils;

import java.util.Set;
import org.wso2.carbon.user.core.UserCoreConstants;

/**
 * Service class that expose count functionality for underline user stores on users, roles and claims.
 */
public class UserStoreCountService {

    /**
     * Get the count of users having a matching user name for the filter
     *
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter by each domain
     */
    public PairDTO[] countUsers(String filter) throws UserStoreCounterException {
        Set<String> userStoreDomains = UserStoreCountUtils.getUserStoreDomains();
        PairDTO[] userCounts = new PairDTO[userStoreDomains.size()];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(userStoreDomain);
            Long count = Long.valueOf(-1);
            if (counter != null) {
                count = counter.countUsers(filter);
            } else {
                //no action
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
        Set<String> userStoreDomains = UserStoreCountUtils.getUserStoreDomains();
        //add 3 more for the counts of Internal, Application and Workflow domains
        PairDTO[] roleCounts = new PairDTO[userStoreDomains.size() + 3];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(userStoreDomain);
            Long count = Long.valueOf(-1);
            if (counter != null) {
                count = counter.countRoles(filter);
            } else {
                //no action
            }

            roleCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }

        roleCounts[i] =  new PairDTO(UserCoreConstants.INTERNAL_DOMAIN, String.valueOf(
                UserStoreCountUtils.getInternalRoleCount(filter)));
        roleCounts[++i] =  new PairDTO(InternalStoreCountConstants.APPLICATION_DOMAIN, String.valueOf(
                UserStoreCountUtils.getApplicationRoleCount(filter)));
        roleCounts[++i] =  new PairDTO(InternalStoreCountConstants.WORKFLOW_DOMAIN, String.valueOf(
                UserStoreCountUtils.getWorkflowRoleCount(filter)));

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
        Set<String> userStoreDomains = UserStoreCountUtils.getUserStoreDomains();
        PairDTO[] claimCounts = new PairDTO[userStoreDomains.size()];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(userStoreDomain);
            Long count = Long.valueOf(-1);
            if (counter != null) {
                count = counter.countClaim(claimURI, valueFilter);
            } else {
                //no action
            }
            claimCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }

        return claimCounts;
    }

    /**
     * Get the count of users, having claim values matching the given filter, for the given claim URI
     *
     * @param claimSetToFilter A map of the claim URIs and filter for each to be used in counting the users
     * @return the number of users matching the claims set based on the filters by each domain
     */
    public PairDTO[] countClaims(PairDTO[] claimSetToFilter) throws UserStoreCounterException {
        Set<String> userStoreDomains = UserStoreCountUtils.getUserStoreDomains();
        PairDTO[] claimsCounts = new PairDTO[userStoreDomains.size()];
        int i = 0;

        for (String userStoreDomain : userStoreDomains) {
            UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(userStoreDomain);
            Long count = Long.valueOf(-1);
            if (counter != null) {
                count = counter.countClaims(UserStoreCountUtils.convertArrayToMap(claimSetToFilter));
            } else {
                //no action
            }
            claimsCounts[i] = new PairDTO(userStoreDomain, Long.toString(count));
            i++;
        }

        return claimsCounts;
    }

    /**
     * Get the count of users having a matching user name for the filter
     *
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter only within this user store domain
     */
    public Long countUsersInDomain(String filter, String domain) throws UserStoreCounterException {
        UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(domain);
        if (counter != null) {
            return counter.countUsers(filter);
        } else {
            return Long.valueOf(-1);
        }
    }

    /**
     * Get the count of roles having a matching role name for the filter
     *
     * @param filter the filter for the role name. Use '*' to have all.
     * @return the number of roles matching the filter within this user store domain
     */
    public Long countRolesInDomain(String filter, String domain) throws UserStoreCounterException {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
            return UserStoreCountUtils.getInternalRoleCount(filter);
        } else if (InternalStoreCountConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
            return UserStoreCountUtils.getApplicationRoleCount(filter);
        } else if (InternalStoreCountConstants.WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
            return UserStoreCountUtils.getWorkflowRoleCount(filter);
        } else {              //Not an internal domain
            UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(domain);
            if (counter != null) {
                return counter.countRoles(filter);
            } else {
                return Long.valueOf(-1);
            }
        }
    }

    /**
     * Get the count of users having claim values matching the given filter for the given claim URI
     *
     * @param claimURI    the claim URI
     * @param valueFilter filter for the claim values
     * @return the number of users matching the given claim and filter within this user store domain
     */
    public Long countByClaimInDomain(String claimURI, String valueFilter, String domain) throws UserStoreCounterException {

        UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(domain);
        if (counter != null) {
            return counter.countClaim(claimURI, valueFilter);
        } else {
            return Long.valueOf(-1);
        }

    }

    /**
     * Get the count of users, having claim values matching the given filter, for the given claim URIs
     *
     * @param claimSetToFilter A map of the claim URIs and filter for each to be used in counting the users
     * @return the number of users matching the claims set based on the filters within this user store domain
     */
    public Long countByClaimsInDomain(PairDTO[] claimSetToFilter, String domain) throws UserStoreCounterException {
        UserStoreCountRetriever counter = UserStoreCountUtils.getCounterInstanceForDomain(domain);
        if (counter != null) {
            return counter.countClaims(UserStoreCountUtils.convertArrayToMap(claimSetToFilter));
        } else {
            return Long.valueOf(-1);
        }

    }

    public String[] getCountEnabledUserStores() throws UserStoreCounterException {
        Set<String> domains = UserStoreCountUtils.getCountEnabledUserStores();
        return domains.toArray(new String[domains.size()]);

    }

}
