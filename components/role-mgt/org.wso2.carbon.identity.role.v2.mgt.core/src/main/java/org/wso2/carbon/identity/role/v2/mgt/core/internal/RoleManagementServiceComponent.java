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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementServiceImpl;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementV2AuditLogger;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Role management V2 service component.
 */
@Component(name = "org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponent",
           immediate = true)
public class RoleManagementServiceComponent {

    private static Log log = LogFactory.getLog(RoleManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(RoleManagementService.class, new RoleManagementServiceImpl(), null);
            bundleContext.registerService(RoleManagementListener.class, new RoleManagementV2AuditLogger(), null);

            log.debug("Role V2 management service is activated.");
        } catch (Throwable e) {
            log.error("Error while activating Role V2 management service.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Role V2 management service is deactivated.");
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service.");
        RoleManagementServiceComponentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("Unsetting the Realm Service.");
        RoleManagementServiceComponentHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        RoleManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        log.debug("IdentityEventService set in Role Management bundle");
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        RoleManagementServiceComponentHolder.getInstance().setIdentityEventService(null);
        log.debug("IdentityEventService set in Role Management bundle");
    }

    @Reference(name = "identity.organization.management.component",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        RoleManagementServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        RoleManagementServiceComponentHolder.getInstance().setOrganizationManager(null);
    }

    @Reference(
            name = "idp.mgt.dscomponent",
            service = IdpManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityProviderManager"
    )
    protected void setIdentityProviderManager(IdpManager idpMgtService) {

        RoleManagementServiceComponentHolder.getInstance().setIdentityProviderManager(idpMgtService);
    }

    protected void unsetIdentityProviderManager(IdpManager idpMgtService) {

        RoleManagementServiceComponentHolder.getInstance().setIdentityProviderManager(null);
    }

    @Reference(
            name = "api.resource.mgt.service.component",
            service = APIResourceManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIResourceManagerService"
    )
    protected void setAPIResourceManagerService(APIResourceManager apiResourceManager) {

        RoleManagementServiceComponentHolder.getInstance().setApiResourceManager(apiResourceManager);
    }

    protected void unsetAPIResourceManagerService(APIResourceManager apiResourceManager) {

        RoleManagementServiceComponentHolder.getInstance().setApiResourceManager(null);
    }

    @Reference(
            name = "role.management.listener",
            service = RoleManagementListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRoleManagementListener"
    )
    protected void setRoleManagementListener(RoleManagementListener roleManagementListener) {

        RoleManagementServiceComponentHolder.getInstance().addRoleManagementListener(roleManagementListener);
    }

    protected void unsetRoleManagementListener(RoleManagementListener roleManagementListener) {

        RoleManagementServiceComponentHolder.getInstance().setRoleManagementListenerList(null);
    }

    @Reference(
            name = "role.management.audit.v2.logger",
            service = RoleManagementV2AuditLogger.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRoleManagementAuditV2Logger"
    )
    protected void setRoleManagementAuditV2Logger(RoleManagementV2AuditLogger roleManagementAuditV2Logger) {

        RoleManagementServiceComponentHolder.getInstance().addRoleManagementListener(roleManagementAuditV2Logger);
    }

    protected void unsetRoleManagementAuditV2Logger(RoleManagementV2AuditLogger roleManagementListener) {

        RoleManagementServiceComponentHolder.getInstance().setRoleManagementListenerList(null);
    }

}
