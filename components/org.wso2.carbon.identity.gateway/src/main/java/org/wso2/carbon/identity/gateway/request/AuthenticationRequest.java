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
package org.wso2.carbon.identity.gateway.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.common.util.Constants;

import java.util.UUID;

/**
 * AuthenticationRequest is the base request type in gateway.
 */
public class AuthenticationRequest extends GatewayRequest {

    private static final long serialVersionUID = -8870055537743402153L;
    protected String requestKey;
    protected String authenticatorName;
    protected String identityProviderName;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestKey = builder.requestDataKey;
        authenticatorName = builder.authenticatorName;
        identityProviderName = builder.identityProviderName;
        if (requestKey == null) {
            requestKey = UUID.randomUUID().toString();
        }
    }

    public String getRequestKey() {
        return requestKey;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public String getSessionKey() {
        String cookie = this.getHeader("Cookie");
        if (StringUtils.isNotEmpty(cookie) && cookie.contains(Constants.GATEWAY_COOKIE)) {
            cookie = cookie.split(Constants.GATEWAY_COOKIE + "=")[1];
            cookie = cookie.split(",")[0];
            return cookie;
        }
        return null;
    }

    /**
     * AuthenticationRequestBuilder is the builder of AuthenticationRequest.
     */
    public static class AuthenticationRequestBuilder extends GatewayRequestBuilder {

        protected String requestDataKey;
        protected String authenticatorName;
        protected String identityProviderName;

        @Override
        public AuthenticationRequest build() throws GatewayRuntimeException {
            return new AuthenticationRequest(this);
        }

        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        public AuthenticationRequestBuilder setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
            return this;
        }

        public AuthenticationRequestBuilder setIdentityProviderName(String identityProviderName) {
            this.identityProviderName = identityProviderName;
            return this;
        }
    }

    /**
     * AuthenticationRequestConstants is hold the constant to use for AuthenticationRequest.
     */
    public static class AuthenticationRequestConstants extends IdentityRequestConstants {
        public static final String AUTHENTICATOR_NAME = "authenticator";
        public static final String IDP_NAME = "idp";
        public static final String SESSION_KEY = "SIOWTOSW";
    }
}
