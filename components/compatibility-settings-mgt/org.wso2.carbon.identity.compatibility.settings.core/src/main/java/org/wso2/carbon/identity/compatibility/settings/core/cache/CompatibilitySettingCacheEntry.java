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

package org.wso2.carbon.identity.compatibility.settings.core.cache;

import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache entry for compatibility settings.
 * Wraps the CompatibilitySetting domain model for caching.
 */
public class CompatibilitySettingCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 3918465720384756129L;

    private final CompatibilitySetting compatibilitySetting;

    /**
     * Constructor for CompatibilitySettingCacheEntry.
     *
     * @param compatibilitySetting Compatibility setting to cache.
     */
    public CompatibilitySettingCacheEntry(CompatibilitySetting compatibilitySetting) {

        this.compatibilitySetting = compatibilitySetting;
    }

    /**
     * Get the cached compatibility setting.
     *
     * @return Compatibility setting.
     */
    public CompatibilitySetting getCompatibilitySetting() {

        return compatibilitySetting;
    }
}
