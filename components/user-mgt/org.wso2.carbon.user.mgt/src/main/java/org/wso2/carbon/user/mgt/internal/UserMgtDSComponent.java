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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.GroupOperationEventListener;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.RolePermissionManagementService;
import org.wso2.carbon.user.mgt.RolePermissionManagementServiceImpl;
import org.wso2.carbon.user.mgt.listeners.GroupManagementV2AuditLogger;
import org.wso2.carbon.user.mgt.listeners.PermissionAuthorizationListener;
import org.wso2.carbon.user.mgt.listeners.UserClaimsAuditLogger;
import org.wso2.carbon.user.mgt.listeners.UserDeletionEventListener;
import org.wso2.carbon.user.mgt.listeners.UserManagementAuditLogger;
import org.wso2.carbon.user.mgt.listeners.UserManagementV2AuditLogger;
import org.wso2.carbon.user.mgt.listeners.UserMgtAuditLogger;
import org.wso2.carbon.user.mgt.listeners.UserMgtFailureAuditLogger;
import org.wso2.carbon.user.mgt.recorder.DefaultUserDeletionEventRecorder;
import org.wso2.carbon.user.mgt.recorder.UserDeletionEventRecorder;
import org.wso2.carbon.user.mgt.permission.ManagementPermissionsAdder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component(
         name = "usermgt.component", 
         immediate = true)
public class UserMgtDSComponent {

    private static final Log log = LogFactory.getLog(UserMgtDSComponent.class);

    private static RegistryService registryService = null;
    private static RealmService realmService = null;
    private static Map<String, UserDeletionEventRecorder> userDeleteEventRecorders = new HashMap<>();
    private static Collection<UserOperationEventListener> userOperationEventListenerCollection;
    private static Map<Integer, UserOperationEventListener> userOperationEventListeners;
    private static Map<Integer, UserManagementErrorEventListener> userManagementErrorEventListeners;
    private static Collection<UserManagementErrorEventListener> userManagementErrorEventListenerCollection;

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("User Mgt bundle is activated ");
        }

        // For new caching, every thread should has its own populated CC. During the deployment time we
        // assume super tenant.
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
            ServiceRegistration serviceRegistration = ctxt.getBundleContext()
                    .registerService(AuthorizationManagerListener.class.getName(),
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

            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                    new UserManagementAuditLogger(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering UserManagementAuditLogger.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserManagementAuditLogger successfully registered.");
                }
            }

            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                    new UserManagementV2AuditLogger(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering UserManagementAuditV2Logger.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserManagementV2AuditV2Logger successfully registered.");
                }
            }

            serviceRegistration = ctxt.getBundleContext().registerService(GroupOperationEventListener.class.getName(),
                    new GroupManagementV2AuditLogger(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering GroupManagementV2AuditLogger.");
            } else {
                log.debug("GroupManagementV2AuditLogger successfully registered.");
            }

            serviceRegistration = ctxt.getBundleContext()
                    .registerService(UserManagementErrorEventListener.class.getName(), new UserMgtFailureAuditLogger(),
                            null);
            if (serviceRegistration == null) {
                log.error("Error while registering UserMgtFailureAuditLogger.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserMgtFailureAuditLogger successfully registered.");
                }
            }

            // Register the UserDeletionEventListener
            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class,
                    new UserDeletionEventListener(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering UserDeletionEventListener.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserDeletionEventListener successfully registered.");
                }
            }

            // Register the default UserDeletionEventRecorder.
            serviceRegistration = ctxt.getBundleContext().registerService(UserDeletionEventRecorder.class,
                    new DefaultUserDeletionEventRecorder(), null);
            if (serviceRegistration == null) {
                log.error("Error while registering DefaultUserDeletionEventRecorder.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("DefaultUserDeletionEventRecorder successfully registered.");
                }
            }

            UserClaimsAuditLogger userClaimsAuditLogger = new UserClaimsAuditLogger();
            userClaimsAuditLogger.init();
            ServiceRegistration userClaimsAuditLoggerSR = ctxt.getBundleContext().registerService
                    (UserOperationEventListener.class.getName(), userClaimsAuditLogger, null);
            if (userClaimsAuditLoggerSR == null) {
                log.error("Error while registering UserClaimsAuditLogger.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserClaimsAuditLogger successfully registered.");
                }
            }

            ServiceRegistration rolePermissionManagementServiceRegistration = ctxt.getBundleContext()
                    .registerService(RolePermissionManagementService.class,
                            new RolePermissionManagementServiceImpl(), null);
            if (rolePermissionManagementServiceRegistration == null) {
                log.error("Error while registering RolePermissionManagementServiceImpl.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("RolePermissionManagementServiceImpl is successfully registered.");
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        // don't throw exception
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("User Mgt bundle is deactivated ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
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

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
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

    @Reference(
            name = "org.wso2.carbon.user.mgt.recorder.UserDeletionEventRecorder",
            service = UserDeletionEventRecorder.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserDeleteEventRecorder")
    protected void setUserDeleteEventRecorder(UserDeletionEventRecorder userDeletionEventRecorder) {

        if (log.isDebugEnabled()) {
            log.debug("Successfully added a user deletion event recorder. " + userDeletionEventRecorder.getClass()
                    .getName());
        }

        UserMgtDSComponent.userDeleteEventRecorders.put(userDeletionEventRecorder.getClass().getName(),
                userDeletionEventRecorder);
    }

    protected void unsetUserDeleteEventRecorder(UserDeletionEventRecorder userDeletionEventRecorder) {

        UserMgtDSComponent.userDeleteEventRecorders.remove(userDeletionEventRecorder.getClass().getName());

        if (log.isDebugEnabled()) {
            log.debug("Successfully removed the user deletion event recorder. " + userDeletionEventRecorder.getClass()
                    .getName());
        }
    }

    @Reference(
            name = "org.wso2.carbon.user.core.listener.UserOperationEventListener",
            service = UserOperationEventListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserOperationEventListenerService")
    protected synchronized void setUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {

        userOperationEventListenerCollection = null;
        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<>();
        }
        userOperationEventListeners
                .put(userOperationEventListenerService.getExecutionOrderId(), userOperationEventListenerService);
    }

    protected synchronized void unsetUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {

        if (userOperationEventListenerService != null && userOperationEventListeners != null) {
            userOperationEventListeners.remove(userOperationEventListenerService.getExecutionOrderId());
            userOperationEventListenerCollection = null;
        }
    }

    @Reference(
            name = "org.wso2.carbon.user.core.listener.UserManagementErrorEventListener",
            service = UserManagementErrorEventListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserManagementErrorEventListenerService")
    protected synchronized void setUserManagementErrorEventListenerService(
            UserManagementErrorEventListener userManagementErrorEventListenerService) {

        userManagementErrorEventListenerCollection = null;
        if (userManagementErrorEventListeners == null) {
            userManagementErrorEventListeners = new TreeMap<>();
        }
        userManagementErrorEventListeners.put(userManagementErrorEventListenerService.getExecutionOrderId(),
                userManagementErrorEventListenerService);
    }

    protected synchronized void unsetUserManagementErrorEventListenerService(
            UserManagementErrorEventListener userManagementErrorEventListenerService) {

        if (userManagementErrorEventListenerService != null && userManagementErrorEventListeners != null) {
            userManagementErrorEventListeners.remove(userManagementErrorEventListenerService.getExecutionOrderId());
            userManagementErrorEventListenerCollection = null;
        }
    }

    @Reference(
             name = "identityCoreInitializedEventService",
             service = org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetIdentityCoreInitializedEventService")
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static Map<String, UserDeletionEventRecorder> getUserDeleteEventRecorders() {
        return userDeleteEventRecorders;
    }

    /**
     * To get UserOperationEventListeners that are registered in particular environment.
     *
     * @return UserOperationEventListeners
     */
    public static synchronized Collection<UserOperationEventListener> getUserOperationEventListeners() {

        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<>();
        }
        if (userOperationEventListenerCollection == null) {
            userOperationEventListenerCollection = userOperationEventListeners.values();
        }
        return userOperationEventListenerCollection;
    }

    /**
     * To get the UserManagementErrorEventListeners that are registered for handling error.
     *
     * @return relevant UserManagementErrorEventListeners that are registered in the current environment.
     */
    public static synchronized Collection<UserManagementErrorEventListener> getUserManagementErrorEventListeners() {

        if (userManagementErrorEventListeners == null) {
            userManagementErrorEventListeners = new TreeMap<>();
        }
        if (userManagementErrorEventListenerCollection == null) {
            userManagementErrorEventListenerCollection = userManagementErrorEventListeners.values();
        }
        return userManagementErrorEventListenerCollection;
    }
}

