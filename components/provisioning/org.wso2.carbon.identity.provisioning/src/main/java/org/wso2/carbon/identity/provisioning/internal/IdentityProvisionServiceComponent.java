/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.provisioning.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.common.ProvisioningConnectorService;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.entitlement.EntitlementService;
import org.wso2.carbon.identity.provisioning.AbstractProvisioningConnectorFactory;
import org.wso2.carbon.identity.provisioning.listener.DefaultInboundUserProvisioningListener;
import org.wso2.carbon.identity.provisioning.listener.ProvisioningApplicationMgtListener;
import org.wso2.carbon.identity.provisioning.listener.ProvisioningIdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

@Component(
         name = "org.wso2.carbon.identity.provision.internal.IdentityProvisionServiceComponent",
         immediate = true)
public class IdentityProvisionServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityProvisionServiceComponent.class);

    /**
     * @return
     */
    public static RealmService getRealmService() {
        return ProvisioningServiceDataHolder.getInstance().getRealmService();
    }

    /**
     * @param realmService
     */
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
        ProvisioningServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * @return
     */
    public static RegistryService getRegistryService() {
        return ProvisioningServiceDataHolder.getInstance().getRegistryService();
    }

    /**
     * @param registryService
     */
    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Registry Service");
        }
        ProvisioningServiceDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * @return
     */
    public static Map<String, AbstractProvisioningConnectorFactory> getConnectorFactories() {
        return ProvisioningServiceDataHolder.getInstance().getConnectorFactories();
    }

    /**
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            ProvisioningServiceDataHolder.getInstance().setBundleContext(context.getBundleContext());
            ProvisioningServiceDataHolder.getInstance().getBundleContext().registerService(UserOperationEventListener.class.getName(), new DefaultInboundUserProvisioningListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("Identity Provision Event listener registered successfully");
            }
            ProvisioningServiceDataHolder.getInstance().getBundleContext().registerService(ApplicationMgtListener.class.getName(), new ProvisioningApplicationMgtListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("Application Management Event listener registered successfully");
            }
            ProvisioningServiceDataHolder.getInstance().getBundleContext().registerService(IdentityProviderMgtListener.class.getName(), new ProvisioningIdentityProviderMgtListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("Identity Provider Management Event listener registered successfully");
            }
            if (log.isDebugEnabled()) {
                log.debug("Identity Provisioning framework bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error while initiating identity provisioning connector framework", e);
        }
    }

    /**
     * @param context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Provision bundle is de-activated");
        }
    }

    /**
     * @param registryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Registry Service");
        }
        ProvisioningServiceDataHolder.getInstance().setRegistryService(null);
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        ProvisioningServiceDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
             name = "provisioning.connector.factory", 
             service = org.wso2.carbon.identity.provisioning.AbstractProvisioningConnectorFactory.class, 
             cardinality = ReferenceCardinality.AT_LEAST_ONE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetProvisioningConnectorFactory")
    protected void setProvisioningConnectorFactory(AbstractProvisioningConnectorFactory connectorFactory) {
        ProvisioningServiceDataHolder.getInstance().getConnectorFactories().put(connectorFactory.getConnectorType(), connectorFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added provisioning connector : " + connectorFactory.getConnectorType());
        }
        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName(connectorFactory.getConnectorType());
        Property[] property = new Property[connectorFactory.getConfigurationProperties().size()];
        provisioningConnectorConfig.setProvisioningProperties(connectorFactory.getConfigurationProperties().toArray(property));
        ProvisioningConnectorService.getInstance().addProvisioningConnectorConfigs(provisioningConnectorConfig);
    }

    protected void unsetProvisioningConnectorFactory(AbstractProvisioningConnectorFactory connectorFactory) {
        ProvisioningServiceDataHolder.getInstance().getConnectorFactories().values().remove(connectorFactory);
        ProvisioningConnectorConfig provisioningConnectorConfig = ProvisioningConnectorService.getInstance().getProvisioningConnectorByName(connectorFactory.getConnectorType());
        ProvisioningConnectorService.getInstance().removeProvisioningConnectorConfigs(provisioningConnectorConfig);
        if (log.isDebugEnabled()) {
            log.debug("Removed provisioning connector : " + connectorFactory.getConnectorType());
        }
    }

    /**
     * @param entitlementService
     */
    @Reference(
             name = "identity.entitlement.service", 
             service = org.wso2.carbon.identity.entitlement.EntitlementService.class, 
             cardinality = ReferenceCardinality.AT_LEAST_ONE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetEntitlementService")
    protected void setEntitlementService(EntitlementService entitlementService) {
        if (log.isDebugEnabled()) {
            log.debug("EntitlementService is set in the Application Authentication Framework bundle");
        }
        ProvisioningServiceDataHolder.getInstance().setEntitlementService(entitlementService);
    }

    /**
     * @param entitlementService
     */
    protected void unsetEntitlementService(EntitlementService entitlementService) {
        if (log.isDebugEnabled()) {
            log.debug("EntitlementService is unset in the Application Authentication Framework bundle");
        }
        ProvisioningServiceDataHolder.getInstance().setEntitlementService(null);
    }
}

