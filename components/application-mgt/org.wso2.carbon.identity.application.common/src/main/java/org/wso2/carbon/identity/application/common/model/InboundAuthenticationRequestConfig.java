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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.annotation.IgnoreNullElement;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Inbound authentication request configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "InboundAuthenticationRequestConfig")
public class InboundAuthenticationRequestConfig implements Serializable {

    private static final long serialVersionUID = -62766721187073002L;

    @XmlElement(name = "InboundAuthKey")
    private String inboundAuthKey;

    @XmlElement(name = "InboundAuthType")
    private String inboundAuthType;

    @XmlElement(name = "InboundConfigType")
    private String inboundConfigType;

    @XmlElement(name = "friendlyName")
    private String friendlyName;

    @JsonIgnore
    @XmlElement(name = "inboundConfiguration", nillable = true)
    private String inboundConfiguration;

    @XmlElement(name = "InboundConfigurationProtocol", nillable = true)
    private InboundConfigurationProtocol inboundConfigurationProtocol;

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    private Property[] properties = new Property[0];
    
    // This is used to store the data related to the inbound protocol. This is not persisted in the database.
    // We use this for auditing purposes.
    @IgnoreNullElement
    @XmlTransient
    @JsonIgnore
    private Map<String, Object> data;

    /*
     * <InboundAuthenticationRequestConfig> <InboundAuthKey></InboundAuthKey>
     * <InboundAuthType></InboundAuthType> <Properties></Properties>
     * </InboundAuthenticationRequestConfig>
     */
    public static InboundAuthenticationRequestConfig build(
            OMElement inboundAuthenticationRequestConfigOM) {

        if (inboundAuthenticationRequestConfigOM == null) {
            return null;
        }

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig;
        inboundAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();

        Iterator<?> members = inboundAuthenticationRequestConfigOM.getChildElements();

        while (members.hasNext()) {
            OMElement member = (OMElement) members.next();

            if ("InboundAuthKey".equalsIgnoreCase(member.getLocalName())) {
                inboundAuthenticationRequestConfig.setInboundAuthKey(member.getText());
            } else if ("InboundAuthType".equalsIgnoreCase(member.getLocalName())) {
                inboundAuthenticationRequestConfig.setInboundAuthType(member.getText());
            } else if ("InboundConfigType".equalsIgnoreCase(member.getLocalName())) {
                inboundAuthenticationRequestConfig.setInboundConfigType(member.getText());
            } else if ("friendlyName".equalsIgnoreCase(member.getLocalName())) {
                inboundAuthenticationRequestConfig.setFriendlyName(member.getText());
            } else if ("Properties".equalsIgnoreCase(member.getLocalName())) {
                Iterator<?> propertiesIter = member.getChildElements();
                List<Property> propertiesArrList = new ArrayList<Property>();

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
                    inboundAuthenticationRequestConfig.setProperties(propertiesArr);
                }
            }
        }
        return inboundAuthenticationRequestConfig;
    }

    /**
     * @return
     */
    public String getInboundAuthKey() {
        return inboundAuthKey;
    }

    /**
     * @param inboundAuthKey
     */
    public void setInboundAuthKey(String inboundAuthKey) {
        this.inboundAuthKey = inboundAuthKey;
    }

    /**
     * @return
     */
    public String getInboundAuthType() {
        return inboundAuthType;
    }

    /**
     * @param inboundAuthType
     */
    public void setInboundAuthType(String inboundAuthType) {
        this.inboundAuthType = inboundAuthType;
    }

    /**
     *
     * @return inboundUIType
     */
    public String getInboundConfigType() {
        return inboundConfigType;
    }

    /**
     * Sets the UIType of the inbound authentication config.
     * @param inboundConfigType
     */
    public void setInboundConfigType(String inboundConfigType) {
        this.inboundConfigType = inboundConfigType;
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
        this.properties = sortPropertiesByDisplayOrder(propertySet);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    private Property[] sortPropertiesByDisplayOrder(Set<Property> propertySet) {

        List<Property> list = new ArrayList(propertySet);
        list.sort(new Comparator<Property>() {
            @Override
            public int compare(Property pro1, Property pro2) {

                return Integer.compare(pro1.getDisplayOrder(), pro2.getDisplayOrder());
            }
        });

        return list.toArray(new Property[0]);
    }

    public String getInboundConfiguration() {

        return inboundConfiguration;
    }

    public void setInboundConfiguration(String inboundConfiguration) {

        this.inboundConfiguration = inboundConfiguration;
    }

    public InboundConfigurationProtocol getInboundConfigurationProtocol() {

        return this.inboundConfigurationProtocol;
    }

    public void setInboundConfigurationProtocol(InboundConfigurationProtocol inboundConfigurationProtocol) {

        this.inboundConfigurationProtocol = inboundConfigurationProtocol;
    }
    
    public Map<String, Object> getData() {
        
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        
        this.data = data;
    }
}
