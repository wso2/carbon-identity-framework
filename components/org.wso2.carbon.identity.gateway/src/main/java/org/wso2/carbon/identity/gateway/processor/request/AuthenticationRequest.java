/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.processor.request;

import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.request.IdentityRequest;

import java.util.UUID;


public class AuthenticationRequest extends IdentityRequest {

    protected String requestKey;
    protected String sessionKey;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestKey = builder.requestDataKey;
        sessionKey = builder.sessionCookie;
        if(requestKey == null){
            requestKey = UUID.randomUUID().toString();
        }
    }

    public String getRequestKey() {
        return requestKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder {

        protected String requestDataKey;
        protected String sessionCookie;

        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        public AuthenticationRequestBuilder setSessionKey(String sessionCookie) {
            this.sessionCookie = sessionCookie;
            return this;
        }

        @Override
        public AuthenticationRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants extends IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
        public static final String SESSION_KEY = "SIOWTOSW";
    }
}
