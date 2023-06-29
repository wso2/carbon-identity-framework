/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.claim.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
         name = "claim.mgt.component", 
         immediate = true)
public class ClaimManagementServiceComponent {

    private static final Log log = LogFactory.getLog(ClaimManagementServiceComponent.class);

    public ClaimManagementServiceComponent() {
    }

    public static BundleContext getBundleContext() {
        return ClaimManagementServiceDataHolder.getInstance().getBundleContext();
    }

    public static RealmService getRealmService() {
        return ClaimManagementServiceDataHolder.getInstance().getRealmService();
    }

    /**
     * @param realmService
     */
    @Reference(
             name = "user.realmservice.default", 
             service = RealmService.class,
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        try {
            ClaimManagementServiceDataHolder.getInstance().setRealmService(realmService);
        } catch (Throwable e) {
            log.error("Failed to get a reference to the Realm Service.", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("RealmService set in Claim Management bundle");
        }
    }


    /**
     * @param ctxt
     */
    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            ClaimManagementServiceDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());
        } catch (Throwable e) {
            log.error("Error occurred while activating Claim Management Service Component", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Claim Management Core bundle is activated");
        }
    }

    /**
     * @param ctxt
     */
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Claim Management bundle is deactivated");
        }
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        ClaimManagementServiceDataHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("RealmService unset in Claim Management bundle");
        }
    }

    @Reference(
             name = "identityCoreInitializedEventService", 
             service = IdentityCoreInitializedEvent.class,
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

