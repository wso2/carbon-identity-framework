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
 *
 */

package org.wso2.carbon.identity.gateway.local;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;


public class LocalAuthenticationResponse extends GatewayResponse {

    private static final long serialVersionUID = -5705077843359711620L;
    protected String endpointURL;
    protected String relayState;
    //#TODO:This has to be improve to transfer more data to the endpoint. For now this is redirection and must be
    // post with all idp:auth list and the execution strategy. Based on that we may have to do lot of stuff in UI.
    protected String identityProviderList;

    protected LocalAuthenticationResponse(LocalAuthenticationResponseBuilder builder) {
        super(builder);
        this.endpointURL = builder.endpointURL;
        this.relayState = builder.relayState;
        this.identityProviderList = builder.identityProviderList;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public String getRelayState() {
        return relayState;
    }

    public String getIdentityProviderList() {
        return identityProviderList;
    }

    public static class LocalAuthenticationResponseBuilder extends GatewayResponseBuilder {
        protected String endpointURL;
        protected String relayState;
        protected String identityProviderList;

        public LocalAuthenticationResponseBuilder(GatewayMessageContext context) {
            this.context = context;
        }

        public LocalAuthenticationResponseBuilder() {

        }

        public GatewayResponseBuilder setEndpointURL(String endpointURL) {
            this.endpointURL = endpointURL;
            return this;
        }

        public GatewayResponseBuilder setIdentityProviderList(String identityProviderList) {
            this.identityProviderList = identityProviderList;
            return this;
        }

        public GatewayResponseBuilder setRelayState(String relayState) {
            this.relayState = relayState;
            return this;
        }

        public LocalAuthenticationResponse build() {
            return new LocalAuthenticationResponse(this);
        }

    }
}
