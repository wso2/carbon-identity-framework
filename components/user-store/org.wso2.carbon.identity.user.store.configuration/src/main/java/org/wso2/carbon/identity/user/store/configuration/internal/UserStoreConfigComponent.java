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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.user.store.configuration.UserStoreConfigService;
import org.wso2.carbon.identity.user.store.configuration.UserStoreConfigServiceImpl;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreHashProviderConfigListenerImpl;
import org.wso2.carbon.identity.user.store.configuration.model.ChangedUserStoreAttribute;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttribute;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttributeMappings;
import org.wso2.carbon.identity.user.store.configuration.utils.DefaultUserStoreAttributeConfigLoader;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreServerException;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreAttributeMappingChangesLoader;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.identity.xds.client.mgt.XDSClientService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.hash.HashProviderFactory;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User store configuration service OSGi component.
 */
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
            UserStoreConfigService userStoreConfigService = new UserStoreConfigServiceImpl();
            ctxt.getBundleContext().registerService(UserStoreConfigService.class.getName(), userStoreConfigService,
                    null);
            UserStoreConfigListenersHolder.getInstance().setUserStoreConfigService(userStoreConfigService);
            UserStoreHashProviderConfigListenerImpl userStoreHashProviderListener =
                    new UserStoreHashProviderConfigListenerImpl();
            ctxt.getBundleContext().registerService(UserStoreConfigListener.class.getName(),
                    userStoreHashProviderListener, null);
            UserStoreConfigListenersHolder.getInstance().
                    setUserStoreConfigListenerService(userStoreHashProviderListener);
            if (serviceRegistration != null) {
                if (log.isDebugEnabled()) {
                    log.debug("FileBasedUserStoreDAOFactory is successfully registered.");
                }
            } else {
                log.error("FileBasedUserStoreDAOFactory could not be registered.");
            }
            readAllowedUserstoreConfiguration();
            readUserStoreAttributeMappingConfigs();

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

    private void readAllowedUserstoreConfiguration() {

        IdentityConfigParser configParser = IdentityConfigParser.getInstance();
        // Read allowed userstore configurations in identity.xml.
        OMElement userstoresConfig = configParser.getConfigElement(UserStoreConfigurationConstant.ALLOWED_USERSTORES);
        if (userstoresConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("'" + UserStoreConfigurationConstant.ALLOWED_USERSTORES + "' config not found.");
            }
            return;
        }
        Set<String> allowedUserstores = new HashSet<>();
        Iterator userstoreItr = userstoresConfig.getChildrenWithLocalName(UserStoreConfigurationConstant
                .ALLOWED_USERSTORE);
        int allowedUserstoreCount = 0;
        if (userstoreItr != null) {
            while (userstoreItr.hasNext()) {
                OMElement userstoreConfig = (OMElement) userstoreItr.next();
                String allowedUserstore = userstoreConfig.getText();
                if (StringUtils.isNotBlank(allowedUserstore)) {
                    allowedUserstores.add(allowedUserstore);
                    allowedUserstoreCount++;
                }
            }
        }
        if (allowedUserstoreCount == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No userstores have been configured under the '" + UserStoreConfigurationConstant
                        .ALLOWED_USERSTORE + "' config.");
            }
        }
        UserStoreConfigListenersHolder.getInstance().setAllowedUserstores(allowedUserstores);
    }

    private void readUserStoreAttributeMappingConfigs() {

        UserStoreAttributeMappings mappings = new UserStoreAttributeMappings();
        Map<String, Map<String, UserStoreAttribute>> userStoreAttributeMappings = new HashMap<>();
        Map<String, UserStoreAttribute> defaultAttributeMappings = null;
        Map<String, Map<String, ChangedUserStoreAttribute>> attributeMappingChanges = null;
        try {
            defaultAttributeMappings =
                    new DefaultUserStoreAttributeConfigLoader().loadDefaultUserStoreAttributeMappings();
            attributeMappingChanges =
                    new UserStoreAttributeMappingChangesLoader().loadUserStoreAttributeMappingChanges();

        } catch (IdentityUserStoreServerException e) {
            log.error("Error occurred while reading userstore attribute mappings configuration files.", e);
        }
        if (MapUtils.isNotEmpty(defaultAttributeMappings) && MapUtils.isNotEmpty(attributeMappingChanges)) {
            for (Map.Entry<String, Map<String, ChangedUserStoreAttribute>> entry :
                    attributeMappingChanges.entrySet()) {
                Map<String, UserStoreAttribute> tempMap =
                        getModifiedAttributeMap(defaultAttributeMappings, entry.getValue());
                userStoreAttributeMappings.put(entry.getKey(), tempMap);
            }
        }
        if (MapUtils.isNotEmpty(defaultAttributeMappings)) {
            mappings.setDefaultUserStoreAttributeMappings(defaultAttributeMappings);
        } else {
            mappings.setDefaultUserStoreAttributeMappings(Collections.emptyMap());
        }
        if (MapUtils.isNotEmpty(userStoreAttributeMappings)) {
            mappings.setUserStoreAttributeMappings(userStoreAttributeMappings);
        } else {
            mappings.setUserStoreAttributeMappings(Collections.emptyMap());
        }
        UserStoreConfigListenersHolder.getInstance().setUserStoreAttributeMappings(mappings);
    }

    /**
     * To merge default attribute mappings and mappings changes of other user stores.
     *
     * @param defaultUserStoreAttrMapping Default userstore attribute mappings.
     * @param changedUserStoreAttrMap     Userstore attribute mapping changes.
     * @return Map of user store type and their attribute mappings.
     */
    private Map<String, UserStoreAttribute> getModifiedAttributeMap(
            Map<String, UserStoreAttribute> defaultUserStoreAttrMapping,
            Map<String, ChangedUserStoreAttribute> changedUserStoreAttrMap) {

        if (defaultUserStoreAttrMapping == null) {
            return null;
        }
        Gson gson = new Gson();
        String serializedDefaultAttrMappings = gson.toJson(defaultUserStoreAttrMapping);
        // To deserialize a hashmap using Gson, need a type object of the hashmap.
        Type type = new TypeToken<HashMap<String, UserStoreAttribute>>() {
        }.getType();
        Map<String, UserStoreAttribute> clonedAttrMap = gson.fromJson(serializedDefaultAttrMappings, type);
        for (Map.Entry<String, ChangedUserStoreAttribute> entry : changedUserStoreAttrMap.entrySet()) {
            if (!clonedAttrMap.containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue().getOperation() == UserStoreConfigurationConstant.UserStoreOperation.UPDATE) {
                UserStoreAttribute defaultUserStoreAttribute = clonedAttrMap.get(entry.getKey());
                UserStoreAttribute newUserStoreAttribute = entry.getValue().getUsAttribute();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(newUserStoreAttribute.getMappedAttribute())) {
                    defaultUserStoreAttribute.setMappedAttribute(newUserStoreAttribute.getMappedAttribute());
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(newUserStoreAttribute.getDisplayName())) {
                    defaultUserStoreAttribute.setDisplayName(newUserStoreAttribute.getDisplayName());
                }
                clonedAttrMap.put(entry.getKey(), defaultUserStoreAttribute);
            } else if (entry.getValue().getOperation() == UserStoreConfigurationConstant.UserStoreOperation.DELETE) {
                clonedAttrMap.remove(entry.getKey());
            }
        }
        return clonedAttrMap;
    }

    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        UserStoreConfigListenersHolder.getInstance().setConfigurationContextService(configurationContextService);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService Instance was set.");
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        UserStoreConfigListenersHolder.getInstance().setConfigurationContextService(null);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService Instance was unset.");
        }
    }

    @Reference(
            name = "hash.provider.component",
            service = org.wso2.carbon.user.core.hash.HashProviderFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHashProviderFactory"
    )
    protected void setHashProviderFactory(HashProviderFactory hashProviderFactory) {

        UserStoreConfigListenersHolder.getInstance().setHashProviderFactory(hashProviderFactory);
    }

    protected void unsetHashProviderFactory(HashProviderFactory hashProviderFactory) {

        UserStoreConfigListenersHolder.getInstance().unbindHashProviderFactory(hashProviderFactory);
    }

    @Reference(
            name = "xds.client.service",
            service = XDSClientService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetXDSClientService"
    )
    protected void setXDSClientService(XDSClientService xdsClientService) {

        UserStoreConfigListenersHolder.getInstance().setXdsClientService(xdsClientService);
    }

    protected void unsetXDSClientService(XDSClientService xdsClientService) {

        UserStoreConfigListenersHolder.getInstance().setXdsClientService(null);
    }
}
