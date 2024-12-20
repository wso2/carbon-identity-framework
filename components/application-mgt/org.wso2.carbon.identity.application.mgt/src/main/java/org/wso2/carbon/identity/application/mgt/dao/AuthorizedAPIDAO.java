/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;

import java.util.Collections;
import java.util.List;

/**
 * Authorized API DAO interface.
 */
public interface AuthorizedAPIDAO {

    /**
     * Adds an authorized API to an application using a policy ID and list of scopes.
     *
     * @param applicationId The application ID.
     * @param apiId         The API ID.
     * @param policyId      The policy ID.
     * @param scopes        The list of scopes to be associated with the API.
     * @param tenantId      The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while adding the authorized API.
     * @deprecated Use the {@link #addAuthorizedAPI(String, AuthorizedAPI, int)} instead.
     */
    @Deprecated
    void addAuthorizedAPI(String applicationId, String apiId, String policyId, List<Scope> scopes,
                          int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Retrieves a list of authorized APIs for a provided application ID and tenant.
     *
     * @param applicationId The application ID.
     * @param tenantId      The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while retrieving authorized APIs.
     */
    List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Updates the authorized API by adding or removing scopes for the specified tenant.
     *
     * @param appId         The application ID.
     * @param apiId         The API ID.
     * @param addedScopes   The list of scopes to add.
     * @param removedScopes The list of scopes to remove.
     * @param tenantId      The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while patching the authorized API.
     * @deprecated Use the {@link #patchAuthorizedAPI(String, String, List, List, List, List, int)} instead.
     */
    @Deprecated
    void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                            List<String> removedScopes, int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Deletes an authorized APIs from provided application ID and API ID for the given tenant.
     *
     * @param appId    The application ID.
     * @param apiId    The API ID.
     * @param tenantId The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while deleting authorized APIs.
     */
    void deleteAuthorizedAPI(String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Retrieves a list of authorized scopes for a provided application ID and tenant.
     *
     * @param applicationId The application ID.
     * @param tenantId      The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while retrieving authorized scopes.
     */
    List<AuthorizedScopes> getAuthorizedScopes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Retrieves an authorized APIs from provided application ID and API ID for the given tenant.
     *
     * @param appId    The application ID.
     * @param apiId    The API ID.
     * @param tenantId The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while retrieving the authorized API.
     */
    AuthorizedAPI getAuthorizedAPI(String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException;

    /**
     * Adds an authorized API to the specified application for a given tenant.
     *
     * @param applicationId The ID of the application where the API will be added.
     * @param authorizedAPI The authorized API object containing API details.
     * @param tenantId      The ID of the tenant domain.
     * @throws IdentityApplicationManagementException If an error occurs while adding the authorized API.
     */
    default void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, int tenantId)
            throws IdentityApplicationManagementException {
    }

    /**
     * Patches the authorized API by adding or removing scopes and authorization details types for the specified tenant.
     *
     * @param appId                             The application ID.
     * @param apiId                             The API ID.
     * @param scopesToAdd                       List of scopes to add.
     * @param scopesToRemove                    List of scopes to remove.
     * @param authorizationDetailsTypesToAdd    List of authorization details types to add.
     * @param authorizationDetailsTypesToRemove List of authorization details types to remove.
     * @param tenantId                          The tenant ID.
     * @throws IdentityApplicationManagementException If an error occurs while patching the authorized API.
     */
    default void patchAuthorizedAPI(String appId, String apiId, List<String> scopesToAdd,
                                    List<String> scopesToRemove, List<String> authorizationDetailsTypesToAdd,
                                    List<String> authorizationDetailsTypesToRemove, int tenantId)
            throws IdentityApplicationManagementException {
    }

    /**
     * Retrieves the list of authorized authorization details types for a given application in the specified tenant.
     *
     * @param applicationId The ID of the application.
     * @param tenantId      The ID of the tenant domain.
     * @return A list of authorization details types, or an empty list if none are found.
     * @throws IdentityApplicationManagementException If an error occurs while fetching the authorization details types.
     */
    default List<AuthorizationDetailsType> getAuthorizedAuthorizationDetailsTypes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        return Collections.emptyList();
    }
}
