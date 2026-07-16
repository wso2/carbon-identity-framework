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

package org.wso2.carbon.identity.device.registration.internal.component;

import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.api.service.DeviceTokenVerifier;

/**
 * Holds OSGi service references for the device registration executor bundle.
 */
public class DeviceRegistrationComponentServiceHolder {

    private static final DeviceRegistrationComponentServiceHolder INSTANCE =
            new DeviceRegistrationComponentServiceHolder();

    private DeviceManagementService deviceManagementService;
    private DevicePolicyEvaluator devicePolicyEvaluator;
    private DeviceTokenVerifier deviceTokenVerifier;

    private DeviceRegistrationComponentServiceHolder() {
    }

    public static DeviceRegistrationComponentServiceHolder getInstance() {
        return INSTANCE;
    }

    public DeviceManagementService getDeviceManagementService() {
        return deviceManagementService;
    }

    public void setDeviceManagementService(DeviceManagementService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    public DevicePolicyEvaluator getDevicePolicyEvaluator() {
        return devicePolicyEvaluator;
    }

    public void setDevicePolicyEvaluator(DevicePolicyEvaluator devicePolicyEvaluator) {
        this.devicePolicyEvaluator = devicePolicyEvaluator;
    }

    public DeviceTokenVerifier getDeviceTokenVerifier() {
        return deviceTokenVerifier;
    }

    public void setDeviceTokenVerifier(DeviceTokenVerifier deviceTokenVerifier) {
        this.deviceTokenVerifier = deviceTokenVerifier;
    }
}
