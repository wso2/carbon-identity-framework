/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.functionality.mgt.internal;

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
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityManager;
import org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityManagerImpl;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi declarative services component which handles registration and un-registration of user functionality management
 * service.
 */
@Component(
        name = "carbon.identity.user.functionality.mgt.component",
        immediate = true
)
public class UserFunctionalityManagerServiceComponent {

    private static final Log log = LogFactory.getLog(UserFunctionalityManagerServiceComponent.class);

    private ServiceRegistration userFunctionalityMgtService;

    /**
     * Register User Functionality Manager as an OSGi service.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            UserFunctionalityManager userFunctionalityManager = new UserFunctionalityManagerImpl();
            userFunctionalityMgtService = bundleContext.registerService(UserFunctionalityManager.class,
                    userFunctionalityManager, null);
            if (log.isDebugEnabled()) {
                log.debug("User Functionality Manager bundle is activated.");
            }
        } catch (Exception e) {
            log.error("Error while activating UserFunctionalityManagerServiceComponent.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.ungetService(userFunctionalityMgtService.getReference());
        if (log.isDebugEnabled()) {
            log.debug("User Functionality Manager bundle is deactivated.");
        }
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
         is started. */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started. */
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the User Functionality Manager bundle");
        }
        UserFunctionalityManagerComponentDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the User Functionality Manager bundle");
        }
        UserFunctionalityManagerComponentDataHolder.getInstance().setRealmService(null);
    }
}
