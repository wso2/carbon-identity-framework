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
        iOS
    }
    public static final String ATTESTATION_HEADER = "x-client-attestation";
    public static final String CLIENT_ATTESTATION_CONTEXT = "client.attestation.context";
    public static final String CLIENT_ID = "client_id";
    public static final String DIRECT = "direct";
    public static final String RESPONSE_MODE = "response_mode";
    public static final String OAUTH2 = "oauth2";
    public static final String UTC = "UTC";
    public static final String PLAY_RECOGNIZED = "PLAY_RECOGNIZED";
    public static final String CLIENT_ATTESTATION_ALLOWED_WINDOW_IN_MILL_SECOND
            = "ClientAttestation.AllowedWindowMillis";

}
