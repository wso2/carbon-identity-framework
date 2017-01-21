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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.msf4j.Request;

import java.util.Properties;


public class HttpIdentityRequestFactory<T extends IdentityRequest.IdentityRequestBuilder> {

    private static Log log = LogFactory.getLog(HttpIdentityRequestFactory.class);
    protected Properties properties;

    public static final String TENANT_DOMAIN_PATTERN = "/t/([^/]+)";
/*
    protected InitConfig initConfig;

    public void init(InitConfig initConfig) throws FrameworkRuntimeException {


        this.initConfig = initConfig;

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (HttpIdentityRequestFactory.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return;
        }

        if(identityEventListenerConfig.getProperties() != null) {
            for(Map.Entry<Object,Object> property:identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String)property.getKey();
                String value = (String)property.getValue();
                if(!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                } else {
                    log.warn("Property key " +  key  + " already exists. Cannot add property!!");
                }
            }
        }


    }*/

    public String getName() {
        return "HttpIdentityRequestFactory";
    }

    public int getPriority() {
        return 100;
    }

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


    public void create(T builder, Request request)
            throws FrameworkClientException {

        request.getHeaders().getAll().forEach(header -> {
            builder.addHeader(header.getName(), header.getValue());
        });

        //#TODO: add properties to request object
        // get all properties
        //request.getProperties().forEach(builder::addProperty);

        builder.setMethod(request.getHttpMethod());
        builder.setContentType(request.getContentType());
        builder.setRequestURI(request.getUri());


        //#TODO: consider this request parameter
     /*
        builder.setContextPath(request.getContextPath());
        builder.setMethod(request.getMethod());
        builder.setPathInfo(request.getPathInfo());
        builder.setPathTranslated(request.getPathTranslated());
        builder.setQueryString(request.getQueryString());
        builder.setRequestURI(requestURI);
        builder.setRequestURL(request.getRequestURL());
        builder.setServletPath(request.getServletPath());
        */
    }




}
