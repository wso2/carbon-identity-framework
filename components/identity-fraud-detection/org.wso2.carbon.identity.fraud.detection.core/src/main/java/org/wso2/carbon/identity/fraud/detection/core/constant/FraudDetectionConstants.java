/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.fraud.detection.core.constant;

/**
 * Constants for the Identity Fraud Detection component.
 */
public class FraudDetectionConstants {

    // Fraud detection common config constants.
    public static final String RESOURCE_TYPE = "fraud-detection";
    public static final String RESOURCE_NAME = "fraud-detection-config";
    public static final String PUBLISH_USER_INFO_ATTR_KEY = "publishUserInfo";
    public static final String PUBLISH_DEVICE_METADATA_ATTR_KEY = "publishDeviceMetadata";
    public static final String LOG_REQUEST_PAYLOAD_ATTR_KEY = "logRequestPayload";

    // Fraud detection event config constants.
    public static final String USER_REGISTRATION_EVENT_PROP_KEY = "Event.User_Registration";
    public static final String UPDATE_CREDENTIAL_EVENT_PROP_KEY = "Event.User_Credential_Update";
    public static final String UPDATE_PROFILE_EVENT_PROP_KEY = "Event.User_Profile_Update";
    public static final String NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY = "Event.Notification_Based_Verification";
    public static final String LOGIN_EVENT_PROP_KEY = "Event.User_Login";
    public static final String LOGOUT_EVENT_PROP_KEY = "Event.User_Logout";

    // Common event constants.
    public static final String INTERNAL_EVENT_NAME = "internal-event-name";

    /**
     * Fraud detection enums.
     */
    public enum FraudDetectionEvents {

        POST_USER_CREATION(USER_REGISTRATION_EVENT_PROP_KEY),
        PRE_UPDATE_PASSWORD_NOTIFICATION(UPDATE_CREDENTIAL_EVENT_PROP_KEY),
        POST_UPDATE_PASSWORD(UPDATE_CREDENTIAL_EVENT_PROP_KEY),
        POST_UPDATE_USER_PROFILE(UPDATE_PROFILE_EVENT_PROP_KEY),
        SELF_REGISTRATION_VERIFICATION_NOTIFICATION(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY),
        POST_SELF_REGISTRATION_VERIFICATION(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY),
        USER_ATTRIBUTE_UPDATE_VERIFICATION_NOTIFICATION(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY),
        POST_USER_ATTRIBUTE_UPDATE_VERIFICATION(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY),
        AUTHENTICATION_STEP_NOTIFICATION_VERIFICATION(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY),
        LOGIN(LOGIN_EVENT_PROP_KEY),
        LOGOUT(LOGOUT_EVENT_PROP_KEY);

        private final String eventConfigName;

        /**
         * Constructor to initialize the event config name.
         *
         * @param eventConfigName Event config name.
         */
        FraudDetectionEvents(String eventConfigName) {

            this.eventConfigName = eventConfigName;
        }

        /**
         * Get the event config name associated with the fraud detection event.
         *
         * @return Event config name.
         */
        public String getEventConfigName() {

            return eventConfigName;
        }
    }

    /**
     * Execution status enums.
     */
    public enum ExecutionStatus {

        SUCCESS("SUCCESS"),
        FAILURE("FAILURE"),
        ERROR("ERROR"),
        SKIPPED("SKIPPED");

        private final String value;

        /**
         * Constructor to initialize the execution status value.
         *
         * @param value Execution status value.
         */
        ExecutionStatus(String value) {
            this.value = value;
        }

        /**
         * Get the execution status value.
         *
         * @return Execution status value.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Error type enums.
     */
    public enum ErrorType {

        INVALID_REQUEST("Invalid Request"),
        INVALID_RESPONSE("Invalid Response"),
        UNSUPPORTED_EVENT("Unsupported Event");

        private final String value;

        /**
         * Constructor to initialize the error type value.
         *
         * @param value Error type value.
         */
        ErrorType(String value) {
            this.value = value;
        }

        /**
         * Get the error type value.
         *
         * @return Error type value.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Error message enums.
     */
    public enum ErrorMessages {

        ERROR_CODE_GETTING_FRAUD_DETECTOR_CONFIG("65001",
                "Error while getting fraud detection config.",
                "Unexpected server error while retrieving fraud detection config for tenant, %s"),
        ERROR_CODE_ADDING_FRAUD_DETECTOR_CONFIG("65002",
                "Error while adding fraud detection config.",
                "Unexpected server error while adding fraud detection config for tenant, %s"),
        ERROR_CODE_UPDATING_FRAUD_DETECTOR_CONFIG("65003",
                "Error while updating fraud detection config.",
                "Unexpected server error while updating fraud detection config for tenant, %s");

        private static final String ERROR_PREFIX = "FD";
        private final String code;
        private final String message;
        private final String description;

        /**
         * Constructor to initialize the error message enum.
         *
         * @param code        Error code.
         * @param message     Error message.
         * @param description Error description.
         */
        ErrorMessages(String code, String message, String description) {

            this.code = ERROR_PREFIX + "-" + code;
            this.message = message;
            this.description = description;
        }

        /**
         * Get the error code.
         *
         * @return Error code.
         */
        public String getCode() {

            return code;
        }

        /**
         * Get the error message.
         *
         * @return Error message.
         */
        public String getMessage() {

            return message;
        }

        /**
         * Get the error description.
         *
         * @return Error description.
         */
        public String getDescription() {

            return description;
        }

        /**
         * Override toString method to return error code and message.
         *
         * @return String representation of the error message enum.
         */
        @Override
        public String toString() {

            return code + ":" + message;
        }
    }

    /**
     * Recovery scenario constants.
     */
    public static class RecoveryScenarios {

        public static final String ASK_PASSWORD = "ASK_PASSWORD";
        public static final String NOTIFICATION_BASED_PW_RECOVERY = "NOTIFICATION_BASED_PW_RECOVERY";
        public static final String ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK
                = "ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK";
        public static final String ADMIN_FORCED_PASSWORD_RESET_VIA_OTP
                = "ADMIN_FORCED_PASSWORD_RESET_VIA_OTP";
        public static final String NOTIFICATION_BASED_PW_RECOVERY_OFFLINE_INVITE
                = "NOTIFICATION_BASED_PW_RECOVERY_OFFLINE_INVITE";
        public static final String EMAIL_VERIFICATION_ON_UPDATE = "EMAIL_VERIFICATION_ON_UPDATE";
        public static final String MOBILE_VERIFICATION_ON_UPDATE = "MOBILE_VERIFICATION_ON_UPDATE";
    }

    // Template type constants.
    public static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";
    public static final String ACCOUNT_CONFIRMATION_TEMPLATE_TYPE = "accountconfirmation";

    /**
     * Log related constants.
     */
    public static class LogConstants {

        public static final String FRAUD_DETECTION_SERVICE = "fraud-detection-service";

        /**
         * Action IDs for logging.
         */
        public static class ActionIDs {

            public static final String PUBLISH_FRAUD_DETECTION_EVENT = "publish-fraud-detection-event";
            public static final String FRAUD_DETECTION_EVENT_RESPONSE = "fraud-detection-event-response";
        }
    }
}
