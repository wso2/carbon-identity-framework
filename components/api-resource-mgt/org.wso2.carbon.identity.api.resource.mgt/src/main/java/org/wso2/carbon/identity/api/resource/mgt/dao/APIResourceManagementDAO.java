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

package org.wso2.carbon.identity.api.resource.mgt.dao;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.List;

/**
 * This interface performs CRUD operations for {@link APIResource} and {@link Scope}
 */
public interface APIResourceManagementDAO {

    /**
     * Retrieve the API resources under a given tenantId.
     *
     * @param limit           Maximum number of records to return.
     * @param tenantId        Tenant Id.
     * @param sortOrder       Sort order for the cursor based pagination.
     * @param expressionNodes Expression nodes.
     * @return List of <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resources.
     */
    List<APIResource> getAPIResources(Integer limit, Integer tenantId, String sortOrder,
                                      List<ExpressionNode> expressionNodes) throws APIResourceMgtException;

    /**
     * Retrieve the API resources under a given tenantId.
     *
     * @param limit              Maximum number of records to return.
     * @param tenantId           Tenant Id.
     * @param sortOrder          Sort order for the cursor based pagination.
     * @param expressionNodes    Expression nodes.
     * @param requiredAttributes List of required attributes
     * @return List of <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resources.
     */
    List<APIResource> getAPIResourcesWithRequiredAttributes(Integer limit, Integer tenantId, String sortOrder,
                                                            List<ExpressionNode> expressionNodes,
                                                            List<String> requiredAttributes)
            throws APIResourceMgtException;

    /**
     * Retrieve the count of API resources under a given tenantId.
     *
     * @param tenantId        Tenant Id.
     * @param expressionNodes Expression nodes.
     * @return Count of API resources.
     * @throws APIResourceMgtException If an error occurs while retrieving the count.
     */
    Integer getAPIResourcesCount(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException;

    /**
     * Retrieve the scopes of a given API resource.
     *
     * @param apiId API resource id.
     * @return List of <code>Scope</code>.
     * @throws APIResourceMgtServerException If an error occurs while retrieving the scopes.
     */
    List<Scope> getScopesByAPI(String apiId, Integer tenantId) throws APIResourceMgtServerException;

    /**
     * Create a new {@link APIResource}.
     *
     * @param apiResource API resource.
     * @param tenantId    Tenant Id.
     * @return Created <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while creating the API resource.
     */
    APIResource addAPIResource(APIResource apiResource, Integer tenantId) throws APIResourceMgtException;

    /**
     * Check if {@link APIResource} exists for the given identifier.
     *
     * @param identifier API resource identifier.
     * @param tenantId   Tenant Id.
     * @return True if exists, false otherwise.
     * @throws APIResourceMgtException If an error occurs while checking the existence.
     */
    boolean isAPIResourceExist(String identifier, Integer tenantId) throws APIResourceMgtException;

    /**
     * Check if {@link APIResource} exists for the given id.
     *
     * @param apiId    API resource id.
     * @param tenantId Tenant Id.
     * @return True if exists, false otherwise.
     * @throws APIResourceMgtException If an error occurs while checking the existence.
     */
    boolean isAPIResourceExistById(String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Retrieve the {@link APIResource} for the given id.
     *
     * @param apiId    API resource id.
     * @param tenantId Tenant Id.
     * @return An <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resource.
     */
    APIResource getAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Retrieve the {@link APIResource} for the given identifier.
     *
     * @param identifier API resource identifier.
     * @param tenantId   Tenant Id.
     * @return An <code>APIResource</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the API resource.
     */
    APIResource getAPIResourceByIdentifier(String identifier, Integer tenantId) throws APIResourceMgtException;

    /**
     * Update the {@link APIResource} for the given id.
     *
     * @param apiResource API resource.
     * @param tenantId    Tenant Id.
     * @throws APIResourceMgtException If an error occurs while updating the API resource.
     */
    void updateAPIResource(APIResource apiResource, List<Scope> addedScopes, List<String> removedScopes,
                           Integer tenantId) throws APIResourceMgtException;

    /**
     * Delete the {@link APIResource} for the given id.
     *
     * @param apiId    API resource id.
     * @param tenantId Tenant Id.
     * @throws APIResourceMgtException If an error occurs while deleting the API resource.
     */
    void deleteAPIResourceById(String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Is {@link Scope} exist by name.
     *
     * @param name     Scope name.
     * @param tenantId Tenant Id.
     * @return True if exists, false otherwise.
     * @throws APIResourceMgtException If an error occurs while checking the existence.
     */
    boolean isScopeExistByName(String name, Integer tenantId) throws APIResourceMgtException;

    /**
     * Is {@link Scope} exist by Id.
     *
     * @param scopeId Scope Id.
     * @return True if exists, false otherwise.
     * @throws APIResourceMgtException If an error occurs while checking the existence.
     */
    boolean isScopeExistById(String scopeId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Retrieve the {@link Scope} for the given name and tenantDomain.
     *
     * @param name     Scope name.
     * @param tenantId Tenant Id.
     * @return An <code>Scope</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the scope.
     */
    Scope getScopeByNameAndTenantId(String name, Integer tenantId) throws APIResourceMgtException;

    /**
     * Retrieve the {@link Scope} for the given name, apiId and tenantId.
     *
     * @param name     Scope name.
     * @param tenantId Tenant Id.
     * @param apiId    API resource id.
     * @return An <code>Scope</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the scope.
     */
    Scope getScopeByNameTenantIdAPIId(String name, Integer tenantId, String apiId)
            throws APIResourceMgtException;

    /**
     * Retrieve the {@link Scope} for the given tenantId.
     *
     * @param tenantId        Tenant Id.
     * @param expressionNodes Expression nodes.
     * @return List of <code>Scope</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the scope.
     */
    List<Scope> getScopesByTenantId(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws APIResourceMgtException;

    /**
     * Add a new {@link Scope} to a given API resource.
     *
     * @param scopes List of scopes.
     * @param apiId  API resource id.
     * @throws APIResourceMgtException If an error occurs while adding the scopes.
     */
    void addScopes(List<Scope> scopes, String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Delete all the {@link Scope} for the given apiId.
     *
     * @param apiId API resource id.
     * @throws APIResourceMgtException If an error occurs while deleting the scopes.
     */
    void deleteAllScopes(String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Delete the {@link Scope} for the given scopeId.
     *
     * @param scopeName Scope id.
     * @throws APIResourceMgtException If an error occurs while deleting the scope.
     */
    void deleteScope(String apiId, String scopeName, Integer tenantId) throws APIResourceMgtException;

    /**
     * Put scopes to the given API resource.
     *
     * @param apiId         API resource id.
     * @param currentScopes Current scopes.
     * @param scopes        New scopes.
     * @param tenantId      Tenant Id.
     * @throws APIResourceMgtException If an error occurs while putting the scopes.
     */
    void putScopes(String apiId, List<Scope> currentScopes, List<Scope> scopes, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Retrieve the subscribed applications for the given apiId.
     *
     * @param apiId API resource id.
     * @return List of <code>ApplicationBasicInfo</code>.
     * @throws APIResourceMgtException If an error occurs while retrieving the subscribed applications.
     */
    List<ApplicationBasicInfo> getSubscribedApplications(String apiId) throws APIResourceMgtException;
}
