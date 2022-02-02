/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.unique.claim.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.unique.claim.mgt.listener.UniqueClaimUserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * A OSGI service for manage unique claim user operations.
 */
@Component(
        name = "org.wso2.carbon.identity.unique.claim.mgt.event.listener.unique.claim",
        immediate = true
)
public class UniqueClaimUserOperationServiceComponent {

    private static Log log = LogFactory.getLog(UniqueClaimUserOperationServiceComponent.class);


    @Activate
    protected void activate(ComponentContext context) {

        try {
            UniqueClaimUserOperationEventListener listener = new UniqueClaimUserOperationEventListener();
            context.getBundleContext().registerService(UserOperationEventListener.class.getName(), listener, null);
            if (log.isDebugEnabled()) {
                log.debug("UniqueClaimUserOperationEventListener bundle activated successfully.");
            }
        } catch (Throwable e) {
            log.error("Error while activating UniqueClaimUserOperationEventListener.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("UniqueClaimUserOperationEventListener is deactivated.");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service.");
        }
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service.");
        }
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "claim.meta.mgt.service",
            service = ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetaMgtService"
    )
    protected void setClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        UniqueClaimUserOperationDataHolder.getInstance().setClaimMetadataManagementService(claimMetaMgtService);

    }

    protected void unsetClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        UniqueClaimUserOperationDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
