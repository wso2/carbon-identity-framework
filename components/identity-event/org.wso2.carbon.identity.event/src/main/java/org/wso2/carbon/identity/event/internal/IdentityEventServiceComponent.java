/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.event.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.handler.MessageHandlerComparator;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.event.services.IdentityEventServiceImpl;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "identity.event.service", 
         immediate = true)
public class IdentityEventServiceComponent {

    private static Log log = LogFactory.getLog(IdentityEventServiceComponent.class);

    private static RealmService realmService;

    private ServiceRegistration serviceRegistration = null;

    // list of all registered event handlers
    public static List<AbstractEventHandler> eventHandlerList = new ArrayList<>();

    @Activate
    protected void activate(ComponentContext context) {
        try {
            IdentityEventServiceDataHolder.getInstance().setEventMgtService(new IdentityEventServiceImpl(eventHandlerList, Integer.parseInt(IdentityEventConfigBuilder.getInstance().getThreadPoolSize())));
            context.getBundleContext().registerService(IdentityEventService.class.getName(), IdentityEventServiceDataHolder.getInstance().getEventMgtService(), null);
        } catch (IdentityEventException e) {
            log.error("Error while initiating IdentityMgtService.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Identity Management Listener is enabled");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Management bundle is de-activated");
        }
    }

    @Reference(
             name = "event.handler", 
             service = org.wso2.carbon.identity.event.handler.AbstractEventHandler.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unRegisterEventHandler")
    protected void registerEventHandler(AbstractEventHandler eventHandler) {
        String handlerName = eventHandler.getName();
        try {
            eventHandler.init(IdentityEventConfigBuilder.getInstance().getModuleConfigurations(handlerName));
        } catch (IdentityEventException | IdentityRuntimeException e) {
            log.warn("Properties for " + handlerName + " is not configured. This event handler will not be activated");
        }
        eventHandlerList.add(eventHandler);
        MessageHandlerComparator messageHandlerComparator = new MessageHandlerComparator(null);
        Collections.sort(eventHandlerList, messageHandlerComparator);
    }

    protected void unRegisterEventHandler(AbstractEventHandler eventHandler) {
    }

    @Reference(
             name = "listener.TenantMgtListener", 
             service = org.wso2.carbon.stratos.common.listeners.TenantMgtListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unRegisterTenantMgtListener")
    protected void registerTenantMgtListener(TenantMgtListener tenantMgtListener) {
    }

    protected void unRegisterTenantMgtListener(TenantMgtListener tenantMgtListener) {
    }

    @Reference(
             name = "realm.service", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        IdentityEventServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        IdentityEventServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
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

    protected void unsetIdpManager(IdpManager idpManager) {
        IdentityEventServiceDataHolder.getInstance().setIdpManager(null);
    }

    @Reference(
             name = "IdentityProviderManager", 
             service = org.wso2.carbon.idp.mgt.IdpManager.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetIdpManager")
    protected void setIdpManager(IdpManager idpManager) {
        IdentityEventServiceDataHolder.getInstance().setIdpManager(idpManager);
    }
}

