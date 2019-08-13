/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.user.store.configuration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "org.wso2.carbon.identity.user.store.configuration.component",
        immediate = true
)
public class UserStoreConfigComponent {
    private static final Log log = LogFactory.getLog(UserStoreConfigComponent.class);
    private static RealmService realmService = null;
    private static RealmConfiguration realmConfiguration = null;
    private static ServerConfigurationService serverConfigurationService = null;

    public UserStoreConfigComponent() {
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        UserStoreConfigComponent.realmService = realmService;
        if (log.isDebugEnabled()) {
            log.debug("Set the Realm Service");
        }
    }

    public static RealmConfiguration getRealmConfiguration() {
        realmConfiguration = UserStoreConfigComponent.getRealmService().getBootstrapRealmConfiguration();
        return realmConfiguration;
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return UserStoreConfigComponent.serverConfigurationService;
    }

    @Reference(
            name = "server.configuration.service",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Set the ServerConfiguration Service");
        }
        UserStoreConfigComponent.serverConfigurationService = serverConfigurationService;

    }

    /**
     * @param ctxt
     */
    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Identity User Store bundle is activated.");
        }
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            AbstractUserStoreDAOFactory fileBasedUserStoreDAOFactory = new FileBasedUserStoreDAOFactory();
            AbstractUserStoreDAOFactory databaseBasedUserStoreDAOFactory = new DatabaseBasedUserStoreDAOFactory();
            ServiceRegistration serviceRegistration = bundleContext
                    .registerService(AbstractUserStoreDAOFactory.class.getName(), fileBasedUserStoreDAOFactory, null);
            bundleContext.registerService(AbstractUserStoreDAOFactory.class.getName(), databaseBasedUserStoreDAOFactory,
                    null);
            if (serviceRegistration != null) {
                if (log.isDebugEnabled()) {
                    log.debug("FileBasedUserStoreDAOFactory is successfully registered.");
                }
            } else {
                log.error("FileBasedUserStoreDAOFactory could not be registered.");
            }

        } catch (Throwable e) {
            log.error("Failed to load user store org.wso2.carbon.identity.user.store.configuration details.", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Identity User Store-Config bundle is activated.");

        }
    }

    @Reference(
            name = "user.store.configuration",
            service = AbstractUserStoreDAOFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserStoreDAOFactory"
    )
    protected void setUserStoreDAOFactory(AbstractUserStoreDAOFactory userStoreDAOFactory) {

        UserStoreConfigListenersHolder.getInstance().getUserStoreDAOFactories()
                .put(userStoreDAOFactory.getRepository(), userStoreDAOFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added UserStoreDAOFactory : " + userStoreDAOFactory.getRepository());
        }
    }

    protected void unsetUserStoreDAOFactory(AbstractUserStoreDAOFactory userStoreDAOFactory) {

        UserStoreConfigListenersHolder.getInstance().getUserStoreDAOFactories()
                .remove(userStoreDAOFactory.getRepository());

        if (log.isDebugEnabled()) {
            log.debug("Removed UserStoreDAOFactory : " + userStoreDAOFactory.getRepository());
        }
    }

    /**
     * @param ctxt
     */
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity User Store-Config bundle is deactivated");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        UserStoreConfigComponent.realmService = null;
        if (log.isDebugEnabled()) {
            log.debug("Unset the Realm Service");
        }
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset the ServerConfiguration Service");
        }
        UserStoreConfigComponent.serverConfigurationService = null;
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "user.store.config.event.listener.service",
            service = UserStoreConfigListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserStoreConfigListenerService"
    )
    protected void setUserStoreConfigListenerService(UserStoreConfigListener userStoreConfigListener) {

        UserStoreConfigListenersHolder.getInstance().setUserStoreConfigListenerService(userStoreConfigListener);
    }

    protected void unsetUserStoreConfigListenerService(UserStoreConfigListener userStoreConfigListener) {

        UserStoreConfigListenersHolder.getInstance().unsetUserStoreConfigListenerService(userStoreConfigListener);
    }

}

