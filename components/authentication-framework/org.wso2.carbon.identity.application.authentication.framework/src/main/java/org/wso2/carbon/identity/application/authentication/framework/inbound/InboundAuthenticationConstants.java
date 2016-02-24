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

public final class InboundAuthenticationConstants {

    public final static String HTTP_PATH_PARAM_REQUEST = "/request";
    public final static String HTTP_PATH_PARAM_RESPONSE = "/response";

    public static class StatusCode {

        public static final int SUCCESS = 200;
        public static final int REDIRECT = 302;
        public static final int ERROR = 500;

        private StatusCode() {
        }
    }

    public static class RequestProcessor {

        public static final String RELYING_PARTY = "RelyingPartyId";
        public static final String CALL_BACK_PATH = "CallbackURI";
        public static final String AUTH_NAME = "Name";
        public static final String AUTH_TYPE = "type";
        public static final String SESSION_DATA_KEY = "sessionDataKey";

        private RequestProcessor() {
        }
    }
}