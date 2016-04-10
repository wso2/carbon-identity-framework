/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.model;

import java.util.Properties;

public class IdentityEventListenerConfig {

    private int order;
    private String enable;
    private IdentityEventListenerConfigKey identityEventListenerConfigKey;
    private Properties properties = new Properties();

    public IdentityEventListenerConfig(String enable, int order, IdentityEventListenerConfigKey
            identityEventListenerConfigKey, Properties properties) {
        this.order = order;
        this.enable = enable;
        this.identityEventListenerConfigKey = identityEventListenerConfigKey;
        if(properties != null && !properties.isEmpty()) {
            this.properties = properties;
        }
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public IdentityEventListenerConfigKey getIdentityEventListenerConfigKey() {
        return identityEventListenerConfigKey;
    }

    public void setIdentityEventListenerConfigKey(IdentityEventListenerConfigKey identityEventListenerConfigKey) {
        this.identityEventListenerConfigKey = identityEventListenerConfigKey;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
