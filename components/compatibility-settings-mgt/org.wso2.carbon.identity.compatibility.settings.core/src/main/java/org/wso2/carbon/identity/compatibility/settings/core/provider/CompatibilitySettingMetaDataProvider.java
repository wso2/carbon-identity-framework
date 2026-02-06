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
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;

/**
 * This interface defines the contract for compatibility setting data providers.
 */
public interface CompatibilitySettingMetaDataProvider {

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
     * Get metadata for all compatibility settings.
     *
     * @return A map of compatibility settings.
     */
    CompatibilitySettingMetaData getMetaData() throws CompatibilitySettingException;

    /**
     * Get metadata for compatibility settings of a specific setting group.
     *
     * @param settingGroup Group of the settings.
     * @return A map of compatibility settings for the specified resource.
     */
    CompatibilitySettingMetaDataGroup getMetaDataByGroup(String settingGroup) throws CompatibilitySettingException;

    /**
     * Get metadata for a specific compatibility setting.
     *
     * @param settingGroup  Name of the settingGroup.
     * @param setting Name of the setting.
     * @return The compatibility setting for the specified settingGroup and setting.
     */
    CompatibilitySettingMetaDataEntry getMetaDataByGroupAndSetting(String settingGroup, String setting)
            throws CompatibilitySettingException;
}

