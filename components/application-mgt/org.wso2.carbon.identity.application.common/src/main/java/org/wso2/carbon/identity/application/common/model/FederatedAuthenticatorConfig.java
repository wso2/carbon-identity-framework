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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Federated authenticator config of the Identity Provider.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FederatedAuthenticatorConfig")
@JsonIgnoreProperties(value = {"valid"})
public class FederatedAuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = -2361107623257323257L;

    @XmlElement(name = "Name")
    protected String name;

    @XmlElement(name = "DisplayName")
    protected String displayName;

    @XmlElement(name = "IsEnabled")
    protected boolean enabled;

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    protected Property[] properties = new Property[0];

    @XmlElement(name = "Tags")
    protected String[] tags;

    public static FederatedAuthenticatorConfig build(OMElement federatedAuthenticatorConfigOM) {

        if (federatedAuthenticatorConfigOM == null) {
            return null;
        }

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();

        Iterator<?> iter = federatedAuthenticatorConfigOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("Name".equals(elementName)) {
                federatedAuthenticatorConfig.setName(element.getText());
            } else if ("DisplayName".equals(elementName)) {
                federatedAuthenticatorConfig.setDisplayName(element.getText());
            } else if ("IsEnabled".equals(elementName)) {
                federatedAuthenticatorConfig.setEnabled(Boolean.parseBoolean(element.getText()));
            } else if ("Tags".equals(elementName)) {
                String[] tagList = StringUtils.split(element.getText(), ",");
                federatedAuthenticatorConfig.setTags(tagList);
            } else if ("Properties".equals(elementName)) {
                Iterator<?> propertiesIter = element.getChildElements();
                List<Property> propertiesArrList = new ArrayList<Property>();

                if (propertiesIter != null) {
                    while (propertiesIter.hasNext()) {
                        OMElement propertiesElement = (OMElement) (propertiesIter.next());
                        propertiesArrList.add(Property.build(propertiesElement));
                    }
                }

                if (CollectionUtils.isNotEmpty(propertiesArrList)) {
                    Property[] propertiesArr = propertiesArrList.toArray(new Property[propertiesArrList.size()]);
                    federatedAuthenticatorConfig.setProperties(propertiesArr);
                }
            }
        }

        return federatedAuthenticatorConfig;
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
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return
     */
    public boolean isValid() {
        return true;
    }

    /**
     * @return
     */
    public Property[] getProperties() {
        return properties;
    }

    /**
     * @param properties
     */
    public void setProperties(Property[] properties) {
        if (properties == null) {
            return;
        }
        Set<Property> propertySet = new HashSet<Property>(Arrays.asList(properties));
        this.properties = propertySet.toArray(new Property[propertySet.size()]);
    }

    /**
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /*
     * <FederatedAuthenticatorConfig> <Name></Name> <DisplayName></DisplayName>
     * <IsEnabled></IsEnabled> <Properties></Properties> </FederatedAuthenticatorConfig>
     */

    /**
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FederatedAuthenticatorConfig)) {
            return false;
        }

        FederatedAuthenticatorConfig that = (FederatedAuthenticatorConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Checks whether given property is not null and property value is not blank
     * @param property
     * @return boolean
     */
    public boolean isValidPropertyValue(Property property) {
        return property != null && StringUtils.isNotBlank(property.getValue());
    }

    /**
     * Get the tag list of the federated authenticator.
     *
     * @return String[]
     */
    public String[] getTags() {

        return tags;
    }

    /**
     * Set the tag list for federated authenticator config.
     *
     * @param tagList tag list of the authenticator.
     */
    public void setTags(String[] tagList) {

        tags = tagList;
    }
}
