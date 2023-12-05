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


package org.wso2.carbon.identity.workflow.mgt.bean.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Template"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="TemplateId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TemplateName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TemplateDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="ParametersMetaData" type="{http://metadata.bean.mgt.workflow.identity.carbon.wso2.org}ParametersMetaData" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="WorkflowImpl"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="WorkflowImplId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="WorkflowImplName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="WorkflowImplDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="TemplateId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ParametersMetaData" type="{http://metadata.bean.mgt.workflow.identity.carbon.wso2.org}ParametersMetaData" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "template",
    "workflowImpl"
})
@XmlRootElement(name = "MetaData")
public class MetaData {

    @XmlElement(name = "Template")
    protected MetaData.Template template;
    @XmlElement(name = "WorkflowImpl")
    protected MetaData.WorkflowImpl workflowImpl;

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link MetaData.Template }
     *     
     */
    public MetaData.Template getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link MetaData.Template }
     *     
     */
    public void setTemplate(MetaData.Template value) {
        this.template = value;
    }

    /**
     * Gets the value of the workflowImpl property.
     * 
     * @return
     *     possible object is
     *     {@link MetaData.WorkflowImpl }
     *     
     */
    public MetaData.WorkflowImpl getWorkflowImpl() {
        return workflowImpl;
    }

    /**
     * Sets the value of the workflowImpl property.
     * 
     * @param value
     *     allowed object is
     *     {@link MetaData.WorkflowImpl }
     *     
     */
    public void setWorkflowImpl(MetaData.WorkflowImpl value) {
        this.workflowImpl = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="TemplateId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TemplateName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TemplateDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="ParametersMetaData" type="{http://metadata.bean.mgt.workflow.identity.carbon.wso2.org}ParametersMetaData" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "templateId",
        "templateName",
        "templateDescription",
        "parametersMetaData"
    })
    public static class Template {

        @XmlElement(name = "TemplateId", required = true)
        protected String templateId;
        @XmlElement(name = "TemplateName", required = true)
        protected String templateName;
        @XmlElement(name = "TemplateDescription")
        protected String templateDescription;
        @XmlElement(name = "ParametersMetaData")
        protected ParametersMetaData parametersMetaData;

        /**
         * Gets the value of the templateId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTemplateId() {
            return templateId;
        }

        /**
         * Sets the value of the templateId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTemplateId(String value) {
            this.templateId = value;
        }

        /**
         * Gets the value of the templateName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTemplateName() {
            return templateName;
        }

        /**
         * Sets the value of the templateName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTemplateName(String value) {
            this.templateName = value;
        }

        /**
         * Gets the value of the templateDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTemplateDescription() {
            return templateDescription;
        }

        /**
         * Sets the value of the templateDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTemplateDescription(String value) {
            this.templateDescription = value;
        }

        /**
         * Gets the value of the parametersMetaData property.
         * 
         * @return
         *     possible object is
         *     {@link ParametersMetaData }
         *     
         */
        public ParametersMetaData getParametersMetaData() {
            return parametersMetaData;
        }

        /**
         * Sets the value of the parametersMetaData property.
         * 
         * @param value
         *     allowed object is
         *     {@link ParametersMetaData }
         *     
         */
        public void setParametersMetaData(ParametersMetaData value) {
            this.parametersMetaData = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="WorkflowImplId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="WorkflowImplName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="WorkflowImplDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="TemplateId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ParametersMetaData" type="{http://metadata.bean.mgt.workflow.identity.carbon.wso2.org}ParametersMetaData" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "workflowImplId",
        "workflowImplName",
        "workflowImplDescription",
        "templateId",
        "parametersMetaData"
    })
    public static class WorkflowImpl {

        @XmlElement(name = "WorkflowImplId", required = true)
        protected String workflowImplId;
        @XmlElement(name = "WorkflowImplName", required = true)
        protected String workflowImplName;
        @XmlElement(name = "WorkflowImplDescription")
        protected String workflowImplDescription;
        @XmlElement(name = "TemplateId", required = true)
        protected String templateId;
        @XmlElement(name = "ParametersMetaData")
        protected ParametersMetaData parametersMetaData;

        /**
         * Gets the value of the workflowImplId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getWorkflowImplId() {
            return workflowImplId;
        }

        /**
         * Sets the value of the workflowImplId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWorkflowImplId(String value) {
            this.workflowImplId = value;
        }

        /**
         * Gets the value of the workflowImplName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getWorkflowImplName() {
            return workflowImplName;
        }

        /**
         * Sets the value of the workflowImplName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWorkflowImplName(String value) {
            this.workflowImplName = value;
        }

        /**
         * Gets the value of the workflowImplDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getWorkflowImplDescription() {
            return workflowImplDescription;
        }

        /**
         * Sets the value of the workflowImplDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWorkflowImplDescription(String value) {
            this.workflowImplDescription = value;
        }

        /**
         * Gets the value of the templateId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTemplateId() {
            return templateId;
        }

        /**
         * Sets the value of the templateId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTemplateId(String value) {
            this.templateId = value;
        }

        /**
         * Gets the value of the parametersMetaData property.
         * 
         * @return
         *     possible object is
         *     {@link ParametersMetaData }
         *     
         */
        public ParametersMetaData getParametersMetaData() {
            return parametersMetaData;
        }

        /**
         * Sets the value of the parametersMetaData property.
         * 
         * @param value
         *     allowed object is
         *     {@link ParametersMetaData }
         *     
         */
        public void setParametersMetaData(ParametersMetaData value) {
            this.parametersMetaData = value;
        }

    }

}
