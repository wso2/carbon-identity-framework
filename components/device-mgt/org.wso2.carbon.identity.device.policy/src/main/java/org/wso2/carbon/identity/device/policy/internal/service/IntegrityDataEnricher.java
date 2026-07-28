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

package org.wso2.carbon.identity.device.policy.internal.service;

import java.util.Map;

/**
 * Enriches the device data map with platform-verified integrity values before policy evaluation.
 *
 * <p>Handles both Android and iOS platforms via the underlying client attestation service, which
 * detects the platform automatically from the token format (JWE for Android, CBOR for iOS).
 * Removes {@code attestationToken} from the map before returning — policy rules never see the raw token.
 * Sets {@code androidIntegrity} for Android devices and {@code iosIntegrity} for iOS devices.
 * When the client attestation service is not available, both fields are set to their failed values.
 */
public interface IntegrityDataEnricher {

    /**
     * Enriches deviceData with platform-verified integrity values.
     *
     * @param deviceData   Mutable map of device attributes.
     * @param appId        Application resource ID for loading attestation credentials.
     * @param tenantDomain Tenant domain.
     */
    void enrich(Map<String, Object> deviceData, String appId, String tenantDomain);
}
