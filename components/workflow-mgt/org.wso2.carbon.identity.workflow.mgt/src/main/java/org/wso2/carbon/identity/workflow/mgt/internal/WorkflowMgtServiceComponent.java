/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.workflow.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.workflow.mgt.handler.WorkflowPendingUserAuthnHandler;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementServiceImpl;
import org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowAuditLogger;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorAuditLogger;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowTenantMgtListener;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
         name = "org.wso2.carbon.identity.workflow.mgt", 
         immediate = true)
public class WorkflowMgtServiceComponent {

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            WorkflowManagementService workflowService = new WorkflowManagementServiceImpl();
            bundleContext.registerService(WorkflowManagementService.class, workflowService, null);
            AbstractEventHandler workflowPendingUserAuthnHandler = new WorkflowPendingUserAuthnHandler();
            bundleContext.registerService(AbstractEventHandler.class, workflowPendingUserAuthnHandler, null);
            WorkflowServiceDataHolder.getInstance().setWorkflowService(workflowService);
            WorkflowServiceDataHolder.getInstance().setBundleContext(bundleContext);
            ServiceRegistration serviceRegistration = context.getBundleContext()
                    .registerService(WorkflowListener.class.getName(), new WorkflowAuditLogger(), null);
            context.getBundleContext()
                    .registerService(WorkflowExecutorManagerListener.class.getName(), new WorkflowExecutorAuditLogger(),
                            null);
            context.getBundleContext()
                    .registerService(TenantMgtListener.class.getName(), new WorkflowTenantMgtListener(), null);

            if (serviceRegistration != null) {
                if (log.isDebugEnabled()) {
                    log.debug("WorkflowAuditLogger registered.");
                }
            } else {
                log.error("Workflow Audit Logger could not be registered.");
            }
            if (System.getProperty(WFConstant.KEYSTORE_SYSTEM_PROPERTY_ID) == null) {
                System.setProperty(WFConstant.KEYSTORE_SYSTEM_PROPERTY_ID,
                        ServerConfiguration.getInstance().getFirstProperty(WFConstant.KEYSTORE_CARBON_CONFIG_PATH));
            }
            if (System.getProperty(WFConstant.KEYSTORE_PASSWORD_SYSTEM_PROPERTY_ID) == null) {
                System.setProperty(WFConstant.KEYSTORE_PASSWORD_SYSTEM_PROPERTY_ID, ServerConfiguration.getInstance()
                        .getFirstProperty(WFConstant.KEYSTORE_PASSWORD_CARBON_CONFIG_PATH));
            }
        } catch (Throwable e) {
            log.error("Failed to start the WorkflowMgtServiceComponent", e);
        }
    }

    private static final Log log = LogFactory.getLog(WorkflowMgtServiceComponent.class);

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        WorkflowServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        WorkflowServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        WorkflowServiceDataHolder.getInstance().setConfigurationContextService(null);
    }

    @Reference(
             name = "org.wso2.carbon.utils.contextservice", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        WorkflowServiceDataHolder.getInstance().setConfigurationContextService(contextService);
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.extension.requesthandler", 
             service = org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowRequestHandler")
    protected void setWorkflowRequestHandler(WorkflowRequestHandler workflowRequestHandler) {
        WorkflowServiceDataHolder.getInstance().addWorkflowRequestHandler(workflowRequestHandler);
    }

    protected void unsetWorkflowRequestHandler(WorkflowRequestHandler workflowRequestHandler) {
        WorkflowServiceDataHolder.getInstance().removeWorkflowRequestHandler(workflowRequestHandler);
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.template.abtracttemplate", 
             service = org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTemplate")
    protected void setTemplate(AbstractTemplate template) {
        WorkflowServiceDataHolder.getInstance().addTemplate(template);
    }

    protected void unsetTemplate(AbstractTemplate template) {
        WorkflowServiceDataHolder.getInstance().removeTemplate(template);
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.workflow.abstractworkflow", 
             service = org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowImplementation")
    protected void setWorkflowImplementation(AbstractWorkflow workflowImplementation) {
        WorkflowServiceDataHolder.getInstance().addWorkflowImplementation(workflowImplementation);
    }

    protected void unsetWorkflowImplementation(AbstractWorkflow workflowImplementation) {
        WorkflowServiceDataHolder.getInstance().removeWorkflowImplementation(workflowImplementation);
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.listener.deleterequest", 
             service = org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowRequestDeleteListener")
    protected void setWorkflowRequestDeleteListener(WorkflowListener workflowListener) {
        WorkflowServiceDataHolder.getInstance().getWorkflowListenerList().add(workflowListener);
    }

    protected void unsetWorkflowRequestDeleteListener(WorkflowListener workflowListener) {
        WorkflowServiceDataHolder.getInstance().getWorkflowListenerList().remove(workflowListener);
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.listener.workflowexecutorlistner", 
             service = org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowExecutorListener")
    protected void setWorkflowExecutorListener(WorkflowExecutorManagerListener workflowListener) {
        WorkflowServiceDataHolder.getInstance().getExecutorListenerList().add(workflowListener);
    }

    protected void unsetWorkflowExecutorListener(WorkflowExecutorManagerListener workflowListener) {
        WorkflowServiceDataHolder.getInstance().getExecutorListenerList().remove(workflowListener);
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
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
}

