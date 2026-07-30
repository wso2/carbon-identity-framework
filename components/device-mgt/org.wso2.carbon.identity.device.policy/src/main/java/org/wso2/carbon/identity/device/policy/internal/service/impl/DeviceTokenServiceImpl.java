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

import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.service.DeviceTokenService;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;

import java.util.Map;

/**
 * Default {@link DeviceTokenService} implementation backed by {@link DeviceTokenExtractor}.
 */
public class DeviceTokenServiceImpl implements DeviceTokenService {

    @Override
    public Map<String, Object> resolveAndVerifyDataFromToken(String token, String base64PublicKey,
            String correlationId, String tenantDomain) throws DevicePolicyException {

        return new DeviceTokenExtractor().extractWithPublicKey(token, base64PublicKey, correlationId, tenantDomain);
    }
}
