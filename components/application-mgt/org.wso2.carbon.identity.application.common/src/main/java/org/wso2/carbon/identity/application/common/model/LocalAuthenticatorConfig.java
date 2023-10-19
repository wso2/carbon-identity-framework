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
import org.wso2.carbon.identity.base.IdentityConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Local authenticator configuration of an application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "LocalAuthenticatorConfig")
@JsonIgnoreProperties(value = {"valid"})
public class LocalAuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = 3363298518257599291L;

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

    /*
     * <LocalAuthenticatorConfig> <Name></Name> <DisplayName></DisplayName> <IsEnabled></IsEnabled>
     * <Properties></Properties> </LocalAuthenticatorConfig>
     */
    public static LocalAuthenticatorConfig build(OMElement localAuthenticatorConfigOM) {
        LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();

        if (localAuthenticatorConfigOM == null) {
            return new LocalAuthenticatorConfig();
        }

        Iterator<?> members = localAuthenticatorConfigOM.getChildElements();

        while (members.hasNext()) {

            OMElement member = (OMElement) members.next();


            if ("Name".equals(member.getLocalName())) {
                localAuthenticatorConfig.setName(member.getText());
            } else if ("DisplayName".equals(member.getLocalName())) {
                localAuthenticatorConfig.setDisplayName(member.getText());
            } else if ("IsEnabled".equals(member.getLocalName())) {
                if (member.getText() != null && member.getText().trim().length() > 0) {
                    localAuthenticatorConfig.setEnabled(Boolean.parseBoolean(member.getText()));
                }
            } else if (IdentityConstants.TAGS.equals(member.getLocalName())) {
                String[] tagList = StringUtils.split(member.getText(), ",");
                localAuthenticatorConfig.setTags(tagList);
            } else if ("Properties".equals(member.getLocalName())) {

                Iterator<?> propertiesIter = member.getChildElements();
                ArrayList<Property> propertiesArrList = new ArrayList<Property>();

                if (propertiesIter != null) {
                    while (propertiesIter.hasNext()) {
                        OMElement propertiesElement = (OMElement) (propertiesIter.next());
                        Property prop = Property.build(propertiesElement);
                        if (prop != null) {
                            propertiesArrList.add(prop);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(propertiesArrList)) {
                    Property[] propertiesArr = propertiesArrList.toArray(new Property[0]);
                    localAuthenticatorConfig.setProperties(propertiesArr);
                }
            }
        }
        return localAuthenticatorConfig;
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
        Set<Property> propertySet = new HashSet<>(Arrays.asList(properties));
        this.properties = propertySet.toArray(new Property[0]);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalAuthenticatorConfig)) {
            return false;
        }
        LocalAuthenticatorConfig that = (LocalAuthenticatorConfig) o;

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
     * Get the tag list of the Local authenticator.
     *
     * @return String[]
     */
    public String[] getTags() {

        return tags;
    }

    /**
     * Set the tag list for Local authenticator config.
     *
     * @param tagList tag list of the authenticator.
     */
    public void setTags(String[] tagList) {

        tags = tagList;
    }
}
