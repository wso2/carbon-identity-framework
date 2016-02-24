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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementServiceImpl;
import org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowAuditLogger;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorAuditLogger;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.identity.workflow.mgt" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.workflow.mgt.extension.requesthandler"
 * interface="org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler"
 * cardinality="0..n" policy="dynamic"
 * bind="setWorkflowRequestHandler"
 * unbind="unsetWorkflowRequestHandler"
 * @scr.reference name="org.wso2.carbon.identity.workflow.mgt.template.abtracttemplate"
 * interface="org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate"
 * cardinality="0..n" policy="dynamic"
 * bind="setTemplate"
 * unbind="unsetTemplate"
 * @scr.reference name="org.wso2.carbon.identity.workflow.mgt.workflow.abstractworkflow"
 * interface="org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow"
 * cardinality="0..n" policy="dynamic"
 * bind="setWorkflowImplementation"
 * unbind="unsetWorkflowImplementation"
 * @scr.reference name="identityCoreInitializedEventService"
 * interface="org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent" cardinality="1..1"
 * policy="dynamic" bind="setIdentityCoreInitializedEventService" unbind="unsetIdentityCoreInitializedEventService"
 * @scr.reference name="org.wso2.carbon.utils.contextservice"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.identity.workflow.mgt.listener.deleterequest"
 * interface="org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener"
 * cardinality="0..n" policy="dynamic"
 * bind="setWorkflowRequestDeleteListener"
 * unbind="unsetWorkflowRequestDeleteListener"
 * @scr.reference name="org.wso2.carbon.identity.workflow.mgt.listener.workflowexecutorlistner"
 * interface="org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener"
 * cardinality="0..n" policy="dynamic"
 * bind="setWorkflowExecutorListener"
 * unbind="unsetWorkflowExecutorListener"
 */
public class WorkflowMgtServiceComponent {


    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        WorkflowManagementService workflowService = new WorkflowManagementServiceImpl();

        bundleContext.registerService(WorkflowManagementService.class, workflowService, null);
        WorkflowServiceDataHolder.getInstance().setWorkflowService(workflowService);


        WorkflowServiceDataHolder.getInstance().setBundleContext(bundleContext);
        ServiceRegistration serviceRegistration = context.getBundleContext().registerService
                (WorkflowListener.class.getName(), new WorkflowAuditLogger(), null);
        context.getBundleContext().registerService
                (WorkflowExecutorManagerListener.class.getName(), new WorkflowExecutorAuditLogger(), null);
        if (serviceRegistration != null) {
            if (log.isDebugEnabled()) {
                log.debug("WorkflowAuditLogger registered.");
            }
        } else {
            log.error("Workflow Audit Logger could not be registered.");
        }
        if (System.getProperty(WFConstant.KEYSTORE_SYSTEM_PROPERTY_ID) == null) {
            System.setProperty(WFConstant.KEYSTORE_SYSTEM_PROPERTY_ID, ServerConfiguration.getInstance()
                    .getFirstProperty(WFConstant.KEYSTORE_CARBON_CONFIG_PATH));
        }
        if (System.getProperty(WFConstant.KEYSTORE_PASSWORD_SYSTEM_PROPERTY_ID) == null) {
            System.setProperty(WFConstant.KEYSTORE_PASSWORD_SYSTEM_PROPERTY_ID, ServerConfiguration.getInstance()
                    .getFirstProperty(WFConstant.KEYSTORE_PASSWORD_CARBON_CONFIG_PATH));
        }
    }

    private static Log log = LogFactory.getLog(WorkflowMgtServiceComponent.class);

    protected void setRealmService(RealmService realmService) {

        WorkflowServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        WorkflowServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        WorkflowServiceDataHolder.getInstance().setConfigurationContextService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        WorkflowServiceDataHolder.getInstance().setConfigurationContextService(contextService);
    }

    protected void setWorkflowRequestHandler(WorkflowRequestHandler workflowRequestHandler) {

        WorkflowServiceDataHolder.getInstance().addWorkflowRequestHandler(workflowRequestHandler);
    }

    protected void unsetWorkflowRequestHandler(WorkflowRequestHandler workflowRequestHandler) {

        WorkflowServiceDataHolder.getInstance().removeWorkflowRequestHandler(workflowRequestHandler);
    }

    protected void setTemplate(AbstractTemplate template) {

        WorkflowServiceDataHolder.getInstance().addTemplate(template);
    }

    protected void unsetTemplate(AbstractTemplate template) {

        WorkflowServiceDataHolder.getInstance().removeTemplate(template);
    }

    protected void setWorkflowImplementation(AbstractWorkflow workflowImplementation) {

        WorkflowServiceDataHolder.getInstance().addWorkflowImplementation(workflowImplementation);
    }

    protected void unsetWorkflowImplementation(AbstractWorkflow workflowImplementation) {

        WorkflowServiceDataHolder.getInstance().removeWorkflowImplementation(workflowImplementation);
    }


    protected void setWorkflowRequestDeleteListener(WorkflowListener workflowListener) {
            WorkflowServiceDataHolder.getInstance().getWorkflowListenerList()
                    .add(workflowListener);
    }

    protected void unsetWorkflowRequestDeleteListener(WorkflowListener workflowListener) {
            WorkflowServiceDataHolder.getInstance().getWorkflowListenerList()
                    .remove(workflowListener);
    }

    protected void setWorkflowExecutorListener(WorkflowExecutorManagerListener workflowListener) {
            WorkflowServiceDataHolder.getInstance().getExecutorListenerList()
                    .add(workflowListener);
    }

    protected void unsetWorkflowExecutorListener(WorkflowExecutorManagerListener workflowListener) {
            WorkflowServiceDataHolder.getInstance().getExecutorListenerList()
                    .remove(workflowListener);
    }



    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }


}
