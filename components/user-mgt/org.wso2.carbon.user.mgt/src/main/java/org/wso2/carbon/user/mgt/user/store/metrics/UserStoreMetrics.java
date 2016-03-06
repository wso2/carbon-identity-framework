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
package org.wso2.carbon.user.mgt.user.store.metrics;

import org.wso2.carbon.user.mgt.user.store.metrics.exception.UserStoreMetricsException;

import java.util.Map;

public interface UserStoreMetrics {

    /**
     * Get the count of users having a matching user name for the filter
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter by each domain
     */
    Long countUsers(String filter) throws UserStoreMetricsException;

    /**
     * Get the count of roles having a matching role name for the filter
     * @param filter the filter for the role name. Use '*' to have all.
     * @return the number of roles matching the filter by each domain
     */
    Long countRoles(String filter) throws UserStoreMetricsException;

    /**
     * Get the count of users having claim values matching the given filter for the given claim URI
     * @param claimURI the claim URI
     * @param valueFilter filter for the claim values
     * @return the number of users matching the given claim and filter by each domain
     */
    Long countClaim(String claimURI, String valueFilter) throws UserStoreMetricsException;

    /**
     * Get the count of users, having claim values matching the given filter, for the given claim URI
     * @param claimSetToFilter A map of the claim URIs and filter for each to be used in counting the users
     * @return the number of users matching the claims set based on the filters by each domain
     */
    Long countClaims(Map<String, String> claimSetToFilter) throws UserStoreMetricsException;

    /**
     * Get the count of users having a matching user name for the filter
     * @param filter the filter for the user name. Use '*' to have all.
     * @return the number of users matching the filter only within this user store domain
     */
    Long countUsersInDomain(String filter, String domain) throws UserStoreMetricsException;

    /**
     * Get the count of roles having a matching role name for the filter
     * @param filter the filter for the role name. Use '*' to have all.
     * @return the number of roles matching the filter within this user store domain
     */
    Long countRolesInDomain(String filter, String domain) throws UserStoreMetricsException;

    /**
     * Get the count of users having claim values matching the given filter for the given claim URI
     * @param claimURI the claim URI
     * @param valueFilter filter for the claim values
     * @return the number of users matching the given claim and filter within this user store domain
     */
    Long countClaimInDomain(String claimURI, String valueFilter, String domain) throws UserStoreMetricsException;

    /**
     * Get the count of users, having claim values matching the given filter, for the given claim URIs
     * @param claimSetToFilter A map of the claim URIs and filter for each to be used in counting the users
     * @return the number of users matching the claims set based on the filters within this user store domain
     */
    Long countClaimsInDomain(Map<String, String> claimSetToFilter, String domain) throws UserStoreMetricsException;

}
