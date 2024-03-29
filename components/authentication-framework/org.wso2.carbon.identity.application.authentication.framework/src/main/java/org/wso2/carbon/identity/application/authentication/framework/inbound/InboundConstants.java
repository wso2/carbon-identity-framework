/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

/**
 * Inbound request constants.
 */
public class InboundConstants {

    /**
     * Inbound request processor constants.
     */
    public static class RequestProcessor {

        public static final String RELYING_PARTY = "RelyingPartyId";
        public static final String CALL_BACK_PATH = "CallbackURI";
        public static final String AUTH_NAME = "Name";
        public static final String AUTH_TYPE = "type";
        // Rename constant value to "contextkey"
        public static final String CONTEXT_KEY = "sessionDataKey";
        public static final String CONTEXT_KEY_CONSENT = "sessionDataKeyConsent";
        public static final String AUTHENTICATION_RESULT = "AuthenticationResult";

        private RequestProcessor() {
        }
    }

    public static final String LOGGED_IN_IDPS = "LoggedInIDPs";
    public static final String PASSIVE_AUTH = "passiveAuth";
    public static final String FORCE_AUTH = "forceAuth";
}
