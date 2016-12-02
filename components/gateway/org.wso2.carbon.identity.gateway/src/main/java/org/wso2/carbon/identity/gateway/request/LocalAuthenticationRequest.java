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

public class LocalAuthenticationRequest extends AuthenticationRequest {

    protected LocalAuthenticationRequest(
            LocalAuthenticationRequestBuilder builder) {
        super(builder);

    }

    public static class LocalAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public LocalAuthenticationRequestBuilder() {
            super();
        }

        public LocalAuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        @Override
        public LocalAuthenticationRequest build() throws FrameworkRuntimeException {
            return new LocalAuthenticationRequest(this);
        }
    }

    public static class LocalAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {

    }
}
