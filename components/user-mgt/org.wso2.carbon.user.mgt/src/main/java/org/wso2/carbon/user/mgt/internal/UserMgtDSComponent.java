/*
 * Copyright (c) 2006 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.listeners.PermissionAuthorizationListener;
import org.wso2.carbon.user.mgt.listeners.UserMgtAuditLogger;
import org.wso2.carbon.user.mgt.permission.ManagementPermissionsAdder;

/**
 * @scr.component name="usermgt.component"" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class UserMgtDSComponent {
    private static final Log log = LogFactory.getLog(UserMgtDSComponent.class);
    private static RegistryService registryService = null;
    private static RealmService realmService = null;

    protected void activate(ComponentContext ctxt) {
        log.debug("User Mgt bundle is activated ");

        // for new cahing, every thread should has its own populated CC. During the deployment time we assume super tenant
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        carbonContext.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);

        UserMgtInitializer userMgtInitializer = new UserMgtInitializer();
        try {
            userMgtInitializer.start(ctxt.getBundleContext(), registryService);
            ManagementPermissionsAdder uiPermissionAdder = new ManagementPermissionsAdder();
            ctxt.getBundleContext().addBundleListener(uiPermissionAdder);
            Bundle[] bundles = ctxt.getBundleContext().getBundles();
            for (Bundle bundle : bundles) {
                if (bundle.getState() == Bundle.ACTIVE) {
                    uiPermissionAdder.addUIPermissionFromBundle(bundle);
                }
            }
            // register the Authorization listener to restriction tenant!=0 setting super tenant
            // specific permissions
            ServiceRegistration serviceRegistration = ctxt.getBundleContext().registerService
                    (AuthorizationManagerListener.class.getName(),
                            new PermissionAuthorizationListener(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering PermissionAuthorizationListener.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("PermissionAuthorizationListener successfully registered.");
                }
            }
            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                    new UserMgtAuditLogger(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering UserMgtAuditLogger.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserMgtAuditLogger successfully registered.");
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            // don't throw exception
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("User Mgt bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        UserMgtDSComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        UserMgtDSComponent.registryService = null;
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        UserMgtDSComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
        UserMgtDSComponent.realmService = null;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

}
