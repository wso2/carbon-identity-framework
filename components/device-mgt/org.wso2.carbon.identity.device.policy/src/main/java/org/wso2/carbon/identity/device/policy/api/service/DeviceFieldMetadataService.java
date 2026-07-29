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

package org.wso2.carbon.identity.device.policy.api.service;

import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.Platform;

import java.util.List;
import java.util.Set;

/**
 * Provides platform applicability metadata for device policy fields.
 */
public interface DeviceFieldMetadataService {

    /**
     * Returns the list of applicable device fields for the specified platform.
     * The returned list already includes universal fields that apply to all platforms.
     * If platform is null, returns all known fields (universal plus every platform's fields, de-duplicated).
     *
     * <p>Consumed by the device policy REST API layer in the identity-api-server repository.
     *
     * @param platform The platform for which to retrieve applicable fields, or null for all fields.
     * @return List of field names applicable to the platform.
     * @throws DevicePolicyServerException If the field configuration is not loaded.
     */
    List<String> getFieldsForPlatform(Platform platform) throws DevicePolicyServerException;

    /**
     * Returns the set of all supported device platforms.
     *
     * @return Set of supported {@link Platform} values.
     */
    Set<Platform> getSupportedPlatforms();
}
