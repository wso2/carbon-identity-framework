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

package org.wso2.carbon.identity.flow.data.provider.dfdp.util;

/**
 * DFDP Framework Constants.
 * Part 6: Integration with Claim Processing - Constants used in DFDP integration components.
 */
public class FrameworkConstants {

    private FrameworkConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * DFDP specific constants.
     */
    public static class DFDP {

        // Request parameters and context properties
        public static final String IS_DFDP_REQUEST = "isDFDPRequest";
        public static final String REQUEST_ID = "dfdpRequestId";
        public static final String INTEGRATION_ENABLED = "dfdpIntegrationEnabled";
        public static final String TARGET_IDP = "dfdpTargetIdp";
        public static final String TARGET_AUTHENTICATOR = "dfdpTargetAuthenticator";
        public static final String TEST_CLAIMS = "dfdpTestClaims";
        public static final String EXPECTED_RESULTS = "dfdpExpectedResults";
        public static final String OUTPUT_FORMAT = "dfdpOutputFormat";

        // Claim event types
        public static final String CLAIM_EVENT_REMOTE_RECEIVED = "REMOTE_CLAIMS_RECEIVED";
        public static final String CLAIM_EVENT_LOCAL_MAPPED = "LOCAL_CLAIMS_MAPPED";
        public static final String CLAIM_EVENT_FINAL_PROCESSED = "FINAL_CLAIMS_PROCESSED";
        public static final String CLAIM_EVENT_ERROR = "CLAIM_PROCESSING_ERROR";

        // Authenticator event types
        public static final String AUTH_EVENT_START = "AUTHENTICATOR_START";
        public static final String AUTH_EVENT_SUCCESS = "AUTHENTICATOR_SUCCESS";
        public static final String AUTH_EVENT_FAILURE = "AUTHENTICATOR_FAILURE";
        public static final String IDP_EVENT_RESPONSE = "IDP_RESPONSE_RECEIVED";
        public static final String CLAIM_EVENT_TRANSFORMATION = "CLAIM_TRANSFORMATION";

        // Analysis stages
        public static final String STAGE_REMOTE_CLAIMS = "REMOTE_CLAIMS_RECEIVED";
        public static final String STAGE_LOCAL_MAPPING = "LOCAL_CLAIM_MAPPING";
        public static final String STAGE_FINAL_PROCESSING = "FINAL_CLAIMS_PROCESSED";
        public static final String STAGE_ERROR = "CLAIM_PROCESSING_ERROR";
        public static final String STAGE_AUTHENTICATOR_START = "AUTHENTICATOR_START";
        public static final String STAGE_AUTHENTICATOR_COMPLETE = "AUTHENTICATOR_COMPLETE";
        public static final String STAGE_IDP_RESPONSE = "IDP_RESPONSE_RECEIVED";
        public static final String STAGE_CLAIM_TRANSFORMATION = "CLAIM_TRANSFORMATION";

        // Event listener priorities
        public static final int PRIORITY_LOGGER = 100;
        public static final int PRIORITY_ANALYZER = 200;
        public static final int PRIORITY_REPORTER = 300;

        // Event listener filters
        public static final String FILTER_REQUEST_ID = "requestId";
        public static final String FILTER_EVENT_TYPE = "eventType";
        public static final String FILTER_STAGE = "stage";
        public static final String FILTER_AUTHENTICATOR = "authenticatorName";
        public static final String FILTER_IDP = "identityProviderName";

        // Analysis result statuses
        public static final String STATUS_SUCCESS = "SUCCESS";
        public static final String STATUS_FAILURE = "FAILURE";
        public static final String STATUS_PARTIAL = "PARTIAL";
        public static final String STATUS_ERROR = "ERROR";
        public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

        // Claim processing statuses
        public static final String CLAIM_STATUS_RECEIVED = "RECEIVED";
        public static final String CLAIM_STATUS_MAPPED = "MAPPED";
        public static final String CLAIM_STATUS_PROCESSED = "PROCESSED";
        public static final String CLAIM_STATUS_ERROR = "ERROR";

        // Context keys for claim data
        public static final String CTX_ORIGINAL_CLAIMS = "originalClaims";
        public static final String CTX_MAPPED_CLAIMS = "mappedClaims";
        public static final String CTX_FINAL_CLAIMS = "finalClaims";
        public static final String CTX_CLAIM_MAPPINGS = "claimMappings";
        public static final String CTX_PROCESSING_DETAILS = "processingDetails";
        public static final String CTX_ERROR_DETAILS = "errorDetails";

        // Event data keys
        public static final String EVENT_DATA_CLAIMS = "claims";
        public static final String EVENT_DATA_CLAIM_COUNT = "claimCount";
        public static final String EVENT_DATA_MAPPING_COUNT = "mappingCount";
        public static final String EVENT_DATA_STAGE = "stage";
        public static final String EVENT_DATA_ERROR_MESSAGE = "errorMessage";
        public static final String EVENT_DATA_ERROR_CODE = "errorCode";
        public static final String EVENT_DATA_PROCESSING_TIME = "processingTime";

        // Authenticator integration keys
        public static final String AUTH_DATA_NAME = "authenticatorName";
        public static final String AUTH_DATA_TYPE = "authenticatorType";
        public static final String AUTH_DATA_SUCCESS = "authenticationSuccess";
        public static final String AUTH_DATA_RAW_RESPONSE = "rawResponse";
        public static final String AUTH_DATA_PARSED_CLAIMS = "parsedClaims";
        public static final String AUTH_DATA_TRANSFORMATION_TYPE = "transformationType";
        public static final String AUTH_DATA_ORIGINAL_CLAIMS = "originalClaims";
        public static final String AUTH_DATA_TRANSFORMED_CLAIMS = "transformedClaims";

        // Report generation
        public static final String REPORT_TYPE_SUMMARY = "SUMMARY";
        public static final String REPORT_TYPE_DETAILED = "DETAILED";
        public static final String REPORT_TYPE_TIMELINE = "TIMELINE";
        public static final String REPORT_TYPE_ANALYSIS = "ANALYSIS";

        // Configuration properties
        public static final String CONFIG_EVENT_ASYNC = "dfdp.event.async";
        public static final String CONFIG_EVENT_TIMEOUT = "dfdp.event.timeout";
        public static final String CONFIG_ANALYSIS_ENABLED = "dfdp.analysis.enabled";
        public static final String CONFIG_LOGGING_ENABLED = "dfdp.logging.enabled";
        public static final String CONFIG_REPORTING_ENABLED = "dfdp.reporting.enabled";

        private DFDP() {
            // Private constructor to prevent instantiation
        }
    }

    /**
     * Integration specific constants.
     */
    public static class Integration {

        // Integration points
        public static final String POINT_CLAIM_HANDLER = "ClaimHandler";
        public static final String POINT_AUTHENTICATOR = "Authenticator";
        public static final String POINT_IDP_RESPONSE = "IdPResponse";
        public static final String POINT_CLAIM_MAPPING = "ClaimMapping";
        public static final String POINT_FINAL_PROCESSING = "FinalProcessing";

        // Integration status
        public static final String STATUS_ENABLED = "ENABLED";
        public static final String STATUS_DISABLED = "DISABLED";
        public static final String STATUS_CONDITIONAL = "CONDITIONAL";

        private Integration() {
            // Private constructor to prevent instantiation
        }
    }
}
