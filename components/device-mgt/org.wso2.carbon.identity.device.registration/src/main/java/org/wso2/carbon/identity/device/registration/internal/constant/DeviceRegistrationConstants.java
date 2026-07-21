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

package org.wso2.carbon.identity.device.registration.internal.constant;

import org.wso2.carbon.identity.device.registration.executor.DeviceRegistrationExecutor;
import org.wso2.carbon.identity.device.registration.listener.RegistrationFlowCompletionListener;

/**
 * Constants shared between {@link DeviceRegistrationExecutor} and
 * {@link RegistrationFlowCompletionListener}.
 */
public class DeviceRegistrationConstants {

    public static final String EXECUTOR_NAME = "DeviceRegistrationExecutor";

    public static final String CTX_DEVICE_REGISTRATION = "device.registration.data";

    public static final String CLAIM_USER_ID = "http://wso2.org/claims/userid";

    public static final String CTX_REGISTRATION_ID = "device.registration.id";
    public static final String CTX_CHALLENGE = "device.registration.challenge";

    public static final String PROP_REGISTRATION_ID = "registrationId";
    public static final String PROP_CHALLENGE = "challenge";

    public static final String FIELD_PUBLIC_KEY = "publicKey";
    public static final String FIELD_SIGNATURE = "signature";
    public static final String FIELD_DEVICE_MODEL = "deviceModel";
    public static final String FIELD_METADATA = "metadata";
    public static final String FIELD_DEVICE_DATA = "deviceData";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_TENANT_DOMAIN = "tenantDomain";
    public static final String FIELD_DEVICE_NAME = "deviceName";

    public static final String META_POLICY_NAME = "policyName";

    private DeviceRegistrationConstants() {

    }
}
