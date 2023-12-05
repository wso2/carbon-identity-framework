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

package org.wso2.carbon.identity.workflow.mgt.dto;

import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;

/**
 * WorkflowWizard is a DTO that represent the complete Workflow related information in one class.
 * UI Workflow Wizard based on this class
 *
 */
public class WorkflowWizard{

    private String workflowId;
    private String workflowName;
    private String workflowDescription;
    private String templateId;
    private String workflowImplId;

    private Template template ;
    private WorkflowImpl workflowImpl;

    private Parameter[] templateParameters ;
    private Parameter[] workflowImplParameters ;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }



    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getWorkflowImplId() {
        return workflowImplId;
    }

    public void setWorkflowImplId(String workflowImplId) {
        this.workflowImplId = workflowImplId;
    }


    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public WorkflowImpl getWorkflowImpl() {
        return workflowImpl;
    }

    public void setWorkflowImpl(WorkflowImpl workflowImpl) {
        this.workflowImpl = workflowImpl;
    }

    public Parameter[] getTemplateParameters() {
        return templateParameters;
    }

    public void setTemplateParameters(Parameter[] templateParameters) {
        this.templateParameters = templateParameters;
    }

    public Parameter[] getWorkflowImplParameters() {
        return workflowImplParameters;
    }

    public void setWorkflowImplParameters(Parameter[] workflowImplParameters) {
        this.workflowImplParameters = workflowImplParameters;
    }
}
