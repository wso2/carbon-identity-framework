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

package org.wso2.carbon.identity.compatibility.settings.core.model.metadata;

import java.util.Map;

/**
 * Model class for Compatibility Setting metadata Group.
 */
public class CompatibilitySettingMetaDataGroup {

    private String settingGroup;
    private final Map<String, CompatibilitySettingMetaDataEntry> settingsMetaData = new java.util.HashMap<>();

    /**
     * Get the group of the setting.
     *
     * @return setting group.
     */
    public String getSettingGroup() {

        return settingGroup;
    }

    /**
     * Set the group of the setting.
     *
     * @param settingGroup setting group.
     */
    public void setSettingGroup(String settingGroup) {

        this.settingGroup = settingGroup;
    }

    /**
     * Get the compatibility settings.
     *
     * @return Map of settings.
     */
    public Map<String, CompatibilitySettingMetaDataEntry> getSettingsMetaData() {

        return settingsMetaData;
    }

    public void addSettingMetaData(String setting, CompatibilitySettingMetaDataEntry settingMetaData) {

        this.settingsMetaData.put(setting, settingMetaData);
    }

    /**
     * Get the setting metadata for a given setting.
     *
     * @param setting Setting name.
     * @return CompatibilitySettingMetaDataEntry
     */
    public CompatibilitySettingMetaDataEntry getSettingMetaDataEntry(String setting) {

        if (!this.settingsMetaData.containsKey(setting)) {
            return null;
        }
        return this.settingsMetaData.get(setting);
    }

    /**
     * Update existing metadata with the provided metadata group. If an entry does not exist, it will be added else
     * the existing entry will be overridden.
     *
     * @param metadataGroup Metadata group to override with.
     */
    public void update(CompatibilitySettingMetaDataGroup metadataGroup) {

        for (Map.Entry<String, CompatibilitySettingMetaDataEntry> entry :
                metadataGroup.getSettingsMetaData().entrySet()) {
            if (!this.settingsMetaData.containsKey(entry.getKey())) {
                addSettingMetaData(entry.getKey(), entry.getValue());
            } else {
                CompatibilitySettingMetaDataEntry existingEntry = this.settingsMetaData.get(entry.getKey());
                existingEntry.override(entry.getValue());
            }
        }
    }
}
