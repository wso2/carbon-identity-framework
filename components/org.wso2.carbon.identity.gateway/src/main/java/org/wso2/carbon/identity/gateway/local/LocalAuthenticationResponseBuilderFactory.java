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

import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import javax.ws.rs.core.Response;
import java.net.URLEncoder;

public class LocalAuthenticationResponseBuilderFactory extends GatewayResponseBuilderFactory {

    private static Logger log = LoggerFactory.getLogger(LocalAuthenticationResponseBuilderFactory.class);

    public boolean canHandle(GatewayResponse gatewayResponse) {
        return gatewayResponse instanceof LocalAuthenticationResponse;
    }

    @Override
    public Response.ResponseBuilder createBuilder(GatewayResponse gatewayResponse) {
        Response.ResponseBuilder builder = Response.noContent();
        createBuilder(builder, gatewayResponse);
        return builder;
    }

    @Override
    public void createBuilder(Response.ResponseBuilder builder, GatewayResponse gatewayResponse) {
        if (gatewayResponse instanceof LocalAuthenticationResponse) {
            LocalAuthenticationResponse localAuthenticationResponse = (LocalAuthenticationResponse) gatewayResponse;
            builder.status(302);
            String url = localAuthenticationResponse.getEndpointURL() + "?callback=" + URLEncoder.encode
                    ("https://localhost:9292/gateway") +
                    "&state=" +
                    localAuthenticationResponse.getRelayState() + "&idplist=" + localAuthenticationResponse
                    .getIdentityProviderList();
            builder.header(HttpHeaders.LOCATION, url);
        }
    }


    public int getPriority() {
        return 100;
    }
}