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
    public static final String INVITE_CONFIRMATION_EXECUTOR_NAME = "ConfirmationCodeValidationExecutor";

    public static final String CLAIM_USER_ID = "http://wso2.org/claims/userid";

    public static final String CTX_DEVICE_REGISTRATION = "device.registration.data";
    public static final String CTX_REGISTRATION_ID = "device.registration.id";
    public static final String CTX_CHALLENGE = "device.registration.challenge";
    public static final String CTX_QR_COUNT = "device.registration.qr.count";
    public static final String CTX_QR_EXPIRES_AT = "device.registration.qr.expiresAt";
    public static final String CTX_ONE_TIME_FLOW_ID = "device.registration.oneTimeFlowId";
    public static final String CTX_PUBLIC_KEY = "device.registration.publicKey";
    public static final String CTX_DEVICE_MODEL = "device.registration.deviceModel";
    public static final String CTX_PAIRING_CODE_HASH = "device.registration.pairing.codeHash";
    public static final String CTX_PAIRING_ATTEMPTS = "device.registration.pairing.attempts";

    // Phases a node runs. A node without a phase runs native device registration.
    public static final String PHASE_NATIVE_DEVICE_REGISTRATION = "NATIVE_DEVICE_REGISTRATION";
    public static final String PHASE_INIT_REGISTRATION = "INIT_REGISTRATION";
    public static final String PHASE_PROCESS_DEVICE = "PROCESS_DEVICE";
    public static final String PHASE_FINISH_REGISTRATION = "FINISH_REGISTRATION";

    public static final String PROP_REGISTRATION_ID = "registrationId";
    public static final String PROP_CHALLENGE = "challenge";
    public static final String PROP_QR_CODE_DATA = "qrCodeData";
    public static final String PROP_DEVICE_ID = "deviceId";
    public static final String PROP_PAIRING_CODE = "pairingCode";
    public static final String PROP_ACCOUNT_HINT = "accountHint";
    public static final String PROP_DEVICE_MODEL = "deviceModel";

    public static final String FIELD_PUBLIC_KEY = "publicKey";
    public static final String FIELD_SIGNATURE = "signature";
    public static final String FIELD_DEVICE_MODEL = "deviceModel";
    public static final String FIELD_DEVICE_DATA = "deviceData";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_TENANT_DOMAIN = "tenantDomain";
    public static final String FIELD_DEVICE_NAME = "deviceName";
    public static final String FIELD_PAIRING_CODE = "pairingCode";

    public static final String META_POLICY_NAME = "policyName";

    public static final int MAX_QR_CODES = 5;
    public static final int MAX_PAIRING_ATTEMPTS = 3;
    public static final int MAX_DEVICE_MODEL_LENGTH = 64;
    public static final String I18N_INVALID_PAIRING_CODE = "device.registration.pairing.code.invalid";
    public static final String INVALID_PAIRING_CODE_MESSAGE =
            "The code does not match the code shown on your device. Try again.";

    public static final String DISABLE_REGISTRATION_EXECUTOR_CONFIG = "DeviceManagement.DisableRegistrationExecutor";

    private DeviceRegistrationConstants() {

    }
}
