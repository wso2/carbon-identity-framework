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

package org.wso2.carbon.identity.gateway.authentication.local;

import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.request.CallbackAuthenticationRequest;


public class LocalAuthenticationRequest extends CallbackAuthenticationRequest {

    private String authenticatorName;
    private String identityProviderName;

    private String userName ;
    private String password ;

    protected LocalAuthenticationRequest(LocalAuthenticationRequestBuilder builder) {
        super(builder);
        authenticatorName = builder.authenticatorName;
        identityProviderName = builder.identityProviderName;
        userName = builder.userName ;
        password = builder.password ;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public static class LocalAuthenticationRequestBuilder extends CallbackAuthenticationRequestBuilder {

        private String authenticatorName;
        private String identityProviderName;
        private String userName ;
        private String password ;

        public LocalAuthenticationRequestBuilder() {
            super();
        }

        @Override
        public LocalAuthenticationRequest build() throws GatewayRuntimeException {
            return new LocalAuthenticationRequest(this);
        }

        public LocalAuthenticationRequestBuilder setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
            return this;
        }

        public LocalAuthenticationRequestBuilder setIdentityProviderName(String identityProviderName) {
            this.identityProviderName = identityProviderName;
            return this;
        }

        public LocalAuthenticationRequestBuilder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public LocalAuthenticationRequestBuilder setPassword(String password) {
            this.password = password;
            return this;
        }
    }

    public static class FrameworkLoginRequestConstants
            extends CallbackAuthenticationRequest.CallbackAuthenticationRequestConstants {
        public static final String AUTHENTICATOR_NAME = "authenticator";
        public static final String IDP_NAME = "idp";
        public static final String REQUEST_DATA_KEY = "state";

        public static final String USER_NAME = "username";

        public static final String PASSWORD = "password";
    }
}
