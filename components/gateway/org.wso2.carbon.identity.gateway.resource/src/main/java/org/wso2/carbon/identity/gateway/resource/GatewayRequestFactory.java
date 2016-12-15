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

package org.wso2.carbon.identity.gateway.resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayRequest.GatewayRequestBuilder;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Default factory implementation of {@link GatewayResponseBuilderFactory}
 */
public class GatewayRequestFactory {

    private static Logger logger = LoggerFactory.getLogger(GatewayRequestFactory.class);

    public String getName() {

        return getClass().getSimpleName();
    }

    public int getPriority() {

        return 100;
    }

    public boolean canHandle(Request request) {

        return true;
    }


    public GatewayRequest getGatewayRequest(Request request) throws FrameworkClientException {

        return create(request);
    }

//    public IdentityRequestBuilder create(Request request) throws FrameworkClientException {
//
//        IdentityRequestBuilder builder = new IdentityRequestBuilder();
//        create(builder, request);
//        return builder;
//    }


    public ResponseBuilder handleException(FrameworkClientException exception, Request request) {

        return Response.status(400).entity(exception.getMessage());
    }


    private GatewayRequest create(Request request) throws FrameworkClientException {

        GatewayRequestBuilder builder = new GatewayRequestBuilder();

        // get all headers
        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        // get all properties
//        request.getProperties().forEach(builder::addProperty);


//        builder.setMethod(request.getHttpMethod());
//        builder.setContentType(request.getContentType());
//        builder.setRequestURI(request.getUri());
//
//
//        // handle queryParams
//        FrameworkUtil.getQueryParamMap(request.getUri()).forEach(builder::addProperty);
//
//        // Handle the message body
//        String contentType = request.getContentType();
//        String body = readRequestBody(request);
//
//        // if it a form we add the form key,values as properties.
//        if (isFormParamRequest(contentType)) {
//            try {
//                handleFormParams(body, builder);
//            } catch (UnsupportedEncodingException e) {
//                logger.error("Error handling form parameters.");
//            }
//        } else {
//            builder.setBody(body);
//        }

        // TODO : handle request parameters as well.
        // TODO : handle cookies
        // TODO: extract SP, tenant info, and others

        if (logger.isDebugEnabled()) {
            logger.debug("Identity Request Builder created from the inbound HTTP Request.");
        }

        return builder.build();
    }

//    private void handleFormParams(String requestBody, IdentityRequestBuilder requestBuilder)
//            throws UnsupportedEncodingException {
//
//        FrameworkUtil.splitQuery(requestBody).forEach(requestBuilder::addProperty);
//
//
////        new QueryStringDecoderUtil(requestBody, false).parameters()
////                .forEach((x, y) -> {
////                    if (!y.isEmpty()) {
////                        requestBuilder.addProperty(x, y.get(0));
////                    }
////                });
//    }

//    private boolean isFormParamRequest(String contentType) {
//
//        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType);
//    }


    /**
     * Read the request body of an {@link Request} as a String.
     *
     * @param request @{@link Request} to read the body from
     * @return the body of the request as a String.
     */
    public static String readRequestBody(Request request) {

        ByteBuffer merge = BufferUtil.merge(request.getFullMessageBody());
        return Charset.defaultCharset().decode(merge).toString();
    }
}
