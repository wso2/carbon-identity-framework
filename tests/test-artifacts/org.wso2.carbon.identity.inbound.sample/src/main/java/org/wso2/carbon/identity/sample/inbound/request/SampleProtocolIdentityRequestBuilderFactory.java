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

package org.wso2.carbon.identity.sample.inbound.request;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class SampleProtocolIdentityRequestBuilderFactory extends GatewayRequestBuilderFactory {

    private static Logger log = LoggerFactory.getLogger(SampleProtocolIdentityRequestBuilderFactory.class);

    @Override
    public String getName() {
        return "SampleIdentityRequestBuilderFactory";
    }

    @Override
    public boolean canHandle(Request request) throws GatewayClientException, GatewayServerException {
        String sampleProtocol = GatewayUtil.getParameter(request, "sampleProtocol");
        String errorWhileCanHandleClient = GatewayUtil.getParameter(request, "canHandleErrorClient");
        String errorWhileCanHandleServer = GatewayUtil.getParameter(request, "canHandleErrorServer");
        if (errorWhileCanHandleClient != null) {
            throw new GatewayClientException("Throwing client exception");
        }

        if (errorWhileCanHandleServer != null) {
            throw new GatewayServerException("Throwing Server exception");
        }
        if (StringUtils.isNotBlank(sampleProtocol)) {
            return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public GatewayRequest.GatewayRequestBuilder create(Request request) throws GatewayClientException {

        GatewayRequest.GatewayRequestBuilder builder = new SampleProtocolRequest.SampleProtocolRequestBuilder(request);
        super.create(builder, request);
        return builder;
    }

    public Response.ResponseBuilder handleException(GatewayClientException exception) {
      return  super.handleException(exception);
    }
}
