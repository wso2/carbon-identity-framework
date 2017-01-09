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

package org.wso2.carbon.identity.gateway.resource.factory;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayRequest.GatewayRequestBuilder;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Default factory implementation of {@link GatewayResponseFactory}
 */
public class GatewayRequestFactory {

    private static Logger logger = LoggerFactory.getLogger(GatewayRequestFactory.class);

    public String getName() {

        return getClass().getSimpleName();
    }

    public int getPriority() {

        return 100;
    }

    public boolean canHandle(Request msf4jRequest) {

        return true;
    }


    /**
     * Build a {@link GatewayRequest} from a {@link Request} object.
     *
     * @param msf4jRequest MSF4J Request
     * @return
     * @throws FrameworkClientException
     */
    public GatewayRequest getGatewayRequest(Request msf4jRequest) throws FrameworkClientException {

        return create(msf4jRequest);
    }

//    public IdentityRequestBuilder create(Request request) throws FrameworkClientException {
//
//        IdentityRequestBuilder builder = new IdentityRequestBuilder();
//        create(builder, request);
//        return builder;
//    }


    public Response handleException(FrameworkClientException exception, Request msf4jRequest) {

        return Response.status(400).entity(exception.getMessage()).build();
    }


    private GatewayRequest create(Request request) throws FrameworkClientException {

        GatewayRequestBuilder builder = new GatewayRequestBuilder();

        // get all headers
        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        // get all properties
        request.getProperties().forEach(builder::addProperty);


        builder.setMethod(request.getHttpMethod());
        builder.setContentType(request.getContentType());
        builder.setRequestUri(request.getUri());

        // handle queryParams
        FrameworkUtil.getQueryParamMap(request.getUri()).forEach((x, y) -> {
            if (StringUtils.equalsIgnoreCase("serviceProvider", x)) {
                builder.setServiceProvider(y);
            }
            builder.addProperty(x, y);
        });

        // Handle the message body
        String contentType = request.getContentType();
        String body = readRequestBody(request);

        // if it a form we add the form key,values as properties.
        if (isFormParamRequest(contentType)) {
            try {
                handleFormParams(body, builder);
            } catch (UnsupportedEncodingException e) {
                logger.error("Error handling form parameters.");
            }
        } else {
            builder.setBody(body);
        }

        // TODO : handle cookies.
        // TODO : extract SP, tenant info, and others.

        if (logger.isDebugEnabled()) {
            logger.debug("Identity Request Builder created from the inbound HTTP Request.");
        }

        return builder.build();
    }

    private void handleFormParams(String requestBody, GatewayRequestBuilder requestBuilder)
            throws UnsupportedEncodingException {

        FrameworkUtil.splitQuery(requestBody).forEach(requestBuilder::addProperty);
    }

    private boolean isFormParamRequest(String contentType) {

        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType);
    }


    /**
     * Read the request body of an {@link Request} as a String.
     *
     * @param msf4jRequest @{@link Request} to read the body from
     * @return the body of the request as a String.
     */
    public static String readRequestBody(Request msf4jRequest) {

        ByteBuffer merge = BufferUtil.merge(msf4jRequest.getFullMessageBody());
        return Charset.defaultCharset().decode(merge).toString();
    }
}
