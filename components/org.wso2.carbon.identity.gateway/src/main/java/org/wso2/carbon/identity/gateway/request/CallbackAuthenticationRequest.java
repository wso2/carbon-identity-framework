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
 * CallbackAuthenticationRequest is the base request to handle subsequent call to the gateway.
 */
public class CallbackAuthenticationRequest extends AuthenticationRequest {

    private static final long serialVersionUID = 6439844622952952687L;

    protected CallbackAuthenticationRequest(CallbackAuthenticationRequestBuilder builder) {
        super(builder);
    }

    /**
     * CallbackAuthenticationRequestBuilder is the builder class for CallbackAuthenticationRequest.
     */
    public static class CallbackAuthenticationRequestBuilder
            extends AuthenticationRequest.AuthenticationRequestBuilder {

        public CallbackAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public CallbackAuthenticationRequest build() throws GatewayRuntimeException {
            return new CallbackAuthenticationRequest(this);
        }
    }


    /**
     * CallbackAuthenticationRequestConstants is hold the constants for CallbackAuthenticationRequest.
     */
    public static class CallbackAuthenticationRequestConstants
            extends AuthenticationRequest.AuthenticationRequestConstants {

    }
}
