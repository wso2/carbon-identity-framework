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

package org.wso2.carbon.identity.role.v2.mgt.core.internal;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Static class to hold services discovered via OSGI on this component,
 * solely for the use within this component.
 */
public class RoleManagementServiceComponentHolder {

    private static RoleManagementServiceComponentHolder instance = new
            RoleManagementServiceComponentHolder();

    private RealmService realmService;

    private IdentityEventService identityEventService;
    private OrganizationManager organizationManager;
    private IdpManager identityProviderManager;
    private ApplicationManagementService applicationManagementService;
    private APIResourceManager apiResourceManager;

    private RoleManagementServiceComponentHolder() {

    }

    public static RoleManagementServiceComponentHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get instance of IdentityEventService.
     *
     * @return IdentityEventService.
     */
    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }

    /**
     * Set instance of IdentityEventService.
     *
     * @param identityEventService Instance of IdentityEventService.
     */
    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }

    /**
     * Get {@link OrganizationManager}.
     *
     * @return organization manager instance {@link OrganizationManager}.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Set {@link OrganizationManager}.
     *
     * @param organizationManager Instance of {@link OrganizationManager}.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Get IdentityProviderManager osgi service.
     *
     * @return IdentityProviderManager
     */
    public IdpManager getIdentityProviderManager() {

        return identityProviderManager;
    }

    /**
     * Set IdentityProviderManager osgi service.
     *
     * @param idpManager IdentityProviderManager.
     */
    public void setIdentityProviderManager(IdpManager idpManager) {

        this.identityProviderManager = idpManager;
    }

    /**
     * Get ApplicationManagementService osgi service.
     *
     * @return ApplicationManagementService
     */
    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    /**
     * Set ApplicationManagementService osgi service.
     *
     * @param applicationManagementService ApplicationManagementService.
     */
    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    /**
     * Get APIResourceManager osgi service.
     *
     * @return APIResourceManager
     */
    public APIResourceManager getApiResourceManager() {

        return apiResourceManager;
    }

    /**
     * Set APIResourceManager osgi service.
     *
     * @param apiResourceManager APIResourceManager.
     */
    public void setApiResourceManager(APIResourceManager apiResourceManager) {

        this.applicationManagementService = applicationManagementService;
    }
}
