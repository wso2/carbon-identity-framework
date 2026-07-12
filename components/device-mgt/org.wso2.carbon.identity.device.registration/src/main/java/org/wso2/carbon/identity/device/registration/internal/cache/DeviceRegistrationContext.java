/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.device.registration.internal.cache;

import java.io.Serializable;

/**
 * Context stored between registration initiation and completion.
 */
public class DeviceRegistrationContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String challenge;
    private final String tenantDomain;

    /**
     * Creates a registration context.
     *
     * @param username     Username of the registering user.
     * @param challenge    Registration challenge.
     * @param tenantDomain Tenant domain.
     */
    public DeviceRegistrationContext(String username, String challenge, String tenantDomain) {
        this.username = username;
        this.challenge = challenge;
        this.tenantDomain = tenantDomain;
    }

    /**
     * Returns the username.
     *
     * @return Username.
     */
    public String getUsername() {

        return username;
    }

    /**
     * Returns the challenge.
     *
     * @return Challenge.
     */
    public String getChallenge() {

        return challenge;
    }

    /**
     * Returns the tenant domain.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }
}

