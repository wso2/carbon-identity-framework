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
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InboundAuthenticationRequestConfig implements Serializable {

    private static final long serialVersionUID = -62766721187073002L;

    private String inboundAuthKey;
    private String inboundAuthType;
    private String friendlyName;
    private Property[] properties = new Property[0];

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
            } else if ("friendlyName".equalsIgnoreCase(member.getLocalName())) {
                inboundAuthenticationRequestConfig.setFriendlyName(member.getText());
            } else if ("Properties".equalsIgnoreCase(member.getLocalName())) {
                Iterator<?> propertiesIter = member.getChildElements();
                List<Property> propertiesArrList = new ArrayList<>();

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
        this.properties = propertySet.toArray(new Property[propertySet.size()]);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
