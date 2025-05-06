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
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.APIResourceTypes.TENANT;
import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.RBAC_AUTHORIZATION;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_ROLE_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_SCOPE_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.USER_SESSION_IMPERSONATION_ENABLED;

/**
 * Listener to handle post tasks of My Account application creation.
 */
public class MyAccountApplicationCreationListener extends AbstractApplicationMgtListener {

    private final AuthorizedAPIManagementService authorizedAPIManagementService = new
            AuthorizedAPIManagementServiceImpl();

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

        return Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_IMPERSONATION_ENABLED));
    }

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String appName = serviceProvider.getApplicationName();
        if (!MY_ACCOUNT_APPLICATION_NAME.equals(appName)) {
            return true;
        }
        String myAccountId = getMyAccountAppId(tenantDomain);
        // Register API Resource.
        if (!isAPIResourceRegistered(myAccountId, tenantDomain)) {
            addApiResourceToApplication(myAccountId, tenantDomain);
        }
        // Create Role.
        if (!isExistingRoleName(myAccountId, tenantDomain)) {
            addImpersonatorRole(myAccountId, tenantDomain);
        }

        return true;
    }

    private void addImpersonatorRole(String myAccountId, String tenantDomain)
            throws IdentityApplicationManagementException {

        List<Permission> permissions = new ArrayList<>();
        Permission permission = new Permission(IMPERSONATE_SCOPE_NAME);
        permissions.add(permission);

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        try {
            roleManagementService.addRole(IMPERSONATE_ROLE_NAME, new ArrayList<>(), new ArrayList<>(), permissions,
                    "application", myAccountId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error occurred while creating impersonator role.");
        }
    }

    private boolean isExistingRoleName(String myAccountId, String tenantDomain)
            throws IdentityApplicationManagementException {

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        try {
            return roleManagementService.isExistingRoleName(IMPERSONATE_ROLE_NAME,
                    "application", myAccountId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error occurred while checking role existence.");
        }
    }

    private void addApiResourceToApplication(String myAccountId, String tenantDomain)
            throws IdentityApplicationManagementException {

        APIResource apiResource = getImpersontionAPIResource(tenantDomain);

        AuthorizedAPI authorizedAPI = new AuthorizedAPI(
                myAccountId,
                apiResource.getId(),
                RBAC_AUTHORIZATION,
                apiResource.getScopes(),
                TENANT
        );
        try {
            authorizedAPIManagementService.addAuthorizedAPI(myAccountId, authorizedAPI, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityApplicationManagementException("Error occured while adding API resource.");
        }
    }

    private boolean isAPIResourceRegistered(String myAccountId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(
                    myAccountId, tenantDomain);
            for (AuthorizedAPI authorizedAPI : authorizedAPIs) {
                if (authorizedAPI.getAPIIdentifier().equals("system:impersonation")) {
                    return true;
                }
            }
            return false;
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityApplicationManagementException("Error occured while adding API resource.");
        }
    }

    private APIResource getImpersontionAPIResource(String tenantDomain)
            throws IdentityApplicationManagementException {

        APIResourceManager apiResourceManager = ApplicationManagementServiceComponentHolder.getInstance()
                .getAPIResourceManager();
        APIResource apiResource = null;
        try {
            apiResource = apiResourceManager.getAPIResourceByIdentifier("system:impersonation",
                    tenantDomain);
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error occurred while retrieving API resource.");
        }
        if (apiResource == null) {
            throw new IdentityApplicationManagementException("Api resources unavailable.");
        }
        return apiResource;
    }

    private String getMyAccountAppId(String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        try {
            String appUUID = applicationManagementService.getApplicationUUIDByName(
                    MY_ACCOUNT_APPLICATION_NAME, tenantDomain);
            if (StringUtils.isBlank(appUUID)) {
                throw new MigrationClientException("ApplicationUUID is null.");
            }
            return appUUID;
        } catch (IdentityApplicationManagementException | MigrationClientException e) {
            throw new IdentityApplicationManagementException("Error while getting ApplicationUUID.");
        }
    }
}
