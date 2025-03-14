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

package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;

import java.util.Collections;
import java.util.List;

/**
 * Authorized API management service.
 */
public interface AuthorizedAPIManagementService {

    /**
     * Authorize an API to the application.
     *
     * @param applicationId Application ID.
     * @param authorizedAPI Authorized API.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs while authorizing the API.
     */
    public void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Delete the authorized API from the application.
     *
     * @param appId        Application ID.
     * @param apiId        API ID.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs while deleting the authorized API.
     */
    public void deleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get authorized APIs of the application.
     *
     * @param applicationId Application ID.
     * @param tenantDomain  Tenant Domain.
     * @return List of authorized APIs.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving the authorized APIs.
     */
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Patch the authorized API of the application.
     *
     * @param appId         Application ID.
     * @param apiId         API ID.
     * @param addedScopes   Added scopes.
     * @param removedScopes Removed scopes.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs while patching the authorized API.
     * @deprecated Use the {@link #patchAuthorizedAPI(String, String, List, List, List, List, String)} instead.
     */
    @Deprecated
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get authorized scopes of the application.
     *
     * @param appId        Application ID.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving the authorized scopes.
     */
    public List<AuthorizedScopes> getAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get an authorized API of the application by ID.
     *
     * @param appId        Application ID.
     * @param apiId        API Resource ID.
     * @param tenantDomain Tenant Domain.
     * @return Authorized API.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving the authorized API.
     */
    public AuthorizedAPI getAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Updates the authorized API by patching the specified scopes and authorization details types.
     * The method adds and removes specified scopes and authorization details for the API,
     * identified by the appId and apiId within the provided tenantDomain.
     *
     * @param appId                            The application ID.
     * @param apiId                            The API ID.
     * @param addedScopes                      List of scopes to add.
     * @param removedScopes                    List of scopes to remove.
     * @param addedAuthorizationDetailsTypes   List of authorization details types to add.
     * @param removedAuthorizationDetailsTypes List of authorization details types to remove.
     * @param tenantDomain                     The tenant domain.
     * @throws IdentityApplicationManagementException If an error occurs while patching the authorized API.
     */
    default void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                    List<String> removedScopes, List<String> addedAuthorizationDetailsTypes,
                                    List<String> removedAuthorizationDetailsTypes, String tenantDomain)
            throws IdentityApplicationManagementException {
    }

    /**
     * Get a list of authorized {@link AuthorizationDetailsType} for the provided application ID.
     *
     * @param appId        Application ID.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving.
     */
    default List<AuthorizationDetailsType> getAuthorizedAuthorizationDetailsTypes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        return Collections.emptyList();
    }
}
