/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class to store sub-properties of {@link Property} model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SubProperty")
public class SubProperty implements Serializable {

    private static final long serialVersionUID = 2423059979331364604L;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Value")
    private String value;

    @XmlElement(name = "IsConfidential")
    private boolean isConfidential;

    @XmlElement(name = "DefaultValue")
    private String defaultValue;

    @XmlElement(name = "DisplayName")
    private String displayName;

    @XmlElement(name = "Required")
    private boolean required;

    @XmlElement(name = "DefaultValue")
    private String description;

    @XmlElement(name = "Type")
    private String type;

    @XmlElement(name = "DisplayOrder")
    private int displayOrder;

    @XmlElement(name = "IsAdvanced")
    private boolean isAdvanced;

    @XmlElement(name = "Regex")
    private String regex;

    @XmlElementWrapper(name = "Options")
    @XmlElement(name = "Option")
    private String[] options = new String[0];

    /*
     * <SubProperty> <Name></Name> <Value></Value> <IsConfidential></IsConfidential>
     * <DefaultValue></DefaultValue> <DisplayName></DisplayName> <Required></Required>
     * <Description></Description> </SubProperty>
    */
    public static SubProperty build(OMElement propertyOM) {

        if (propertyOM == null) {
            return null;
        }

        SubProperty property = new SubProperty();

        Iterator<?> iter = propertyOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("Name".equals(elementName)) {
                property.setName(element.getText());
            } else if ("Value".equals(elementName)) {
                property.setValue(element.getText());
            } else if ("IsConfidential".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    property.setConfidential(Boolean.parseBoolean(element.getText()));
                }
            } else if ("defaultValue".equals(elementName)) {
                property.setDefaultValue(element.getText());
            } else if ("DisplayName".equals(elementName)) {
                property.setDisplayName(element.getText());
            } else if ("Required".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    property.setRequired(Boolean.parseBoolean(element.getText()));
                }
            } else if ("Description".equals(elementName)) {
                property.setDescription(element.getText());
            } else if ("DisplayOrder".equals(elementName)) {
                property.setDisplayOrder(Integer.parseInt(element.getText()));
            } else if ("Type".equals(elementName)) {
                property.setType(element.getText());
            } else if ("IsAdvanced".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    property.setAdvanced(Boolean.parseBoolean(element.getText()));
                }
            } else if ("Regex".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    property.setRegex(element.getText());
                }
            } else if ("Options".equals(elementName)) {
                Iterator<?> optionsIter = element.getChildElements();
                List<String> optionsArrList = new ArrayList<>();

                while (optionsIter.hasNext()) {
                    OMElement optionsElement = (OMElement) (optionsIter.next());
                    if (optionsElement.getText() != null) {
                        optionsArrList.add(optionsElement.getText());
                    }
                }
                if (CollectionUtils.isNotEmpty(optionsArrList)) {
                    String[] optionsArr = optionsArrList.toArray(new String[0]);
                    property.setOptions(optionsArr);
                }
            }
        }
        return property;
    }

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean isConfidential() {

        return isConfidential;
    }

    public void setConfidential(boolean isConfidential) {

        this.isConfidential = isConfidential;
    }

    public String getDefaultValue() {

        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {

        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public int getDisplayOrder() {

        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {

        this.displayOrder = displayOrder;
    }

    public boolean isAdvanced() {

        return isAdvanced;
    }

    public void setAdvanced(boolean isAdvanced) {

        this.isAdvanced = isAdvanced;
    }

    public String getRegex() {

        return regex;
    }

    public void setRegex(String regex) {

        this.regex = regex;
    }

    public String[] getOptions() {

        return options;
    }

    public void setOptions(String[] options) {

        this.options = options;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof SubProperty)) {
            return false;
        }

        SubProperty property = (SubProperty) o;

        return (name != null ? name.equals(property.name) : property.name == null) &&
                (value != null ? value.equals(property.value) : property.value == null);
    }

    @Override
    public int hashCode() {

        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
