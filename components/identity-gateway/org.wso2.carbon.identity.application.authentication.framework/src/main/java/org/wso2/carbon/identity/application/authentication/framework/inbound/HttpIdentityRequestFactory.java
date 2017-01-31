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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.common.base.handler.AbstractMessageHandler;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.messaging.Header;
import org.wso2.msf4j.Request;

import java.net.HttpCookie;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.ws.rs.core.Cookie;

public class HttpIdentityRequestFactory extends AbstractMessageHandler {

    private static Log log = LogFactory.getLog(HttpIdentityRequestFactory.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    public void init(InitConfig initConfig) {

        this.initConfig = initConfig;

//        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
//                (HttpIdentityRequestFactory.class.getName(), this.getClass().getName());
//
//        if (identityEventListenerConfig == null) {
//            return;
//        }
//
//        if (identityEventListenerConfig.getProperties() != null) {
//            for (Map.Entry<Object, Object> property : identityEventListenerConfig.getProperties().entrySet()) {
//                String key = (String) property.getKey();
//                String value = (String) property.getValue();
//                if (!properties.containsKey(key)) {
//                    properties.setProperty(key, value);
//                } else {
//                    log.warn("Property key " + key + " already exists. Cannot add property!!");
//                }
//            }
//        }
    }

    public boolean canHandle(Request request) {
        return true;
    }

    public IdentityRequest.IdentityRequestBuilder create(Request request)
            throws FrameworkClientException {

        IdentityRequest.IdentityRequestBuilder builder = new IdentityRequest.IdentityRequestBuilder(request);
        create(builder, request);
        return builder;
    }

    public void create(IdentityRequest.IdentityRequestBuilder builder, Request request)
            throws FrameworkClientException {

        builder.addHeaders(request.getHeaders().getAll().stream()
                .collect(Collectors.toMap(Header::getName, Header::getValue)));
        builder.addAttributes(request.getProperties());
        String cookieHeader = request.getHeader("Cookie");
        List<HttpCookie> httpCookies = HttpCookie.parse(cookieHeader);
        List<Cookie> cookies = httpCookies.stream().map(
                httpCookie -> new Cookie(httpCookie.getName(), httpCookie.getValue(), httpCookie.getPath(),
                        httpCookie.getDomain(), httpCookie.getVersion()))
                .collect(Collectors.toList());
        builder.addCookies(cookies.stream().collect(Collectors.toMap(cookie -> cookie.getName(), cookie -> cookie)));
        String requestURI = request.getUri();
        builder.setContentType(request.getContentType());
//        builder.setContextPath(request.getContextPath());
        builder.setMethod(request.getHttpMethod());
//        builder.setPathInfo(request.getPathInfo());
//        builder.setPathTranslated(request.getPathTranslated());
//        builder.setQueryString(request.getQueryString());
        builder.setRequestURI(requestURI);
        builder.setRequestURL(new StringBuffer(request.getUri()));

    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(400);
        builder.setBody(exception.getMessage());
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }

    @Override
    public String getName() {
        return null;
    }
}
