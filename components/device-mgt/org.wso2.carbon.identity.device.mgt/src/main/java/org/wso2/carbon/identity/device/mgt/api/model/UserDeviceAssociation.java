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
 * Concrete representation of a user-device association.
 */
public class UserDeviceAssociation extends DeviceAssociation {

    private final String userId;

    /**
     * Constructs a user-device association.
     *
     * @param deviceId Device identifier.
     * @param userId   User identifier.
     */
    public UserDeviceAssociation(String deviceId, String userId) {

        super(deviceId);
        this.userId = userId;
    }

    /**
     * Returns the user identifier of the association.
     *
     * @return User identifier.
     */
    public String getUserId() {

        return userId;
    }
}
