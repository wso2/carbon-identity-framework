/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.api.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.response.HttpGatewayResponse;
import org.wso2.carbon.identity.gateway.common.util.Constants;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;

public class GatewayRequestBuilderFactory<T extends GatewayRequest.IdentityRequestBuilder> extends AbstractHandler {

    private Logger log = LoggerFactory.getLogger(GatewayRequestBuilderFactory.class);

    public boolean canHandle(Request request) {
        return true;
    }

    public GatewayRequest.IdentityRequestBuilder create(Request request)
            throws GatewayClientException {

        GatewayRequest.IdentityRequestBuilder builder = new GatewayRequest.IdentityRequestBuilder();
        this.create((T) builder, request);
        return builder;
    }

    public void create(T builder, Request request)
            throws GatewayClientException {

        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        builder.setHttpMethod(request.getHttpMethod());
        builder.setContentType(request.getContentType());
        builder.setRequestURI(request.getUri());
        builder.setHttpMethod(request.getHttpMethod());
        builder.setAttributes((Map)request.getProperties());
        builder.addParameter(Constants.QUERY_PARAMETERS, (Serializable) request.getProperty(Constants.QUERY_PARAMETERS));
        builder.addParameter(Constants.BODY_PARAMETERS, (Serializable)request.getProperty(Constants.BODY_PARAMETERS));

        String[] queryStringParams = request.getUri().split("\\?");
        if (queryStringParams != null && queryStringParams.length > 1) {
            builder.setQueryString(queryStringParams[1]);
        } else {
            builder.setQueryString(queryStringParams[0]);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    public Response.ResponseBuilder handleException(GatewayClientException exception) {
        Response.ResponseBuilder builder = Response.noContent();
        builder.status(400);
        builder.entity(exception.getMessage());
        return builder;
    }

    public HttpGatewayResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception) {
        HttpGatewayResponse.HttpIdentityResponseBuilder builder =
                new HttpGatewayResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        builder.setBody(exception.getMessage());
        return builder;
    }
}
