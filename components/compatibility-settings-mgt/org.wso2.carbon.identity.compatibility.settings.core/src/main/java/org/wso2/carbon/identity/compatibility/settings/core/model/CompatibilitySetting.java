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

package org.wso2.carbon.identity.compatibility.settings.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for Compatibility Settings Data Transfer Object.
 */
public class CompatibilitySetting {

    private final Map<String, CompatibilitySettingGroup> compatibilitySettings = new HashMap<>();

    /**
     * Get the compatibility settings.
     *
     * @return Map of compatibility settings.
     */
    public Map<String, CompatibilitySettingGroup> getCompatibilitySettings() {

        return compatibilitySettings;
    }

    /**
     * Add a compatibility setting.
     *
     * @param setting Compatibility setting to be added.
     */
    public void addCompatibilitySetting(CompatibilitySettingGroup setting) {

        this.compatibilitySettings.put(setting.getSettingGroup(), setting);
    }

    /**
     * Update compatibility setting.
     *
     * @param setting Compatibility setting to be updated.
     */
    public void updateCompatibilitySetting(CompatibilitySettingGroup setting) {

        if (!this.compatibilitySettings.containsKey(setting.getSettingGroup())) {
            addCompatibilitySetting(setting);
            return;
        }
        CompatibilitySettingGroup existingSetting = this.compatibilitySettings.get(setting.getSettingGroup());
        existingSetting.getSettings().putAll(setting.getSettings());
    }

    /**
     * Get a compatibility setting by its setting group.
     *
     * @param settingGroup Group of the setting.
     * @return Compatibility setting for the specified setting group, or null if not found.
     */
    public CompatibilitySettingGroup getCompatibilitySetting(String settingGroup) {

        if (!this.compatibilitySettings.containsKey(settingGroup)) {
            return null;
        }
        return this.compatibilitySettings.get(settingGroup);
    }

    /**
     * Update multiple compatibility settings.
     *
     * @param settings CompatibilitySettingsDTO containing settings to be updated.
     */
    public void updateCompatibilitySetting(CompatibilitySetting settings) {

        settings.getCompatibilitySettings().values().forEach(this::updateCompatibilitySetting);
    }
}

