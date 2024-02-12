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

import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Console authorized API listener.
 */
public class ConsoleAuthorizedAPIListener extends AbstractAuthorizedAPIManagementListener {

    @Override
    public int getExecutionOrderId() {
        return 1;
    }

    @Override
    public int getDefaultOrderId() {
        return 1;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void preAddAuthorizedAPI(String appId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (appId.equals(getConsoleAppId(tenantDomain))) {
            throw new IdentityApplicationManagementClientException("Adding authorized APIs to the console application" +
                    " is not allowed");
        }

    }

    @Override
    public void preDeleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (appId.equals(getConsoleAppId(tenantDomain))) {
            throw new IdentityApplicationManagementClientException("Deleting authorized APIs from the console " +
                    "application is not allowed");
        }
    }

    @Override
    public void prePatchAuthorizedAPI(String appId, String apiId, List<String> addedScopes, List<String> removedScopes,
                                      String tenantDomain) throws IdentityApplicationManagementException {

        if (appId.equals(getConsoleAppId(tenantDomain))) {
            throw new IdentityApplicationManagementClientException("Patching authorized APIs of the console " +
                    "application is not allowed");
        }
    }

    @Override
    public void postGetAuthorizedAPIs(List<AuthorizedAPI> authorizedAPIList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            if (appId.equals(getConsoleAppId(tenantDomain))) {
                List<APIResource> systemAPIResources = APIResourceManagementUtil.getSystemAPIs(tenantDomain);
                for (APIResource systemAPIResource : systemAPIResources) {
                    AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .appId(appId)
                            .apiId(systemAPIResource.getId())
                            .scopes(getScopes(systemAPIResource.getId(), tenantDomain))
                            .policyId("RBAC")
                            .type(systemAPIResource.getType())
                            .build();
                    authorizedAPI.setAPIName(systemAPIResource.getName());
                    authorizedAPI.setAPIIdentifier(systemAPIResource.getIdentifier());
                    authorizedAPIList.add(authorizedAPI);
                }
            }
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while retrieving system APIs", e);
        }
    }

    @Override
    public void postGetAuthorizedScopes(List<AuthorizedScopes> authorizedScopesList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (appId.equals(getConsoleAppId(tenantDomain))) {
            try {
                List<Scope> systemAPIScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                AuthorizedScopes authorizedScopes = new AuthorizedScopes("RBAC", systemAPIScopes.stream()
                        .map(Scope::getName).collect(Collectors.toList()));
                authorizedScopesList.add(authorizedScopes);
            } catch (APIResourceMgtException e) {
                throw new IdentityApplicationManagementException("Error while retrieving system APIs", e);
            }
        }
    }

    @Override
    public AuthorizedAPI postGetAuthorizedAPI(AuthorizedAPI authorizedAPI, String appId, String apiId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        if (appId.equals(getConsoleAppId(tenantDomain))) {
            try {
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(apiId, tenantDomain);
                AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                        .appId(appId)
                        .apiId(apiResource.getId())
                        .scopes(apiResource.getScopes())
                        .policyId("RBAC")
                        .type(apiResource.getType())
                        .build();
                authorizedAPI1.setAPIName(apiResource.getName());
                authorizedAPI1.setAPIIdentifier(apiResource.getIdentifier());
                return authorizedAPI1;
            } catch (APIResourceMgtException e) {
                throw new IdentityApplicationManagementException("Error while retrieving system API", e);
            }
        }
        return authorizedAPI;
    }

    private String getConsoleAppId(String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        return applicationManagementService.getApplicationResourceIDByInboundKey("CONSOLE",
                "oauth2", tenantDomain);
    }

    private List<Scope> getScopes(String apiId, String tenantDomain) throws IdentityApplicationManagementException {

        try {
            return ApplicationManagementServiceComponentHolder.getInstance().getAPIResourceManager()
                    .getAPIScopesById(apiId, tenantDomain);
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while retrieving scopes of the system API", e);
        }
    }
}
