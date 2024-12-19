/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;

import java.util.List;

/**
 * Authorized API management listener.
 */
public interface AuthorizedAPIManagementListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Get the default order identifier for this listener.
     *
     * @return default order id
     */
    int getDefaultOrderId();

    /**
     * Check whether the listener is enabled or not
     *
     * @return true if enabled
     */
    boolean isEnable();

    /**
     * Invoked before adding an authorized API to the application.
     *
     * @param appId         ID of the application.
     * @param authorizedAPI Authorized API being added.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void preAddAuthorizedAPI(String appId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after adding an authorized API to the application.
     *
     * @param appId         ID of the application.
     * @param authorizedAPI Authorized API being added.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void postAddAuthorizedAPI(String appId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked before deleting an authorized API from the application.
     *
     * @param appId        ID of the application.
     * @param apiId        ID of the Authorized API.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void preDeleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after deleting an authorized API from the application.
     *
     * @param appId        ID of the application.
     * @param apiId        ID of the Authorized API.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void postDeleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked before retrieving authorized APIs of the application.
     *
     * @param appId        ID of the application.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void preGetAuthorizedAPIs(String appId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after retrieving authorized APIs of the application.
     *
     * @param authorizedAPIList List of authorized APIs.
     * @param appId             ID of the application.
     * @param tenantDomain      Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void postGetAuthorizedAPIs(List<AuthorizedAPI> authorizedAPIList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked before patching the authorized API of the application.
     *
     * @param appId         ID of the application.
     * @param apiId         ID of the Authorized API.
     * @param addedScopes   Added scopes.
     * @param removedScopes Removed scopes.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void prePatchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                               List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after patching the authorized API of the application.
     *
     * @param appId         ID of the application.
     * @param apiId         ID of the Authorized API.
     * @param addedScopes   Added scopes.
     * @param removedScopes Removed scopes.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void postPatchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked before retrieving authorized scopes of the application.
     *
     * @param appId        ID of the application.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void preGetAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after retrieving authorized scopes of the application.
     *
     * @param authorizedScopesList List of authorized scopes.
     * @param appId                ID of the application.
     * @param tenantDomain         Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void postGetAuthorizedScopes(List<AuthorizedScopes> authorizedScopesList, String appId,
                                 String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Invoked before retrieving an authorized API of the application by ID.
     *
     * @param appId        ID of the application.
     * @param apiId        ID of the Authorized API.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    void preGetAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Invoked after retrieving an authorized API of the application by ID.
     *
     * @param authorizedAPI Authorized API.
     * @param appId         ID of the application.
     * @param apiId         ID of the Authorized API.
     * @param tenantDomain  Tenant Domain.
     * @throws IdentityApplicationManagementException if an error occurs.
     */
    AuthorizedAPI postGetAuthorizedAPI(AuthorizedAPI authorizedAPI, String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException;
}
