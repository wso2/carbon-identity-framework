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

package org.wso2.carbon.identity.gateway.api.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayException;
import org.wso2.carbon.identity.gateway.common.util.Constants;
import org.wso2.msf4j.Request;
import java.io.Serializable;
import java.util.Map;
import javax.ws.rs.core.Response;


/**
 * GatewayRequestBuilderFactory is a base class to create GatewayRequestBuilder based on different
 * Protocols. This also will register as a Service and can be used as a default request builder.
 *
 * @param <T> Extended type of GatewayRequest.GatewayRequestBuilder
 */

public class GatewayRequestBuilderFactory<T extends GatewayRequest.GatewayRequestBuilder> extends AbstractHandler {

    private Logger log = LoggerFactory.getLogger(GatewayRequestBuilderFactory.class);

    /**
     * This is default can handler true if there are no any extended type of the factory in the factory registry.
     *
     * @param request
     * @return boolean
     */
    public boolean canHandle(Request request) throws GatewayException {
        return true;
    }

    /**
     * Create GatewayRequestBuilder.
     *
     * @param request
     * @return
     * @throws GatewayClientException
     */
    public T create(Request request)
            throws GatewayClientException {
        GatewayRequest.GatewayRequestBuilder builder = new GatewayRequest.GatewayRequestBuilder();
        this.create((T) builder, request);
        return (T) builder;
    }

    /**
     * Update GatewayRequestBuilder.
     *
     * @param builder
     * @param request
     * @throws GatewayClientException
     */
    protected void create(T builder, Request request)
            throws GatewayClientException {

        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        builder.setHttpMethod(request.getHttpMethod());
        builder.setContentType(request.getContentType());
        builder.setRequestURI(request.getUri());
        builder.setHttpMethod(request.getHttpMethod());
        builder.setAttributes((Map) request.getProperties());
        builder.addParameter(Constants.QUERY_PARAMETERS,
                (Serializable) request.getProperty(Constants.QUERY_PARAMETERS));
        builder.addParameter(Constants.BODY_PARAMETERS, (Serializable) request.getProperty(Constants.BODY_PARAMETERS));

        String[] queryStringParams = request.getUri().split("\\?");
        if (queryStringParams.length > 1) {
            builder.setQueryString(queryStringParams[1]);
        } else {
            builder.setQueryString(queryStringParams[0]);
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully Updated the request builder in GatewayRequestBuilderFactory.");
        }
    }

    //#TODO: Priority value should be checked to put here.
    @Override
    public int getPriority() {
        return 200;
    }

    //#TODO: Think about more this exception handling.

    /**
     * Handling exception for GatewayClientException.
     *
     * @param exception
     * @return
     */
    public Response.ResponseBuilder handleException(GatewayClientException exception) {
        Response.ResponseBuilder builder = Response.noContent();
        builder.status(400);
        builder.entity(exception.getMessage());
        return builder;
    }

    //#TODO: Think about more this exception handling.

    /**
     * Handling exception for RuntimeException.
     *
     * @param exception
     * @return
     */
    public Response.ResponseBuilder handleException(RuntimeException exception) {
        Response.ResponseBuilder builder = Response.noContent();
        builder.status(500);
        builder.entity("something went wrong");
        return builder;
    }
}
