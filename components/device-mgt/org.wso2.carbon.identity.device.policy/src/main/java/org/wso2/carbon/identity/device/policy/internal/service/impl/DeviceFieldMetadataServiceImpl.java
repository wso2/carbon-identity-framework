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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.wso2.carbon.identity.device.policy.api.service.DeviceFieldMetadataService;
import org.wso2.carbon.identity.device.policy.internal.config.DeviceFieldConfig;
import org.wso2.carbon.identity.device.policy.internal.config.DeviceFieldConfigLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DeviceFieldMetadataService backed by the cached DeviceFieldConfigLoader.
 */
public class DeviceFieldMetadataServiceImpl implements DeviceFieldMetadataService {

    @Override
    public Map<String, List<String>> getFieldApplicablePlatforms() {

        Map<String, List<String>> result = new HashMap<>();
        for (DeviceFieldConfig f : DeviceFieldConfigLoader.getInstance().getFields()) {
            if (!f.isUniversal()) {
                result.put(f.getName(), f.getApplicablePlatforms());
            }
        }
        return result;
    }
}
