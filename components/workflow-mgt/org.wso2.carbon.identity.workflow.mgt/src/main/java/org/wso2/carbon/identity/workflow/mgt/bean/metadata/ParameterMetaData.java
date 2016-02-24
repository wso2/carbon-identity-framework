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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ParameterMetaData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterMetaData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="DefaultValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="RegExForValidate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="InputData" type="{http://metadata.bean.mgt.workflow.identity.carbon.wso2.org}InputData" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DataType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="InputType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="CustomInputType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="isRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="Validate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="isInputDataRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterMetaData", propOrder = {
    "displayName",
    "defaultValue",
    "regExForValidate",
    "inputData"
})
public class ParameterMetaData {

    @XmlElement(name = "DisplayName", required = true)
    protected String displayName;
    @XmlElement(name = "DefaultValue")
    protected String defaultValue;
    @XmlElement(name = "RegExForValidate")
    protected String regExForValidate;
    @XmlElement(name = "InputData")
    protected InputData inputData;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "DataType")
    protected String dataType;
    @XmlAttribute(name = "InputType")
    protected String inputType;
    @XmlAttribute(name = "CustomInputType")
    protected String customInputType;
    @XmlAttribute(name = "isRequired")
    protected Boolean isRequired;
    @XmlAttribute(name = "Validate")
    protected Boolean validate;
    @XmlAttribute(name = "isInputDataRequired")
    protected Boolean isInputDataRequired;

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the regExForValidate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegExForValidate() {
        return regExForValidate;
    }

    /**
     * Sets the value of the regExForValidate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegExForValidate(String value) {
        this.regExForValidate = value;
    }

    /**
     * Gets the value of the inputData property.
     * 
     * @return
     *     possible object is
     *     {@link InputData }
     *     
     */
    public InputData getInputData() {
        return inputData;
    }

    /**
     * Sets the value of the inputData property.
     * 
     * @param value
     *     allowed object is
     *     {@link InputData }
     *     
     */
    public void setInputData(InputData value) {
        this.inputData = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataType(String value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the inputType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInputType() {
        return inputType;
    }

    /**
     * Sets the value of the inputType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInputType(String value) {
        this.inputType = value;
    }

    /**
     * Gets the value of the customInputType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomInputType() {
        return customInputType;
    }

    /**
     * Sets the value of the customInputType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomInputType(String value) {
        this.customInputType = value;
    }

    /**
     * Gets the value of the isRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsRequired() {
        if (isRequired == null) {
            return false;
        } else {
            return isRequired;
        }
    }

    /**
     * Sets the value of the isRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRequired(Boolean value) {
        this.isRequired = value;
    }

    /**
     * Gets the value of the validate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isValidate() {
        if (validate == null) {
            return true;
        } else {
            return validate;
        }
    }

    /**
     * Sets the value of the validate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValidate(Boolean value) {
        this.validate = value;
    }

    /**
     * Gets the value of the isInputDataRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsInputDataRequired() {
        if (isInputDataRequired == null) {
            return false;
        } else {
            return isInputDataRequired;
        }
    }

    /**
     * Sets the value of the isInputDataRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsInputDataRequired(Boolean value) {
        this.isInputDataRequired = value;
    }

}
