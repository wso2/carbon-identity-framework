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

import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParametersMetaData;

/**
 * WorkflowImpl class is a DTO that is represent basic Workflow Implementation
 * based on a Template and the Workflow Implementation specific MetaData
 */
public class WorkflowImpl {
    private String workflowImplId;
    private String workflowImplName;
    private ParametersMetaData parametersMetaData;

    private String templateId;

    public String getWorkflowImplId() {
        return workflowImplId;
    }

    public void setWorkflowImplId(String workflowImplId) {
        this.workflowImplId = workflowImplId;
    }

    public String getWorkflowImplName() {
        return workflowImplName;
    }

    public void setWorkflowImplName(String workflowImplName) {
        this.workflowImplName = workflowImplName;
    }

    public ParametersMetaData getParametersMetaData() {
        return parametersMetaData;
    }

    public void setParametersMetaData(
            ParametersMetaData parametersMetaData) {
        this.parametersMetaData = parametersMetaData;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
