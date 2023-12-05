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

package org.wso2.carbon.identity.workflow.mgt.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.MetaData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParametersMetaData;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;

import javax.xml.bind.JAXBException;

/**
 * Abstract Template class can be extend to create concrete
 * template that is providing its own details and metadata
 */
public abstract class AbstractTemplate {

    private Log log = LogFactory.getLog(AbstractTemplate.class);
    private ParametersMetaData parametersMetaData = null;
    private MetaData metaData;

    protected abstract InputData getInputData(String parameterName) throws WorkflowException;

    /**
     * AbstractTemplate Constructor with metadata xml string parameter
     *
     * @param metaDataXML metadata xml string that is validated against ParameterMetaData.xsd
     * @throws WorkflowRuntimeException
     */
    public AbstractTemplate(String metaDataXML) throws WorkflowRuntimeException {
        try {
            this.metaData = WorkflowManagementUtil.unmarshalXML(metaDataXML, MetaData.class);
            if (this.metaData == null || this.metaData.getTemplate() == null) {
                throw new WorkflowRuntimeException("Error occurred while Loading Template Meta Data");
            }
            this.parametersMetaData = this.metaData.getTemplate().getParametersMetaData();
        } catch (JAXBException e) {
            String errorMsg = "Error occured while converting template parameter data to object : " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
    }

    /**
     * Retrieve template specific metadata
     *
     * @return ParametersMetaData object that is contain all the template specific parameter metadata.
     */
    public ParametersMetaData getParametersMetaData() throws WorkflowException {
        if (parametersMetaData != null) {
            ParameterMetaData[] parameterMetaData = parametersMetaData.getParameterMetaData();
            for (ParameterMetaData metaData : parameterMetaData) {
                if (metaData.isIsInputDataRequired()) {
                    InputData inputData = getInputData(metaData.getName());
                    metaData.setInputData(inputData);
                }
            }
        }
        return parametersMetaData;
    }

    public void setParametersMetaData(
            ParametersMetaData parametersMetaData) {
        this.parametersMetaData = parametersMetaData;
    }

    /**
     * Template Id is unique representation of the template
     *
     * @return String templateId
     */
    public String getTemplateId() {
        return this.metaData.getTemplate().getTemplateId();
    }

    /**
     * Template Name
     *
     * @return String Template Name
     */
    public String getName() {
        return this.metaData.getTemplate().getTemplateName();
    }

    /**
     * Template Description
     *
     * @return String description
     */
    public String getDescription() {
        return this.metaData.getTemplate().getTemplateDescription();
    }
}
