/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.device.mgt.api.model.Device;

/**
 * Cache entry for Device Management.
 * This class is used to store a Device object in cache.
 */
public class DeviceCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 1L;

    private Device device;

    /**
     * Creates a cache entry wrapping the given device.
     *
     * @param device Device to be cached.
     */
    public DeviceCacheEntry(Device device) {

        this.device = device;
    }

    /**
     * Returns the cached device.
     *
     * @return Cached device.
     */
    public Device getDevice() {

        return device;
    }

    /**
     * Sets the cached device.
     *
     * @param device Device to be cached.
     */
    public void setDevice(Device device) {

        this.device = device;
    }
}
