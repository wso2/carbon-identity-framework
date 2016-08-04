/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;
import java.util.Iterator;

public class Property implements Serializable {

    private static final long serialVersionUID = 2423059969331364604L;

    private String name;
    private String value;
    private boolean isConfidential;
    private String defaultValue;
    private String displayName;
    private boolean required;
    private String description;
    private String type;
    private int displayOrder;

    public Property() {

    }

    /*
         * <Property> <Name></Name> <Value></Value> <IsConfidential></IsConfidential>
         * <DefaultValue></DefaultValue> <DisplayName></DisplayName> <Required></Required>
         * <Description></Description> </Property>
         */
    public static Property build(OMElement propertyOM) {

        if (propertyOM == null) {
            return null;
        }

        Property property = new Property();

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
            }
        }

        return property;
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public boolean isConfidential() {
        return isConfidential;
    }

    /**
     * @param isConfidential
     */
    public void setConfidential(boolean isConfidential) {
        this.isConfidential = isConfidential;
    }

    /**
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Property)) {
            return false;
        }

        Property property = (Property) o;

        if (name != null ? !name.equals(property.name) : property.name != null) {
            return false;
        }
        if (value != null ? !value.equals(property.value) : property.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
