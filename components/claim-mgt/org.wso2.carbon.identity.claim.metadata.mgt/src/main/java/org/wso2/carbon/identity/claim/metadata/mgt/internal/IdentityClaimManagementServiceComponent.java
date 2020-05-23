/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.claim.metadata.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataStoreFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimConfigListener;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataTenantMgtListener;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.claim.ClaimManagerFactory;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@SuppressWarnings("unused")
@Component(
         name = "identity.claim.metadata.component", 
         immediate = true)
public class IdentityClaimManagementServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityClaimManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleCtx = ctxt.getBundleContext();

            IdentityClaimManagementServiceDataHolder.getInstance().setBundleContext(bundleCtx);

            ClaimMetadataStoreFactory claimMetadataStoreFactory = new ClaimMetadataStoreFactory();
            bundleCtx.registerService(ClaimManagerFactory.class.getName(), claimMetadataStoreFactory, null);

            ClaimMetadataManagementService claimManagementService = new ClaimMetadataManagementServiceImpl();
            bundleCtx.registerService(ClaimMetadataManagementService.class.getName(), claimManagementService, null);
            IdentityClaimManagementServiceDataHolder.getInstance().setClaimManagementService(claimManagementService);

            bundleCtx.registerService(TenantMgtListener.class.getName(),
                    new ClaimMetadataTenantMgtListener(), null);

            registerClaimConfigListener(bundleCtx);

            if (log.isDebugEnabled()) {
                log.debug("Identity Claim Management Core bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Identity Claim Management Service Component", e);
        }
    }

    /**
     * Register ClaimConfigListener as a UserStoreConfigListener service.
     *
     * @param bundleCtx BundleContext
     */
    private void registerClaimConfigListener(BundleContext bundleCtx) {

        UserStoreConfigListener claimConfigListener = new ClaimConfigListener();
        ServiceRegistration mappedClaimConfigListenerSR =
                bundleCtx.registerService(UserStoreConfigListener.class.getName(), claimConfigListener
                        , null);
        if (mappedClaimConfigListenerSR != null) {
            if (log.isDebugEnabled()) {
                log.debug("ClaimConfigListener Service registered.");
            }
        } else {
            log.error("Error registering ClaimConfigListener Service.");
        }
    }


    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Claim Management bundle is deactivated");
        }
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        IdentityClaimManagementServiceDataHolder.getInstance().setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("RealmService set in Identity Claim Management bundle");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        IdentityClaimManagementServiceDataHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("RealmService unset in Identity Claim Management bundle");
        }
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        IdentityClaimManagementServiceDataHolder.getInstance().setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in Identity Claim Management bundle");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        IdentityClaimManagementServiceDataHolder.getInstance().setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in Identity Claim Management bundle");
        }
    }

    @Reference(
             name = "claim.manager.listener.service", 
             service = org.wso2.carbon.user.core.listener.ClaimManagerListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetClaimManagerListener")
    public synchronized void setClaimManagerListener(ClaimManagerListener claimManagerListener) {
        IdentityClaimManagementServiceDataHolder.getInstance().setClaimManagerListener(claimManagerListener);
        if (log.isDebugEnabled()) {
            log.debug("ClaimManagerListener set in Identity Claim Management bundle");
        }
    }

    public synchronized void unsetClaimManagerListener(ClaimManagerListener claimManagerListener) {
        IdentityClaimManagementServiceDataHolder.getInstance().unsetClaimManagerListener(claimManagerListener);
        if (log.isDebugEnabled()) {
            log.debug("ClaimManagerListener unset in Identity Claim Management bundle");
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
}

