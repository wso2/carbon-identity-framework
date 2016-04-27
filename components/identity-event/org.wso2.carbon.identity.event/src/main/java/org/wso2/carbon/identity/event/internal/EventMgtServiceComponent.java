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
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.event.EventMgtException;
import org.wso2.carbon.identity.event.EventMgtConfigBuilder;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.listener.TenantCreationEventListener;
import org.wso2.carbon.identity.event.services.EventMgtService;
import org.wso2.carbon.identity.event.services.EventMgtServiceImpl;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @scr.component name="org.wso2.carbon.identity.event.internal.EventMgtServiceComponent"
 * immediate="true
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="event.handler"
 * interface="org.wso2.carbon.identity.event.handler.AbstractEventHandler"
 * cardinality="0..n" policy="dynamic"
 * bind="registerEventHandler"
 * unbind="unRegisterEventHandler"
 * @scr.reference name="listener.TenantMgtListener"
 * interface="org.wso2.carbon.stratos.common.listeners.TenantMgtListener"
 * cardinality="0..n" policy="dynamic"
 * bind="registerTenantMgtListener" unbind="unRegisterTenantMgtListener"
 * @scr.reference name="identityCoreInitializedEventService"
 * interface="org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent" cardinality="1..1"
 * policy="dynamic" bind="setIdentityCoreInitializedEventService" unbind="unsetIdentityCoreInitializedEventService"
 * @scr.reference name="IdentityProviderManager"
 * interface="org.wso2.carbon.idp.mgt.IdpManager" cardinality="1..1"
 * policy="dynamic" bind="setIdpManager" unbind="unsetIdpManager"
 */

public class EventMgtServiceComponent {

    private static Log log = LogFactory.getLog(EventMgtServiceComponent.class);

    private static RealmService realmService;

    private ServiceRegistration serviceRegistration = null;

    // list of all registered event handlers
    public static List<AbstractEventHandler> eventHandlerList = new ArrayList<>();

    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(TenantMgtListener.class.getName(),
                new TenantCreationEventListener(), null);
        try {
            EventMgtServiceDataHolder.getInstance().setEventMgtService(new EventMgtServiceImpl(eventHandlerList,
                    Integer.parseInt(EventMgtConfigBuilder.getInstance().getThreadPoolSize())));

            context.getBundleContext().registerService(EventMgtService.class.getName(),  EventMgtServiceDataHolder
                    .getInstance().getEventMgtService(), null);
        } catch (EventMgtException e) {
            log.error("Error while initiating IdentityMgtService.");
        }
        init();
        if (log.isDebugEnabled()) {
            log.debug("Identity Management Listener is enabled");
        }
    }


    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Management bundle is de-activated");
        }
    }

    protected void registerEventHandler(AbstractEventHandler eventHandler) throws EventMgtException {
        String handlerName = eventHandler.getName();
        eventHandler.init(EventMgtConfigBuilder.getInstance().getModuleConfigurations(handlerName).getModuleProperties());
        eventHandlerList.add(eventHandler);
    }

    protected void unRegisterEventHandler(AbstractEventHandler eventHandler) {

    }

    protected void registerTenantMgtListener(TenantMgtListener tenantMgtListener) {
    }

    protected void unRegisterTenantMgtListener(TenantMgtListener tenantMgtListener) {
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        EventMgtServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        EventMgtServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    private void init() {
        try {
            EventMgtService identityMgtService = EventMgtServiceDataHolder.getInstance().getEventMgtService();
            Properties properties = identityMgtService.addConfiguration(MultitenantConstants.SUPER_TENANT_ID);
        } catch (EventMgtException ex) {
            log.error("Error when storing super tenant configurations.");
        }
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdpManager(IdpManager idpManager) {
        EventMgtServiceDataHolder.getInstance().setIdpManager(null);
    }

    protected void setIdpManager(IdpManager idpManager) {
        EventMgtServiceDataHolder.getInstance().setIdpManager(idpManager);
    }
}
