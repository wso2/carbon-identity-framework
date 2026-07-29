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

import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DeviceField;
import org.wso2.carbon.identity.device.policy.api.model.Platform;
import org.wso2.carbon.identity.device.policy.api.service.DeviceFieldMetadataService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of DeviceFieldMetadataService backed by the DeviceField enum.
 */
public class DeviceFieldMetadataServiceImpl implements DeviceFieldMetadataService {

    @Override
    public List<String> getFieldsForPlatform(Platform platform) throws DevicePolicyServerException {

        List<String> fields = new ArrayList<>();
        for (DeviceField field : DeviceField.values()) {
            if (field.appliesTo(platform)) {
                fields.add(field.getName());
            }
        }
        return fields;
    }

    @Override
    public Set<Platform> getSupportedPlatforms() {

        return EnumSet.allOf(Platform.class);
    }
}
