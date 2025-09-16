/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.mgt.model;

import org.wso2.carbon.identity.flow.mgt.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.flow.mgt.Constants.Properties.IS_AUTO_LOGIN_ENABLED;

/**
 * Flow configuration model class.
 */
public class FlowConfigDTO {

    private String flowType;
    private Boolean isEnabled;
    private final Map<String, String> properties = new HashMap<>();

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public Boolean getIsEnabled() {

        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {

        isEnabled = enabled;
    }

    public Boolean getIsAutoLoginEnabled() {

        return Boolean.parseBoolean(getProperty(IS_AUTO_LOGIN_ENABLED));
    }

    public void setIsAutoLoginEnabled(Boolean autoLoginEnabled) {

        addProperty(IS_AUTO_LOGIN_ENABLED, String.valueOf(autoLoginEnabled));
    }

    public void addProperty(Constants.Properties property, String value) {

        if (property != null) {
            this.properties.put(property.getName(), value);
        }
    }

    public void addAllProperties(ArrayList<Constants.Properties> properties) {

        for (Constants.Properties flag : properties) {
            addProperty(flag, flag.getDefaultValue());
        }
    }

    public void addAllProperties(Map<String, String> properties) {

        this.properties.putAll(properties);
    }

    public Map<Constants.Properties, String> getProperties(ArrayList<Constants.Properties> propertyList) {

        Map<Constants.Properties, String> selectedFlags = new HashMap<>();
        for (Constants.Properties flag : propertyList) {
            if (properties.containsKey(flag.getName())) {
                selectedFlags.put(flag, properties.get(flag.getName()));
            }
        }
        return selectedFlags;
    }

    public String getProperty(Constants.Properties property) {

        return properties.get(property.getName());
    }

    public Map<String, String> getAllProperties() {

        return properties;
    }
}
