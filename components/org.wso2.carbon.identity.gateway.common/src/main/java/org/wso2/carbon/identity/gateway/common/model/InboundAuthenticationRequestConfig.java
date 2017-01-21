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

package org.wso2.carbon.identity.gateway.common.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InboundAuthenticationRequestConfig implements Serializable {

    private static final long serialVersionUID = -62766721187073002L;

    private String inboundAuthKey;
    private String inboundAuthType;
    private String inboundConfigType;
    private String friendlyName;
    private Property[] properties = new Property[0];


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
        this.properties = propertySet.toArray(new Property[propertySet.size()]);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
