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

public class ClientAuthenticationRequest extends AuthenticationRequest {

    private String uniqueId;
    private String type;

    protected ClientAuthenticationRequest(
            ClientAuthenticationRequestBuilder builder, String uniqueId, String type) {
        super(builder);
        this.uniqueId = uniqueId;
        this.type = type;
    }

    public String getUniqueId() {
        return uniqueId;
    }


    public String getType() {
        return type;
    }

    public static class ClientAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        private String uniqueId;
        private String type;


        public ClientAuthenticationRequestBuilder() {
            super();
        }

        public ClientAuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        public ClientAuthenticationRequestBuilder setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public ClientAuthenticationRequestBuilder setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public ClientAuthenticationRequest build() throws FrameworkRuntimeException {
            return new ClientAuthenticationRequest(this, uniqueId, type);
        }
    }

    public static class ClientAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {

    }
}
