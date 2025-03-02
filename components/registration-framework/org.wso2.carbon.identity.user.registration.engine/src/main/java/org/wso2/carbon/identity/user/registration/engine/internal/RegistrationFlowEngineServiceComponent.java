/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.internal;

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
import org.wso2.carbon.identity.user.registration.engine.UserRegistrationFlowService;
import org.wso2.carbon.identity.user.registration.engine.graph.Executor;
import org.wso2.carbon.identity.user.registration.engine.graph.UserOnboardingExecutor;
import org.wso2.carbon.identity.user.registration.engine.graph.Node;
import org.wso2.carbon.identity.user.registration.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi declarative services component which handles registration flow engine service.
 */
@Component(
        name = "user.registration.flow.engine.component",
        immediate = true)
public class RegistrationFlowEngineServiceComponent {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngineServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(UserRegistrationFlowService.class.getName(),
                                          UserRegistrationFlowService.getInstance(), null);
            bundleContext.registerService(Executor.class.getName(), new UserOnboardingExecutor(), null);
            LOG.debug("Registration Flow Engine service successfully activated.");
        } catch (Throwable e) {
            LOG.error("Error while initiating Registration Flow Engine service", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(UserRegistrationFlowService.class));
            LOG.debug("Registration Flow Engine service successfully deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Registration Flow Engine service.", e);
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        LOG.debug("Setting the Realm Service in the UserRegistration component.");
        RegistrationFlowEngineDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        LOG.debug("Unsetting the Realm Service in the UserRegistration component.");
        RegistrationFlowEngineDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "RegistrationFlowMgtService",
            service = RegistrationFlowMgtService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistrationFlowMgtService")
    protected void setRegistrationFlowMgtService(RegistrationFlowMgtService registrationFlowMgtService) {

        LOG.debug("Setting the Registration Flow Management Service in the Registration Flow Engine component.");
        RegistrationFlowEngineDataHolder.getInstance().setRegistrationFlowMgtService(registrationFlowMgtService);
    }

    protected void unsetRegistrationFlowMgtService(RegistrationFlowMgtService registrationFlowMgtService) {

        LOG.debug("Unsetting the Registration Flow Management Service in the Registration Flow Engine component.");
        RegistrationFlowEngineDataHolder.getInstance().setRegistrationFlowMgtService(null);
    }

    @Reference(
            name = "Executor",
            service = Executor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetExecutors")
    protected void setExecutors(Executor executor) {

        LOG.debug("Setting executor in the Registration Flow Engine component.");
        RegistrationFlowEngineDataHolder.getInstance().getExecutors().put(executor.getName(), executor);
    }

    protected void unsetExecutors(Executor executor) {

        LOG.debug("Unsetting executor in the Registration Flow Engine component.");
        RegistrationFlowEngineDataHolder.getInstance().getExecutors().remove(executor.getName());
    }
}
