/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.request;


import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrameworkLoginRequest extends LocalAuthenticationRequest {

    private String authenticatorName;
    private String identityProviderName;

    protected FrameworkLoginRequest(
            FrameworkLoginBuilder builder) {
        super(builder);
        authenticatorName = builder.authenticatorName;
        identityProviderName = builder.identityProviderName;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public static class FrameworkLoginBuilder extends LocalAuthenticationRequest.LocalAuthenticationRequestBuilder {

        private String authenticatorName;
        private String identityProviderName;

        public FrameworkLoginBuilder() {
            super();
        }

        public FrameworkLoginBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }


        public FrameworkLoginBuilder setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
            return this;
        }

        public FrameworkLoginBuilder setIdentityProviderName(String identityProviderName) {
            this.identityProviderName = identityProviderName;
            return this;
        }

        @Override
        public FrameworkLoginRequest build() throws FrameworkRuntimeException {
            return new FrameworkLoginRequest(this);
        }
    }

    public static class FrameworkLoginRequestConstants extends LocalAuthenticationRequest.LocalAuthenticationRequestConstants {
        public static final String AUTHENTICATOR_NAME = "authenticator";
        public static final String IDP_NAME = "idp";
    }
}
