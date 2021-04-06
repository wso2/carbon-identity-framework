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

package org.wso2.carbon.identity.workflow.mgt.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.MetaData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParametersMetaData;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * AbstractWorkflow can be used to implement different workflow implementation based on different template implementation
 *
 * TemplateInitializer and WorkFlowExecutor should be provided to execute
 *
 */
public abstract class AbstractWorkflow {

    private Log log = LogFactory.getLog(AbstractWorkflow.class);

    private MetaData metaData = null;
    private ParametersMetaData parametersMetaData = null ;

    private Class<? extends TemplateInitializer> templateInitializerClass;
    private Class<? extends WorkFlowExecutor> workFlowExecutorClass;

    /**
     *
     *
     * @param metaDataXML Parameter Metadata XML string
     * @throws WorkflowRuntimeException
     */
    public AbstractWorkflow(Class<? extends TemplateInitializer> templateInitializerClass, Class<? extends
            WorkFlowExecutor> workFlowExecutorClass, String metaDataXML) throws WorkflowRuntimeException {
        try {

            this.templateInitializerClass = templateInitializerClass;
            this.workFlowExecutorClass = workFlowExecutorClass;

            this.metaData = WorkflowManagementUtil.unmarshalXML(metaDataXML, MetaData.class);
            if (this.metaData == null || this.metaData.getWorkflowImpl() == null) {
                throw new WorkflowRuntimeException("Error occurred while Loading WorkflowImpl Meta Data");
            }
            this.parametersMetaData = this.metaData.getWorkflowImpl().getParametersMetaData();
        } catch (JAXBException e) {
            String errorMsg = "Error occurred while converting workflow parameter data to object : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
    }

    /**
     * Deploy artifacts of a workflow
     *
     * @param parameterList Parameter of the workflow
     * @throws WorkflowException
     */
    public void deploy(List <Parameter> parameterList) throws WorkflowException {

        TemplateInitializer initializer = getTemplateInitializer();
        if (initializer != null) {
            initializer.initialize(parameterList);
        }
    }


    /**
     * Execute workflow request at profile
     *
     * @param workFlowRequest
     * @throws WorkflowException
     */
    public void execute(WorkflowRequest workFlowRequest, List<Parameter> parameterList) throws WorkflowException {

        WorkFlowExecutor executor = getWorkFlowExecutor();
        if (executor != null) {
            executor.initialize(parameterList);
            executor.execute(workFlowRequest);
        }
    }

    /**
     * Can be retrieve workflow implementation specific meta data
     *
     * @return
     * @throws WorkflowException
     */
    public ParametersMetaData getParametersMetaData() throws WorkflowException{
        if (parametersMetaData != null) {
            ParameterMetaData[] parameterMetaData = parametersMetaData.getParameterMetaData();
            for (ParameterMetaData metaData: parameterMetaData){
                if (metaData.isIsInputDataRequired()) {
                    InputData inputData = getInputData(metaData);
                    metaData.setInputData(inputData);
                }
            }
        }
        return parametersMetaData;
    }


    /**
     * To provide Workflow implementation specific data can be load by this method. Implementation depend on the
     * parameter metadata.
     *
     * @param parameterMetaData
     * @return
     * @throws WorkflowException
     */
    protected abstract InputData getInputData(ParameterMetaData parameterMetaData) throws WorkflowException;


    /**
     * Get the ID of current template
     *
     * @return
     */
    public String getTemplateId() {
        return this.metaData.getWorkflowImpl().getTemplateId();
    }

    public void setTemplateId(String templateId) {
        this.metaData.getWorkflowImpl().setTemplateId(templateId);
    }

    public String getWorkflowImplId() {
        return this.metaData.getWorkflowImpl().getWorkflowImplId();
    }

    public String getWorkflowImplName() {
        return this.metaData.getWorkflowImpl().getWorkflowImplName();
    }

    public String getDescription() {
        return this.metaData.getWorkflowImpl().getWorkflowImplDescription();
    }

    public WorkFlowExecutor getWorkFlowExecutor() {
        WorkFlowExecutor workFlowExecutor = null;
        try {
            workFlowExecutor = workFlowExecutorClass.newInstance();
        } catch (InstantiationException e) {
            String errorMsg = "Error occurred while initializing WorkFlowExecutor : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Error occurred while initializing WorkFlowExecutor : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
        return workFlowExecutor;
    }



    public TemplateInitializer getTemplateInitializer() throws WorkflowRuntimeException {
        TemplateInitializer templateInitializer = null;
        try {
            templateInitializer = templateInitializerClass.newInstance();
        } catch (InstantiationException e) {
            String errorMsg = "Error occurred while initializing TemplateInitializer : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Error occurred while initializing TemplateInitializer : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
        return templateInitializer;
    }


}
