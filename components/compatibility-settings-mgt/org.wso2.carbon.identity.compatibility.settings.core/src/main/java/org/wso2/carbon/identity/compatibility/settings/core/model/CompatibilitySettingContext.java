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

import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for Compatibility Setting Context.
 * Data required for processing compatibility settings such as metadata and values are passed using this context.
 */
public class CompatibilitySettingContext {

    private String tenantDomain;
    private CompatibilitySetting compatibilitySetting;
    private CompatibilitySettingMetaData compatibilitySettingMetaData;
    private Map<String, Object> properties = new HashMap<>();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CompatibilitySettingContext() {

    }

    /**
     * Get Tenant Domain.
     *
     * @return Tenant Domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Set Tenant Domain.
     *
     * @param tenantDomain Tenant Domain.
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Get Compatibility Settings.
     *
     * @return Compatibility Settings.
     */
    public CompatibilitySetting getCompatibilitySettings() {

        return compatibilitySetting;
    }

    /**
     * Set Compatibility Settings DTO.
     *
     * @param compatibilitySetting Compatibility Settings DTO.
     */
    public void setCompatibilitySettings(CompatibilitySetting compatibilitySetting) {

        this.compatibilitySetting = compatibilitySetting;
    }

    /**
     * Get Compatibility Setting MetaData.
     *
     * @return Compatibility Setting MetaData.
     */
    public CompatibilitySettingMetaData getCompatibilitySettingMetaData() {

        return compatibilitySettingMetaData;
    }

    /**
     * Set Compatibility Setting MetaData.
     *
     * @param compatibilitySettingMetaData Compatibility Setting MetaData.
     */
    public void setCompatibilitySettingMetaData(CompatibilitySettingMetaData compatibilitySettingMetaData) {

        this.compatibilitySettingMetaData = compatibilitySettingMetaData;
    }

    /**
     * Create Compatibility Setting Context.
     *
     * @return Compatibility Setting Context.
     */
    public static CompatibilitySettingContext create() {

        return new CompatibilitySettingContext();
    }

    /**
     * Get additional properties.
     *
     * @return Additional properties map.
     */
    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * Set additional properties.
     *
     * @param properties Additional properties map.
     */
    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    /**
     * Add a property to the context.
     *
     * @param key   Property key.
     * @param value Property value.
     */
    public void addProperty(String key, Object value) {

        this.properties.put(key, value);
    }
}
