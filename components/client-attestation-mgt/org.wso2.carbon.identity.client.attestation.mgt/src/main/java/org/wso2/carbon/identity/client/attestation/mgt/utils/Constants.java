/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.utils;

/**
 * Constant.
 */
public class Constants {

    /**
     * Enum representing different types of client applications.
     */
    public enum ClientTypes {

         // Represents an Android client application.
        ANDROID,
        // Represents an iOS client application.
        IOS
    }
    public static final String ATTESTATION_HEADER = "x-client-attestation";
    public static final String CLIENT_ATTESTATION_CONTEXT = "client.attestation.context";
    public static final String CLIENT_ID = "client_id";
    public static final String DIRECT = "direct";
    public static final String RESPONSE_MODE = "response_mode";
    public static final String OAUTH2 = "oauth2";
    public static final String UTC = "UTC";
    public static final String CLIENT_ATTESTATION_ALLOWED_WINDOW_IN_MILL_SECOND
            = "ClientAttestation.AllowedWindowMillis";
    public static final String APPLE_ATTESTATION_ROOT_CERTIFICATE_PATH
            = "ClientAttestation.AppleAttestationRootCertificatePath";
    public static final String APPLE_ATTESTATION_REVOCATION_CHECK_ENABLED
            = "ClientAttestation.AppleAttestationRevocationCheckEnabled";

    // Constants related to Android Attestation
    public static final String PLAY_RECOGNIZED = "PLAY_RECOGNIZED";

    // Constants related to Apple Attestation
    public static final String AUTH_DATA = "authData";
    public static final String ATT_STMT = "attStmt";
    public static final String FMT = "fmt";
    public static final String X5C = "x5c";
    public static final String APPLE_APP_ATTEST = "apple-appattest";
    public static final String SHA_256 = "SHA-256";
    public static final String X_509_CERTIFICATE_TYPE = "X.509";
    public static final String PKIX = "PKIX";
    public static final int CERTIFICATE_EXPIRY_THRESHOLD = 90;
    // Milli seconds in days 24 * 60 * 60 * 1000
    public static final int MILLI_SECOND_IN_DAY = 86400000;

}
