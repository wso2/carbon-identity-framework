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

package org.wso2.carbon.identity.application.authentication.framework.handler.device;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Extension point for resolving verified device data from an authentication initiation request.
 * <p>
 * Implementations are registered as OSGi services and bound by the framework as an optional
 * reference. The resolved payload is stored on the authentication context under
 * {@code FrameworkConstants.DEVICE_DATA} and is exposed to adaptive authentication scripts as
 * {@code context.deviceData}.
 * <p>
 * Device data resolution is best-effort: the framework functions normally when no implementation
 * is registered, and implementations must not throw to abort an authentication flow.
 */
public interface DeviceDataResolver {

    /**
     * Resolves verified device data from the given initiation request.
     *
     * @param request      The initial authentication request, carrying the device token.
     * @param tenantDomain Tenant domain of the authentication flow.
     * @return The verified device payload, or an empty optional if no device token is present or
     *         verification failed.
     */
    Optional<Map<String, Object>> resolveDeviceData(HttpServletRequest request, String tenantDomain);
}
