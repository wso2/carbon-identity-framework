/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.provider;

import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;


/**
 * This interface defines the contract for compatibility setting configuration providers.
 */
public interface CompatibilitySettingConfigurationProvider {

    /**
     * Get name of the provider.
     *
     * @return Name of the provider.
     */
    String getName();

    /**
     * Get priority of the provider.
     *
     * @return Priority of the provider.
     */
    int getPriority();

    /**
     * Get configurations for all compatibility settings.
     *
     * @param tenantDomain Tenant domain that data is requested for.
     * @return A map of compatibility settings.
     * @throws CompatibilitySettingException If an error occurs while retrieving the configurations.
     */
    CompatibilitySetting getConfigurations(String tenantDomain) throws CompatibilitySettingException;

    /**
     * Get configurations for compatibility settings of a specific setting group.
     *
     * @param settingGroup Group of the settings.
     * @param tenantDomain Tenant domain that data is requested for.
     * @return A map of compatibility settings for the specified resource.
     * @throws CompatibilitySettingException If an error occurs while retrieving the configuration.
     */
    CompatibilitySettingGroup getConfigurationsByGroup(String settingGroup, String tenantDomain)
            throws CompatibilitySettingException;

    /**
     * Get configurations for a specific compatibility setting.
     *
     * @param settingGroup  Name of the settingGroup.
     * @param setting Name of the setting.
     * @param tenantDomain Tenant domain that data is requested for.
     * @return The compatibility setting for the specified settingGroup and setting.
     * @throws CompatibilitySettingException If an error occurs while retrieving the configuration.
     */
    String getConfigurationsByGroupAndSetting(String settingGroup, String setting, String tenantDomain)
            throws CompatibilitySettingException;

    /**
     * Update configurations for compatibility settings.
     *
     * @param compatibilitySetting Compatibility settings to be updated.
     * @param tenantDomain       Tenant domain that data is requested for.
     * @return The updated compatibility settings.
     * @throws CompatibilitySettingException If an error occurs while updating the configurations.
     */
    CompatibilitySetting updateConfiguration(CompatibilitySetting compatibilitySetting, String tenantDomain)
            throws CompatibilitySettingException;

    /**
     * Update configuration for a specific compatibility setting group.
     *
     * @param settingGroup       Name of the settingGroup.
     * @param compatibilitySettingGroup Compatibility setting to be updated.
     * @param tenantDomain       Tenant domain that data is requested for.
     * @return The updated compatibility setting.
     * @throws CompatibilitySettingException If an error occurs while updating the configuration.
     */
    CompatibilitySettingGroup updateConfigurationGroup(String settingGroup,
                                                       CompatibilitySettingGroup compatibilitySettingGroup,
                                                       String tenantDomain) throws CompatibilitySettingException;
}

