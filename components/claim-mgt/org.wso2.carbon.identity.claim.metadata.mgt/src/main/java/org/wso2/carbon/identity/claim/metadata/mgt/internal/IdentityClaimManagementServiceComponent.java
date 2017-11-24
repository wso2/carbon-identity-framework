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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataStoreFactory;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.claim.ClaimManagerFactory;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.claim.metadata.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="claim.manager.listener.service"
 * interface="org.wso2.carbon.user.core.listener.ClaimManagerListener"
 * cardinality="0..n" policy="dynamic"
 * bind="setClaimManagerListener"
 * unbind="unsetClaimManagerListener" *
 * @scr.reference name="identityCoreInitializedEventService"
 * interface="org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent" cardinality="1..1"
 * policy="dynamic" bind="setIdentityCoreInitializedEventService" unbind="unsetIdentityCoreInitializedEventService"
 */
@SuppressWarnings("unused")
public class IdentityClaimManagementServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityClaimManagementServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            IdentityClaimManagementServiceDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());

            ClaimMetadataStoreFactory claimMetadataStoreFactory = new ClaimMetadataStoreFactory();
            ctxt.getBundleContext().registerService(ClaimManagerFactory.class.getName(), claimMetadataStoreFactory,
                    null);

            ClaimMetadataManagementService claimManagementService = new ClaimMetadataManagementServiceImpl();
            ctxt.getBundleContext().registerService(ClaimMetadataManagementService.class.getName(),
                    claimManagementService, null);
            IdentityClaimManagementServiceDataHolder.getInstance().setClaimManagementService(claimManagementService);
            if (log.isDebugEnabled()) {
                log.debug("Identity Claim Management Core bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Identity Claim Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Claim Management bundle is deactivated");
        }
    }

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

    public static synchronized void setClaimManagerListener(ClaimManagerListener claimManagerListener) {

        IdentityClaimManagementServiceDataHolder.getInstance().setClaimManagerListener(claimManagerListener);
        if (log.isDebugEnabled()) {
            log.debug("ClaimManagerListener set in Identity Claim Management bundle");
        }
    }

    public static synchronized void unsetClaimManagerListener(ClaimManagerListener claimManagerListener) {

        IdentityClaimManagementServiceDataHolder.getInstance().unsetClaimManagerListener(claimManagerListener);
        if (log.isDebugEnabled()) {
            log.debug("ClaimManagerListener unset in Identity Claim Management bundle");
        }
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }
}
