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

/**
 * Flow configuration model class.
 */
public class FlowConfigDTO {

    private String flowType;
    private Boolean isEnabled;
    private final Map<String, String> flowCompletionConfigs = new HashMap<>();

    /**
     * Get the flow type.
     *
     * @return Flow type.
     */
    public String getFlowType() {

        return flowType;
    }

    /**
     * Set the flow type.
     *
     * @param flowType Flow type.
     */
    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    /**
     * Get whether the flow is enabled or not.
     *
     * @return True if the flow is enabled, false otherwise.
     */
    public Boolean getIsEnabled() {

        return isEnabled;
    }

    /**
     * Set whether the flow is enabled or not.
     *
     * @param enabled True if the flow is enabled, false otherwise.
     */
    public void setIsEnabled(Boolean enabled) {

        isEnabled = enabled;
    }

    /**
     * Add a flow completion configuration.
     *
     * @param flowCompletionConfig Flow completion configuration enum.
     * @param value                Value of the flow completion configuration.
     */
    public void addFlowCompletionConfig(Constants.FlowCompletionConfig flowCompletionConfig, String value) {

        if (flowCompletionConfig != null) {
            this.flowCompletionConfigs.put(flowCompletionConfig.getConfig(), value);
        }
    }

    /**
     * Add all flow completion configurations from a list.
     *
     * @param flowCompletionConfigs List of flow completion configuration enums.
     */
    public void addAllFlowCompletionConfigs(ArrayList<Constants.FlowCompletionConfig> flowCompletionConfigs) {

        for (Constants.FlowCompletionConfig flowCompletionConfig : flowCompletionConfigs) {
            addFlowCompletionConfig(flowCompletionConfig, flowCompletionConfig.getDefaultValue());
        }
    }

    /**
     * Add all flow completion configurations from a map.
     *
     * @param configs Map of flow completion configurations.
     */
    public void addAllFlowCompletionConfigs(Map<String, String> configs) {

        this.flowCompletionConfigs.putAll(configs);
    }

    /**
     * Get specific flow completion configurations.
     *
     * @param configList List of flow completion configuration enums.
     * @return Map of flow completion configurations.
     */
    public Map<Constants.FlowCompletionConfig, String> getFlowCompletionConfigs(
            ArrayList<Constants.FlowCompletionConfig> configList) {

        Map<Constants.FlowCompletionConfig, String> selectedConfigs = new HashMap<>();
        for (Constants.FlowCompletionConfig config : configList) {
            if (flowCompletionConfigs.containsKey(config.getConfig())) {
                selectedConfigs.put(config, flowCompletionConfigs.get(config.getConfig()));
            }
        }
        return selectedConfigs;
    }

    /**
     * Get a specific flow completion configuration.
     *
     * @param flowCompletionConfig Flow completion configuration enum.
     * @return Value of the flow completion configuration.
     */
    public String getFlowCompletionConfig(Constants.FlowCompletionConfig flowCompletionConfig) {

        String value = flowCompletionConfigs.get(flowCompletionConfig.getConfig());
        return value != null ? value : flowCompletionConfig.getDefaultValue();
    }

    /**
     * Check if a specific flow completion configuration is present.
     *
     * @param flowCompletionConfig Flow completion configuration enum.
     * @return True if the flow completion configuration is present, false otherwise.
     */
    public boolean isFlowCompletionConfigPresent(Constants.FlowCompletionConfig flowCompletionConfig) {

        return flowCompletionConfigs.containsKey(flowCompletionConfig.getConfig());
    }

    /**
     * Get all flow completion configurations.
     *
     * @return Map of flow completion configurations.
     */
    public Map<String, String> getAllFlowCompletionConfigs() {

        return flowCompletionConfigs;
    }
}
