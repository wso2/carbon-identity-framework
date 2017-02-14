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


import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;


public class CallbackAuthenticationRequest extends AuthenticationRequest {

    protected CallbackAuthenticationRequest(CallbackAuthenticationRequestBuilder builder) {
        super(builder);

    }

    public static class CallbackAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public CallbackAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public CallbackAuthenticationRequest build() throws FrameworkRuntimeException {
            return new CallbackAuthenticationRequest(this);
        }
    }

    public static class CallbackAuthenticationRequestConstants extends AuthenticationRequest.AuthenticationRequestConstants {

    }
}
