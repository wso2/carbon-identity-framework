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

package org.wso2.carbon.identity.compatibility.settings.core;

import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;

import java.util.Map;

/**
 * Interface for managing compatibility settings evaluation.
 * This manager coordinates evaluators and providers to determine compatibility settings.
 */
public interface CompatibilitySettingsManager {

    /**
     * Evaluate compatibility settings based on the provided context.
     * This method uses registered evaluators to determine the appropriate settings.
     *
     * @param context Compatibility setting context containing tenant information and metadata.
     * @return Evaluated compatibility settings.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluate(CompatibilitySettingContext context) throws CompatibilitySettingException;

    /**
     * Evaluate compatibility settings for a specific setting group.
     *
     * @param settingGroup Setting group to evaluate.
     * @param context      Compatibility setting context containing tenant information and metadata.
     * @return Evaluated compatibility settings for the specified group.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluateByGroup(String settingGroup, CompatibilitySettingContext context)
            throws CompatibilitySettingException;

    /**
     * Evaluate a specific compatibility setting within a setting group.
     *
     * @param settingGroup Setting group containing the setting.
     * @param setting      Specific setting to evaluate.
     * @param context      Compatibility setting context containing tenant information and metadata.
     * @return Evaluated compatibility setting.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluateByGroupAndSetting(String settingGroup,
                                                   String setting,
                                                   CompatibilitySettingContext context)
            throws CompatibilitySettingException;

    /**
     * Get the compatibility settings for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return Evaluated compatibility settings for the tenant.
     * @throws CompatibilitySettingException If an error occurs during retrieval or evaluation.
     */
    CompatibilitySetting getCompatibilitySettings(String tenantDomain) throws CompatibilitySettingException;

    /**
     * Get the compatibility settings for a specific setting group within a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @param settingGroup Setting group to retrieve.
     * @return Evaluated compatibility settings for the specified group and tenant.
     * @throws CompatibilitySettingException If an error occurs during retrieval or evaluation.
     */
    CompatibilitySetting getCompatibilitySettingsByGroup(String tenantDomain, String settingGroup)
            throws CompatibilitySettingException;

    /**
     * Get a specific compatibility setting within a setting group for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @param settingGroup Setting group containing the setting.
     * @param setting      Specific setting to retrieve.
     * @return Evaluated compatibility setting.
     * @throws CompatibilitySettingException If an error occurs during retrieval or evaluation.
     */
    CompatibilitySetting getCompatibilitySettingsByGroupAndSetting(String tenantDomain,
                                                                   String settingGroup,
                                                                   String setting) throws CompatibilitySettingException;

    /**
     * Update compatibility settings for a tenant.
     *
     * @param tenantDomain         Tenant domain.
     * @param compatibilitySetting Compatibility settings to update.
     * @return Updated compatibility settings.
     * @throws CompatibilitySettingException If an error occurs during update.
     */
    CompatibilitySetting updateCompatibilitySettings(String tenantDomain, CompatibilitySetting compatibilitySetting)
            throws CompatibilitySettingException;

    /**
     * Update compatibility settings for a specific setting group within a tenant.
     *
     * @param tenantDomain              Tenant domain.
     * @param settingGroup              Setting group to update.
     * @param compatibilitySettingGroup Compatibility setting group to update.
     * @return Updated compatibility settings.
     * @throws CompatibilitySettingException If an error occurs during update.
     */
    CompatibilitySetting updateCompatibilitySettingsGroup(String tenantDomain, String settingGroup,
                                                          CompatibilitySettingGroup compatibilitySettingGroup)
            throws CompatibilitySettingException;

    /**
     * Get the supported settings.
     * Returns a map where keys are setting group names and values are arrays of supported setting names.
     *
     * @return Map of setting group names to their supported setting names.
     */
    Map<String, String[]> getSupportedSettings();
}
