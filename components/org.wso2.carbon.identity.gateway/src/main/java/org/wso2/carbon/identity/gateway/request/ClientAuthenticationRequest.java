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


import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;

/**
 * ClientAuthenticationRequest is the base request to handle initial call to the gateway.
 */
public class ClientAuthenticationRequest extends AuthenticationRequest {

    private static final long serialVersionUID = 1202704178758481721L;

    protected ClientAuthenticationRequest(ClientAuthenticationRequest.ClientAuthenticationRequestBuilder builder) {
        super(builder);
    }

    /**
     * ClientAuthenticationRequestBuilder is the builder class for ClientAuthenticationRequest.
     */
    public static class ClientAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public ClientAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public ClientAuthenticationRequest build() throws GatewayRuntimeException {
            return new ClientAuthenticationRequest(this);
        }
    }

    /**
     * ClientAuthenticationRequestConstants is hold the constants for ClientAuthenticationRequest.
     */
    public static class ClientAuthenticationRequestConstants extends AuthenticationRequestConstants {

    }
}
