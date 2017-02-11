/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.msf4j.Request;


public class HttpIdentityRequestFactory<T extends IdentityRequest.IdentityRequestBuilder> extends AbstractHandler {

    private Logger log = LoggerFactory.getLogger(HttpIdentityRequestFactory.class);

    public boolean canHandle(Request request) {
        return true;
    }

    public IdentityRequest.IdentityRequestBuilder create(Request request)
            throws FrameworkClientException {

        IdentityRequest.IdentityRequestBuilder builder = new IdentityRequest.IdentityRequestBuilder();
        this.create((T)builder, request);
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception) {
        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(400);
        builder.setBody(exception.getMessage());
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkRuntimeException exception) {
        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        builder.setBody(exception.getMessage());
        return builder;
    }


    public void create(T builder, Request request)
            throws FrameworkClientException {

        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        //#TODO: add properties to request object
        // get all properties
        //request.getProperties().forEach(builder::addProperty);

        request.get
        builder.setHttpMethod(request.getHttpMethod());
        builder.setContentType(request.getContentType());
        builder.setRequestURI(request.getUri());
        builder.setFullMessageBody(request.getFullMessageBody());
        builder.setMessageBody(request.getMessageBody());
    }
}
