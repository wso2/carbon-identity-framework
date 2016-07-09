/*
 *Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.model.Property;

/**
 * To get the unique key, either we have to set a value to AuthKey or
 * set a key to relying party and read through the property or
 * get the sp name as the key.
 */
public abstract class AbstractInboundAuthenticatorConfig {

    /**
     * Get AuthKey. This is already deprecated and still implemented here to make this backward compatible.
     *
     * @return name
     */
    @Deprecated
    public String getAuthKey() {
        return null;
    }

    /**
     * Get Type
     *
     * @return type
     */
    public abstract String getName();

    /**
     * Get friendly name
     *
     * @return friendly name
     */
    public abstract String getFriendlyName();

    /**
     * Get configurations
     *
     * @return property array
     */
    public abstract Property[] getConfigurationProperties();

    /**
     * Relying party key.
     *
     * @return name
     */
    public String getRelyingPartyKey() {
        return null;
    }

    /**
     * Check whether the RelyingPartyKey is configured with the UI properties.
     *
     * @return
     */
    public boolean isReplyingPartyKeyConfigured() {
        Property[] configurationProperties = getConfigurationProperties();
        if (configurationProperties != null) {
            for (Property property : configurationProperties) {
                if (property != null && property.getName() != null && property.getName().equals(getRelyingPartyKey())) {
                    return true;
                }
            }
        }
        return false;
    }
}