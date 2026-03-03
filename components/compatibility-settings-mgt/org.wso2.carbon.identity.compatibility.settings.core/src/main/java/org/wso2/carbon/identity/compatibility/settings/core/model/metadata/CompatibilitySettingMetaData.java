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

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for Compatibility Setting Meta Data.
 */
public class CompatibilitySettingMetaData {

    private final Map<String, CompatibilitySettingMetaDataGroup> settingsMetaData = new HashMap<>();

    /**
     * Get the compatibility settings meta data.
     *
     * @return Map of settings meta data.
     */
    public Map<String, CompatibilitySettingMetaDataGroup> getSettingsMetaData() {

        return settingsMetaData;
    }

    /**
     * Get the compatibility setting metadata group for the given setting group.
     *
     * @param settingGroup Setting group.
     * @return Compatibility setting meta data group.
     */
    public CompatibilitySettingMetaDataGroup getSettingMetaDataGroup(String settingGroup) {

        if (!this.settingsMetaData.containsKey(settingGroup)) {
            return null;
        }
        return this.settingsMetaData.get(settingGroup);
    }

    public CompatibilitySettingMetaDataEntry getSettingMetaDataEntry(String settingGroup, String setting) {

        CompatibilitySettingMetaDataGroup settingMetaDataGroup = getSettingMetaDataGroup(settingGroup);
        if (settingMetaDataGroup == null) {
            return null;
        }
        return settingMetaDataGroup.getSettingMetaDataEntry(setting);
    }

    /**
     * Add a compatibility setting meta data group.
     *
     * @param settingGroup Setting group.
     * @param settingMetaDataGroup Compatibility setting meta data group.
     */
    public void addSettingMetaDataGroup(String settingGroup,
                                        CompatibilitySettingMetaDataGroup settingMetaDataGroup) {

        this.settingsMetaData.put(settingGroup, settingMetaDataGroup);
    }

    /**
     * Update a compatibility setting metadata group. If the setting group does not exist, it will be added else
     * the existing group will be updated.
     *
     * @param settingGroup Setting group.
     * @param settingMetaDataGroup Compatibility setting meta data group.
     */
    public void updateSettingMetaDataGroup(String settingGroup,
                                        CompatibilitySettingMetaDataGroup settingMetaDataGroup) {

        if (!this.settingsMetaData.containsKey(settingGroup)) {
            this.settingsMetaData.put(settingGroup, settingMetaDataGroup);
        }
        CompatibilitySettingMetaDataGroup existingGroup = this.settingsMetaData.get(settingGroup);
        existingGroup.update(settingMetaDataGroup);
    }

    /**
     * Update a compatibility setting metadata group. If the setting group does not exist, it will be added else
     * the existing group will be updated.
     *
     * @param settingMetaDataGroup Compatibility setting meta data group.
     */
    public void updateSettingMetaDataGroup(CompatibilitySettingMetaDataGroup settingMetaDataGroup) {

        updateSettingMetaDataGroup(settingMetaDataGroup.getSettingGroup(), settingMetaDataGroup);
    }

    /**
     * Update multiple compatibility setting metadata groups.
     *
     * @param compatibilitySettingMetaData Compatibility setting meta data containing groups to be updated.
     */
    public void update(CompatibilitySettingMetaData compatibilitySettingMetaData) {

        compatibilitySettingMetaData.getSettingsMetaData().forEach(this::updateSettingMetaDataGroup);
    }
}
