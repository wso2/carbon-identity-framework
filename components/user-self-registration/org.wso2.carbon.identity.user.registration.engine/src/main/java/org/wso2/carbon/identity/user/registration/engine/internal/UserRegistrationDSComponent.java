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
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.engine.UserRegistrationFlowService;
import org.wso2.carbon.identity.user.registration.engine.executor.Executor;
import org.wso2.carbon.identity.user.registration.engine.executor.impl.UserOnboardingExecutor;
import org.wso2.carbon.identity.user.registration.engine.node.Node;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.identity.user.registration.engine.node.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.node.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.registration.engine.node.ViewPromptingNode;
import org.wso2.carbon.identity.user.registration.engine.temp.servlet.RegistrationOrchestrationServlet;
import org.wso2.carbon.identity.user.registration.engine.temp.servlet.RegistrationPortalServlet;
import org.wso2.carbon.identity.user.registration.engine.temp.GoogleSignupTest;
import org.wso2.carbon.identity.user.registration.engine.temp.PasswordOnboarderTest;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.Servlet;

@Component(
         name = "user.registration.flow.engine.component",
         immediate = true)
public class UserRegistrationDSComponent {

    private static final Log LOG = LogFactory.getLog(UserRegistrationDSComponent.class);

    private HttpService httpService;
    private static RealmService realmService = null;

    @Activate
    protected void activate(ComponentContext context) {

        String registrationOrchestrationPath = "/reg-orchestration/config";
        String registrationPortalPath = "/reg-orchestration/portal";

        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(UserRegistrationFlowService.class.getName(), UserRegistrationFlowService.getInstance(), null);
        bundleContext.registerService(Executor.class.getName(), new UserOnboardingExecutor(), null);
        bundleContext.registerService(Executor.class.getName(), new PasswordOnboarderTest(), null);
        bundleContext.registerService(Executor.class.getName(), new GoogleSignupTest(), null);

        // Register the different node implementations.
        bundleContext.registerService(Node.class.getName(), new TaskExecutionNode(), null);
        bundleContext.registerService(Node.class.getName(), new UserChoiceDecisionNode(), null);
        bundleContext.registerService(Node.class.getName(), new ViewPromptingNode(), null);

        Servlet registrationOrchestrationServlet =
                new ContextPathServletAdaptor(new RegistrationOrchestrationServlet(), registrationOrchestrationPath);
        Servlet registrationPortalServlet =
                new ContextPathServletAdaptor(new RegistrationPortalServlet(), registrationPortalPath);
        try {
            httpService.registerServlet(registrationOrchestrationPath, registrationOrchestrationServlet, null, null);
            httpService.registerServlet(registrationPortalPath, registrationPortalServlet, null, null);
        } catch (Throwable e) {
            LOG.error("Error when registering RegistrationOrchestrationServlet via the OSGi HttpService.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("UserRegistration bundle is deactivated ");
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    @Reference(
             name = "user.realmservice.default",
             service = org.wso2.carbon.user.core.service.RealmService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        LOG.debug("Setting the Realm Service in the UserRegistration component.");
        UserRegistrationDSComponent.realmService = realmService;
        UserRegistrationServiceDataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        LOG.debug("Unsetting the Realm Service in the UserRegistration component.");
        UserRegistrationDSComponent.realmService = null;
        UserRegistrationServiceDataHolder.setRealmService(null);
    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService")
    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        UserRegistrationServiceDataHolder.setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        UserRegistrationServiceDataHolder.setApplicationManagementService(null);
    }

    @Reference(
            name = "RegistrationFlowMgtService",
            service = RegistrationFlowMgtService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistrationFlowMgtService")
    protected void setRegistrationFlowMgtService(RegistrationFlowMgtService registrationFlowMgtService) {

        UserRegistrationServiceDataHolder.setRegistrationFlowMgtService(registrationFlowMgtService);
    }

    protected void unsetRegistrationFlowMgtService(RegistrationFlowMgtService registrationFlowMgtService) {

        UserRegistrationServiceDataHolder.setRegistrationFlowMgtService(null);
    }

    @Reference(
            name = "Executor",
            service = Executor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetExecutors")
    protected void setExecutors(Executor executor) {

        UserRegistrationServiceDataHolder.getExecutors().add(executor);
    }

    protected void unsetExecutors(Executor executor) {

        UserRegistrationServiceDataHolder.getExecutors().remove(executor);
    }

    @Reference(
            name = "osgi.http.service",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(HttpService httpService) {

        LOG.debug("HTTP Service is set in UserRegistration bundle");
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {

        LOG.debug("HTTP Service is unset in the UserRegistration bundle");
        this.httpService = null;
    }
}
