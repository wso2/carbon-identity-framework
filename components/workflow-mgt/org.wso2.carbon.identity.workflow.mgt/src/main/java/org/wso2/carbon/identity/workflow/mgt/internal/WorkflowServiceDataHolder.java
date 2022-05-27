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
import org.wso2.carbon.identity.workflow.mgt.WorkflowExecutorManagerService;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowServiceDataHolder {

    private static WorkflowServiceDataHolder instance = new WorkflowServiceDataHolder();
    private static final Log log = LogFactory.getLog(WorkflowServiceDataHolder.class);

    private RealmService realmService;
    private ConfigurationContextService configurationContextService;
    private BundleContext bundleContext;

    private Map<String, WorkflowRequestHandler> workflowRequestHandlers =  new HashMap<String, WorkflowRequestHandler>();

    private List<WorkflowListener> workflowListenerList = new ArrayList<>();
    private List<WorkflowExecutorManagerListener> executorListenerList = new ArrayList<>();

    public List<WorkflowListener> getWorkflowListenerList() {
        return workflowListenerList;
    }

    public void setWorkflowListenerList(
            List<WorkflowListener> workflowListenerList) {
        this.workflowListenerList = workflowListenerList;
    }

    public List<WorkflowExecutorManagerListener> getExecutorListenerList() {
        return executorListenerList;
    }

    public void setExecutorListenerList(
            List<WorkflowExecutorManagerListener> executorListenerList) {
        this.executorListenerList = executorListenerList;
    }

    private Map<String, AbstractTemplate> templates = new HashMap<String, AbstractTemplate>();
    private Map<String, Map<String,AbstractWorkflow>> workflowImpls = new HashMap<String, Map<String,AbstractWorkflow>>();

    private WorkflowManagementService workflowService = null ;

    private WorkflowServiceDataHolder() {

    }

    public ConfigurationContextService getConfigurationContextService() {

        return configurationContextService;
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {

        this.configurationContextService = configurationContextService;
    }

    public BundleContext getBundleContext() {

        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public static WorkflowServiceDataHolder getInstance() {

        return instance;
    }

    public void addTemplate(AbstractTemplate template) {
        templates.put(template.getTemplateId(), template);
    }

    public void removeTemplate(AbstractTemplate template) {
        if (template != null && template.getTemplateId() != null) {
            templates.remove(template.getTemplateId());
        }
    }


    public Map<String, AbstractTemplate> getTemplates() {
        return templates;
    }

    public Map<String, Map<String,AbstractWorkflow>> getWorkflowImpls() {
        return workflowImpls;
    }

    public void addWorkflowImplementation(AbstractWorkflow abstractWorkflow) {
        Map<String, AbstractWorkflow> abstractWorkflowMap = workflowImpls.get(abstractWorkflow.getTemplateId());
        if(abstractWorkflowMap == null){
            abstractWorkflowMap = new HashMap<>();
            workflowImpls.put(abstractWorkflow.getTemplateId(),abstractWorkflowMap);
        }
        abstractWorkflowMap.put(abstractWorkflow.getWorkflowImplId(),abstractWorkflow);
    }

    public void removeWorkflowImplementation(AbstractWorkflow abstractWorkflow) {

        if (abstractWorkflow != null && abstractWorkflow.getWorkflowImplId() != null) {
            workflowImpls.remove(abstractWorkflow.getWorkflowImplId());
        }
    }


    public void addWorkflowRequestHandler(WorkflowRequestHandler requestHandler) {

        if (requestHandler != null) {
            workflowRequestHandlers.put(requestHandler.getEventId(), requestHandler);
        }
    }

    public void removeWorkflowRequestHandler(WorkflowRequestHandler requestHandler) {

        if (requestHandler != null) {
            workflowRequestHandlers.remove(requestHandler.getEventId());
        }
    }

    public WorkflowRequestHandler getRequestHandler(String eventId) {

        return workflowRequestHandlers.get(eventId);
    }

    public List<WorkflowRequestHandler> listRequestHandlers() {

        return new ArrayList<>(workflowRequestHandlers.values());
    }


    public WorkflowManagementService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowManagementService workflowService) {
        this.workflowService = workflowService;
    }
    private WorkflowExecutorManagerService workflowExecutorManagerService = null;

    public WorkflowExecutorManagerService getWorkflowExecutorManagerService() {

        return workflowExecutorManagerService;
    }

    public void setWorkflowExecutorManagerService(WorkflowExecutorManagerService workflowExecutorManagerService) {

        this.workflowExecutorManagerService = workflowExecutorManagerService;
    }
}

