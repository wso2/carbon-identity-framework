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

package org.wso2.carbon.identity.compatibility.settings.core.service;

import org.wso2.carbon.identity.compatibility.settings.core.CompatibilitySettingsManager;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;

/**
 * Service for managing identity compatibility settings.
 * Provides functionality to get and update compatibility settings for tenants.
 */
public class CompatibilitySettingsService {

    /**
     * Get compatibility settings for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return Compatibility settings.
     * @throws CompatibilitySettingException If an error occurs while retrieving settings.
     */
    public CompatibilitySetting getCompatibilitySettings(String tenantDomain)
            throws CompatibilitySettingException {

        CompatibilitySetting cachedSettings =
                IdentityCompatibilitySettingsUtil.getFromCache(tenantDomain);
        if (cachedSettings != null) {
            return cachedSettings;
        }
        CompatibilitySettingsManager manager = getCompatibilitySettingsManager();
        CompatibilitySetting settings = manager.getCompatibilitySettings(tenantDomain);
        IdentityCompatibilitySettingsUtil.addToCache(tenantDomain, settings);
        return settings;
    }

    /**
     * Get compatibility settings for a specific setting group of a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @param settingGroup Setting group name.
     * @return Compatibility settings for the specified group.
     * @throws CompatibilitySettingException If an error occurs while retrieving settings.
     */
    public CompatibilitySetting getCompatibilitySettingsByGroup(String tenantDomain, String settingGroup)
            throws CompatibilitySettingException {

        CompatibilitySetting cachedSettings =
                IdentityCompatibilitySettingsUtil.getFromCache(tenantDomain, settingGroup);
        if (cachedSettings != null) {
            return cachedSettings;
        }
        CompatibilitySettingsManager manager = getCompatibilitySettingsManager();
        return manager.getCompatibilitySettingsByGroup(tenantDomain, settingGroup);
    }

    /**
     * Get compatibility settings for a specific setting of a setting group of a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @param settingGroup Setting group name.
     * @param setting      Specific setting name.
     * @return Compatibility settings for the specified setting.
     * @throws CompatibilitySettingException If an error occurs while retrieving settings.
     */
    public CompatibilitySetting getCompatibilitySettingsByGroupAndSetting(String tenantDomain, String settingGroup,
                                                                          String setting)
            throws CompatibilitySettingException {

        CompatibilitySetting cachedSettings =
                IdentityCompatibilitySettingsUtil.getFromCache(tenantDomain, settingGroup);
        if (cachedSettings != null) {
            return cachedSettings;
        }
        CompatibilitySettingsManager manager = getCompatibilitySettingsManager();
        return manager.getCompatibilitySettingsByGroupAndSetting(tenantDomain, settingGroup, setting);
    }

    /**
     * Update compatibility settings for a tenant.
     *
     * @param tenantDomain        Tenant domain.
     * @param compatibilitySetting Compatibility settings to update.
     * @return Updated compatibility settings.
     * @throws CompatibilitySettingException If an error occurs while updating settings.
     */
    public CompatibilitySetting updateCompatibilitySettings(String tenantDomain,
                                                            CompatibilitySetting compatibilitySetting)
            throws CompatibilitySettingException {

        CompatibilitySettingsManager manager = getCompatibilitySettingsManager();
        CompatibilitySetting updatedSettings = manager.updateCompatibilitySettings(tenantDomain, compatibilitySetting);
        IdentityCompatibilitySettingsUtil.clearCache(tenantDomain);
        return updatedSettings;
    }

    /**
     * Update compatibility settings for a specific setting group of a tenant.
     *
     * @param tenantDomain              Tenant domain.
     * @param settingGroup              Setting group name.
     * @param compatibilitySettingGroup Compatibility setting group to update.
     * @return Updated compatibility settings for the specified group.
     * @throws CompatibilitySettingException If an error occurs while updating settings.
     */
    public CompatibilitySetting updateCompatibilitySettings(
            String tenantDomain, String settingGroup, CompatibilitySettingGroup compatibilitySettingGroup)
            throws CompatibilitySettingException {

        CompatibilitySettingsManager manager = getCompatibilitySettingsManager();
        CompatibilitySetting updatedSettings = manager.updateCompatibilitySettingsGroup(
                tenantDomain, settingGroup, compatibilitySettingGroup);
        IdentityCompatibilitySettingsUtil.clearCache(tenantDomain);
        return updatedSettings;
    }

    /**
     * Helper method to get the CompatibilitySettingsManager instance.
     *
     * @return CompatibilitySettingsManager instance.
     * @throws CompatibilitySettingException If the manager is not available.
     */
    private CompatibilitySettingsManager getCompatibilitySettingsManager() throws CompatibilitySettingException {

        CompatibilitySettingsManager manager =
                IdentityCompatibilitySettingsDataHolder.getInstance().getCompatibilitySettingsManager();

        if (manager == null) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    IdentityCompatibilitySettingsConstants.
                            ErrorMessages.ERROR_CODE_COMPATIBILITY_SETTING_MANAGER_NOT_INITIALIZED
            );
        }
        return manager;
    }
}

