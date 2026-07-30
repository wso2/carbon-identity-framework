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

package org.wso2.carbon.identity.device.policy.internal.constant;

/**
 * Timing constants for device token freshness and replay validation.
 */
public final class DeviceTokenConstants {

    private DeviceTokenConstants() {
    }

    // A device token is accepted only within this window after its iat. The expiry stored in the
    // replay store equals iat + this window, so a jti is retained for exactly as long as the
    // token could still be replayed.
    public static final long TOKEN_FRESHNESS_WINDOW_MILLIS = 5 * 60 * 1000L;

    // A small allowance granted for the device clock running ahead of the server.
    public static final long CLOCK_SKEW_MILLIS = 30 * 1000L;

    // Key under which the client attestation token is carried in the device data map, consumed
    // by IntegrityDataEnricher and populated either from the device-data JWT claims or, when
    // present, from the x-client-attestation request header (see DeviceDataResolverImpl).
    public static final String ATTESTATION_TOKEN_KEY = "attestationToken";
}
