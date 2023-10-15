/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt;

import org.wso2.carbon.identity.api.resource.mgt.model.APIResourceSearchResult;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;

import java.util.List;

/**
 * API Resource Manager Interface.
 */
public interface APIResourceManager {

    /**
     * Get API resources.
     *
     * @param after        Get API resources after this value.
     * @param before       Get API resources before this value.
     * @param limit        Number of API resources to retrieve.
     * @param filter       Filter expression.
     * @param sortOrder    Sort order.
     * @param tenantDomain Tenant domain.
     * @return API resource search result.
     * @throws APIResourceMgtException If an error occurs while retrieving API resources.
     */
    APIResourceSearchResult getAPIResources(String after, String before, Integer limit, String filter, String sortOrder,
                                            String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Get API resource by id.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @return An <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resource.
     */
    APIResource getAPIResourceById(String apiResourceId, String tenantDomain) throws APIResourceMgtException;

    /**
     * Add API resource.
     *
     * @param apiResource  API resource.
     * @param tenantDomain Tenant domain.
     * @return An <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while adding the API resource.
     */
    APIResource addAPIResource(APIResource apiResource, String tenantDomain) throws APIResourceMgtException;

    /**
     * Delete API resource by id.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurs while deleting the API resource.
     */
    void deleteAPIResourceById(String apiResourceId, String tenantDomain) throws APIResourceMgtException;

    /**
     * Update API resource.
     *
     * @param apiResource  API resource.
     * @param tenantDomain Tenant domain.
     * @throws APIResourceMgtException If an error occurs while updating the API resource.
     */
    void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                           String tenantDomain) throws APIResourceMgtException;

    /**
     * Get API resource by identifier.
     *
     * @param apiResourceIdentifier API resource identifier.
     * @param tenantDomain          Tenant domain.
     * @return An <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resource.
     */
    APIResource getAPIResourceByIdentifier(String apiResourceIdentifier, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Get API scopes by id.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @return List of <Code>Scope</Code>.
     * @throws APIResourceMgtException If an error occurs while retrieving API scopes.
     */
    List<Scope> getAPIScopesById(String apiResourceId, String tenantDomain) throws APIResourceMgtException;

    /**
     * Delete API scopes by id.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurs while deleting API scopes.
     */
    void deleteAPIScopesById(String apiResourceId, String tenantDomain) throws APIResourceMgtException;

    /**
     * Delete API scope by scope id.
     *
     * @param apiResourceId API resource id.
     * @param scopeName     Scope id.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurs while deleting API scope.
     */
    void deleteAPIScopeByScopeName(String apiResourceId, String scopeName, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Put scopes to API resource.
     *
     * @param apiResourceId API resource id.
     * @param currentScopes List of <Code>Scope</Code> in API resource.
     * @param scopes        List of <Code>Scope</Code>.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurs while putting scopes to API resource.
     */
    void putScopes(String apiResourceId, List<Scope> currentScopes, List<Scope> scopes, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Get scopes by tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @param filter       Filter expression.
     * @return List of <Code>Scope</Code>.
     * @throws APIResourceMgtException If an error occurs while retrieving scopes.
     */
    List<Scope> getScopesByTenantDomain(String tenantDomain, String filter) throws APIResourceMgtException;

    /**
     * Get scope by name.
     *
     * @param scopeName    Scope name.
     * @param tenantDomain Tenant domain.
     * @return Scope.
     * @throws APIResourceMgtException If an error occurs while retrieving scope.
     */
    Scope getScopeByName(String scopeName, String tenantDomain) throws APIResourceMgtException;

    /**
     * Get scope metadata by scope names and tenant domain.
     *
     * @param scopeNames   List of scope names.
     * @param tenantDomain Tenant domain.
     * @return Scope metadata grouped by API Resource.
     * @throws APIResourceMgtException If an error occurs while retrieving scope metadata.
     */
    List<APIResource> getScopeMetadata(List<String> scopeNames, String tenantDomain)
            throws APIResourceMgtException;
}
