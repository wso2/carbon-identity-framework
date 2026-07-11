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

package org.wso2.carbon.identity.device.mgt.api.model;

/**
 * Response model returned when a device registration is initiated.
 */
public class DeviceRegistrationInitiation {

    private final String registrationId;
    private final String challenge;

    /**
     * Creates an initiation response.
     *
     * @param registrationId Registration identifier.
     * @param challenge Base64url encoded challenge.
     */
    public DeviceRegistrationInitiation(String registrationId, String challenge) {

        this.registrationId = registrationId;
        this.challenge = challenge;
    }

    /**
     * Returns the registration identifier.
     *
     * @return Registration identifier.
     */
    public String getRegistrationId() {

        return registrationId;
    }

    /**
     * Returns the challenge.
     *
     * @return Challenge.
     */
    public String getChallenge() {

        return challenge;
    }
}
