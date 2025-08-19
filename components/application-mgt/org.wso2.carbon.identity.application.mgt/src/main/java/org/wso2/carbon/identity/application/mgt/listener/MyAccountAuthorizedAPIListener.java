/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.Arrays;
import java.util.List;

/**
 * MyAccount authorized API listener.
 */
public class MyAccountAuthorizedAPIListener extends AbstractAuthorizedAPIManagementListener {

    private static final List<String> authorizedNoPolicyAPIIdentifiers = Arrays.asList(
            "/api/users/v1/me/approval-tasks",
            "/o/api/users/v1/me/approval-tasks");
    private static final List<String> authorizedNoPolicyScopes = Arrays.asList(
            "internal_approval_task_view",
            "internal_approval_task_update",
            "internal_org_approval_task_view",
            "internal_org_approval_task_update");

    @Override
    public int getExecutionOrderId() {

        return 2;
    }

    @Override
    public int getDefaultOrderId() {

        return 2;
    }

    @Override
    public boolean isEnable() {

        return true;
    }

    @Override
    public void postGetAuthorizedAPIs(List<AuthorizedAPI> authorizedAPIList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            if (appId.equals(getMyAccountAppId(tenantDomain))) {
                List<APIResource> systemAPIResources = APIResourceManagementUtil.getSystemAPIs(tenantDomain);
                for (APIResource systemAPIResource : systemAPIResources) {
                    if (!authorizedNoPolicyAPIIdentifiers.contains(systemAPIResource.getIdentifier())) {
                        continue;
                    }
                    AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .appId(appId)
                            .apiId(systemAPIResource.getId())
                            .scopes(getScopes(systemAPIResource.getId(), tenantDomain))
                            .policyId(APIResourceManagementConstants.NO_POLICY)
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

        if (StringUtils.equals(appId, getMyAccountAppId(tenantDomain))) {
            AuthorizedScopes authorizedScopes =
                    new AuthorizedScopes(APIResourceManagementConstants.NO_POLICY, authorizedNoPolicyScopes);
            authorizedScopesList.add(authorizedScopes);
        }
    }

    @Override
    public AuthorizedAPI postGetAuthorizedAPI(AuthorizedAPI authorizedAPI, String appId, String apiId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        if (StringUtils.equals(appId, getMyAccountAppId(tenantDomain))) {
            try {
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(apiId, tenantDomain);
                if (apiResource == null || !authorizedNoPolicyAPIIdentifiers.contains(apiResource.getIdentifier())) {
                    return authorizedAPI;
                }
                AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                        .appId(appId)
                        .apiId(apiResource.getId())
                        .scopes(apiResource.getScopes())
                        .policyId(APIResourceManagementConstants.NO_POLICY)
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

    private String getMyAccountAppId(String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        String myAccountInboundKey = buildMyAccountInboundKey(tenantDomain);
        return applicationManagementService.getApplicationResourceIDByInboundKey(myAccountInboundKey,
                "oauth2", tenantDomain);
    }

    private String buildMyAccountInboundKey(String tenantDomain) {

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled() ||
                ApplicationConstants.SUPER_TENANT.equalsIgnoreCase(tenantDomain)) {
            return ApplicationConstants.MY_ACCOUNT_APPLICATION_CLIENT_ID;
        } else {
            return ApplicationConstants.MY_ACCOUNT_APPLICATION_CLIENT_ID + "_" + tenantDomain;
        }
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
