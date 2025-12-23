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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
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

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_ORG_SCOPE_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_SCOPE_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATION_API_RESOURCE;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATION_ORG_API_RESOURCE;

/**
 * MyAccount authorized API listener.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.mgt.listener.AuthorizedAPIManagementListener",
                "service.scope=singleton"
        }
)
public class MyAccountAuthorizedAPIListener extends AbstractAuthorizedAPIManagementListener {

    private static final List<String> authorizedNoPolicyAPIIdentifiers = Arrays.asList(
            "/api/users/v2/me/approval-tasks",
            "/o/api/users/v2/me/approval-tasks");
    private static final List<String> authorizedNoPolicyScopes = Arrays.asList(
            "internal_approval_task_view",
            "internal_approval_task_update",
            "internal_org_approval_task_view",
            "internal_org_approval_task_update");
    private static final List<String> authorizedRBACAPIIdentifiers = Arrays.asList(
            IMPERSONATION_API_RESOURCE, IMPERSONATION_ORG_API_RESOURCE);
    private static final List<String> authorizedRBACScopes = Arrays.asList(
            IMPERSONATE_SCOPE_NAME, IMPERSONATE_ORG_SCOPE_NAME);

    private static final Log log = LogFactory.getLog(MyAccountAuthorizedAPIListener.class);

    /**
     * Gets the execution order ID for this listener.
     *
     * @return The execution order ID (2).
     */
    @Override
    public int getExecutionOrderId() {

        return 2;
    }

    /**
     * Gets the default order ID for this listener.
     *
     * @return The default order ID (2).
     */
    @Override
    public int getDefaultOrderId() {

        return 2;
    }

    /**
     * Checks if this listener is enabled.
     *
     * @return True indicating this listener is enabled.
     */
    @Override
    public boolean isEnable() {

        return true;
    }

    /**
     * Post-processes the authorized APIs list for MyAccount application.
     *
     * @param authorizedAPIList The list of authorized APIs to be modified.
     * @param appId The application ID.
     * @param tenantDomain The tenant domain.
     * @throws IdentityApplicationManagementException If an error occurs while processing.
     */
    @Override
    public void postGetAuthorizedAPIs(List<AuthorizedAPI> authorizedAPIList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            if (appId.equals(getMyAccountAppId(tenantDomain))) {
                if (log.isDebugEnabled()) {
                    log.debug("Post-processing authorized APIs for my account application in tenant: " + tenantDomain);
                }
                List<APIResource> systemAPIResources = APIResourceManagementUtil.getSystemAPIs(tenantDomain);
                for (APIResource systemAPIResource : systemAPIResources) {
                    if (!authorizedNoPolicyAPIIdentifiers.contains(systemAPIResource.getIdentifier())
                            && !authorizedRBACAPIIdentifiers.contains(systemAPIResource.getIdentifier())) {
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Adding authorized API: " + systemAPIResource.getIdentifier() + " for MyAccount app.");
                    }
                    String policyId = APIResourceManagementConstants.NO_POLICY;
                    if (authorizedRBACAPIIdentifiers.contains(systemAPIResource.getIdentifier())) {
                        policyId = APIResourceManagementConstants.RBAC_AUTHORIZATION;
                    }
                    AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .appId(appId)
                            .apiId(systemAPIResource.getId())
                            .scopes(getScopes(systemAPIResource.getId(), tenantDomain))
                            .policyId(policyId)
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

    /**
     * Post-processes the authorized scopes list for MyAccount application.
     *
     * @param authorizedScopesList The list of authorized scopes to be modified.
     * @param appId The application ID.
     * @param tenantDomain The tenant domain.
     * @throws IdentityApplicationManagementException If an error occurs while processing.
     */
    @Override
    public void postGetAuthorizedScopes(List<AuthorizedScopes> authorizedScopesList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Post-processing authorized scopes for app: " + appId + " in tenant: " + tenantDomain);
        }
        if (StringUtils.equals(appId, getMyAccountAppId(tenantDomain))) {
            AuthorizedScopes noPolicyScopes =
                    new AuthorizedScopes(APIResourceManagementConstants.NO_POLICY, authorizedNoPolicyScopes);
            AuthorizedScopes rbacAuthorizedScopes =
                    new AuthorizedScopes(APIResourceManagementConstants.RBAC_AUTHORIZATION, authorizedRBACScopes);
            authorizedScopesList.add(noPolicyScopes);
            log.debug("Added no-policy scopes for MyAccount application");
            authorizedScopesList.add(rbacAuthorizedScopes);
            log.debug("Added RBAC scopes for MyAccount application");
        }
    }

    /**
     * Post-processes a specific authorized API for MyAccount application.
     *
     * @param authorizedAPI The original authorized API.
     * @param appId The application ID.
     * @param apiId The API resource ID.
     * @param tenantDomain The tenant domain.
     * @return The modified authorized API or the original if not applicable.
     * @throws IdentityApplicationManagementException If an error occurs while processing.
     */
    @Override
    public AuthorizedAPI postGetAuthorizedAPI(AuthorizedAPI authorizedAPI, String appId, String apiId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(
                    "Post-processing authorized API: " + apiId + " for app: " + appId + " in tenant: " + tenantDomain);
        }
        if (StringUtils.equals(appId, getMyAccountAppId(tenantDomain))) {
            try {
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(apiId, tenantDomain);
                if (apiResource == null || (!authorizedNoPolicyAPIIdentifiers.contains(apiResource.getIdentifier())
                        && !authorizedRBACAPIIdentifiers.contains(apiResource.getIdentifier()))) {
                    return authorizedAPI;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Adding authorized API: " + apiResource.getIdentifier() + " for MyAccount app.");
                }
                String policyId = APIResourceManagementConstants.NO_POLICY;
                if (authorizedRBACAPIIdentifiers.contains(apiResource.getIdentifier())) {
                    policyId = APIResourceManagementConstants.RBAC_AUTHORIZATION;
                }
                AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                        .appId(appId)
                        .apiId(apiResource.getId())
                        .scopes(apiResource.getScopes())
                        .policyId(policyId)
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

        if (log.isDebugEnabled()) {
            log.debug("Retrieving MyAccount application ID for tenant: " + tenantDomain);
        }
        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        String myAccountInboundKey = buildMyAccountInboundKey(tenantDomain);
        return applicationManagementService.getApplicationResourceIDByInboundKey(myAccountInboundKey,
                "oauth2", tenantDomain);
    }

    private String buildMyAccountInboundKey(String tenantDomain) {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving MyAccount application ID for tenant: " + tenantDomain);
        }
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled() ||
                ApplicationConstants.SUPER_TENANT.equalsIgnoreCase(tenantDomain)) {
            return ApplicationConstants.MY_ACCOUNT_APPLICATION_CLIENT_ID;
        } else {
            return ApplicationConstants.MY_ACCOUNT_APPLICATION_CLIENT_ID + "_" + tenantDomain;
        }
    }

    private List<Scope> getScopes(String apiId, String tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving scopes for API: " + apiId + " in tenant: " + tenantDomain);
        }
        try {
            return ApplicationManagementServiceComponentHolder.getInstance().getAPIResourceManager()
                    .getAPIScopesById(apiId, tenantDomain);
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while retrieving scopes of the system API", e);
        }
    }
}
