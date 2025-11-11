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

package org.wso2.carbon.identity.vc.config.management.constant;

/**
 * Constants for VC Config Management.
 */
public class VCConfigManagementConstants {

    private VCConfigManagementConstants() {}

    public static final String DEFAULT_VC_FORMAT = "jwt_vc_json";
    public static final int MIN_EXPIRES_IN_SECONDS = 60;
    public static final String DEFAULT_SIGNING_ALGORITHM = "RS256";

    /**
     * Error message codes and default messages used in VC config management.
     */
    public enum ErrorMessages {
        // Client errors
        ERROR_CODE_CONFIG_ID_MISMATCH("VCM-4001", "Configuration id path and payload mismatch."),
        ERROR_CODE_IDENTIFIER_ALREADY_EXISTS("VCM-4002",
                "Configuration with the same identifier already exists."),
        ERROR_CODE_INVALID_REQUEST("VCM-4004", "Invalid verifiable credential configuration payload."),
        ERROR_CODE_CONFIG_NOT_FOUND("VCM-4005", "Configuration not found."),
        ERROR_CODE_UNSUPPORTED_VC_FORMAT("VCM-4006", "Unsupported verifiable credential format."),
        ERROR_CODE_OFFER_ID_MISMATCH("VCM-4007", "Offer id path and payload mismatch."),
        ERROR_CODE_OFFER_NOT_FOUND("VCM-4008", "Offer not found."),

        // Server errors
        ERROR_CODE_PERSISTENCE_ERROR("VCM-5001", "Error while persisting configuration."),
        ERROR_CODE_RETRIEVAL_ERROR("VCM-5002", "Error while retrieving configuration."),
        ERROR_CODE_DELETION_ERROR("VCM-5003", "Error while deleting configuration.");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
