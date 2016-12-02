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
import org.wso2.carbon.identity.framework.request.IdentityRequest;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequest extends IdentityRequest {

    private String requestDataKey;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
        if (requestDataKey == null) {
            requestDataKey = UUIDGenerator.generateUUID();
        }
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public static class AuthenticationRequestBuilder extends IdentityRequest.IdentityRequestBuilder {

        private String requestDataKey;

        public AuthenticationRequestBuilder() {
            super();
        }


        public AuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }


        @Override
        public AuthenticationRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants extends IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
    }
}
